package com.carsharing.invoices_microservice.data;

import com.carsharing.invoices_microservice.entities.Invoice;
import com.carsharing.invoices_microservice.entities.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface InvoiceRepository extends MongoRepository<Invoice, String> {
    Optional<Invoice> findByRentalIdAndUserIdAndCarId(String rental_id, String user_id, String car_id);

    Optional<Invoice> findByRentalIdAndUserIdAndTotalAmount(String rental_id, String user_id, Double amount_paid);

    Optional<Invoice> findFirstByInvoiceStatusOrderByCountAsc(InvoiceStatus status);

    Page<Invoice> findAllByUserId(String user_id, Pageable pageable);
}
