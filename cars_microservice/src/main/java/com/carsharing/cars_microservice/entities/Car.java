package com.carsharing.cars_microservice.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Getter
@Setter
@ToString
@NoArgsConstructor
@Document(collection = "cars")
public class Car {

    @Id
    private String carId;

    private String code;

    private String location;

    private Double lat;
    private Double lon;

    private CarStatus carStatus;

}
