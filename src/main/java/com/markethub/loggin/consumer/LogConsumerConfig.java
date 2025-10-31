package com.markethub.loggin.consumer;

import com.markethub.loggin.model.LogMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class LogConsumerConfig {

    @Bean
    public Consumer<LogMessage> logConsumer() {
        // Save to DB or Elasticsearch
        return message -> log.info("Received log: {} " , message);
    }
}