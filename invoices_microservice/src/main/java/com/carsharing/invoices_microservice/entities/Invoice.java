package com.carsharing.invoices_microservice.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Document(collection = "invoices")
public class Invoice {

    @Id
    private String id;

    private String rentalId;
    private String carId;
    private String userId;

    private Integer count;

    private Double totalAmount;
    private Double price_per_minute;

    private InvoiceStatus invoiceStatus;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date start_timestamp, end_timestamp;


}
