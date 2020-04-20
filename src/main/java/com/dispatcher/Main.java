package com.dispatcher;

public class Main {
    public static void main(String[] args) {
        com.dispatcher.Dispatcher dispatcher = new com.dispatcher.Dispatcher();
        for(int i=0; i<2; i++){
            dispatcher.assignTasks(); //symulacja działania w pętli
        }
    }
}
