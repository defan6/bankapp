package com.bankapp.userservice.filter;

import com.bankapp.userservice.service.AuthService;
import com.bankapp.userservice.service.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getServletPath().startsWith("/api/users/login") || request.getServletPath().startsWith("/api/users/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);

         if(token != null) {
             Optional<Authentication> authentication = authService.authenticateToken(token);

             authentication.ifPresent(auth ->
                     SecurityContextHolder.getContext().setAuthentication(auth)
             );
         }


        filterChain.doFilter(request, response);
    }

     private String extractToken(HttpServletRequest request) {

         String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
         String token = null;

         if (authHeader != null && authHeader.startsWith("Bearer ")) {
             token = authHeader.substring(7);
         }

         return token;
     }
}
