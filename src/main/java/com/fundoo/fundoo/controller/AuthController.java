package com.fundoo.fundoo.controller;

import com.fundoo.fundoo.model.User;
import com.fundoo.fundoo.model.payload.request.AuthenticationRequest;
import com.fundoo.fundoo.model.payload.request.RegistrationRequest;
import com.fundoo.fundoo.model.payload.response.AuthenticationResponse;
import com.fundoo.fundoo.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registration")
    public ResponseEntity<String> register(@RequestBody RegistrationRequest request) {
        System.out.println("registration");
        User user = authService.register(request);
        return new ResponseEntity<>("Registration Successful",HttpStatus.OK);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        System.out.println("+++++++++++++++++++");
        return authService.createAuthenticationToken(authenticationRequest);
    }
}
