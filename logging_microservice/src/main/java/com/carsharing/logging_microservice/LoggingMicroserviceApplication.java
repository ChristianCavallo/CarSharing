package com.carsharing.logging_microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class LoggingMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoggingMicroserviceApplication.class, args);
	}

}
