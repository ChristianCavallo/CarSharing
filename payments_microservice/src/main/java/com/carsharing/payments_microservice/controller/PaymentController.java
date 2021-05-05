package com.carsharing.payments_microservice.controller;

import com.carsharing.payments_microservice.data.PaymentRepository;
import com.carsharing.payments_microservice.entities.Payment;
import com.carsharing.payments_microservice.entities.PaymentStatus;
import com.carsharing.payments_microservice.service.PaymentService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping(path = "/${api_base}")
public class PaymentController {

    @Autowired
    PaymentRepository repository;

    @Autowired
    PaymentService service;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.application.name}")
    String service_name;

    @Value("${business_email}")
    String business_email;

    @Value("${kafka_invoice_topic}")
    String invoice_topic;

    @Value("${kafka_logging_topic}")
    String logging_topic;

    @Value("${http_errors_key}")
    String http_error_key;

    @Value("${bad_ipn_error_key}")
    String bad_ipn_error_key;

    @Value("${business_ipn_error_key}")
    String business_ipn_error_key;

    @Value("${rental_payment_failure_key}")
    String rental_payment_failure_key;

    @Value("${rental_paid_key}")
    String rental_paid_key;

    @Value("${admin_user_id}")
    String admin_user_id;

    public void sendHttpError(HttpServletRequest servletRequest, Object error){
        Map<String, Object> message = new HashMap<>();
        message.put("timestamp", Instant.now().getEpochSecond());
        message.put("sourceIp", servletRequest.getRemoteAddr());
        message.put("service", service_name);
        message.put("request", servletRequest.getMethod() + " " + servletRequest.getRequestURI());
        message.put("error", error);
        kafkaTemplate.send(logging_topic, http_error_key, new Gson().toJson(message));
    }

    // POST /ipn
    @PostMapping(path="/ipn", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public @ResponseBody String postIPN(@RequestParam MultiValueMap<String,String> paramMap,
                                        HttpServletRequest request){
        System.out.println("New IPN Request: " + paramMap);

        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("timestamp", Instant.now().getEpochSecond());
        errorMap.putAll(paramMap);

        // Checks
        if(!paramMap.containsKey("invoice") || !paramMap.containsKey("item_id") || !paramMap.containsKey("mc_gross")){
            System.out.println("Missing parameters on the IPN request.");
            kafkaTemplate.send(logging_topic, bad_ipn_error_key, new Gson().toJson(errorMap));
            return "";
        }

        String rental_id = paramMap.get("invoice").get(0);
        String user_id = paramMap.get("item_id").get(0);
        Double amount = Double.parseDouble(paramMap.get("mc_gross").get(0));

        Optional<Payment> p = repository.findByRentalIdAndUserIdAndAmount(rental_id, user_id, amount);

        if(!p.isPresent()){
            System.out.println("The payment doesn't exists.");
            kafkaTemplate.send(logging_topic, bad_ipn_error_key, new Gson().toJson(errorMap));
            return "";
        }

        if(paramMap.get("business") == null || !paramMap.get("business").get(0).equals(business_email)){
            System.out.println("Business field is invalid.");
            kafkaTemplate.send(logging_topic, business_ipn_error_key, new Gson().toJson(errorMap));
            return "";
        }

        ResponseEntity<String> res = service.SendIpnVerificationResponse(paramMap);
        if(res.getStatusCodeValue() != 200){
            System.out.println("Error during the Ipn Verification: " + res.toString());
            kafkaTemplate.send(logging_topic, rental_payment_failure_key, new Gson().toJson(errorMap));
            return "";
        }

        if(!res.getBody().equals("VERIFIED")){
            System.out.println("IPN Invalid");
            kafkaTemplate.send(logging_topic, rental_payment_failure_key, new Gson().toJson(errorMap));
            return "";
        }

        System.out.println("IPN Verified.");

        // Saving the payment to the DB
        p.get().setBusiness(paramMap.get("business").get(0));
        p.get().setCurrency(paramMap.get("mc_currency").get(0));
        p.get().setPayer_email(paramMap.get("payer_email").get(0));
        p.get().setTimestamp(Instant.now().getEpochSecond());
        p.get().setPaymentStatus(PaymentStatus.PAID);
        repository.save(p.get());

        // Sending kafka confirmation
        Map<String, Object> message = new HashMap<>();
        message.put("rental_id", p.get().getRentalId());
        message.put("user_id", p.get().getUserId());
        message.put("amount_paid", p.get().getAmount());
        message.put("currency", p.get().getCurrency());
        message.put("payer_email", p.get().getPayer_email());
        message.put("timestamp", Instant.now().getEpochSecond());

        kafkaTemplate.send(invoice_topic, rental_paid_key, new Gson().toJson(message));

        System.out.println("Payment updated: " + p.get().toString());
        return "";
    }

    @GetMapping(path = "/transactions")
    public @ResponseBody String getPaymentsByTimestamps(@RequestParam(defaultValue = "0") Long fromTimestamp,
                                                       @RequestParam(defaultValue = "9999999999") Long endTimestamp,
                                                        @RequestHeader("X-User-ID") String user_id,
                                                       HttpServletRequest servletRequest){

        if(!user_id.equals(admin_user_id)){
            sendHttpError(servletRequest,"Unauthorized access to transactions data.");
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You aren't allowed to access data."
            );
        }

        List<Payment> p = repository.findByTimestampBetween(fromTimestamp, endTimestamp);

        return new Gson().toJson(p);
    }

    @GetMapping(path="/ping")
    public @ResponseBody String getPong(){
        return "Pong";
    }


}
