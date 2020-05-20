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
    void updateNextCheckTime() {
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
            dispatcher.updateTaskStatus("5e8f0102fa09ae5a06e2600f", "to do");
            JSONObject jsonObject1 =         dispatcher.fetchObject("robots/tasks/", "5e8f0102fa09ae5a06e2600f");
            assertEquals(new String("to do"), jsonObject1.getString("status"));
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
    }

    @Test
    void sendRobotsToCharge() {
    }

    @Test
    void restoreRobotsAndTasks() {
    }

    @Test
    void chooseRobotAndTask() {
    }

}