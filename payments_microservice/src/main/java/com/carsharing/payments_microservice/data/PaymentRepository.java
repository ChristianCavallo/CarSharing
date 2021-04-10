package com.carsharing.payments_microservice.data;

import com.carsharing.payments_microservice.entities.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    List<Payment> findAllByTimestampBetween(long t1, long t2);
    List<Payment> findByTimestampBetween(long from, long to);
    Optional<Payment> findByRentalIdAndUserIdAndAmount(String rental_id, String user_id, Double amount);
}
