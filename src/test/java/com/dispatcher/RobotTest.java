package com.dispatcher;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class RobotTest {

    Robot robot = new Robot(new JSONObject("{\n" + "  \"id\": \"qwerty\",\n" + "  \"robotIp\": \"123.123.123.12\",\n" +
            "  \"available\": false,\n" + "  \"extraRobotElement\": {\n" + "    \"id\": \"5e3aee7617f5305d51b5a53a\",\n" +
            "    \"name\": \"qwerty\",\n" + "    \"dimensions\": \"22x22\",\n" + "    \"functionalityList\": [\n" +
            "      {\n" + "        \"id\": \"5e36ba105d542c1d96fa2087\",\n" + "        \"name\": \"oświetlenie\"\n" +
            "      },\n" + "      {\n" + "        \"id\": \"5e36ba295d542c1d96fa2088\",\n" + "        \"name\": \"migacz\"\n" +
            "      },\n" + "      {\n" + "        \"id\": \"5e36ba105d542c1d96fa2087\",\n" + "        \"name\": \"oświetlenie\"\n" +
            "      },\n" + "      {\n" + "        \"id\": \"5e36ba295d542c1d96fa2088\",\n" + "        \"name\": \"migacz\"\n" +
            "      },\n" + "      {\n" + "        \"id\": \"5e36ba105d542c1d96fa2087\",\n" + "        \"name\": \"oświetlenie\"\n" +
            "      }\n" + "    ]\n" + "  },\n" + "  \"model\": {\n" + "    \"id\": \"5e19e3b29d0ce61f6f234128\",\n" +
            "    \"name\": \"R2D2\",\n" + "    \"maxLiftingCapacity\": \"500kg\",\n" + "    \"maxVelocity\": \"20km/h\",\n" +
            "    \"length\": \"200cm\",\n" + "    \"width\": \"250cm\",\n" + "    \"height\": \"200cm\",\n" +
            "    \"turningRadius\": \"30 deg\",\n" + "    \"propulsionType\": {\n" + "      \"id\": \"5e19e3b19d0ce61f6f23410a\",\n" +
            "      \"name\": \"mechanical drive\"\n" + "    },\n" + "    \"batteryType\": {\n" + "      \"id\": null,\n" +
            "      \"name\": \"Lithium Ion\",\n" + "      \"capacity\": \"3200\",\n" + "      \"ratedVoltage\": \"2.1\",\n" +
            "      \"maxCurrent\": \"9.0\"\n" + "    }\n" + "  },\n" + "  \"battery\": {\n" + "    \"id\": null,\n" +
            "    \"productionDate\": null,\n" + "    \"type\": {\n" + "      \"id\": null,\n" + "      \"name\": null,\n" +
            "      \"capacity\": null,\n" + "      \"ratedVoltage\": null,\n" +"      \"maxCurrent\": null\n" + "    }\n" +
            "  },\n" + "  \"pose\": {\n" + "    \"position\": {\n" + "      \"x\": 0,\n" + "      \"y\": 0,\n" + "      \"z\": 0\n" +
            "    },\n" + "    \"orientation\": {\n" + "      \"x\": 0,\n" + "      \"y\": 0,\n" + "      \"z\": 0,\n" +
            "      \"w\": 0\n" + "    }\n" +"  },\n" +"  \"batteryLevel\": 100,\n" + "  \"status\": [\n" + "    {\n" +
            "      \"id\": \"5e19e3b19d0ce61f6f23410e\",\n" + "      \"name\": \"on the way\"\n" + "    },\n" +
            "    {\n" + "      \"id\": \"5e19e3b19d0ce61f6f23410f\",\n" + "      \"name\": \"charging needed\"\n" +
            "    }\n" + "  ],\n" + "  \"timestamp\": \"28/04/2020 14:37:24\"\n" + "}"));

    Task task = new Task(new JSONObject("{\n" + "  \"id\": \"5e8f0102fa09ae5a06e2600f\",\n" + "  \"robot\": {\n" +
            "    \"id\": \"string\",\n" + "    \"robotIp\": \"string\",\n" + "    \"available\": false,\n" +
            "    \"extraRobotElement\": {\n" + "      \"id\": \"string\",\n" + "      \"name\": \"string\",\n" +
            "      \"dimensions\": \"string\",\n" + "      \"functionalityList\": [\n" + "        {\n" +
            "          \"id\": \"string\",\n" + "          \"name\": \"string\"\n" + "        }\n" + "      ]\n" +
            "    },\n" + "    \"model\": {\n" + "      \"id\": \"string\",\n" + "      \"name\": \"string\",\n" +
            "      \"maxLiftingCapacity\": \"string\",\n" + "      \"maxVelocity\": \"string\",\n" + "      \"length\": \"string\",\n" +
            "      \"width\": \"string\",\n" + "      \"height\": \"string\",\n" + "      \"turningRadius\": \"string\",\n" +
            "      \"propulsionType\": {\n" + "        \"id\": \"string\",\n" + "        \"name\": \"string\"\n" + "      },\n" +
            "      \"batteryType\": {\n" + "        \"id\": \"string\",\n" + "        \"name\": \"string\",\n" +
            "        \"capacity\": \"string\",\n" + "        \"ratedVoltage\": \"string\",\n" + "        \"maxCurrent\": \"string\"\n" +
            "      }\n" + "    },\n" + "    \"battery\": {\n" + "      \"id\": \"string\",\n" + "      \"productionDate\": \"string\",\n" +
            "      \"type\": {\n" + "        \"id\": \"string\",\n" + "        \"name\": \"string\",\n" + "        \"capacity\": \"string\",\n" +
            "        \"ratedVoltage\": \"string\",\n" + "        \"maxCurrent\": \"string\"\n" + "      }\n" + "    },\n" +
            "    \"pose\": {\n" + "      \"position\": {\n" + "        \"x\": 0,\n" + "        \"y\": 0,\n" + "        \"z\": 0\n" +
            "      },\n" + "      \"orientation\": {\n" + "        \"x\": 0,\n" + "        \"y\": 0,\n" + "        \"z\": 0,\n" +
            "        \"w\": 0\n" + "      }\n" + "    },\n" + "    \"batteryLevel\": 0,\n" + "    \"status\": [\n" +
            "      {\n" + "        \"id\": \"string\",\n" + "        \"name\": \"string\"\n" + "      }\n" + "    ],\n" +
            "    \"timestamp\": \"string\"\n" + "  },\n" + "  \"name\": \"asdf\",\n" + "  \"behaviours\": [\n" + "    {\n" +
            "      \"id\": \"123456789\",\n" + "      \"name\": \"GO_TO\",\n" + "      \"parameters\": \"{\\\"start\\\":\\\"5e4691cf59f001700ceaf72a\\\",\\\"end\\\":\\\"5e4602ba9184b62beee348c9\\\"}\"\n" +
            "    },\n" + "    {\n" + "      \"id\": \"5e19e3b39d0ce61f6f23413f\",\n" + "      \"name\": \"DOCK\",\n" +
            "      \"parameters\": \"\"\n" + "    },\n" + "    {\n" + "      \"id\": \"5e19e3b39d0ce61f6f23413d\",\n" +
            "      \"name\": \"WAIT\",\n" + "      \"parameters\": \"{\\\"message\\\":\\\"string\\\"}\"\n" + "    },\n" +
            "    {\n" + "      \"id\": \"123456789\",\n" + "      \"name\": \"GO_TO\",\n" + "      \"parameters\": \"{\\\"start\\\":\\\"5e4602ba9184b62beee348c9\\\",\\\"end\\\":\\\"5e4691cf59f001700ceaf72a\\\"}\"\n" +
            "    },\n" + "    {\n" + "      \"id\": \"5e19e3b39d0ce61f6f23413d\",\n" + "      \"name\": \"WAIT\",\n" +
            "      \"parameters\": \"{\\\"message\\\":\\\"string\\\"}\"\n" + "    },\n" + "    {\n" + "      \"id\": \"5e19e3b39d0ce61f6f23413z\",\n" +
            "      \"name\": \"DOCK\",\n" + "      \"parameters\": \"* WILL BE JSON *\"\n" + "    },\n" + "    {\n" +
            "      \"id\": \"5e19e3b39d0ce61f6f23413d\",\n" + "      \"name\": \"WAIT\",\n" + "      \"parameters\": \"{\\\"message\\\":\\\"string\\\"}\"\n" +
            "    },\n" + "    {\n" + "      \"id\": \"5e19e3b39d0ce61f6f23413e\",\n" + "      \"name\": \"GO_TO\",\n" +
            "      \"parameters\": \"{\\\"destination\\\":\\\"Stand\\\"}\"\n" +"    }\n" + "  ],\n" + "  \"startTime\": \"9.04.2020, 20:28:31\",\n" +
            "  \"priority\": {\n" + "    \"id\": \"5e19e3b19d0ce61f6f23411a\",\n" + "    \"name\": \"critical\",\n" +
            "    \"weight\": 1\n" + "  },\n" + "  \"status\": \"to do\",\n" + "  \"userID\": \"5e3dac584db09f0fa80fda31\"\n" + "}"));

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
}