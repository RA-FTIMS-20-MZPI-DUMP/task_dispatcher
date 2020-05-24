package com.dispatcher;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class DispatcherTest {

    Dispatcher dispatcher = new Dispatcher();

    @Test
    void assignTasks() {


    }

    @Test
    void updateNextCheckTime1() {
        dispatcher.updateNextCheckTime();
        assertEquals(30000, dispatcher.getNextCheckTime());
    }

    @Test
    void updateNextCheckTime2() {
        dispatcher.initWebTarget();
        JSONObject jsonObject = dispatcher.fetchObject("robots/", "5e19e3b29d0ce61f6f23412b");
        Robot newRobot = new Robot(jsonObject);
        Date now = new Date();
        now.setSeconds(now.getSeconds() + 5);
        newRobot.setAvailableOn(now);
        dispatcher.getBusyRobots().add(newRobot);
        dispatcher.updateNextCheckTime();
        assertEquals(5000, dispatcher.getNextCheckTime());
    }

    @Test
    void getDateDiff() {
        Date date1 = new Date(2020, 5, 2020-05-20, 22, 15, 00);
        Date date2 = new Date(2020, 5, 2020-05-20, 22, 15, 15);
        assertEquals(15000, dispatcher.getDateDiff(date1, date2));
    }

    @Test
    void checkEmptyTaskList() {
    }

    @Test
    void checkEmptyRobotList() {
    }

    @Test
    void fetchAvailableRobots() {
        dispatcher.initWebTarget();
        dispatcher.fetchAvailableRobots();
        for(Object robot : dispatcher.getRobots() ){
            assertTrue(robot instanceof Robot);
            Robot testRobot = (Robot) robot;
            JSONObject jsonObject = dispatcher.fetchObject("robots/", testRobot.getId());
            assertTrue(jsonObject.getBoolean("available"));
        }
    }

    @Test
    void updateRobotAvailability() {
        dispatcher.initWebTarget();
        JSONObject jsonObject = dispatcher.fetchObject("robots/", "5e19e3b29d0ce61f6f234129");
        if(jsonObject.getBoolean("available")){
            dispatcher.updateRobotAvailability("5e19e3b29d0ce61f6f234129", false);
            JSONObject jsonObject1 = dispatcher.fetchObject("robots/", "5e19e3b29d0ce61f6f234129");
            assertFalse(jsonObject1.getBoolean("available"));
        }
        else{
            dispatcher.updateRobotAvailability("5e19e3b29d0ce61f6f234129", true);
            JSONObject jsonObject1 = dispatcher.fetchObject("robots/", "5e19e3b29d0ce61f6f234129");
            assertTrue(jsonObject1.getBoolean("available"));
        }
    }

    @Test
    void chooseTask() { // Będzie działało jak nie będzie śmieci w bazie :(
        HashMap<String, Point> points = new HashMap<String,Point>();
        dispatcher.initWebTarget();
        dispatcher.fetchPoints();
        dispatcher.fetchTasks();
        Task chosenTask = dispatcher.chooseTask();
        assertEquals(1, chosenTask.getPriority());
    }

    @Test
    void updateTaskStatus() {
        dispatcher.initWebTarget();
        JSONObject jsonObject = dispatcher.fetchObject("robots/tasks/", "5e8f0102fa09ae5a06e2600f");
        if(jsonObject.getString("status").equals("rejected")){
            dispatcher.updateTaskStatus("5e8f0102fa09ae5a06e2600f", "new");
            JSONObject jsonObject1 =         dispatcher.fetchObject("robots/tasks/", "5e8f0102fa09ae5a06e2600f");
            assertEquals(new String("new"), jsonObject1.getString("status"));
        }
        else{
            dispatcher.updateTaskStatus("5e8f0102fa09ae5a06e2600f", "rejected");
            JSONObject jsonObject1 =         dispatcher.fetchObject("robots/tasks/", "5e8f0102fa09ae5a06e2600f");
            assertEquals(new String("rejected"), jsonObject1.getString("status"));
        }
    }

    @Test
    void updateTaskPriority() {
        dispatcher.initWebTarget();
        JSONObject jsonObject = dispatcher.fetchObject("robots/tasks/", "5e8f0102fa09ae5a06e2600f");
        if(jsonObject.getJSONObject("priority").getInt("weight") == 1){
            dispatcher.updateTaskPriority("5e8f0102fa09ae5a06e2600f", 2);
            JSONObject jsonObject1 = dispatcher.fetchObject("robots/tasks/", "5e8f0102fa09ae5a06e2600f");
            assertEquals(2, jsonObject1.getJSONObject("priority").getInt("weight"));
        }
        else{
            dispatcher.updateTaskPriority("5e8f0102fa09ae5a06e2600f", 1);
            JSONObject jsonObject1 = dispatcher.fetchObject("robots/tasks/", "5e8f0102fa09ae5a06e2600f");
            assertEquals(1, jsonObject1.getJSONObject("priority").getInt("weight"));
        }
    }

    @Test
    void fetchTasks() {
        dispatcher.initWebTarget();
        dispatcher.fetchPoints();
        dispatcher.fetchTasks();
        for(Task task : dispatcher.getTasks() ){
            assertTrue(task instanceof Task);
        }
    }

    @Test
    void initWebTarget() {
        dispatcher.initWebTarget();
        assertEquals(200, dispatcher.getWebTarget().path("robots/all").request(MediaType.APPLICATION_JSON).head().getStatus());
    }

    @Test
    void fetchData() {
        JSONArray jsonArray = new JSONArray("[\n" + "{\n" + "\"id\": \"test1\",\n" + "\"name\": \"test1\"\n" + "},\n" +
                "{\n" + "\"id\": \"on the way\",\n" + "\"name\": \"on the way\"\n" + "},\n" + "{\n" + "\"id\": \"free\",\n" +
                "\"name\": \"free\"\n" + "},\n" + "{\n" + "\"id\": \"charging needed\",\n" + "\"name\": \"charging needed\"\n" +
                "},\n" + "{\n" + "\"id\": \"during task\",\n" + "\"name\": \"during task\"\n" + "}\n" + "]");
        dispatcher.initWebTarget();
        assertEquals(jsonArray.toString(), dispatcher.fetchData("type/robot-statuses/all").toString());
    }

    @Test
    void fetchObject() {
        JSONObject jsonObject = new JSONObject("{\n"+"\"id\": \"test1\",\n" + "\"name\": \"test1\"\n" + "}");
        dispatcher.initWebTarget();
        assertEquals(jsonObject.toString(), dispatcher.fetchObject("type/robot-statuses/", "test1").toString());
    }

    @Test
    void fetchPoints() {
        dispatcher.initWebTarget();
        dispatcher.fetchPoints();
        for(Point point : dispatcher.getPointsValues() ){
            assertTrue(point instanceof Point);
        }
    }

    @Test
    void sendRobotsToCharge() {

    }

    @Test
    void restoreRobotsAndTasks() {
        dispatcher.initWebTarget();
        dispatcher.fetchPoints();
        JSONObject jsonObject1 = dispatcher.fetchObject("robots/tasks/", "5e8f4909fa09ae5a06e26019");
        Task newTask = new Task(jsonObject1, dispatcher.getPoints());
        JSONObject jsonObject = dispatcher.fetchObject("robots/", "5e19e3b29d0ce61f6f234129");
        Robot newRobot = new Robot(jsonObject);
        Date now = new Date();
        now.setSeconds(now.getSeconds() - 5);
        newRobot.setAvailableOn(now);
        newRobot.setCurrentTask(newTask);
        dispatcher.getBusyRobots().add(newRobot);
        dispatcher.restoreRobotsAndTasks();
        JSONObject taskAfterUpdate = dispatcher.fetchObject("robots/tasks/", "5e8f4909fa09ae5a06e26019");
        JSONObject robotAfterUpdate = dispatcher.fetchObject("robots/", "5e19e3b29d0ce61f6f234129");
        assertEquals("done", taskAfterUpdate.getString("status"));
        assertTrue(robotAfterUpdate.getBoolean("available"));

    }

    @Test
    void chooseRobotAndTask() {
        dispatcher.initWebTarget();
        dispatcher.fetchPoints();
        JSONObject jsonObject = dispatcher.fetchObject("robots/tasks/", "5e8f4909fa09ae5a06e26019");
        JSONObject jsonObject2 = dispatcher.fetchObject("robots/tasks/", "5e8f0102fa09ae5a06e2600f");
        Task task1 = new Task(jsonObject, dispatcher.getPoints());
        Task task2 = new Task(jsonObject2, dispatcher.getPoints());
        dispatcher.getTasks().add(task1);
        dispatcher.getTasks().add(task2);
        JSONObject jsonObject3 = dispatcher.fetchObject("robots/", "5e19e3b29d0ce61f6f234129");
        JSONObject jsonObject4 = dispatcher.fetchObject("robots/", "5e19e3b29d0ce61f6f23412a");
        Robot robot1 = new Robot(jsonObject3);
        Robot robot2 = new Robot(jsonObject4);
        dispatcher.getRobots().add(robot1);
        dispatcher.getRobots().add(robot2);
        dispatcher.chooseRobotAndTask();
        assertTrue(dispatcher.getBusyRobots().contains(robot1));
        assertFalse(dispatcher.getRobots().contains(robot1));
        assertFalse(dispatcher.getTasks().contains(task2));
    }
}