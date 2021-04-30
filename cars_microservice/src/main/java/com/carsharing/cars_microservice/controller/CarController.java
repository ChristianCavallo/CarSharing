package com.carsharing.cars_microservice.controller;


import com.carsharing.cars_microservice.entities.Car;
import com.carsharing.cars_microservice.entities.CarStatus;
import com.carsharing.cars_microservice.service.CarService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(path = "/${api_base}")
public class CarController {

    @Autowired
    CarService service;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${http_errors_key}")
    private String http_errors_key;

    @Value("${kafka_car_topic}")
    private String car_topic;

    @Value("${kafka_logging_topic}")
    private String logging_topic;

    @Value("${spring.application.name}")
    private String service_name;


    @Value("${admin_user_id}")
    private String admin_user_id;

    public void sendHttpError(HttpServletRequest servletRequest, Object error) {
        Map<String, Object> message = new HashMap<>();
        message.put("timestamp", Instant.now().getEpochSecond());
        message.put("sourceIp", servletRequest.getRemoteAddr());
        message.put("service", service_name);
        message.put("request", servletRequest.getMethod() + " " + servletRequest.getRequestURI());
        message.put("error", error);
        kafkaTemplate.send(logging_topic, http_errors_key, new Gson().toJson(message));
    }

    //Post//add
    @PostMapping(path = "/add")
    private @ResponseBody
    String addcar(@RequestBody Car c, @RequestHeader("X-User-ID") String user_id, HttpServletRequest servletRequest) {

        if (!user_id.equals(admin_user_id)) {
            sendHttpError(servletRequest, "Unauthorized access.");
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't own this resource."
            );

        }
        if (c.getLocation() == null || c.getCode() == null) {
            sendHttpError(servletRequest, "Missing parameters");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Malformed request"
            );
        }

        c.setCarStatus(CarStatus.LOCKED);
        c.setLat(0.0);
        c.setLon(0.0);
        Car car = service.AddCar(c);
        return new Gson().toJson(c);

    }


    //GET /cars?loc=X
    @GetMapping(path = "/cars")
    private @ResponseBody
    String getCarsbyposition(@RequestParam String loc, HttpServletRequest servletRequest) {
        List<Car> c = service.SearchCarsByPosition(loc);

        if (c.isEmpty()) {
            sendHttpError(servletRequest, 404);
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, ""
            );
        }

        return new Gson().toJson(c);
    }

    @GetMapping(path = "/ping")
    public @ResponseBody
    String getPong() {
        return "Pong";
    }
}
