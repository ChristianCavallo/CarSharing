package com.carsharing.users_microservice.entities;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@ToString
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
}
