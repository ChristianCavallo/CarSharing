package com.carsharing.invoices_microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class InvoicesMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvoicesMicroserviceApplication.class, args);
	}

}
