package com.dispatcher;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Date;

public class Robot {

    private String id;
    private Point currentPosition;
    private ArrayList<String> functionality = new ArrayList<String>();
    private double velocity;
    private Task currentTask;
    private Date availableOn;
    private int battery;

    Robot(JSONObject jsonObject) {
        this.id = jsonObject.getString("id");
        JSONObject extraElements = jsonObject.getJSONObject("extraRobotElement");
        if (!extraElements.isNull("functionalityList")) {
            JSONArray functions = extraElements.getJSONArray("functionalityList");
            for (int i = 0; i < functions.length(); i++) {
                String function = functions.getJSONObject(i).getString("name");
                this.functionality.add(function);
            }
        }
        this.currentPosition = new Point(jsonObject);
        this.battery = jsonObject.getInt("batteryLevel");
        String velocity = jsonObject.getJSONObject("model").getString("maxVelocity").split("km")[0];
        //this.velocity = Float.parseFloat(velocity) * 1000 / 3600 * 3 / 4;
        this.velocity = 0.09;
    }

    public int getTaskExecutionTime(Task task) {
        double distances = task.getDistance();
        float time = task.getTime();
        distances += currentPosition.getDistance(task.getStart());
        double sumTime = time * 1000 + distances / this.velocity;
        return (int) Math.round(sumTime);
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

    public void setAvailableOn(Date availableOn) {
        this.availableOn = availableOn;
    }

    public Date getAvailableOn() {
        return this.availableOn;
    }

    public int getBaterry(){ return this.battery; }

}
