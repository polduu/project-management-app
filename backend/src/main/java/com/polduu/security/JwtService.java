package com.polduu.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    // Secret key for signing the JWT token
    @Value("${jwt.secret}")
    private String secretKey;

    // Expiration time of the token (for example, 15 minutes in milliseconds)
    @Value ("${jwt.expiration:900000}")
    private long jwtExpirationMs; 

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }



    // Token generation method with userId as the subject
    public String generateToken(UUID userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }


    // Extract the UserId (subject) from the token
    public UUID extractUserId(String token) {
        String subject = extractClaim(token, Claims::getSubject);
        if (subject == null) throw new io.jsonwebtoken.JwtException("Missing subject");
        return UUID.fromString(subject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    public long getJwtExpirationTime() {
        return jwtExpirationMs / 1000; // Convert milliseconds to seconds
    }
}