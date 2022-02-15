package com.fundoo.fundoo.service;

import com.fundoo.fundoo.auth.ApplicationUserService;
import com.fundoo.fundoo.jwt.JwtUtil;
import com.fundoo.fundoo.model.User;
import com.fundoo.fundoo.model.payload.request.AuthenticationRequest;
import com.fundoo.fundoo.model.payload.request.RegistrationRequest;
import com.fundoo.fundoo.model.payload.response.AuthenticationResponse;
import com.fundoo.fundoo.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final ApplicationUserService userDetailsService;
    private final IUserRepository repository;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager, ApplicationUserService userDetailsService, IUserRepository repository, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.repository = repository;
        this.jwtUtil = jwtUtil;
    }

    public User register(RegistrationRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setFirstName(request.getLastName());
        user.setLastName(request.getLastName());

        boolean userExists = repository.findByEmail(request.getEmail()).isPresent();

        if(userExists)
            throw new IllegalStateException("email already taken");

        user.setCreatedAt(LocalDateTime.now());
        return repository.save(user);
    }

    public ResponseEntity<AuthenticationResponse> createAuthenticationToken(AuthenticationRequest authenticationRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new Exception("Incorrect username and password", e);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        String token = jwtUtil.generateToken(userDetails);
        System.out.println(token+ userDetails.getUsername());
//
//        HttpHeaders responseHeaders = new HttpHeaders();
//        responseHeaders.set("Authorization", token);
//        responseHeaders.set("email", userDetails.getUsername());
//        return new ResponseEntity<>("success", responseHeaders, HttpStatus.OK);
        return new ResponseEntity<AuthenticationResponse>(
                new AuthenticationResponse(token, userDetails.getUsername()),
                HttpStatus.OK);
        //return  null;
    }
}
