package com.fundoo.fundoo.model.payload.request;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
public class RegistrationRequest {
    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;
}