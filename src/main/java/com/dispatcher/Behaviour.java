package com.dispatcher;

public class Behaviour {
    public enum Type {
        GO_TO, DOCK, WAIT;
    }

    private Type type;
    private float time;
}
