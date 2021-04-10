package com.carsharing.logging_microservice.controller;

import com.carsharing.logging_microservice.data.LoggingRepository;
import com.carsharing.logging_microservice.entities.Log;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping(path="/${api_base}")
public class LoggingController {

    @Autowired
    private LoggingRepository repository;

    @Value("${http_errors_key}")
    private String http_errors_key;

    // GET /keys/{key}?from=unixTimestampStart&end=unixTimestampEnd
    @GetMapping(path = "/keys/{key}")
    public @ResponseBody String getLogsBetweenTimestamps(@PathVariable String key,
                                                         @RequestParam long from,
                                                         @RequestParam long end){
        List<Log> l = repository.findByKeyAndTimestampBetween(key, from, end);
        return new Gson().toJson(l);
    }

    //GET /http_errors/services/{service}?from=unixTimestampStart&end=unixTimestampEnd
    @GetMapping(path="/http_errors/services/{service}")
    public @ResponseBody String getHttpErrorsByServiceAndTimestamps(@PathVariable String service,
                                                                    @RequestParam(defaultValue = "0") long from,
                                                                    @RequestParam(defaultValue = "9999999999") long end){
        List<Log> l = repository.findByServiceAndKeyAndTimestampBetween(service, http_errors_key, from, end);
        return new Gson().toJson(l);
    }

    @GetMapping(path="/ping")
    public @ResponseBody String getPong(){
        return "Pong";
    }

}
