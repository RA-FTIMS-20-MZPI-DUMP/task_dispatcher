package com.dispatcher;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class Dispatcher extends TimerTask {

    public WebTarget getWebTarget() {
        return webTarget;
    }

    private WebTarget webTarget;
    private ArrayList<Robot> robots = new ArrayList<Robot>(); //dostepne roboty
    private ArrayList<Robot> busyRobots = new ArrayList<Robot>(); //zajete roboty
    private ArrayList<Task> tasks = new ArrayList<Task>(); //zadania
    private HashMap<String, Point> points = new HashMap<String, Point>();
    private long nextCheckTime = 60000;
    static Timer timer = new Timer();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private ArrayList<Future<Response>> futures = new ArrayList<Future<Response>>();
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";

    public Dispatcher(ArrayList<Robot> busyRobots) {
        this.busyRobots = busyRobots;
    }

    public Dispatcher() {
    }


    private Robot chooseRobot(Task task) {
        //TODO
        //Wybranie robota na podstawie czasu zakończenia zadania
        //Teraz zwraca pierwszy z listy
        return robots.get(0);
    }

    void fetchAvailableRobots() {
        JSONArray jsonArray = fetchData("robots/all");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String robotId = jsonObject.getString("id");
            boolean noStartPosition = jsonObject.isNull("pose");
            if (noStartPosition) {
                System.out.println(ANSI_RED + "No start position for robot " + robotId);
                continue;
            }

            if(jsonObject.isNull("robotIp")){
                System.out.println(ANSI_RED + "Unknown robot IP " + robotId);
                continue;
            }
            if(jsonObject.isNull("batteryLevel")){
                System.out.println(ANSI_RED + "Unknown battery level " + robotId);
                continue;
            }
            boolean available = jsonObject.getBoolean("available");
            if (available) {
                try {
                    Robot newRobot = new Robot(jsonObject);
                    System.out.println(ANSI_GREEN + "ROBOT IS AVAILABLE: " + newRobot.getId());
                    robots.add(newRobot);
                } catch (Exception e) {
                    System.out.println(ANSI_RED + "Error in robot data " + robotId);
                }
            }
        }
    }

    void updateRobotAvailability(Robot robot, boolean value) {
        JSONObject status = robot.json;
        status.put("available", value);
        WebTarget update = this.webTarget.path("robots/update");
        Future<Response> future = update.request(MediaType.APPLICATION_JSON_TYPE).async().post(Entity.json(status.toString()));
        this.futures.add(future);

    }

    Task chooseTask() {
        this.tasks.sort(new PriorityComparator());
        Task chosenTask = this.tasks.get(0);
        this.updateTaskStatus(chosenTask, "in-progress");
        this.tasks.remove(chosenTask);
        return chosenTask;
    }

    public class PriorityComparator implements Comparator<Task> {
        @Override
        public int compare(Task t1, Task t2) {
            return t1.getPriority() - t2.getPriority();
        }
    }

    void updateTaskStatus(Task task, String value) {
        JSONObject json = task.json;
        json.put("status", value);
        WebTarget update = this.webTarget.path("robots/tasks/update");
        Future<Response> future = update.request(MediaType.APPLICATION_JSON_TYPE).async().post(Entity.json(json.toString()));
        this.futures.add(future);
    }

    void updateTaskPriority(String id, int priority) {
        JSONObject task = fetchObject("robots/tasks/", id);
        task.getJSONObject("priority").put("weight", priority);
        WebTarget update = this.webTarget.path("robots/tasks/update");
        Future<Response> future = update.request(MediaType.APPLICATION_JSON_TYPE).async().post(Entity.json(task.toString()));
        this.futures.add(future);
    }


    JSONArray fetchTasks() {
        JSONArray jsonArray = fetchData("robots/tasks/all");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (jsonObject.getString("status").compareTo("new") == 0) {
                this.tasks.add(new Task(jsonObject, this.points));
            }
        }
        return jsonArray;
    }

    void initWebTarget() {
        ClientConfig clientConfig = new ClientConfig();
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic("dispatchTest", "gBrZzVbCbMsr");
        clientConfig.register(feature);
        Client client = ClientBuilder.newClient(clientConfig);
        this.webTarget = client.target("http://adrastea.westus2.cloudapp.azure.com:3333/");
    }

    JSONArray fetchData(String path) {
        WebTarget data = this.webTarget.path(path);
        String json = data.request(MediaType.APPLICATION_JSON).get(String.class);
        return new JSONArray(json);
    }

    JSONObject fetchObject(String path, String id) {
        WebTarget data = this.webTarget.path(path + id);
        String json = data.request(MediaType.APPLICATION_JSON).get(String.class);
        return new JSONObject(json);
    }

    void fetchPoints() {
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

    void sendRobotsToCharge() {
    // TO DO
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

    void restoreRobotsAndTasks() {
        for (Robot robot: new ArrayList<Robot>(this.busyRobots)) {
            if (robot.getAvailableOn().before(new Date())) {
                updateRobotAvailability(robot, true);
                Task currentTask = robot.getCurrentTask();
                updateTaskStatus(currentTask, "done");
                System.out.println(ANSI_BLUE + "Done task " + currentTask.getId());
            }
            this.busyRobots.remove(robot);
        }
        sendRobotsToCharge();
    }

    void chooseRobotAndTask() {
        Task chosenTask = this.chooseTask();
        Robot chosenRobot = this.chooseRobot(chosenTask);
        System.out.println(ANSI_BLUE + "Chosen robot: " + chosenRobot.getId() + " for task: " + chosenTask.getId());
        int executionTime = chosenRobot.getTaskExecutionTime(chosenTask);
        chosenRobot.setCurrentTask(chosenTask);
        System.out.println("Estimated working time is: " + executionTime + " ms");
        Date availableOn = new Date(new Date().getTime() + executionTime);
        System.out.println("Task will be end: " + sdf.format(availableOn));
        chosenRobot.setAvailableOn(availableOn);
        updateTaskStatus(chosenTask, "in progress");
        updateRobotAvailability(chosenRobot, false);
        busyRobots.add(chosenRobot);
        robots.remove(chosenRobot);
        tasks.remove(chosenTask);
    }


    public void updateNextCheckTime() {
        if (this.busyRobots.size() == 0) {
            System.out.println(ANSI_YELLOW + "Brak dostępnych robotów lub zadań, ponowne uruchomienie algorytmu za 30 sekund");
            this.nextCheckTime = 30000;
            return;
        } else {
            List<Date> nextUpdates = this.busyRobots.stream().map(Robot::getAvailableOn).collect(Collectors.toList());
            Date nextUpdate = Collections.min(nextUpdates);
            long difference = getDateDiff(new Date(), nextUpdate);
            if (difference > 0) {
                this.nextCheckTime = difference;
            } else {
                this.nextCheckTime = 1;
            }
        }
        System.out.println("Algorithm restart in " + nextCheckTime + " ms ");
    }

    public void run() {
        this.waitForFutures();
        this.assignTasks();
        this.updateNextCheckTime();
        this.waitForFutures();
        timer.schedule(new Dispatcher(this.busyRobots), this.nextCheckTime);
    }

    void waitForFutures(){
        for (Future future: futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public static long getDateDiff(Date date1, Date date2) {
        return date2.getTime() - date1.getTime();
    }

    public Collection<Point> getPointsValues() {return this.points.values();}

    public HashMap<String,Point> getPoints() {return this.points;}

    public ArrayList<Task> getTasks() {return this.tasks;}

    public ArrayList<Robot> getRobots() {return this.robots;}

    public ArrayList<Robot> getBusyRobots() {return this.busyRobots;}

    public long getNextCheckTime() {return this.nextCheckTime; }

}


