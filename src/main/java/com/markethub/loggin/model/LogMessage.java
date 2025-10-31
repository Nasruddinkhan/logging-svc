package com.markethub.loggin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogMessage {
    private String level;
    private String message;
    private String serviceName;
    private String timestamp;
}
