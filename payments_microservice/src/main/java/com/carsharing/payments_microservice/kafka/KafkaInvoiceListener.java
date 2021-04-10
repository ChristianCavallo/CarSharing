package com.carsharing.payments_microservice.kafka;

import com.carsharing.payments_microservice.service.PaymentService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Map;

@Component
public class KafkaInvoiceListener {

    @Autowired
    private PaymentService service;

    @Value("${invoice_created_key}")
    private String invoice_created_key;

    @KafkaListener(topics = "${kafka_invoice_topic}", groupId = "${spring.application.name}")
    private void consumeInvoiceTopic(ConsumerRecord<String, String> data){
        if(data.key().equals(invoice_created_key)){
            try{
                Type type = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, Object> m = new Gson().fromJson(data.value(), type);

                String rental_id = m.get("rental_id").toString();
                String user_id = m.get("user_id").toString();
                Double amount = Double.parseDouble(m.get("totalAmount").toString());

                service.CreatePayment(rental_id, user_id, amount);

            }catch(Exception e){
                e.printStackTrace();
            }

        }
    }

}
