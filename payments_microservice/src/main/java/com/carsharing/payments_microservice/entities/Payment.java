package com.carsharing.payments_microservice.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@Getter @Setter
@ToString
@Document(collection = "payments")
public class Payment {
    @Id
    private String id;

    private String rentalId;
    private String userId;
    private Double amount;
    private String currency;
    private String business;
    private String payer_email;

    private PaymentStatus paymentStatus;

    private Long timestamp;

}
