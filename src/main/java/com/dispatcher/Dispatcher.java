package com.dispatcher;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import java.sql.Date;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import static java.lang.System.currentTimeMillis;

public class Dispatcher {

    private WebTarget webTarget;
    private ArrayList<Robot> availableRobots = new ArrayList<Robot>();
    private ArrayList<Robot> robotsWithTasks = new ArrayList<Robot>();
    private ArrayList<Task> tasks = new ArrayList<Task>();
    private HashMap<String, Point> points = new HashMap<String, Point>();

    // zwraca listę robotow, które skończa zadanie za mniej niż 5 sekund
    private void findFinishingRobots() {
        for (Robot robot : this.robotsWithTasks) {
            Duration duration = Duration.between(robot.getCurrentTask().getExecutionStart(), LocalDateTime.now());
            long seconds = duration.getSeconds();
            if (robot.getCurrentTask().getTime() - seconds <= 0) {
                robot.setCurrentTask(null);
                robotsWithTasks.remove(robot);
                availableRobots.add(robot);
            } else if (robot.getCurrentTask().getTime() - seconds <= 5) {
                availableRobots.add(robot);
            }
        }
    }

    // zwraca zadanie, które ma być teraz wykonane
    private Task chooseTask() {
        Task taskToDo = null;
        if(tasks.size() > 0) {
            taskToDo = tasks.get(0);
        }
        for (Task task : this.tasks) {
            if (task.getPriority() <= taskToDo.getPriority()){
                if (task.getTime() < taskToDo.getTime()){
                    taskToDo = task;
                }
            }
        }
        return taskToDo;
    }

    // zwraca robota, który ma wykonać wybrane zadanie
    private Robot chooseRobot(Task task) { // tu implementacja algorytmu do wyboru robota
        if (availableRobots.size() > 0) {
            return availableRobots.get(0);
        }
        return null;
    }

    // jeśli od 15 sekund zadanie jest w kolejce to zwiększamy mu priorytet
    private void updatePriority() {
        for (Task task : this.tasks) {
            Duration duration = Duration.between(task.getAppearanceTime(), LocalDateTime.now());
            long seconds = duration.getSeconds();
            if(seconds > 15) {
                if(task.getPriority() != 1){
                    task.setPriority(task.getPriority()-1);
                }
            }
        }
    }

    private void initWebTarget() {
        ClientConfig clientConfig = new ClientConfig();
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic("dispatchTest", "gBrZzVbCbMsr");
        clientConfig.register(feature);
        Client client = ClientBuilder.newClient(clientConfig);
        this.webTarget = client.target("http://adrastea.westus2.cloudapp.azure.com:3333/");
    }

    private JSONArray fetchData(String path) {
        WebTarget data = this.webTarget.path(path);
        String json = data.request(MediaType.APPLICATION_JSON).get(String.class);
        return new JSONArray(json);
    }

    private boolean findTask(String name){
        for (int i=0; i<tasks.size(); i++){
            if(tasks.get(i).getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    // wybieranie z Mongo tylko nowych zadań
    private void fetchTasks() {
        JSONArray jsonArray = fetchData("robots/tasks/all");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Task newTask = new Task(jsonObject, this.points);
            if(!findTask(newTask.getName())){
                tasks.add(newTask);
            }
        }
    }

    private void fetchPoints() {
        this.points.clear();
        JSONArray jsonArray = fetchData("movement/stands/all");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            this.points.put(
                    jsonObject.getString("id"),
                    new Point(jsonObject)
            );
        }
    }

    private boolean findRobot(String id){
        for (int i=0; i<availableRobots.size(); i++){
            if(availableRobots.get(i).getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    // wybieranie z Mongo tylko nowych, dostępnych robotów
    private void fetchAvailableRobots() {
        JSONArray jsonArray = fetchData("robots/all");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            boolean noStartPosition = jsonObject.isNull("pose");
            if (noStartPosition) {
                System.out.println("No start position for robot");
                continue;
            }
            boolean available = jsonObject.getBoolean("available");
            if (available) {
                Robot newRobot = new Robot(jsonObject);
                if(!findRobot(newRobot.getId())){
                    availableRobots.add(newRobot);
                }
            }
        }
    }

    public void assignTasks() {
        this.initWebTarget();
        this.fetchPoints();
        this.fetchAvailableRobots(); //pobieranie dostepnych robotow
        this.findFinishingRobots(); //dodanie robotow, które zaraz kończa
        this.fetchTasks(); //pobranie nowych zadan
        this.updatePriority(); //aktualizacja priorytetów
        Task toDo = chooseTask(); //wybór zadania do realizacji
        Robot chosenRobot = chooseRobot(toDo); //wybór robota, który je zrealizuje
        chosenRobot.setCurrentTask(toDo); //ustawiamy robotowi zadanie, które wykonuje
        toDo.setExecutionStart(LocalDateTime.now()); //timestamp rozpoczęcia realizacji zadania
        robotsWithTasks.add(chosenRobot); // dodajemy robota do listy robotów z zadaniami
        availableRobots.remove(chosenRobot); // + trzeba ustawić robotowi w Mongo nie-available
        tasks.remove(toDo); // + trzeba usunąć też task z Mongo
        Double time = chosenRobot.getTaskExecutionTime(toDo);
        System.out.printf("Robot: %s Task: %s Execution time: %f\n", chosenRobot.getId(), toDo.getName(), time);
    }
}

