package com.carsharing.cars_microservice.data;

import com.carsharing.cars_microservice.entities.Car;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CarRepository extends MongoRepository<Car, String> {
    List<Car> findByLocation(String loc);

    Optional<Car> findByCarId(String id);
}
