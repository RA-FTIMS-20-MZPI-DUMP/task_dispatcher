package com.dispatcher;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    JSONObject jsonObject = new JSONObject("{\n" + "\"id\": \"5e8f02f2fa09ae5a06e26010\",\n" + "\"robot\": {\n" +
            "\"id\": \"string\",\n" + "\"robotIp\": \"string\",\n" + "\"available\": false,\n" +
            "\"extraRobotElement\": {\n" + "\"id\": \"string\",\n" + "\"name\": \"string\",\n" + "\"dimensions\": \"string\",\n" + "\"functionalityList\": [\n" + "{\n" +
            "\"id\": \"string\",\n" + "\"name\": \"string\"\n" + "}\n" + "]\n" + "},\n" + "\"model\": {\n" + "\"id\": \"string\",\n" + "\"name\": \"string\",\n" +
            "\"maxLiftingCapacity\": \"string\",\n" + "\"maxVelocity\": \"string\",\n" + "\"length\": \"string\",\n" +
            "\"width\": \"string\",\n" + "\"height\": \"string\",\n" + "\"turningRadius\": \"string\",\n" +
            "\"propulsionType\": {\n" + "\"id\": \"string\",\n" + "\"name\": \"string\"\n" + "},\n" + "\"batteryType\": {\n" + "\"id\": \"string\",\n" + "\"name\": \"string\",\n" +
            "\"capacity\": \"string\",\n" + "\"ratedVoltage\": \"string\",\n" + "\"maxCurrent\": \"string\"\n" + "}\n" + "},\n" + "\"battery\": {\n" + "\"id\": \"string\",\n" + "\"productionDate\": \"string\",\n" +
            "\"type\": {\n" + "\"id\": \"string\",\n" + "\"name\": \"string\",\n" + "\"capacity\": \"string\",\n" + "\"ratedVoltage\": \"string\",\n" + "\"maxCurrent\": \"string\"\n" + "}\n" + "},\n" +
            "\"pose\": {\n" + "\"position\": {\n" + "\"x\": 0,\n" + "\"y\": 0,\n" + "\"z\": 0\n" + "},\n" + "\"orientation\": {\n" + "\"x\": 0,\n" + "\"y\": 0,\n" +
            "\"z\": 0,\n" + "\"w\": 0\n" + "}\n" + "},\n" + "\"batteryLevel\": 0,\n" + "\"status\": [\n" + "{\n" + "\"id\": \"string\",\n" + "\"name\": \"string\"\n" +
            "}\n" + "],\n" + "\"timestamp\": \"string\"\n" + "},\n" + "\"name\": \"TestTaks1\",\n" + "\"behaviours\": [\n" + "{\n" + "\"id\": \"123123123\",\n" + "\"name\": \"GO_TO\",\n" +
            "\"parameters\": \"{\\\"start\\\":\\\"5e4691cf59f001700ceaf72a\\\",\\\"end\\\":\\\"5e4602ba9184b62beee348c9\\\"}\"\n" +
            "},\n" + "{\n" + "\"id\": \"23456ygdhsaj\",\n" + "\"name\": \"DOCK\",\n" + "\"parameters\": \"\"\n" +
            "},\n" + "{\n" + "\"id\": \"4jc1v2h4kjb132kj\",\n" + "\"name\": \"WAIT\",\n" + "\"parameters\": \"{\\\"time\\\":5}\"\n" + "},\n" + "{\n" + "\"id\": \"4iho231j54l213\",\n" +
            "\"name\": \"GO_TO\",\n" + "\"parameters\": \"{\\\"start\\\":\\\"5e4602ba9184b62beee348c9\\\",\\\"end\\\":\\\"5e4691cf59f001700ceaf72a\\\"}\"\n" +
            "}\n" + "],\n" + "\"startTime\": \"null\",\n" + "\"priority\": {\n" + "\"id\": \"5e19e3b19d0ce61f6f23411b\",\n" +
            "\"name\": \"important\",\n" + "\"weight\": 1\n" + "},\n" + "\"status\": \"new\",\n" + "\"userID\": \"user123\"\n" + "}");

    Point point1 = new Point(new JSONObject("{\n" + "\"id\": \"5e4602ba9184b62beee348c9\",\n" + "\"name\": \"prawy dolny rog\",\n" + "    \"pose\": {\n" + "      \"position\": {\n" +
            "\"x\": 2.6013180016895987,\n" + "\"y\": -2.0532330810546986,\n" + "\"z\": 0\n" + "},\n" + "\"orientation\": {\n" + "\"x\": -0.7056393576110463,\n" + "\"y\": 0,\n" +
            "\"z\": 0.7085711657908963,\n" + "\"w\": 0\n" + "}\n" + "},\n" + "\"parkingType\": {\n" + "\"id\": null,\n" + "\"name\": null\n" + "},\n" +
            "\"standType\": {\n" + "\"id\": \"5e19e3b19d0ce61f6f234115\",\n" + "\"name\": \"receiving\"\n" + "},\n" + "\"standStatus\": {\n" + "\"id\": null,\n" + "\"name\": null\n" + "}\n" + "}"));

     Point point2 = new Point(new JSONObject("{\n" + "\"id\": \"5e4691cf59f001700ceaf72a\",\n" + "\"name\": \"A\",\n" + "\"pose\": {\n" + "\"position\": {\n" + "\"x\": -0.8981947615396955,\n" +
             "\"y\": 0.12110796514095412,\n" + "\"z\": 0\n" + "},\n" + "\"orientation\": {\n" + "\"x\": 0.7124362556553991,\n" + "\"y\": 0,\n" + "\"z\": 0.7017368321726564,\n" +
             "\"w\": 0\n" + "}\n" + "},\n" + "\"parkingType\": {\n" + "\"id\": null,\n" + "\"name\": null\n" + "},\n" + "\"standType\": {\n" + "\"id\": \"5e19e3b19d0ce61f6f234115\",\n" +
             "\"name\": \"receiving\"\n" + "},\n" + "\"standStatus\": {\n" + "\"id\": null,\n" + "\"name\": null\n" + "}\n" +"  }"));

    HashMap<String, Point> points = new HashMap<String,Point>();

    @Test
    void isJSONValid() {
        points.put("5e4602ba9184b62beee348c9", point1);
        points.put("5e4691cf59f001700ceaf72a", point2);
        Task task = new Task(jsonObject, points);
        assertTrue(task.isJSONValid(jsonObject.toString()));
    }

    @Test
    void getTime() {
        points.put("5e4602ba9184b62beee348c9", point1);
        points.put("5e4691cf59f001700ceaf72a", point2);
        Task task = new Task(jsonObject, points);
        assertEquals(5.0, task.getTime());
    }

    @Test
    void getDistance() {
        points.put("5e4602ba9184b62beee348c9", point1);
        points.put("5e4691cf59f001700ceaf72a", point2);
        Task task = new Task(jsonObject, points);
        assertEquals(8.239, task.getDistance(), 0.001);
    }

    @Test
    void getPriority() {
        points.put("5e4602ba9184b62beee348c9", point1);
        points.put("5e4691cf59f001700ceaf72a", point2);
        Task task = new Task(jsonObject, points);
        assertEquals(1, task.getPriority());
    }

    @Test
    void getStart() {
        points.put("5e4602ba9184b62beee348c9", point1);
        points.put("5e4691cf59f001700ceaf72a", point2);
        Task task = new Task(jsonObject, points);
        assertEquals(-0.898, task.getStart().getX(),0.001);
        assertEquals(0.121, task.getStart().getY(),0.001);
    }

    @Test
    void getEnd() {
        points.put("5e4602ba9184b62beee348c9", point1);
        points.put("5e4691cf59f001700ceaf72a", point2);
        Task task = new Task(jsonObject, points);
        assertEquals(-0.898, task.getEnd().getX(),0.001);
        assertEquals(0.121, task.getEnd().getY(),0.001);
    }

    @Test
    void setStart() {
        points.put("5e4602ba9184b62beee348c9", point1);
        points.put("5e4691cf59f001700ceaf72a", point2);
        Task task = new Task(jsonObject, points);
        Point start = new Point(2,0);
        task.setStart(start);
        assertEquals(2, task.getStart().getX());
        assertEquals(0, task.getStart().getY());
    }

    @Test
    void setEnd() {
        points.put("5e4602ba9184b62beee348c9", point1);
        points.put("5e4691cf59f001700ceaf72a", point2);
        Task task = new Task(jsonObject, points);
        Point end = new Point(2,0);
        task.setEnd(end);
        assertEquals(2, task.getEnd().getX());
        assertEquals(0, task.getEnd().getY());
    }

    @Test
    void getId() {
        points.put("5e4602ba9184b62beee348c9", point1);
        points.put("5e4691cf59f001700ceaf72a", point2);
        Task task = new Task(jsonObject, points);
        assertEquals("5e8f02f2fa09ae5a06e26010", task.getId());
    }

}