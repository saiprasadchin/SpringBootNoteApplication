package com.fundoo.fundoo.jwt;

import com.fundoo.fundoo.auth.AppUserDetails;
import com.fundoo.fundoo.auth.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private ApplicationUserService applicationUserService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        System.out.println("DOooooooooooooooooooooooooooooooooooooooooooooooooooooo");
        final String authorizationHeader = request.getHeader("Authorization");


        String username = null;
        String jwtToken = null;

        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            System.out.println("Inside If");
            jwtToken = authorizationHeader.substring(7);
            System.out.println("Authorization  "+authorizationHeader);
            System.out.println("Token  "+jwtToken);
            username = jwtUtil.extractUsername(jwtToken);

            System.out.println("Username  "+username);
        }

        System.out.println("+==================================================================");
        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = applicationUserService.loadUserByUsername(username);
            AppUserDetails appUserDetails = (AppUserDetails)userDetails;
            if(jwtUtil.validateToken(jwtToken, appUserDetails.getUser())) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        appUserDetails, null, appUserDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        System.out.println("+==================================================================");
        filterChain.doFilter(request, response);
    }
}
