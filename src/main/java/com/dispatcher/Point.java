package com.dispatcher;
import org.json.JSONObject;

public class Point {
     private double x;
    private double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    Point(JSONObject jsonObject) {
        this.x = jsonObject.getJSONObject("pose").getJSONObject("position").getDouble("x");
        this.y = jsonObject.getJSONObject("pose").getJSONObject("position").getDouble("y");
    }

    double getDistance(Point point) {
        return Math.sqrt((point.y - this.y) * (point.y - this.y) + (point.x - this.x) * (point.x - this.x));
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

}
