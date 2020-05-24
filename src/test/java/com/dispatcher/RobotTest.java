package com.dispatcher;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class RobotTest {

    JSONObject robotData = new JSONObject(new JSONTokener(this.getClass().getResourceAsStream("test_robot.json")));
    Robot robot = new Robot(robotData);
    JSONObject taskData = new JSONObject(new JSONTokener(this.getClass().getResourceAsStream("test_task.json")));
    Task task = new Task(taskData);
    Date date = new Date(2020-05-21);

    @Test
    void getTaskExecutionTime() {
        task.setStart(new Point(5, 7));
        task.setEnd(new Point(3, 2));
        int time = robot.getTaskExecutionTime(task);
        assertEquals(96, time);
    }

    @Test
    void getId() {
        assertEquals("qwerty", robot.getId());
    }

    @Test
    void getCurrentTask() {
        robot.setCurrentTask(task);
        assertEquals(task, robot.getCurrentTask());
    }

    @Test
    void setCurrentTask() {
        robot.setCurrentTask(task);
        assertEquals(task, robot.getCurrentTask());
    }

    @Test
    void setAvailableOn() {
        robot.setAvailableOn(date);
        assertEquals(date, robot.getAvailableOn());
    }

    @Test
    void getAvailableOn() {
        robot.setAvailableOn(date);
        assertEquals(date, robot.getAvailableOn());
    }

    @Test
    void getBattery() {
        assertEquals(100, robot.getBaterry());
    }
}