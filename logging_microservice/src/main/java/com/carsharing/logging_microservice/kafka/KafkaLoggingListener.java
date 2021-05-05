package com.carsharing.logging_microservice.kafka;

import com.carsharing.logging_microservice.data.LoggingRepository;
import com.carsharing.logging_microservice.entities.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Map;

@Component
public class KafkaLoggingListener {

    @Autowired
    private LoggingRepository repository;

    @Value("${http_errors_key}")
    private String http_errors_key;

    @KafkaListener(topics = "${kafka_logging_topic}", groupId = "${kafka_logging_group}")
    public void logsRequestsHandler(ConsumerRecord<String, String> data) {
        Log l = new Log();
        l.setKey(data.key());
        l.setPayload(data.value());

        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> m = new Gson().fromJson(data.value(), type);

        if(m.containsKey("service")){
            l.setService(m.get("service").toString());
        } else {
            l.setService("Unknown");
        }

        if(m.containsKey("timestamp")){
            Double t = Double.parseDouble(m.get("timestamp").toString());
            System.out.println(t);
            l.setTimestamp(Double.valueOf(t).longValue());
        }

        repository.save(l);
        System.out.println("New log saved: " + l.toString());
    }
}
