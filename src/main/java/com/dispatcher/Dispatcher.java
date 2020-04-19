package com.dispatcher;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class Dispatcher {

    private WebTarget webTarget;
    private ArrayList<Robot> availableRobots = new ArrayList<Robot>();
    private ArrayList<Task> tasks = new ArrayList<Task>();
    private HashMap<String, Point> points = new HashMap<String, Point>();

    private List<Robot> findUnused() {
        return null;
    } // zwraca listę robotów, które będą wolne za mniej niż 10 sekund

    private Task chooseTask() {
        return null;
    } //zwraca najbliższe zadanie do wykonania

    private void updatePriority() {

    } // aktualizacja priorytetów zadań

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

    private void fetchTasks() {
        JSONArray jsonArray = fetchData("robots/tasks/all");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            this.tasks.add(new Task(jsonObject, this.points));
        }
    }

    private void fetchPoints() {
        JSONArray jsonArray = fetchData("movement/stands/all");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            this.points.put(
                jsonObject.getString("id"),
                new Point(jsonObject)
            );
        }
    }

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
                this.availableRobots.add(new Robot(jsonObject));
            }
        }

    }

    public void assignTasks() {
        this.initWebTarget();
        this.fetchPoints();
        this.fetchAvailableRobots();
        this.fetchTasks();
        for (Robot robot : this.availableRobots) {
            for (Task task: this.tasks) {
                Double time = robot.getTaskExecutionTime(task);
                System.out.printf("Robot: %s Task: %s Execution time: %f\n", robot.getId(), task.getName(), time);
            }
        }
    }

}
