package com.polduu.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
 
@Entity 
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor 
@Builder 
public class User {

    @Id 
    @GeneratedValue 
    @UuidGenerator 
    private UUID id;

    @Email 
    @NotBlank 
    @Column (unique = true, nullable = false)    
    private String email;

    @Column (name = "image_url")
    private String imageUrl;

    @Column (name = "password_hash")
    private String passwordHash;

    @NotBlank
    @Column (unique = true, nullable = false)
    private String username;

    @NotBlank 
    @Column (name = "full_name", nullable = false)
    private String fullName;

    @Enumerated (EnumType.STRING)
    @Column (name = "auth_provider", nullable = false)
    private AuthProvider authProvider;

    @CreationTimestamp 
    @Column (name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;




}
