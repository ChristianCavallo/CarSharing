package com.carsharing.payments_microservice.kafka;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class CarListener {

    @Getter @Setter
    @NoArgsConstructor
    class CarRequest implements Serializable {
        private String car_id;
        private String operation;
    }

    @Getter @Setter
    @NoArgsConstructor
    public class CarResponse implements Serializable {
        private String car_id;
        private Boolean success;
    }

    @KafkaListener(topics = "car_requests", groupId = "car_replies_group")
    @SendTo("car_responses")
    public String carRequestsHandler(String data) {
        CarRequest r = new Gson().fromJson(data, CarRequest.class);

        CarResponse c = new CarResponse();
        c.setCar_id(r.getCar_id());
        c.setSuccess(true);

        return new Gson().toJson(c);
    }
}
