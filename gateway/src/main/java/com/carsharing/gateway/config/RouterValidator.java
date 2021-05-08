package com.carsharing.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Component
public class RouterValidator {

    @Value("${open_endpoints}")
    private String val_openEndpoints;

    public List<String> openApiEndpoints;

    @PostConstruct
    private void onConstruct(){
        String[] endpoints = val_openEndpoints.split(",");
        openApiEndpoints = new ArrayList<>();
        for (String e:endpoints) {
            openApiEndpoints.add(e);
            System.out.println("Added open endpoint: " + e);
        }
    }

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));

}