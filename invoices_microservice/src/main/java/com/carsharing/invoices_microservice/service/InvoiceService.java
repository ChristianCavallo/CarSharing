package com.carsharing.invoices_microservice.service;

import com.carsharing.invoices_microservice.data.InvoiceRepository;
import com.carsharing.invoices_microservice.entities.Invoice;
import com.carsharing.invoices_microservice.entities.InvoiceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InvoiceService {

    @Autowired
    InvoiceRepository repository;

    public Optional<Invoice> GetInvoiceById(String id) {
        return repository.findById(id);
    }


    public Page<Invoice> GetInvoicePage(Integer page, Integer per_page) {
        Pageable paging = PageRequest.of(page, per_page);
        return repository.findAll(paging);
    }

    public Page<Invoice> GetInvoicePage(String user_id, Integer page, Integer per_page) {
        Pageable paging = PageRequest.of(page, per_page);
        return repository.findAllByUserId(user_id, paging);
    }


    public Invoice CreateInvoice(Invoice i) {

        Optional<Invoice> invoice = repository.findByRentalIdAndUserIdAndCarId(i.getRentalId(), i.getUserId(), i.getCarId());
        if (invoice.isPresent()) {
            System.out.println("Invoice already exists: " + invoice.get().toString());
            return null;
        }

        i.setCount(-1);
        i.setInvoiceStatus(InvoiceStatus.PENDING);

        repository.save(i);

        System.out.println("New invoice created: " + i.toString());
        return i;
    }


    public Optional<Invoice> GetInvoice(String rental_id, String user_id, Double amount_paid) {
        return repository.findByRentalIdAndUserIdAndTotalAmount(rental_id, user_id, amount_paid);
    }

    public Optional<Invoice> GetLastPaidInvoice() {
        return repository.findFirstByInvoiceStatusOrderByCountAsc(InvoiceStatus.PAID);
    }

    public Invoice UpdateInvoiceStatus(Invoice i, InvoiceStatus s) {
        i.setInvoiceStatus(s);
        return repository.save(i);
    }

}
