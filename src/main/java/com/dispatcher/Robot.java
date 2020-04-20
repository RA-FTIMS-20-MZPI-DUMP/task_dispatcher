package com.dispatcher;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class Robot {

    private String id;
    private Point currentPosition;
    private ArrayList<String> functionality = new ArrayList<String>();
    private float velocity;
    private Task currentTask; //aktualnie wykonywane zadanie

    Robot(JSONObject jsonObject) {
        this.id = jsonObject.getString("id");
        JSONObject extraElements = jsonObject.getJSONObject("extraRobotElement");
        System.out.println(jsonObject);
        if (!extraElements.isNull("functionalityList")) {
            JSONArray functions = extraElements.getJSONArray("functionalityList");
            for (int i = 0; i < functions.length(); i++) {
                String function = functions.getJSONObject(i).getString("name");
                this.functionality.add(function);
            }
        }
        this.currentPosition = new Point(jsonObject);
        String velocity = jsonObject.getJSONObject("model").getString("maxVelocity").split("km")[0];
        this.velocity = Float.parseFloat(velocity) * 1000 / 3600 * 3 / 4;
    }

    public double getTaskExecutionTime(Task task) {
        double distances = task.getDistance();
        float time = task.getTime();
        distances += currentPosition.getDistance(task.getStart());
        return time + distances / this.velocity;
    }

    public String getId() {
        return id;
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }
}
