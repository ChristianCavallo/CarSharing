package com.carsharing.logging_microservice.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@ToString
@Document(collection = "logs")
public class Log {
    @Id
    private String id;
    private String key;
    private String service;
    private String payload;
    private Long timestamp;

    public Log() {
        this.key = "";
        this.service = "";
        this.payload = "";
        this.timestamp = Instant.now().getEpochSecond();
    }
}
