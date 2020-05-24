package com.dispatcher;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.glassfish.jersey.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegrationTest {

    @Test
    //Sprawdzenie poprawności połączenia z API
    void checkConnection() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.initWebTarget();
        WebTarget target = dispatcher.getWebTarget();
        int statusCode = target.path("robots/all").request(MediaType.APPLICATION_JSON).head().getStatus();
        assertEquals(200, statusCode);
    }

    @Test
    //Sprawdzenie poprawnośći wszystkich zadań w API
    void checkTasks() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.initWebTarget();
        JSONArray jsonArray = dispatcher.fetchData("robots/tasks/all");
        InputStream inputStream = this.getClass().getResourceAsStream("task_schema.json");
        JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
        Schema schema = SchemaLoader.load(rawSchema);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            schema.validate(jsonObject);
        }
    }

    @Test
    //Sprawdzenie poprawności robotów w bazie danych
    void checkRobots() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.initWebTarget();
        JSONArray jsonArray = dispatcher.fetchData("robots/all");
        InputStream inputStream = this.getClass().getResourceAsStream("robot_schema.json");
        JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
        Schema schema = SchemaLoader.load(rawSchema);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            schema.validate(jsonObject);
        }
    }

    @Test
    //Sprawdzenie czy robot po ustawieniu robta na dostepny zostanie przydzielony do zadań
    void checkAddRobotDuringExecution() {
        String id = "5e19e3b29d0ce61f6f234129";
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.initWebTarget();
        dispatcher.updateRobotAvailability(id, true);
        dispatcher.run();
        assertTrue(dispatcher.getRobots().stream().filter(robot -> robot.getId().equals(id)).toArray().length > 0);
    }

    @Test
    //Sprawdzenie czy robot po ustawieniu avaialable na false nie bedzie przydzielany do kolejnych zadan
    void checkRemoveRobotDuringExecution() {
        String id = "5e19e3b29d0ce61f6f234129";
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.initWebTarget();
        dispatcher.updateRobotAvailability(id, false);
        dispatcher.run();
        assertEquals(0, dispatcher.getBusyRobots().stream().filter(robot -> robot.getId().equals(id)).toArray().length);
    }

    @Test
    //Sprawdzenie czy usuniecie niewykonanego tasku usuwa go z kolejki
    void checkRemoveTaskDuringExecution() {
        String id = "5e8f0102fa09ae5a06e2600f";
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.initWebTarget();
        dispatcher.updateTaskStatus(id, "error");
        dispatcher.run();
        assertEquals(0, dispatcher.getTasks().stream().filter(task -> task.getId().equals(id)).toArray().length);
    }

    @Test
    //Sprawdzenie czy działa dodanie tasku podczas działania programu
    void checkAddTaskDuringExecution() {
        String id = "5e8f0102fa09ae5a06e2600f";
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.initWebTarget();
        dispatcher.updateTaskStatus(id, "new");
        dispatcher.run();
        assertTrue( dispatcher.getTasks().stream().filter(task -> task.getId().equals(id)).toArray().length > 0);
    }

    @Test
    //Sprawdzenie zmiany statusu zadania na zawieszone
    void suspendTask() {
        String id = "5e8f0102fa09ae5a06e2600f";
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.initWebTarget();
        dispatcher.updateTaskStatus(id, "suspended");
        dispatcher.run();
        assertEquals(0, dispatcher.getTasks().stream().filter(task -> task.getId().equals(id)).toArray().length);
    }

    @Test
    //Sprawdzenie zmiany statsu robota na ładowanie
    void sendRobotToCharge() {

    }

    @Test
    //Sprawdzenie czy task został oznaczony jako poprawnie wykonany
    void checkTaskCorrectlyDone() {
        String robotId = "5e19e3b29d0ce61f6f234129";
        String taskId = "5e8f0102fa09ae5a06e2600f";
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.initWebTarget();
        dispatcher.updateTaskStatus(taskId, "new");
        dispatcher.updateRobotAvailability(robotId, true);
        dispatcher.run();
        assertEquals(0, dispatcher.getTasks().stream().filter(task -> task.getId().equals(taskId)).toArray().length);
    }

    @Test
    //Sprawdzenie czy zmienia sie status robota (dostepny / niedostepny)
    void checkRobotStatus() {
        String id = "5e19e3b29d0ce61f6f234129";
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.initWebTarget();
        dispatcher.updateRobotAvailability(id, false);
        dispatcher.run();
        assertEquals(0, dispatcher.getRobots().stream().filter(robot -> robot.getId().equals(id)).toArray().length);
        dispatcher.updateRobotAvailability(id, true);
        dispatcher.run();
        assertEquals(1, dispatcher.getRobots().stream().filter(robot -> robot.getId().equals(id)).toArray().length);
    }

    @Test
    //Sprawdzenie przyjęcia zadania do realizacji (jeśli task jest ok, to ustawiamy in-progress)
    void checkTaskConfirmation() {
        String robotId = "5e19e3b29d0ce61f6f234129";
        String taskId = "5e8f0102fa09ae5a06e2600f";
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.initWebTarget();
        dispatcher.updateTaskStatus(taskId, "new");
        dispatcher.updateRobotAvailability(robotId, true);
        dispatcher.run();
        JSONObject inProgressTask  = dispatcher.fetchObject("robots/tasks/", taskId);
        assertTrue(inProgressTask.toString().contains("status\":\"in progress\""));
    }

    @Test
    //Sprawdzenie odrzucenia zadania do realizacji
    void checkTaskRejection() {
        String id = "5e8f0102fa09ae5a06e2600f";
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.initWebTarget();
        dispatcher.updateTaskStatus(id, "rejected");
        dispatcher.run();
        assertEquals(0, dispatcher.getTasks().stream().filter(task -> task.getId().equals(id)).toArray().length);
    }

}