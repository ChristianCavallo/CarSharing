package com.carsharing.invoices_microservice.controller;


import com.carsharing.invoices_microservice.entities.Invoice;
import com.carsharing.invoices_microservice.service.InvoiceService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping(path = "/${api_base}")
public class InvoiceController {

    @Autowired
    InvoiceService service;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${http_errors_key}")
    private String http_errors_key;

    @Value("${kafka_invoice_topic}")
    private String invoice_topic;

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

    @GetMapping(path = "/invoices/{id}")
    private @ResponseBody
    String getInvoice(@PathVariable String id, @RequestHeader("X-User-ID") String user_id, HttpServletRequest servletRequest) {

        Optional<Invoice> i = service.GetInvoiceById(id);

        if (!user_id.equals(i.get().getUserId()) && !user_id.equals(admin_user_id)) {
            sendHttpError(servletRequest, "Unauthorized access");
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, ""
            );
        }

        if (!i.isPresent()) {
            sendHttpError(servletRequest, 404);
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Specified object can't be found."
            );
        }

        return new Gson().toJson(i.get());
    }


    @GetMapping(path = "/invoices")
    public @ResponseBody
    String getUserWithPagination(@RequestHeader("X-User-ID") String user_id,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int per_page,
                                 HttpServletRequest servletRequest) {

        Page<Invoice> invoicesPage;
        if (user_id.equals(admin_user_id)) {
            invoicesPage = service.GetInvoicePage(page, per_page);
        } else {
            invoicesPage = service.GetInvoicePage(user_id, page, per_page);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("invoices", invoicesPage.getContent());
        response.put("currentPage", invoicesPage.getNumber());
        response.put("totalItems", invoicesPage.getTotalElements());
        response.put("totalPages", invoicesPage.getTotalPages());

        return new Gson().toJson(response);

    }

    @GetMapping(path = "/ping")
    public @ResponseBody
    String getPong() {
        return "Pong";
    }

}
