package com.dispatcher;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

public class Dispatcher {

    private WebTarget webTarget;
    private List<Robot> robots;
    private List<Task> tasks;

    private List<Robot> findUnused() {
        return null;
    } // zwraca listę robotów, które będą wolne za mniej niż 10 sekund

    private Task chooseTask() {
        return null;
    } //zwraca najbliższe zadanie do wykonania

    private void updatePriority() {

    } // aktualizacja priorytetów zadań

    private void initWebTarget() {
        ClientConfig clientConfig = new ClientConfig();
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic("dispatchTest", "gBrZzVbCbMsr");
        clientConfig.register(feature) ;
        Client client = ClientBuilder.newClient(clientConfig);
        this.webTarget = client.target("http://adrastea.westus2.cloudapp.azure.com:3333/");

    }

    private void fetchTasks() {
        WebTarget points =  this.webTarget.path("robots/tasks/all");
        JsonArray jsonArray = points.request(MediaType.APPLICATION_JSON).get(JsonArray.class);
        for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
            System.out.println(jsonObject);
        }
    }

    public void assignTasks() {
        this.initWebTarget();
        this.fetchTasks();
    }

}
