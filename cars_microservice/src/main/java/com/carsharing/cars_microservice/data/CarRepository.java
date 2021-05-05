package com.carsharing.cars_microservice.data;

import com.carsharing.cars_microservice.entities.Car;
import com.carsharing.cars_microservice.entities.CarStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CarRepository extends MongoRepository<Car, String> {
    List<Car> findByLocationAndAndCarStatus(String loc, CarStatus status);

    Optional<Car> findByCode(String code);

    Optional<Car> findByCarId(String id);
}
