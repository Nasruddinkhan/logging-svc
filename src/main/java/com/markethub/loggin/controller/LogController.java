package com.markethub.loggin.controller;


import com.markethub.loggin.model.LogMessage;
import com.markethub.loggin.publisher.LogPublisher;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogPublisher publisher;

    public LogController(LogPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/send")
    public String sendLog(@RequestParam String level, @RequestParam String message) {
        LogMessage log = LogMessage.builder()
                .level(level)
                .message(message)
                .serviceName("logging-svc")
                .timestamp(Instant.now().toString())
                .build();

        publisher.publishLog(log);
        return "✅ Log published successfully!";
    }
}