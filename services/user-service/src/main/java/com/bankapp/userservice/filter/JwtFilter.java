package com.bankapp.userservice.filter;

import com.bankapp.userservice.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String BEARER_HEADER = "Bearer ";

    private final AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getServletPath().startsWith("/api/users/login") || request.getServletPath().startsWith("/api/users/register") ||
        request.getServletPath().startsWith("/refresh")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);

         if(token != null) {
             Optional<Authentication> authentication = authService.getAuthentication(token);

             authentication.ifPresent(auth ->
                     SecurityContextHolder.getContext().setAuthentication(auth)
             );
         }


        filterChain.doFilter(request, response);
    }

     private String extractToken(HttpServletRequest request) {

         String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
         String token = null;

         if (authHeader != null && authHeader.startsWith(BEARER_HEADER)) {
             token = authHeader.substring(7);
         }

         return token;
     }
}
