package com.dispatcher;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntegrationTest {

    @Test
    //Sprawdzenie poprawności połączenia z API
    void checkConnection() {
    }

    @Test
    //Sprawdzenie poprawnośći wszystkich zadań w API
    void checkTasks() {
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