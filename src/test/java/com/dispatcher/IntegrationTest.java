package com.dispatcher;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.glassfish.jersey.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

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
    }

    @Test
    //Sprawdzenie czy robot po dodaniu do bazy zostanie dodany w czasie działania prgramu
    void checkAddRobotDuringExecution() {

    }

    @Test
    //Sprawdzenie czy robot po ustawieniu avaialable na false nie bedzie przydzielany do kolejnych zadan
    void checkRemoveRobotDuringExecution() {

    }

    @Test
    //Sprawdzenie czy usuniecie niewykonanego tasku usuwa go z kolejki
    void checkRemoveTaskDuringExecution() {

    }

    @Test
    //Sprawdzenie czy działa dodanie tasku podczas działania programu
    void checkAddTaskDuringExecution() {

    }

    @Test
    //Sprawdzenie zmiany statusu zadania na zawieszone
    void suspendTask() {

    }

    @Test
    //Sprawdzenie zmiany statsu robota na ładowanie
    void sendRobotToCharge() {

    }

    @Test
    //Sprawdzenie czy task został oznaczony jako poprawnie wykonany
    void checkTaskCorrectlyDone() {

    }

    @Test
    //Sprawdzenie czy zmienia sie status robota (dostepny / niedostepny)
    void checkRobotStatus() {

    }

    @Test
    //Sprawdzenie przyjęcia zadania do realizacji (jeśli task jest ok, to ustawiamy in-progress)
    void checkTaskConfirmation() {

    }

    @Test
    //Sprawdzenie odrzucenia zadania do realizacji (jeśli task jest niepoprawny ustawiamy rejected)
    void checkTaskRejection() {

    }

}