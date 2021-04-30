package com.carsharing.users_microservice.controller;


import com.carsharing.users_microservice.entities.User;
import com.carsharing.users_microservice.service.UserService;
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
public class UserController {

    @Autowired
    UserService service;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka_logging_topic}")
    private String logging_topic;

    @Value("${kafka_user_topic}")
    private String user_topic;

    @Value("${user_registered_key}")
    private String user_registered_key;

    @Value("${http_errors_key}")
    private String http_errors_key;

    @Value("${admin_user_id}")
    private String admin_user_id;

    @Value("${spring.application.name}")
    private String service_name;


    public void sendHttpError(HttpServletRequest servletRequest, Object error) {
        Map<String, Object> message = new HashMap<>();
        message.put("timestamp", Instant.now().getEpochSecond());
        message.put("sourceIp", servletRequest.getRemoteAddr());
        message.put("service", service_name);
        message.put("request", servletRequest.getMethod() + " " + servletRequest.getRequestURI());
        message.put("error", error);
        kafkaTemplate.send(logging_topic, http_errors_key, new Gson().toJson(message));
    }


    @PostMapping(path = "/register")
    private @ResponseBody
    String registerUser(@RequestBody User u) {

        Optional<User> user = service.getUserByEmail(u.getEmail());

        if (user.isPresent()) {
            String message = "Email already registered.";
            return message;
        }

        User u2 = service.AddUser(u);

        kafkaTemplate.send(user_topic, user_registered_key, new Gson().toJson(u2));
        u2.setPassword("");

        return new Gson().toJson(u2);

    }

    @GetMapping(path = "/id/{id}")
    private @ResponseBody
    User getUser(@PathVariable String id, HttpServletRequest servletRequest) {
        Optional<User> user = service.getUserById(id);

        if (!user.isPresent()) {
            sendHttpError(servletRequest, 404);
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Specified object can't be found."
            );
        }

        return user.get();
    }

    @GetMapping(path = "/email/{email}")
    private @ResponseBody
    User getUser(@PathVariable String email, @RequestHeader("X-User-ID") String user_id,
                 HttpServletRequest servletRequest) {

        Optional<User> user = service.getUserByEmail(email);
        if (!user.isPresent()) {
            sendHttpError(servletRequest, 404);
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Specified object can't be found."
            );
        }

        if (user_id != user.get().getId() && !user_id.equals(admin_user_id)) {
            sendHttpError(servletRequest, "Unauthorized access.");
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't own this resource."
            );
        }

        return user.get();
    }


    @GetMapping(path = "/users")
    public @ResponseBody
    String getUserWithPagination(@RequestHeader("X-User-ID") String user_id,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int per_page,
                                 HttpServletRequest servletRequest) {

        if (!user_id.equals(admin_user_id)) {
            sendHttpError(servletRequest, "Unauthorized access.");
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't own this resource."
            );
        }

        Page<User> usersPage = service.getUsersPage(page, per_page);

        Map<String, Object> response = new HashMap<>();
        response.put("users", usersPage.getContent());
        response.put("currentPage", usersPage.getNumber());
        response.put("totalItems", usersPage.getTotalElements());
        response.put("totalPages", usersPage.getTotalPages());

        return new Gson().toJson(response);
    }

    @GetMapping(path = "/ping")
    public @ResponseBody
    String getPong() {
        return "Pong";
    }

}
