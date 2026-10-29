package com.jobboard.auth.domain.model;

import com.jobboard.auth.domain.exception.InvalidUserDataException;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private final List<String> skills;

    public User(Long id, String name, String email, String passwordHash, UserRole role, String companyName, Instant createdAt) {
        this(id, name, email, passwordHash, role, companyName, createdAt, Collections.emptyList());
    }

    public User(Long id, String name, String email, String passwordHash, UserRole role, String companyName, Instant createdAt, List<String> skills) {
        validate(name, email, passwordHash, role);
        this.id = id;
        this.name = name.trim();
        this.email = email.trim().toLowerCase();
        this.passwordHash = passwordHash.trim();
        this.role = role;
        this.companyName = (companyName != null && !companyName.isBlank()) ? companyName.trim() : null;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.skills = normalizeSkills(skills);
    }

    public static User createNew(String name, String email, String passwordHash, UserRole role, String companyName) {
        return createNew(name, email, passwordHash, role, companyName, Collections.emptyList());
    }

    public static User createNew(String name, String email, String passwordHash, UserRole role, String companyName, List<String> skills) {
        return new User(null, name, email, passwordHash, role, companyName, Instant.now(), skills);
    }

    public User withId(Long id) {
        return new User(id, this.name, this.email, this.passwordHash, this.role, this.companyName, this.createdAt, this.skills);
    }

    public User withUpdatedProfile(String newName, String newEmail, String newCompanyName, String newPasswordHash) {
        return withUpdatedProfile(newName, newEmail, newCompanyName, newPasswordHash, this.skills);
    }

    public User withUpdatedProfile(String newName, String newEmail, String newCompanyName, String newPasswordHash, List<String> newSkills) {
        String targetName = (newName != null && !newName.isBlank()) ? newName : this.name;
        String targetEmail = (newEmail != null && !newEmail.isBlank()) ? newEmail : this.email;
        String targetPasswordHash = (newPasswordHash != null && !newPasswordHash.isBlank()) ? newPasswordHash : this.passwordHash;
        List<String> targetSkills = (newSkills != null) ? normalizeSkills(newSkills) : this.skills;
        return new User(this.id, targetName, targetEmail, targetPasswordHash, this.role, newCompanyName, this.createdAt, targetSkills);
    }

    private static List<String> normalizeSkills(List<String> rawSkills) {
        if (rawSkills == null || rawSkills.isEmpty()) {
            return List.of();
        }
        Map<String, String> deduplicated = new LinkedHashMap<>();
        for (String skill : rawSkills) {
            if (skill != null) {
                String trimmed = skill.trim();
                if (!trimmed.isBlank()) {
                    String lowerKey = trimmed.toLowerCase(Locale.ROOT);
                    deduplicated.putIfAbsent(lowerKey, trimmed);
                }
            }
        }
        return List.copyOf(deduplicated.values());
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

    public List<String> getSkills() {
        return skills;
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
                ", skills=" + skills +
                '}';
    }
}
