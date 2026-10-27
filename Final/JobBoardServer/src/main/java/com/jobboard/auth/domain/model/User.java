package com.jobboard.auth.domain.model;

import com.jobboard.auth.domain.exception.InvalidUserDataException;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain entity representing an authenticated User in the JobBoard system.
 * Pure Java model without any framework or persistence annotations.
 */
public class User {
    private final Long id;
    private final String name;
    private final String email;
    private final String passwordHash;
    private final UserRole role;
    private final String companyName;
    private final Instant createdAt;

    public User(Long id, String name, String email, String passwordHash, UserRole role, String companyName, Instant createdAt) {
        validate(name, email, passwordHash, role);
        this.id = id;
        this.name = name.trim();
        this.email = email.trim().toLowerCase();
        this.passwordHash = passwordHash.trim();
        this.role = role;
        this.companyName = (companyName != null && !companyName.isBlank()) ? companyName.trim() : null;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static User createNew(String name, String email, String passwordHash, UserRole role, String companyName) {
        return new User(null, name, email, passwordHash, role, companyName, Instant.now());
    }

    public User withId(Long id) {
        return new User(id, this.name, this.email, this.passwordHash, this.role, this.companyName, this.createdAt);
    }

    public User withUpdatedProfile(String newName, String newEmail, String newCompanyName, String newPasswordHash) {
        String targetName = (newName != null && !newName.isBlank()) ? newName : this.name;
        String targetEmail = (newEmail != null && !newEmail.isBlank()) ? newEmail : this.email;
        String targetPasswordHash = (newPasswordHash != null && !newPasswordHash.isBlank()) ? newPasswordHash : this.passwordHash;
        return new User(this.id, targetName, targetEmail, targetPasswordHash, this.role, newCompanyName, this.createdAt);
    }

    private static void validate(String name, String email, String passwordHash, UserRole role) {
        if (name == null || name.isBlank()) {
            throw new InvalidUserDataException("A felhasználónév nem lehet üres.");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new InvalidUserDataException("Érvényes e-mail cím megadása kötelező.");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new InvalidUserDataException("A jelszó hash nem lehet üres.");
        }
        if (role == null) {
            throw new InvalidUserDataException("A szerepkör kiválasztása kötelező.");
        }
    }

    public boolean isCompanyManager() {
        return role == UserRole.COMPANY_MANAGER;
    }

    public boolean isApplicant() {
        return role == UserRole.APPLICANT;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public String getCompanyName() {
        return companyName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        if (id != null && user.id != null) {
            return Objects.equals(id, user.id);
        }
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : Objects.hash(email);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", companyName='" + companyName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
