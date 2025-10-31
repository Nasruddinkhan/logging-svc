package com.markethub.loggin.consumer;

import com.markethub.loggin.model.LogMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class LogConsumerConfig {

    @Bean
    public Consumer<LogMessage> logConsumer() {
        return log -> {
            System.out.println("📥 Received log: " + log);
            // Save to DB or Elasticsearch
        };
    }
}