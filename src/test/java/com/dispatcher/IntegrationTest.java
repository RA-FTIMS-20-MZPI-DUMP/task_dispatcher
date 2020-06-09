package com.dispatcher;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.Test;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegrationTest {

    String taskId = "5e8f0102fa09ae5a06e2600f";
    String robotId = "5e19e3b29d0ce61f6f234129";
    
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
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.initWebTarget();
        JSONObject jsonObject = dispatcher.fetchObject("robots/", robotId);
        Robot robot = new Robot(jsonObject);
        dispatcher.updateRobotAvailability(robot, true);
        dispatcher.run();
        assertTrue((dispatcher.getBusyRobots().stream().filter(r -> r.getId().equals(robot.getId())).toArray().length > 0)
                    || (dispatcher.getRobots().stream().filter(r -> r.getId().equals(robot.getId())).toArray().length > 0));
    }

    @Test
    //Sprawdzenie czy robot po ustawieniu avaialable na false nie bedzie przydzielany do kolejnych zadan
    void checkRemoveRobotDuringExecution() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.initWebTarget();
        JSONObject jsonObject = dispatcher.fetchObject("robots/", robotId);
        Robot robot = new Robot(jsonObject);
        dispatcher.updateRobotAvailability(robot, false);
        dispatcher.run();
        assertEquals(0, dispatcher.getBusyRobots().stream().filter(r -> r.getId().equals(robot.getId())).toArray().length);
    }

    @Test
    //Sprawdzenie czy usuniecie niewykonanego tasku usuwa go z kolejki
    void checkRemoveTaskDuringExecution() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.initWebTarget();
        JSONObject jsonObject = dispatcher.fetchObject("robots/tasks/", taskId);
        Task task = new Task(jsonObject);
        dispatcher.updateTaskStatus(task, "error");
        dispatcher.run();
        assertEquals(0, dispatcher.getTasks().stream().filter(t -> t.getId().equals(task.getId())).toArray().length);
    }

    @Test
    //Sprawdzenie czy działa dodanie tasku podczas działania programu
    void checkAddTaskDuringExecution() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.initWebTarget();
        JSONObject jsonObject = dispatcher.fetchObject("robots/tasks/", taskId);
        Task task = new Task(jsonObject);
        dispatcher.updateTaskStatus(task, "new");
        dispatcher.run();
        assertTrue( dispatcher.getTasks().stream().filter(t -> t.getId().equals(task.getId())).toArray().length > 0);
    }

    @Test
    //Sprawdzenie zmiany statusu zadania na zawieszone
    void suspendTask() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.initWebTarget();
        JSONObject jsonObject = dispatcher.fetchObject("robots/tasks/", taskId);
        Task task = new Task(jsonObject);
        dispatcher.updateTaskStatus(task, "suspended");
        dispatcher.run();
        assertEquals(0, dispatcher.getTasks().stream().filter(t -> t.getId().equals(task.getId())).toArray().length);
    }

    @Test
    void sendRobotToCharge() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.initWebTarget();
        dispatcher.fetchPoints();
        Robot robot1 = new Robot(new JSONObject(new JSONTokener(this.getClass().getResourceAsStream("test_robot2.json"))));
        dispatcher.getRobots().add(robot1);
        dispatcher.sendRobotsToCharge();
        dispatcher.waitForFutures();
        JSONObject jsonObject = dispatcher.fetchObject("robots/", robot1.getId());
        assertFalse(jsonObject.getBoolean("available"));
    }

    @Test
    //Sprawdzenie czy task został oznaczony jako poprawnie wykonany
    void checkTaskCorrectlyDone() {
        Date date = new Date();
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.initWebTarget();
        this.resetDbData(dispatcher);
        dispatcher.run();
        Robot robot = dispatcher.getBusyRobots().get(0);
        robot.setAvailableOn(date);
        dispatcher.restoreRobotsAndTasks();
        dispatcher.waitForFutures();
        JSONObject doneTask = dispatcher.fetchObject("robots/tasks/", robot.getCurrentTask().getId());
        assertEquals("done", doneTask.getString("status"));
    }

    @Test
    //Sprawdzenie czy zmienia sie status robota (dostepny / niedostepny)
    void checkRobotStatus() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.initWebTarget();
        JSONObject jsonObject = dispatcher.fetchObject("robots/", robotId);
        Robot robot = new Robot(jsonObject);
        dispatcher.updateRobotAvailability(robot, false);
        dispatcher.waitForFutures();
        dispatcher.run();
        assertEquals(0, dispatcher.getRobots().stream().filter(r -> r.getId().equals(robot.getId())).toArray().length);
        dispatcher.updateRobotAvailability(robot, true);
        dispatcher.waitForFutures();
        dispatcher.fetchAvailableRobots();
        assertEquals(1, dispatcher.getRobots().stream().filter(r -> r.getId().equals(robot.getId())).toArray().length);
    }

    @Test
    //Sprawdzenie przyjęcia zadania do realizacji (jeśli task jest ok, to ustawiamy in-progress)
    void checkTaskConfirmation() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.initWebTarget();
        this.resetDbData(dispatcher);
        dispatcher.run();
        String inProgressId = dispatcher.getBusyRobots().get(0).getCurrentTask().getId();
        JSONObject inProgressTask = dispatcher.fetchObject("robots/tasks/", inProgressId);
        assertTrue(inProgressTask.toString().contains("in progress"));
    }

    @Test
    //Sprawdzenie odrzucenia zadania do realizacji
    void checkTaskRejection() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.initWebTarget();
        JSONObject jsonObject = dispatcher.fetchObject("robots/tasks/", taskId);
        Task task = new Task(jsonObject);
        dispatcher.updateTaskStatus(task, "rejected");
        dispatcher.run();
        assertEquals(0, dispatcher.getTasks().stream().filter(t -> t.getId().equals(task.getId())).toArray().length);
    }

    void resetDbData(Dispatcher dispatcher) {
        JSONObject jsonObject = dispatcher.fetchObject("robots/", robotId);
        JSONObject jsonObject1 = dispatcher.fetchObject("robots/tasks/", taskId);
        Robot robot = new Robot(jsonObject);
        Task task = new Task(jsonObject1);
        dispatcher.updateTaskStatus(task, "new");
        dispatcher.updateRobotAvailability(robot, true);
        dispatcher.waitForFutures();
    }

}