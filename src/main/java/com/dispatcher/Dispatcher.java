package com.dispatcher;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

public class Dispatcher extends TimerTask {

    private WebTarget webTarget;
    private ArrayList<Robot> robots = new ArrayList<Robot>(); //dostepne roboty
    private ArrayList<Robot> busyRobots = new ArrayList<Robot>(); //zajete roboty
    private ArrayList<Task> tasks = new ArrayList<Task>(); //zadania
    private HashMap<String, Point> points = new HashMap<String, Point>();
    private long nextCheckTime = 60000;
    static Timer timer = new Timer();

    public Dispatcher(ArrayList<Robot> busyRobots) {
        this.busyRobots = busyRobots;
    }

    public Dispatcher() {}


    private Robot chooseRobot(Task task) {
        //TODO
        //Wybranie robota na podstawie czasu zakończenia zadania
        //Teraz zwraca pierwszy z listy
        return robots.get(0);
    }

    private void fetchAvailableRobots() {
        JSONArray jsonArray = fetchData("robots/all");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String robotId = jsonObject.getString("id");
            boolean noStartPosition = jsonObject.isNull("pose");
            if (noStartPosition) {
                System.out.println("No start position for robot " + robotId);
                continue;
            }

            if(jsonObject.isNull("robotIp")){
                System.out.println("Unknown robot IP " + robotId);
                continue;
            }
            if(jsonObject.isNull("batteryLevel")){
                System.out.println("Unknown battery level " + robotId);
                continue;
            }
            boolean available = jsonObject.getBoolean("available");
            if (available) {
                try {
                    Robot newRobot = new Robot(jsonObject);
                    robots.add(newRobot);
                } catch (Exception e) {
                    System.out.println("Error in robot data " + robotId);
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

    private Task chooseTask() {
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

    private void sendRobotsToCharge() {
        //TODO
        //Sprawdz czy jakis robot potrzebuje ładowania, jeśli tak to trzeba go usunąc z availableRobots i chyba wyslac do ladowania
        //Chyba trzeba tez ustawic w bazie avaiable = false
    }

    public void assignTasks() {
       this.initWebTarget();
       this.restoreRobotsAndTasks();
       this.fetchPoints();
       this.fetchAvailableRobots();
       this.sendRobotsToCharge();
       this.fetchTasks();
       while (robots.size() > 0 && tasks.size() > 0) {
            chooseRobotAndTask();
       }
    }

    private void restoreRobotsAndTasks() {
        for (Robot robot: new ArrayList<Robot>(this.busyRobots)) {
            if (robot.getAvailableOn().before(new Date())) {
                updateRobotAvailability(robot.getId(), true);
                updateTaskStatus(robot.getCurrentTask().getId(), "done");
            }
            this.robots.remove(robot);
        }
    }

    private void chooseRobotAndTask() {
        Task chosenTask = this.chooseTask();
        Robot chosenRobot = this.chooseRobot(chosenTask);
        System.out.println("Chosen robot: " + chosenRobot.getId() + " for task: " + chosenTask.getId());
        int executionTime = chosenRobot.getTaskExecutionTime(chosenTask);
        chosenRobot.setCurrentTask(chosenTask);
        System.out.println("Estimated working time is: " + executionTime + " ms");
        Date availableOn = new Date(System.currentTimeMillis() + executionTime);
        chosenRobot.setAvailableOn(availableOn);
        updateTaskStatus(chosenTask.getId(), "in progress");
        updateRobotAvailability(chosenRobot.getId(), false);
        busyRobots.add(chosenRobot);
        robots.remove(chosenRobot);
        tasks.remove(chosenTask);
    }


    public void updateNextCheckTime() {
        if (this.busyRobots.size() == 0) {
            this.nextCheckTime = 30000;
            return;
        }
        Date nextUpdate = this.busyRobots.stream().map(Robot::getAvailableOn).min(Date::compareTo).get();
        long difference = new Date().getTime() - nextUpdate.getTime();
        if (difference > 0) {
            this.nextCheckTime = difference;
        } else {
            this.nextCheckTime = 1;
        }
    }

    public void run() {
        this.assignTasks();
        this.updateNextCheckTime();
        timer.schedule(new Dispatcher(this.busyRobots), this.nextCheckTime);
    }

}


