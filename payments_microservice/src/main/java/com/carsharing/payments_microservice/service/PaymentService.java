package com.carsharing.payments_microservice.service;

import com.carsharing.payments_microservice.data.PaymentRepository;
import com.carsharing.payments_microservice.entities.Payment;
import com.carsharing.payments_microservice.entities.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    PaymentRepository paymentRepository;

    @Value("${ipn_verification_url}")
    private String ipn_verification_url;

    public ResponseEntity<String> SendIpnVerificationResponse(MultiValueMap<String,String> paramsMap){
        MultiValueMap<String, String> finalMap = new LinkedMultiValueMap<>();
        finalMap.add("cmd", "_notify-validate");
        finalMap.addAll(paramsMap);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String,String>> request = new HttpEntity<MultiValueMap<String,String>>(finalMap, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(ipn_verification_url, request , String.class);
        return response;
    }

    public void CreatePayment(String rental_id, String user_id, double amount){
        Optional<Payment> t = paymentRepository.findByRentalIdAndUserIdAndAmount(rental_id, user_id, amount);
        if(t.isPresent()){
            System.out.println("Payment already exists: " + t.get().toString());
            return;
        }

        Payment p = new Payment();
        p.setRentalId(rental_id);
        p.setUserId(user_id);
        p.setAmount(amount);
        p.setTimestamp(Instant.now().getEpochSecond());
        p.setPaymentStatus(PaymentStatus.CREATED);

        paymentRepository.save(p);

        System.out.println("New payment created: " + p.toString());
    }

}
