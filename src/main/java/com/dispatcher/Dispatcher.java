package com.dispatcher;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class Dispatcher {

    private WebTarget webTarget;
    private ArrayList<Robot> availableRobots = new ArrayList<Robot>(); //dostepne roboty
    private ArrayList<Robot> robotsWithTasks = new ArrayList<Robot>(); //zajete roboty
    private ArrayList<Task> tasks = new ArrayList<Task>(); //zadania
    private HashMap<String, Point> points = new HashMap<String, Point>();

    // + sprawdzanie czy jest kontakt z robotami - może Paweł przez Websocketa?
    // + sprawdzanie czy robot nie utknął - nie mam pojęcia :(

    // zwraca roboty, które skończą zadania za mniej niż 10 sekund
    // a przy okazji sprawdza któryś z robotów już skończył zadanie i zmienia mu statusy
    private void findFinishingRobots() throws IOException {
        try {
            for(int i=0; i<robotsWithTasks.size(); i++) {
            Duration duration = Duration.between(robotsWithTasks.get(i).getCurrentTask().getExecutionStart(), LocalDateTime.now());
            long seconds = duration.getSeconds();
            if (robotsWithTasks.get(i).getCurrentTask().getTime() - seconds <= 0) {
                robotsWithTasks.get(i).getCurrentTask().setStatus("done");
                updateTask(robotsWithTasks.get(i).getCurrentTask().getId(), "status", "done");
                robotsWithTasks.get(i).setCurrentTask(null);
                if(!findRobot(robotsWithTasks.get(i).getId())) {
                    availableRobots.add(robotsWithTasks.get(i));
                }
                updateRobot(robotsWithTasks.get(i).getId(), "status", "free");
                updateRobot(robotsWithTasks.get(i).getId(), "available", "true");
                robotsWithTasks.remove(robotsWithTasks.get(i));
            } else if (robotsWithTasks.get(i).getCurrentTask().getTime() - seconds <= 10) {
                availableRobots.add(robotsWithTasks.get(i));
            } } }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }

    // zwraca robota, który ma wykonac zadanie
    private Robot chooseRobot(Task task) {
       // musi tu byc sprawdzenie poziomu baterii - jesli jest nizszy niz 10% wtedy:
       // updateRobot(availableRobots.get(i).getId(), "status", "charging needed");
        if (availableRobots.size() > 0) {
            return availableRobots.get(0);
        }
        return null;
    }

    private boolean findRobot(String id){
        for (int i=0; i<availableRobots.size(); i++){
            if(availableRobots.get(i).getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    // zwraca wolne roboty z bazy (!!! nie mogą mieć zadnych nulli poza battery !!!)
    private void fetchAvailableRobots() {
        JSONArray jsonArray = fetchData("robots/all");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            boolean noStartPosition = jsonObject.isNull("pose");
            if (noStartPosition) {
                System.out.println("No start position for robot");
                continue;
            }
            if(jsonObject.get("robotIp").equals(null)){
                System.out.println("Unknown robot IP");
                continue;
            }
            if(jsonObject.getJSONObject("model").get("maxVelocity").equals(null)){
                System.out.println("Unknown max velocity");
                continue;
            }
            if(jsonObject.get("batteryLevel").equals(null)){
                System.out.println("Unknown battery level");
                continue;
            }
            boolean available = jsonObject.getBoolean("available");
            if (available && checkRobot(jsonObject)) {
                Robot newRobot = new Robot(jsonObject);
                if(!findRobot(newRobot.getId())){
                    availableRobots.add(newRobot);
                }
            }
        }
    }

    // póki co dziala dla dodawania nowego statusu i zmieniania available
    private void updateRobot(String id, String field, String value) throws IOException {
        try {
            JSONArray jsonArray = fetchData("robots/all");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if(jsonObject.getString("id").equals(id)){
                    if(field == "available") {
                        boolean availability = Boolean.getBoolean(value);
                        jsonObject.put(field, availability);}
                    else if (field == "status"){
                        JSONObject status = new JSONObject();
                        status.put("id", value);
                        status.put("name", value);
                        jsonObject.getJSONArray("status").put(status);
                    }
                    String curl = "curl -X POST -u dispatchTest:gBrZzVbCbMsr \"http://adrastea.westus2.cloudapp.azure.com:3333/robots/update\" -H  \"accept: */*\" -H  \"Content-Type: application/json\" -d \"" + transformJsonRobot(jsonObject) + "\"";
                    Runtime.getRuntime().exec("cmd cd C:\\Users\\WIN10\\Downloads\\curl-7.69.1_2-win64-mingw\\curl-7.69.1-win64-mingw\\bin");
                    Runtime.getRuntime().exec(curl);                }
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    // zwraca następne zadanie do wykonania
    private Task chooseTask() {
        Task taskToDo = new Task();
        for (Task task : this.tasks){ // inicjalizacja zadania pierwszym z brzegu
            if (task.getStatus().equals("new")){
                taskToDo = task;
                break;
            }
        }

        for (Task task : this.tasks) { // wybranie zadania z najwyzszym priorytetem i najszybszym czasem
            if (task.getStatus().equals("new") && task.getPriority() < taskToDo.getPriority()){
                taskToDo = task;
            }
            if (task.getStatus().equals("new") && task.getPriority() == taskToDo.getPriority()){
                if (task.getTime() < taskToDo.getTime()){
                    taskToDo = task;
                }
            }
        }
        return taskToDo;
    }

    // aktualizacja priorytetów jeśli zadanie oczekuje wiecej niz 30 sekund
    private void updatePriority() throws IOException {
        try {
            for (Task task : this.tasks) {
                Duration duration = Duration.between(task.getAppearanceTime(), LocalDateTime.now());
                long seconds = duration.getSeconds();
                if (seconds > 30 && task.getStatus().equals("new")) {
                    if (task.getPriority() != 1) {
                        task.setPriority(task.getPriority() - 1);
                        updateTask(task.getId(), "priority", String.valueOf(task.getPriority()));
                    }
                }
            }
        }
         catch(Exception ex){
                ex.printStackTrace();
            }
    }

    private boolean findTask(String id){
        for (int i=0; i<tasks.size(); i++){
            if(tasks.get(i).getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    // update statusu tasku - póki co zmiana statusu i prioritetu
    private void updateTask(String id, String field, String value) throws IOException {
        try {
            JSONArray jsonArray = fetchData("robots/tasks/all");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if(jsonObject.getString("id").equals(id)){
                    if(field.equals("priority")){
                        jsonObject.getJSONObject("priority").put("weight", value);
                    }
                    else{
                        jsonObject.put(field, value);
                    }
                    String curl = "curl -X POST -u dispatchTest:gBrZzVbCbMsr \"http://adrastea.westus2.cloudapp.azure.com:3333/robots/tasks/update\" -H  \"accept: */*\" -H  \"Content-Type: application/json\" -d \"" + transformJsonTask(jsonObject) + "\"";
                    Runtime.getRuntime().exec("cmd cd C:\\Users\\WIN10\\Downloads\\curl-7.69.1_2-win64-mingw\\curl-7.69.1-win64-mingw\\bin");
                    Runtime.getRuntime().exec(curl); //"curl -X POST -u dispatchTest: \"http://adrastea.westus2.cloudapp.azure.com:3333/robots/tasks/update\" -H  \"accept: */*\" -H  \"Content-Type: application/json\" -d \"{\"robot\":null,\"name\":\"TestTaks1\",\"startTime\":null,\"id\":\"5e8efec5fa09ae5a06e2600e\",\"priority\":{\"name\":\"important\",\"weight\":2,\"id\":\"5e19e3b19d0ce61f6f23411b\"},\"behaviours\":[{\"name\":\"GO_TO\",\"id\":null,\"parameters\":\"{\\\"start\\\":\\\"5e4691cf59f001700ceaf72a\\\",\\\"end\\\":\\\"5e4602ba9184b62beee348c9\\\"}\"},{\"name\":\"DOCK\",\"id\":null,\"parameters\":\"\"},{\"name\":\"WAIT\",\"id\":null,\"parameters\":\"{\\\"time\\\":5}\"},{\"name\":\"GO_TO\",\"id\":null,\"parameters\":\"{\\\"start\\\":\\\"5e4602ba9184b62beee348c9\\\",\\\"end\\\":\\\"5e4691cf59f001700ceaf72a\\\"}\"}],\"userID\":null,\"status\":\"in progress\"}");
                }
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    // wybieranie z Mongo nowych zadań (nie mogą mieć pustych name, userID, status oraz behaviours) !!! ROBOT MUSI BYC NULLem !!!
    private void fetchTasks() throws IOException {
        try {
            JSONArray jsonArray = fetchData("robots/tasks/all");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if((!jsonObject.get("name").equals(null)) && (!jsonObject.get("userID").equals(null))
                        && (!jsonObject.get("status").equals(null)) && (jsonObject.get("status").equals("new"))) {
                    if(checkBehaviours(jsonObject.getJSONArray("behaviours"))){
                        Task newTask = new Task(jsonObject, this.points);
                        if(!findTask(newTask.getId())){
                            tasks.add(newTask);
                        }
                    }
                }
                else if (jsonObject.get("status").equals("new")) {
                    updateTask(jsonObject.getString("id"), "status", "rejected");
                }
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    // inicjalizacja połączenia
    private void initWebTarget() {
        ClientConfig clientConfig = new ClientConfig();
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic("dispatchTest", "gBrZzVbCbMsr");
        clientConfig.register(feature);
        Client client = ClientBuilder.newClient(clientConfig);
        this.webTarget = client.target("http://adrastea.westus2.cloudapp.azure.com:3333/");
    }

    private JSONArray fetchData(String path) {
        WebTarget data = this.webTarget.path(path);
        String json = data.request(MediaType.APPLICATION_JSON).get(String.class);
        return new JSONArray(json);
    }

    private void fetchPoints() {
        this.points.clear();
        JSONArray jsonArray = fetchData("movement/stands/all");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            this.points.put(
                    jsonObject.getString("id"),
                    new Point(jsonObject)
            );
        }
    }

    public void assignTasks() throws IOException {
       try {
           this.initWebTarget();
           this.fetchPoints();
           this.fetchAvailableRobots(); // wybranie robotów z bazy
           this.findFinishingRobots(); // wybranie robotów, które kończą zadanie
           this.fetchTasks(); // wybranie zadań z bazy
           this.updatePriority(); // aktualizacja priorytetów
           Task toDo = chooseTask(); // wybranie zadania do wykonania
           if (toDo.getId() != null){
               Robot chosenRobot = chooseRobot(toDo); // wybranie robota do wykonania zadania
               chosenRobot.setCurrentTask(toDo);
               robotsWithTasks.add(chosenRobot);
               availableRobots.remove(chosenRobot);
               updateRobot(chosenRobot.getId(), "available", "false");
               updateRobot(chosenRobot.getId(), "status", "during task");
               toDo.setStatus("in progress");
               updateTask(toDo.getId(), "status", "in progress");
               // tu wysłanie komunikatu do robota
               toDo.setExecutionStart(LocalDateTime.now());
               Double time = chosenRobot.getTaskExecutionTime(toDo);
               System.out.printf("Robot: %s Task: %s Execution time: %f\n", chosenRobot.getId(), toDo.getName(), time);
           }
       }
        catch(Exception ex){
           ex.printStackTrace();
        }
    }

    // konfiguracja stringa do CURLa = update statusu dla Taska
    private String transformJsonTask(JSONObject jsonObject){
        JSONArray behaviours = jsonObject.getJSONArray("behaviours");
        String json = "{\\\"robot\\\":null,\\\"name\\\":\\\"" + jsonObject.getString("name") +
                "\\\",\\\"startTime\\\":";
        if(jsonObject.get("startTime") == null ){
           json += jsonObject.get("startTime");
        }
        else {
            json += "\\\"" + jsonObject.get("startTime") + "\\\"";
        }
        json += ",\\\"id\\\":\\\"" +
        jsonObject.getString("id") + "\\\",\\\"priority\\\":{\\\"name\\\":\\\"" +
        jsonObject.getJSONObject("priority").getString("name") + "\\\",\\\"weight\\\":" + jsonObject.getJSONObject("priority").getInt("weight")
        + ",\\\"id\\\":\\\"" + jsonObject.getJSONObject("priority").getString("id") + "\\\"},\\\"behaviours\\\":[";
        for(int i=0; i<behaviours.length(); i++){
            JSONObject behaviour = behaviours.getJSONObject(i);
            json += "{\\\"name\\\":\\\"" + behaviour.getString("name") + "\\\",\\\"id\\\":\\\""
                    + behaviour.get("id") + "\\\",\\\"parameters\\\":\\\"";
            String parameters = behaviour.getString("parameters");
            String newParameters = "";
            if(behaviour.getString("name").equals("DOCK")){
                json += parameters + "\\\"}";
            }
            else if (behaviour.getString("name").equals("WAIT")) {
                for(int j=0; j<parameters.length(); j++){
                    if(parameters.charAt(j) == '"'){
                        newParameters += "\\\\\\";
                    }
                    newParameters += parameters.charAt(j);
                }
                json += newParameters + "\\\"}";
            }
            else if (behaviour.getString("name").equals("GO_TO")){
                for(int j=0; j<parameters.length(); j++){
                    if(parameters.charAt(j) == '"'){
                        newParameters += "\\\\\\";
                    }
                    newParameters += parameters.charAt(j);
                }
            json += newParameters + "\\\"}";
            }

            if(i != behaviours.length()-1) {
                json += ",";
            }
        }
        json += "],\\\"userID\\\":\\\"" + jsonObject.get("userID") +
                "\\\",\\\"status\\\":\\\"" + jsonObject.getString("status") + "\\\"}";
        return json;
    }

    // konfiguracja robota do CURLa
    private String transformJsonRobot(JSONObject jsonObject) {
        String json = "{\\\"extraRobotElement\\\":{";
        JSONObject extraElement = jsonObject.getJSONObject("extraRobotElement");
        json += "\\\"name\\\":\\\"" + extraElement.getString("name") + "\\\",\\\"id\\\":\\\"" +
                extraElement.getString("id") + "\\\",\\\"functionalityList\\\":";
        if(extraElement.get("functionalityList").equals(null)){
            json += "null";
        }
        else{
            json += "[";
            JSONArray functionalities = extraElement.getJSONArray("functionalityList");
            for(int j=0; j<functionalities.length(); j++){
                json += "{\\\"name\\\":\\\"" + functionalities.getJSONObject(j).getString("name") +
                        "\\\",\\\"id\\\":\\\"" + functionalities.getJSONObject(j).getString("id") +
                        "\\\"}";
                if(j != functionalities.length()-1){
                    json += ",";
                }
            }
            json += "]";
        }
            json += ",\\\"dimensions\\\":\\\"" + extraElement.getString("dimensions") +
                    "\\\"},\\\"pose\\\":{";
            JSONObject pose = jsonObject.getJSONObject("pose");
            json += "\\\"orientation\\\":{\\\"w\\\":" + pose.getJSONObject("orientation").getInt("w") +
                    ",\\\"x\\\":" + pose.getJSONObject("orientation").getDouble("x") + ",\\\"y\\\":" +
                    pose.getJSONObject("orientation").getDouble("y") + ",\\\"z\\\":" +
                    pose.getJSONObject("orientation").getDouble("z") + "},\\\"position\\\":{\\\"x\\\":" +
                    pose.getJSONObject("position").getDouble("x") + ",\\\"y\\\":" +
                    pose.getJSONObject("position").getDouble("y") + ",\\\"z\\\":" +
                    pose.getJSONObject("position").getDouble("z") + "}},\\\"robotIp\\\":\\\"" +
                    jsonObject.getString("robotIp") + "\\\",\\\"available\\\":" + jsonObject.getBoolean("available") +
                    ",\\\"model\\\":{\\\"maxLiftingCapacity\\\":\\\"" + jsonObject.getJSONObject("model").getString("maxLiftingCapacity") +
                    "\\\",\\\"propulsionType\\\":{\\\"name\\\":\\\"" + jsonObject.getJSONObject("model").getJSONObject("propulsionType").getString("name")
                    + "\\\",\\\"id\\\":\\\"" + jsonObject.getJSONObject("model").getJSONObject("propulsionType").getString("id") +
                    "\\\"},\\\"name\\\":\\\"" + jsonObject.getJSONObject("model").getString("name") +
                    "\\\",\\\"length\\\":\\\"" + jsonObject.getJSONObject("model").getString("length") +
                    "\\\",\\\"width\\\":\\\"" + jsonObject.getJSONObject("model").getString("width") +
                    "\\\",\\\"maxVelocity\\\":\\\"" + jsonObject.getJSONObject("model").getString("maxVelocity") +
                    "\\\",\\\"turningRadius\\\":\\\"" + jsonObject.getJSONObject("model").getString("turningRadius") +
                    "\\\",\\\"id\\\":\\\"" + jsonObject.getJSONObject("model").getString("id") +
                    "\\\",\\\"batteryType\\\":{\\\"maxCurrent\\\":\\\"" + jsonObject.getJSONObject("model").getJSONObject("batteryType").getString("maxCurrent") +
                    "\\\",\\\"ratedVoltage\\\":\\\"" + jsonObject.getJSONObject("model").getJSONObject("batteryType").getString("ratedVoltage") +
                    "\\\",\\\"name\\\":\\\"" + jsonObject.getJSONObject("model").getJSONObject("batteryType").getString("name") +
                    "\\\",\\\"id\\\":\\\"" + jsonObject.getJSONObject("model").getJSONObject("batteryType").getString("id") +
                    "\\\",\\\"capacity\\\":\\\"" + jsonObject.getJSONObject("model").getJSONObject("batteryType").getString("capacity") +
                    "\\\"},\\\"height\\\":\\\"" + jsonObject.getJSONObject("model").getString("height") +
                    "\\\"},\\\"id\\\":\\\"" + jsonObject.getString("id") + "\\\",\\\"battery\\\":" +
                    jsonObject.get("battery") + ",\\\"batteryLevel\\\":" + jsonObject.getDouble("batteryLevel") +
                    ",\\\"status\\\":[";
                    JSONArray status = jsonObject.getJSONArray("status");
                    for(int i=0; i<status.length(); i++){
                        json += "{\\\"name\\\":\\\"" + status.getJSONObject(i).getString("name") +
                                "\\\",\\\"id\\\":\\\"" + status.getJSONObject(i).getString("id") + "\\\"}";
                        if(i != status.length()-1){
                            json += ",";
                        }
                    }
                    json += "],\\\"timestamp\\\":\\\"" + jsonObject.getString("timestamp") + "\\\"}";
            return json;
        }

    // sprawdzenie poprawności robota z bazy
    private boolean checkRobot(JSONObject jsonObject){
        if(jsonObject.get("extraRobotElement").equals(null)){
            return false;
        }
        JSONObject extraElements = jsonObject.getJSONObject("extraRobotElement");
        if((extraElements.get("id").equals(null)) || (extraElements.get("name").equals(null))
                || (extraElements.get("dimensions").equals(null)) || (extraElements.get("functionalityList").equals(null))){
            return false;
        }
        JSONArray functionalities = extraElements.getJSONArray("functionalityList");
        for(int i=0; i<functionalities.length(); i++){
            if((functionalities.getJSONObject(i).get("id").equals(null)) ||
                    (functionalities.getJSONObject(i).get("name").equals(null))){
                return false;
            }
        }
        JSONObject model = jsonObject.getJSONObject("model");
        if((model.get("id").equals(null)) || (model.get("name").equals(null))
                || (model.get("maxLiftingCapacity").equals(null)) || (model.get("length").equals(null))
                || (model.get("width").equals(null)) || (model.get("height").equals(null))
                || (model.get("turningRadius").equals(null)) || (model.get("propulsionType").equals(null))
                || (model.getJSONObject("propulsionType").get("id").equals(null)) || (model.getJSONObject("propulsionType").get("name").equals(null))
                || (model.get("batteryType").equals(null)) || (model.getJSONObject("batteryType").get("id").equals(null))
                || (model.getJSONObject("batteryType").get("name").equals(null)) || model.getJSONObject("batteryType").get("capacity").equals(null)
                || (model.getJSONObject("batteryType").get("ratedVoltage").equals(null)) || (model.getJSONObject("batteryType").get("maxCurrent").equals(null))){
            return false;
        }
        if(jsonObject.get("timestamp").equals(null)){
            return false;
        }
        JSONArray status = jsonObject.getJSONArray("status");
        for(int i=0; i<status.length(); i++){
            if(status.getJSONObject(i).get("id").equals(null) || status.getJSONObject(i).get("name").equals(null)){
                return false;
            }
        }
        return true;
    }

    // sprawdzenie poprawnosci behaviours
    private boolean checkBehaviours(JSONArray behaviours){
        JSONObject jsonObject = null;
        for(int i=0; i<behaviours.length(); i++){
            jsonObject = behaviours.getJSONObject(i);
            if((jsonObject.get("id").equals(null)) || (jsonObject.get("name").equals(null))
                    || (jsonObject.get("parameters").equals(null)) ||
                    ((!jsonObject.get("name").equals("GO_TO")) && (!jsonObject.get("name").equals("DOCK"))
                            && (!jsonObject.get("name").equals("WAIT")))){
                return false;
            }
        }
        return true;
    }

}


