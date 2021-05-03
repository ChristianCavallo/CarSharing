package com.carsharing.rentals_microservice.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class CarRequest implements Serializable {
    private String car_id;
    private String operation;
    private String user_id;
    private String rental_id;
    private Double lat;
    private Double lon;
}
