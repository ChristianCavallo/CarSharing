package com.carsharing.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@EnableDiscoveryClient
@SpringBootApplication
public class GatewayApplication {
	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

//@Bean
//public RouteLocator myRoutes(RouteLocatorBuilder builder) {
//	return builder.routes()
//			.route(p -> p
//					.path("/" + payments_api_base + "/**")
//					.uri(payments_uri))
//			.route(p -> p
//					.path("/" + rentals_base_api + "/**")
//					.uri(rentals_uri))
//			.route(p -> p
//					.path("/" + logging_base_api + "/**")
//					.uri(logging_uri))
//			.build();
//}
}
