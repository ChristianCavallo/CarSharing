package com.carsharing.rentals_microservice.data;

import com.carsharing.rentals_microservice.entities.Rental;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RentalRepository extends MongoRepository<Rental, String> {
    Page<Rental> findByUserIdOrderByStartTimestamp(String user_id, Pageable pageable);
}
