package com.carsharing.cars_microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class CarsMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarsMicroserviceApplication.class, args);
	}

}
