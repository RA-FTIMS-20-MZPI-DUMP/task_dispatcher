package com.dispatcher;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class Dispatcher {

    private WebTarget webTarget;
    private ArrayList<Robot> availableRobots = new ArrayList<Robot>(); //dostepne roboty
    private ArrayList<Robot> robotsWithTasks = new ArrayList<Robot>(); //zajete roboty
    private ArrayList<Task> tasks = new ArrayList<Task>(); //zadania
    private HashMap<String, Point> points = new HashMap<String, Point>();

    // + sprawdzanie czy jest kontakt z robotami - może Paweł przez Websocketa?
    // + sprawdzanie czy robot nie utknął - nie mam pojęcia :(

    // zwraca roboty, które skończą zadania za mniej niż 10 sekund
    // a przy okazji sprawdza któryś z robotów już skończył zadanie i zmienia mu statusy
    private void findFinishingRobots() throws IOException {
        try {
            for(int i=0; i<robotsWithTasks.size(); i++) {
            Duration duration = Duration.between(robotsWithTasks.get(i).getCurrentTask().getExecutionStart(), LocalDateTime.now());
            long seconds = duration.getSeconds();
            if (robotsWithTasks.get(i).getCurrentTask().getTime() - seconds <= 0) {
                robotsWithTasks.get(i).getCurrentTask().setStatus("done");
                updateTaskStatus(robotsWithTasks.get(i).getCurrentTask().getId(), "done");
                robotsWithTasks.get(i).setCurrentTask(null);
                if(!findRobot(robotsWithTasks.get(i).getId())) {
                    availableRobots.add(robotsWithTasks.get(i));
                }
                updateRobotAvailability(robotsWithTasks.get(i).getId(), true);
                robotsWithTasks.remove(robotsWithTasks.get(i));
            } else if (robotsWithTasks.get(i).getCurrentTask().getTime() - seconds <= 10) {
                availableRobots.add(robotsWithTasks.get(i));
            } } }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }

    // zwraca robota, który ma wykonac zadanie
    private Robot chooseRobot(Task task) {
       // musi tu byc sprawdzenie poziomu baterii - jesli jest nizszy niz 10% wtedy:
       // updateRobot(availableRobots.get(i).getId(), "status", "charging needed");
        if (availableRobots.size() > 0) {
            return availableRobots.get(0);
        } else {
            System.out.println("No available robots");
            System.exit(1);
            return null;
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

    // zwraca wolne roboty z bazy (!!! nie mogą mieć zadnych nulli poza battery !!!)
    private void fetchAvailableRobots() {
        JSONArray jsonArray = fetchData("robots/all");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            boolean noStartPosition = jsonObject.isNull("pose");
            if (noStartPosition) {
                System.out.println("No start position for robot");
                continue;
            }

            if(jsonObject.isNull("robotIp")){
                System.out.println("Unknown robot IP");
                continue;
            }
            if(jsonObject.getJSONObject("model").isNull("maxVelocity")){
                System.out.println("Unknown max velocity");
                continue;
            }
            if(jsonObject.isNull("batteryLevel")){
                System.out.println("Unknown battery level");
                continue;
            }
            boolean available = jsonObject.getBoolean("available");
            if (available) {
                try {
                    Robot newRobot = new Robot(jsonObject);
                    availableRobots.add(newRobot);
                } catch (Exception e) {
                    System.out.println("Error in robot data");
                }
            }
        }
    }

    private void  updateRobotAvailability(String id, boolean value) {
        JSONObject status = fetchObject("robots/", id);
        status.put("available", value);
        WebTarget update = this.webTarget.path("robots/update");
        update.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(status.toString()));
    }



    // zwraca następne zadanie do wykonania
    private Task chooseTask() {
//        Task taskToDo = new Task();
//        for (Task task : this.tasks){ // inicjalizacja zadania pierwszym z brzegu
//            if (task.getStatus().equals("new")){
//                taskToDo = task;
//                break;
//            }
//        }
//
//        for (Task task : this.tasks) { // wybranie zadania z najwyzszym priorytetem i najszybszym czasem
//            if (task.getStatus().equals("new") && task.getPriority() < taskToDo.getPriority()){
//                taskToDo = task;
//            }
//            if (task.getStatus().equals("new") && task.getPriority() == taskToDo.getPriority()){
//                if (task.getTime() < taskToDo.getTime()){
//                    taskToDo = task;
//                }
//            }
//        }
        this.tasks.sort(new PriorityComparator());
        Task chosenTask = this.tasks.get(0);
        this.tasks.remove(chosenTask);
        return chosenTask;
    }

    public class PriorityComparator implements Comparator<Task> {
        @Override
        public int compare(Task t1, Task t2) {
            return t2.getPriority() - t1.getPriority();
        }
    }

    // aktualizacja priorytetów jeśli zadanie oczekuje wiecej niz 30 sekund
    private void updatePriority() {
        try {
            for (Task task : this.tasks) {
                Duration duration = Duration.between(task.getAppearanceTime(), LocalDateTime.now());
                long seconds = duration.getSeconds();
                if (seconds > 30 && task.getStatus().equals("new")) {
                    if (task.getPriority() != 1) {
                        task.setPriority(task.getPriority() - 1);
                        updateTaskPriority(task.getId(), task.getPriority());
                    }
                }
            }
        }
         catch(Exception ex){
                ex.printStackTrace();
            }
    }

    private void updateTaskStatus(String id, String value) {
        JSONObject task = fetchObject("robots/tasks/", id);
        task.put("status", value);
        WebTarget update = this.webTarget.path("robots/tasks/update");
        update.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(task.toString()));
    }

    private void updateTaskPriority(String id, int priority) {
        JSONObject task = fetchObject("robots/tasks/", id);
        task.getJSONObject("priority").put("weight", priority);
        WebTarget update = this.webTarget.path("robots/tasks/update");
        update.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(task.toString()));
    }


    private void fetchTasks() {
        JSONArray jsonArray = fetchData("robots/tasks/all");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            this.tasks.add(new Task(jsonObject, this.points));
        }
    }


    // inicjalizacja połączenia
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

    private JSONObject fetchObject(String path, String id) {
        WebTarget data = this.webTarget.path(path + id);
        String json = data.request(MediaType.APPLICATION_JSON).get(String.class);
        System.out.println(json);
        return new JSONObject(json);
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

    public void assignTasks() {
       try {
           this.initWebTarget();
           this.fetchPoints();
           this.fetchAvailableRobots(); // wybranie robotów z bazy
           this.findFinishingRobots(); // wybranie robotów, które kończą zadanie
           this.fetchTasks(); // wybranie zadań z bazy
           this.updatePriority(); // aktualizacja priorytetów
           Task toDo = chooseTask(); // wybranie zadania do wykonania
           if (toDo != null){
               Robot chosenRobot = chooseRobot(toDo); // wybranie robota do wykonania zadania
               chosenRobot.setCurrentTask(toDo);
               robotsWithTasks.add(chosenRobot);
               availableRobots.remove(chosenRobot);
               updateRobotAvailability(chosenRobot.getId(), false);
               toDo.setStatus("in progress");
               updateTaskStatus(toDo.getId(),  "in progress");
               // tu wysłanie komunikatu do robota
               toDo.setExecutionStart(LocalDateTime.now());
               Double time = chosenRobot.getTaskExecutionTime(toDo);
               System.out.printf("Robot: %s Task: %s Execution time: %f\n", chosenRobot.getId(), toDo.getName(), time);
           }
       }
        catch(Exception ex){
           ex.printStackTrace();
        }
    }

}


