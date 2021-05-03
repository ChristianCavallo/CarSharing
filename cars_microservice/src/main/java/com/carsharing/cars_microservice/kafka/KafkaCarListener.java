package com.carsharing.cars_microservice.kafka;

import com.carsharing.cars_microservice.service.CarService;
import com.google.gson.Gson;
import entities.car.CarOperation;
import entities.car.CarRequest;
import entities.car.CarResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class KafkaCarListener {

    @Autowired
    private CarService service;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    //kafka_car_topic=car
    @Value("${kafka_car_topic}")
    private String car_topic;

    //kafka_logging_topic=logging
    @Value("${kafka_logging_topic}")
    private String logging_topic;

    //car_unlocked_key=car_unlocked
    @Value("${car_unlocked_key}")
    private String car_unlocked;

    //car_locked_key=car_locked
    @Value("${car_locked_key}")
    private String car_locked;

    //car_lock_failure_key=car_lock_failure
    @Value("${car_lock_failure_key}")
    private String car_lock_failure;

    //car_unlock_failure_key=car_unlock_failure
    @Value("${car_unlock_failure_key}")
    private String car_unlock_failure;

    private void SendKafkaResponse(String topic, String key, CarRequest req, CarResponse res) {
        Map<String, Object> message = new HashMap<>();
        message.put("car_id", req.getCar_id());
        message.put("user_id", req.getUser_id());
        message.put("rental_id", req.getRental_id());
        message.put("timestamp", Instant.now().getEpochSecond());
        message.put("message", res.getMessage());
        message.put("success", res.getSuccess());

        kafkaTemplate.send(topic, key, new Gson().toJson(message));
    }

    @KafkaListener(topics = "car_requests", groupId = "car_replies_group")
    @SendTo("car_responses")
    public String carRequestsHandler(String data) {
        System.out.println("New request: " + data);
        CarRequest request = new Gson().fromJson(data, CarRequest.class);
        CarResponse response = service.SwitchCarStatus(request);

        System.out.println(response.toString());

        if (response.getSuccess()) {
            String key = request.getOperation() == CarOperation.LOCK ? car_locked : car_unlocked;

           /* EQUIVALE A FARE QUESTO
           if(request.getOperation() == CarOperation.LOCK){
                key = car_locked
            } else {
                key = car_unlocked
            } */

            SendKafkaResponse(car_topic, key, request, response);

        } else {
            String key = request.getOperation() == CarOperation.LOCK ? car_lock_failure : car_unlock_failure;
            SendKafkaResponse(logging_topic, key, request, response);
        }

        return new Gson().toJson(response);

    }

}
