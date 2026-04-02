package com.starwars.auth.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * JPA entity representing an authenticated user of the platform.
 *
 * <p>Passwords are stored as BCrypt hashes — never in plain text.
 */
@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // =========================================================
    // Constructors
    // =========================================================

    public AppUser() {}

    public AppUser(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // =========================================================
    // Getters & Setters
    // =========================================================

    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Instant getCreatedAt() { return createdAt; }
}