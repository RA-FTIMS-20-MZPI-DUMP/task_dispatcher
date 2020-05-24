package com.dispatcher;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    JSONObject jsonObject = new JSONObject(new JSONTokener(this.getClass().getResourceAsStream("test_task.json")));
    JSONObject point1Data = new JSONObject(new JSONTokener(this.getClass().getResourceAsStream("test_point_1.json")));
    JSONObject point2Data = new JSONObject(new JSONTokener(this.getClass().getResourceAsStream("test_point_2.json")));
    Point point1 = new Point(point1Data);
    Point point2 = new Point(point2Data);
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