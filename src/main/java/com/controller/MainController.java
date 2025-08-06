package com.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    @GetMapping("/api/v1/name")
    public String microserviceName() {
        return "This microservice is the [SEATING-SERVICE]!";
    }

    @GetMapping("/api/v1/processTopic")
    public void processTopic() {
        System.out.println("Received an incoming topic... Processing now!");

    }

}
