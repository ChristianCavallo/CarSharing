package com.carsharing.rentals_microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class RentalsMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentalsMicroserviceApplication.class, args);
	}

}
