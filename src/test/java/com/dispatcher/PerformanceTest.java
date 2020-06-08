package com.dispatcher;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class PerformanceTest {

    ArrayList<Future> addFutures = new ArrayList<Future>();
    ArrayList<Future> updateFutures = new ArrayList<Future>();

    @Test
    void checkLargeNumberOfRobotsAndTasks() throws ExecutionException, InterruptedException {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.initWebTarget();
        JSONObject robotData = new JSONObject(new JSONTokener(this.getClass().getResourceAsStream("test_robot.json")));
        JSONObject taskData = new JSONObject(new JSONTokener(this.getClass().getResourceAsStream("test_task.json")));
        WebTarget createRobot = dispatcher.getWebTarget().path("robots/add");
        WebTarget robotUpdate = dispatcher.getWebTarget().path("robots/update");
        WebTarget createTask = dispatcher.getWebTarget().path("robots/tasks/add");
        WebTarget taskDelete = dispatcher.getWebTarget().path("robots/tasks/delete");
        int numberOfTests = 50;
        for (int i = 0; i < numberOfTests; i++) {
            String robotId = String.format("robot%d", i);
            String taskId = String.format("task%d", i);
            robotData.put("id", robotId);
            taskData.put("id", taskId);
            Future<Response> robotAddFuture = robotUpdate.request(MediaType.APPLICATION_JSON_TYPE).async().post(Entity.json(robotData.toString()));
            Future<Response> taskAddFuture = createTask.request(MediaType.APPLICATION_JSON_TYPE).async().post(Entity.json(taskData.toString()));
            addFutures.add(robotAddFuture);
            addFutures.add(taskAddFuture);
            if (i % 10 == 0) {
                resolveFutures(addFutures);
            }
        }
        dispatcher.run();
        for (int i = 0; i < numberOfTests; i++) {
            String robotId = String.format("robot%d", i);
            String taskId = String.format("task%d", i);
            robotData.put("id", robotId);
            robotData.put("available", false);
            taskData.put("id", taskId);
            Future<Response> robotUpdateFuture = robotUpdate.request(MediaType.APPLICATION_JSON_TYPE).async().post(Entity.json(robotData.toString()));
            Future<Response> taskDeleteFuture = taskDelete.request(MediaType.APPLICATION_JSON_TYPE).async().post(Entity.json(robotData.toString()));
            updateFutures.add(robotUpdateFuture);
            updateFutures.add(taskDeleteFuture);
            if (i % 10 == 0) {
                resolveFutures(updateFutures);
            }
        }
    }

    void resolveFutures(ArrayList<Future> futures) throws ExecutionException, InterruptedException {
        for (Future future : futures) {
            future.get();
        }
        futures.clear();
    }
}
