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
    public void carRequestsHandler(ConsumerRecord<String, String> data) {
        Log l = new Log();
        l.setKey(data.key());
        l.setPayload(data.value());

        try{
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, Object> m = new Gson().fromJson(data.value(), type);

            if(data.key().equals(http_errors_key)){
                l.setService(m.get("service").toString());
            }

            if(m.containsKey("timestamp")){
                l.setTimestamp(Long.parseLong(m.get("timestamp").toString()));
            } else {
                l.setTimestamp(Instant.now().getEpochSecond());
            }

        }catch(Exception e){

        }

        repository.save(l);
        System.out.println("New log saved: " + l.toString());
    }
}
