package com.dispatcher;

import java.io.IOException;
import java.net.MalformedURLException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

public class Main {
    public static void main(String[] args) throws IOException {
       try {
           com.dispatcher.Dispatcher dispatcher = new com.dispatcher.Dispatcher();
           for (int i = 0; i < 5; i++) {
               dispatcher.assignTasks(); //symulacja działania w pętli
           }
       }
       catch(Exception ex) {
           ex.printStackTrace();
       }
    }
}
