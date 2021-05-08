package com.carsharing.gateway;

import com.carsharing.gateway.config.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
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

	@Autowired
	AuthenticationFilter filter;

	@Value("${services}")
	private String services;

	@Value("${api_services}")
	private String api;

	@Bean
	public RouteLocator myRoutes(RouteLocatorBuilder builder) {
		RouteLocatorBuilder.Builder b = builder.routes();

		String[] s = services.split(",");
		String[] a = api.split(",");

		for(int i = 0; i < s.length; i++){
			String service = s[i];
			String api_base = a[i];
			b = b.route(service, r -> r.path( String.format("/%s/**", api_base))
					.filters(f -> f.filter(filter))
					.uri("lb://" + service));

			System.out.println("Added route for service " + service + " -> /" + api_base + "/**");
		}

		return b.build();
	}

}
