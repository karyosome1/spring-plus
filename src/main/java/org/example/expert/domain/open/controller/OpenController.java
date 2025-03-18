package org.example.expert.domain.open.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenController {
    @GetMapping("/open")
    public String open() {
        return "Welcome to the application!";
    }
}
