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

    @Value("${spring.application.name}")
    private String service_name;

    //kafka_rental_paid_key=rental_paid
    @Value("${kafka_rental_paid_key}")
    private String rental_paid_key;

    //kafka_invoice_unavailable_key=invoice_unavailable
    @Value("${kafka_invoice_unavailable_key}")
    private String invoice_unavailable_key;

    //kafka_rental_payment_failure_key=rental_payment_failure
    @Value("${kafka_rental_payment_failure_key}")
    private String payment_failure_key;

    private void SendMessageToLogging(String key, String u, String r){
        Map<String, Object> errorInv = new HashMap<>();
        errorInv.put("user_id", u);
        errorInv.put("rental_id", r);
        errorInv.put("timestamp", Instant.now().getEpochSecond());
        errorInv.put("service", service_name);
        kafkaTemplate.send(logging_topic, invoice_unavailable_key, new Gson().toJson(errorInv));
    }

    @KafkaListener(topics = "${kafka_invoice_topic}", groupId = "${spring.application.name}")
    private void consumePaymentTopic(ConsumerRecord<String, String> data) {
        if (data.key().equals(rental_paid_key)) {
            try {
                Type type = new TypeToken<Map<String, String>>() {
                }.getType();
                Map<String, Object> m = new Gson().fromJson(data.value(), type);

                String rental_id = m.get("rental_id").toString();
                String user_id = m.get("user_id").toString();

                Double amount_paid = Double.parseDouble(m.get("amount_paid").toString());

                Optional<Invoice> i = service.GetInvoice(rental_id, user_id, amount_paid);

                if (!i.isPresent()) {
                    SendMessageToLogging(invoice_unavailable_key, user_id, rental_id);
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
    }

    @KafkaListener(topics = "${kafka_logging_topic}", groupId = "${spring.application.name}")
    private void consumeErrorPaymentTopic(ConsumerRecord<String, String> data) {
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
                    SendMessageToLogging(invoice_unavailable_key, user_id, rental_id);
                    return;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
