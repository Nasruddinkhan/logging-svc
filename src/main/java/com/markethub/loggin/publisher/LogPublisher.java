package com.markethub.loggin.publisher;

import com.markethub.loggin.model.LogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
@RequiredArgsConstructor
@Service
@Slf4j
public class LogPublisher {

    private final StreamBridge streamBridge;

    public void publishLog(LogMessage message) {
        streamBridge.send("logProducer-out-0", message);
       log.info("Published log: {} " ,  message);
    }
}
