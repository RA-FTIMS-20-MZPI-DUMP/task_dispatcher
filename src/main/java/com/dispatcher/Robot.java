package com.dispatcher;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class Robot {

    private Point currentPosition;
    private ArrayList<String> functionality = new ArrayList<String>();
    private float velocity;

    Robot(JSONObject jsonObject) {
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
        this.velocity = Float.parseFloat(velocity) * 1000 / 3600;
    }
}
