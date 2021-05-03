package com.carsharing.rentals_microservice.controller;

import com.carsharing.rentals_microservice.data.RentalRepository;
import com.carsharing.rentals_microservice.entities.Rental;
import com.carsharing.rentals_microservice.entities.RentalStatus;
import com.google.gson.Gson;
import entities.car.CarOperation;
import entities.car.CarRequest;
import entities.car.CarResponse;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping(path = "/${api_base}")
public class RentalController {

    @Autowired
    RentalRepository repository;

    @Autowired
    private ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.application.name}")
    private String service_name;

    @Value("${kafka_rental_topic}")
    private String rental_topic;

    @Value("${kafka_logging_topic}")
    private String logging_topic;

    @Value("${http_errors_key}")
    private String http_error_key;

    @Value("${kafka_car_requests_topic}")
    private String car_requests_topic;

    @Value("${kafka_rental_accepted_key}")
    private String rental_accepted_key;

    @Value("${kafka_rental_completed_key}")
    private String rental_completed_key;

    @Value("${kafka_rental_car_locking_failure_key}")
    private String rental_car_locking_failure_key;

    @Value("${kafka_rental_car_not_available_key}")
    private String rental_car_not_available_key;

    @Value("${price_per_minute}")
    private Double price_per_minute;

    @Value("${admin_user_id}")
    private String admin_user_id;

    public void sendHttpError(HttpServletRequest servletRequest, Object error){
        Map<String, Object> message = new HashMap<>();
        message.put("timestamp", Instant.now().getEpochSecond());
        message.put("sourceIp", servletRequest.getRemoteAddr());
        message.put("service", service_name);
        message.put("request", servletRequest.getMethod() + " " + servletRequest.getRequestURI());
        message.put("error", error);
        kafkaTemplate.send(logging_topic, http_error_key, new Gson().toJson(message));
    }

    public CarResponse sendCarRequest(CarRequest r) throws ExecutionException, InterruptedException {
        ProducerRecord<String, String> record = new ProducerRecord<>(car_requests_topic, "car_request", new Gson().toJson(r));
        RequestReplyFuture<String, String, String> future = replyingKafkaTemplate.sendAndReceive(record);
        ConsumerRecord<String, String> response = future.get();
        return new Gson().fromJson(response.value(), CarResponse.class);
    }

    @GetMapping(path = "/{id}")
    public @ResponseBody String getRentalById(@PathVariable String id,
                                              @RequestHeader("X-User-ID") String user_id,
                                              HttpServletRequest servletRequest){

        Optional<Rental> r = repository.findById(id);
        if(!r.isPresent()){
            sendHttpError(servletRequest,404);
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Specified object can't be found."
            );
        }

        if(r.get().getUserId().equals(admin_user_id) || r.get().getUserId().equals(user_id)){
            return new Gson().toJson(r.get());
        }

        sendHttpError(servletRequest,"Unauthorized access to rental's data");
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "You don't own this resource."
        );

    }

    @PostMapping(path = "/start")
    public @ResponseBody String startRental(@RequestParam String car_id,
                                            @RequestParam Double lat,
                                            @RequestParam Double lon,
                                            @RequestHeader("X-User-ID") String user_id,
                                            HttpServletRequest servletRequest) {

        CarRequest r = new CarRequest();
        r.setCar_id(car_id);
        r.setOperation(CarOperation.UNLOCK);

        CarResponse c;
        try{
            c = sendCarRequest(r);
        } catch (Exception e){
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            sendHttpError(servletRequest, sw.toString());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Service communication error."
            );
        }

        if(!c.getSuccess()){
            Map<String, Object> message = new HashMap<>();
            message.put("timestamp", Instant.now().getEpochSecond());
            message.put("user_id", user_id);
            message.put("car_id", car_id);
            message.put("message", c.getMessage());
            kafkaTemplate.send(logging_topic, rental_car_not_available_key, new Gson().toJson(message));
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, c.getMessage()
            );
        }

        Rental l = new Rental();
        l.setCar_id(r.getCar_id());
        l.setStartTimestamp(Instant.now().getEpochSecond());
        l.setStatus(RentalStatus.STARTED);
        l.setAmount_to_pay(0.0);
        l.setPrice_per_minute(price_per_minute);
        l.setUserId(user_id);

        repository.save(l);

        kafkaTemplate.send(rental_topic, rental_accepted_key, new Gson().toJson(l));

        return new Gson().toJson(l);
    }

    @PostMapping(path = "/stop")
    public @ResponseBody String stopRental(@RequestParam String rental_id,
                                           @RequestParam Double lat,
                                           @RequestParam Double lon,
                                           @RequestHeader("X-User-ID") String user_id,
                                           HttpServletRequest servletRequest) {

        // Small Security Checks
        // 1. is a valid rental? If yes, do you own it?
        Optional<Rental> r = repository.findById(rental_id);
        if(!r.isPresent()){
            sendHttpError(servletRequest, 404);
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Specified object can't be found."
            );
        }

        if(!r.get().getUserId().equals(user_id) && r.get().getUserId().equals(admin_user_id)){
            sendHttpError(servletRequest,"Unauthorized access to rental's data");
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't own this resource."
            );
        }

        if(r.get().getStatus() == RentalStatus.COMPLETED){
            sendHttpError(servletRequest,"This rental is already completed.");
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Invalid operation."
            );
        }

        CarRequest carRequest = new CarRequest();
        carRequest.setCar_id(r.get().getCar_id());
        carRequest.setOperation(CarOperation.LOCK);

        CarResponse c;
        try{
            c = sendCarRequest(carRequest);
        } catch (Exception e){
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            sendHttpError(servletRequest, sw.toString());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Service communication error."
            );
        }

        if(!c.getSuccess()){
            Map<String, Object> message = new HashMap<>();
            message.put("timestamp", Instant.now().getEpochSecond());
            message.put("user_id", user_id);
            message.put("car_id", r.get().getCar_id());
            kafkaTemplate.send(logging_topic, rental_car_locking_failure_key, new Gson().toJson(message));

            // FREEZE THE TIMER
            r.get().setStatus(RentalStatus.FROZEN);
            r.get().setStopTimestamp(Instant.now().getEpochSecond());
            repository.save(r.get());

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Vehicle issue. Contact the support."
            );
        }

        Rental l = r.get();
        if(l.getStatus() == RentalStatus.STARTED){
            l.setStopTimestamp(Instant.now().getEpochSecond());
        }
        l.setStatus(RentalStatus.COMPLETED);
        long diff = l.getStopTimestamp() - l.getStartTimestamp();
        Double to_pay = Math.round(diff / 60d * l.getPrice_per_minute() *100) / 100d;
        l.setAmount_to_pay(to_pay);

        repository.save(l);

        kafkaTemplate.send(rental_topic, rental_completed_key, new Gson().toJson(l));

        return new Gson().toJson(l);
    }


    // GET /rentals?page=0&size=10
    @GetMapping(path="/rentals")
    public @ResponseBody String getRentalsWithPagination(
            @RequestHeader("X-User-ID") String user_id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int per_page,
            HttpServletRequest request) {

        try {
            Pageable paging = PageRequest.of(page, per_page);
            Page<Rental> rentalsPage;
            if (user_id.equals(admin_user_id)){
                rentalsPage = repository.findAll(paging);
            } else {
                rentalsPage = repository.findByUserIdOrderByStartTimestamp(user_id, paging);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("rentals", rentalsPage.getContent());
            response.put("currentPage", rentalsPage.getNumber());
            response.put("totalItems", rentalsPage.getTotalElements());
            response.put("totalPages", rentalsPage.getTotalPages());

            return new Gson().toJson(response);

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            sendHttpError(request, sw.toString());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error. Contact administration."
            );
        }
    }


    @GetMapping(path="/ping")
    public @ResponseBody String getPong(){
        return "Pong";
    }

}
