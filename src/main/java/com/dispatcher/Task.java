package com.dispatcher;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;

public class Task {
    private String name;
    private int priority;
    private double distance = 0;
    private float time = 0;
    private Point start;
    private Point end;

    public enum BehaviourType {
        GO_TO, DOCK, WAIT, DOCKING;
    }

    Task(JSONObject jsonObject, HashMap<String, Point> points) {
        this.name = jsonObject.getString("name");
        this.priority = jsonObject.getJSONObject("priority").getInt("weight");
        JSONArray behaviours = jsonObject.getJSONArray("behaviours");
        for (int i=0; i<behaviours.length(); i++) {
            JSONObject behaviour = behaviours.getJSONObject(i);
            BehaviourType type = behaviour.getEnum(BehaviourType.class, "name");
            String parametersString = behaviour.getString("parameters");
            if (!isJSONValid(parametersString)) {
                continue;
            }
            JSONObject parameters = new JSONObject(parametersString);
            if (type == BehaviourType.GO_TO) {
                Point start = points.get(parameters.getString("start"));
                Point end = points.get(parameters.getString("end"));
                if (this.start == null) {
                    this.start = start;
                }
                this.end = end;
                this.distance += start.getDistance(end);
            }
            if (type == BehaviourType.WAIT) {
                this.time += parameters.getInt("time");
            }
        }
    }

    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    public float getTime() {
        return time;
    }

    public double getDistance() {
        return distance;
    }

    public int getPriority() {
        return priority;
    }

    public String getName() {
        return name;
    }

    public Point getEnd() {
        return end;
    }

    public Point getStart() {
        return start;
    }
}
