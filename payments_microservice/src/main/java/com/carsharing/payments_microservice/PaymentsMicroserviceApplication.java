package com.carsharing.payments_microservice;

import com.carsharing.payments_microservice.data.PaymentRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableEurekaClient
public class PaymentsMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentsMicroserviceApplication.class, args);
	}

}
