package com.polduu.security;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component 
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;

    
    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override 
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
        @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
        throws ServletException, IOException 
    {
        final String authHeader = request.getHeader("Authorization");
        final String token;
        final String email;
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        token = authHeader.substring(7);

        try{
            // Extract email from the token
            email = jwtService.extractEmail(token);

            // Authenticate only when the request does not already have an authentication
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // If the token is valid, set the authentication in the security context
                if (jwtService.isTokenValid(token, email)) {
                    // Create an authentication token and set it in the security context
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            Collections.emptyList() // Here you can set authorities/roles if needed
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);    
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Invalid JWT token");
                    return;
                }
            }
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            logger.error("User authentication failed at JwtAuthenticationFilter", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid JWT token");
            return;
        }

        filterChain.doFilter(request, response);
    }
    
}
