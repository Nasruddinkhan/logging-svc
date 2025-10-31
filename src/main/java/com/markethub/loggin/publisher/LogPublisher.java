package com.markethub.loggin.publisher;

import com.markethub.loggin.model.LogMessage;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
public class LogPublisher {

    private final StreamBridge streamBridge;
    public LogPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publishLog(LogMessage message) {
        streamBridge.send("logProducer-out-0", message);
        System.out.println("✅ Published log: " + message);
    }
}
