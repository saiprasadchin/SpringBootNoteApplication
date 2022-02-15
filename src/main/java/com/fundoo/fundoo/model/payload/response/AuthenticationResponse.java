package com.fundoo.fundoo.model.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AuthenticationResponse {
    private final String token;
    private final String email;
}
