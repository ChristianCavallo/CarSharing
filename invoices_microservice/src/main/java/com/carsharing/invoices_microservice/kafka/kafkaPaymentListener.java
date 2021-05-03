package com.carsharing.invoices_microservice.kafka;

import com.carsharing.invoices_microservice.entities.Invoice;
import com.carsharing.invoices_microservice.entities.InvoiceStatus;
import com.carsharing.invoices_microservice.service.InvoiceService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.*;


@Component
public class kafkaPaymentListener {

    @Autowired
    private InvoiceService service;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    //kafka_logging_topic=logging
    @Value("${kafka_logging_topic}")
    private String logging_topic;

    //kafka_rental_paid_key=rental_paid
    @Value("${kafka_rental_paid_key}")
    private String rental_paid_key;

    //kafka_invoice_unavailable_key=invoice_unavailable
    @Value("${kafka_invoice_unavailable_key}")
    private String invoice_unavailable_key;

    //kafka_rental_payment_failure_key=rental_payment_failure
    @Value("${kafka_rental_payment_failure_key}")
    private String payment_failure_key;

    @KafkaListener(topics = "${kafka_payment_topic}", groupId = "${spring.application.name}")
    private void consumePaymentTopic(ConsumerRecord<String, String> data) {
        if (data.key().equals(rental_paid_key)) {
            try {
                Type type = new TypeToken<Map<String, String>>() {
                }.getType();
                Map<String, Object> m = new Gson().fromJson(data.value(), type);

                String rental_id = m.get("rental_id").toString();
                String user_id = m.get("user_id").toString();

                Double amount_paid = Double.parseDouble(m.get("mc_gross").toString());

                Optional<Invoice> i = service.GetInvoice(rental_id, user_id, amount_paid);

                if (!i.isPresent()) {
                    Map<String, Object> errorInv = new HashMap<>();
                    errorInv.put("user_id", user_id);
                    errorInv.put("rental_id", rental_id);
                    errorInv.put("timestamp", Instant.now().getEpochSecond());
                    kafkaTemplate.send(logging_topic, invoice_unavailable_key, new Gson().toJson(errorInv));
                    return;
                }

                i.get().setCount(1);

                Optional<Invoice> lastInvoice = service.GetLastPaidInvoice();

                if (lastInvoice.isPresent()) {
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris"));
                    cal.setTime(lastInvoice.get().getStart_timestamp());
                    int y1 = cal.get(Calendar.YEAR);

                    cal.setTime(i.get().getEnd_timestamp());
                    int y2 = cal.get(Calendar.YEAR);
                    if (y2 == y1) {
                        i.get().setCount(lastInvoice.get().getCount() + 1);
                    }
                }

                service.UpdateInvoiceStatus(i.get(), InvoiceStatus.PAID);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (data.key().equals(payment_failure_key)) {
            try {
                Type type = new TypeToken<Map<String, String>>() {
                }.getType();
                Map<String, Object> m = new Gson().fromJson(data.value(), type);


                String rental_id = m.get("rental_id").toString();
                String user_id = m.get("user_id").toString();
                Double amount_paid = Double.parseDouble(m.get("mc_gross").toString());

                Optional<Invoice> i = service.GetInvoice(rental_id, user_id, amount_paid);

                if (i.isPresent()) {
                    service.UpdateInvoiceStatus(i.get(), InvoiceStatus.ABORT);
                } else {
                    Map<String, Object> errorInv = new HashMap<>();
                    errorInv.put("user_id", user_id);
                    errorInv.put("rental_id", rental_id);
                    errorInv.put("timestamp", Instant.now().getEpochSecond());
                    kafkaTemplate.send(logging_topic, invoice_unavailable_key, new Gson().toJson(errorInv));
                    return;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
