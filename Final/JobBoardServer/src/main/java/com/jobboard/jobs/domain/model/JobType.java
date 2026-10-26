package com.jobboard.jobs.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum JobType {
    FULL_TIME("Full-time"),
    PART_TIME("Part-time"),
    CONTRACT("Contract"),
    REMOTE("Remote");

    private final String displayName;

    JobType(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    public static JobType fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("A foglalkoztatási típus nem lehet üres.");
        }
        return Arrays.stream(values())
                .filter(type -> type.displayName.equalsIgnoreCase(value.trim()) || type.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Érvénytelen foglalkoztatási típus: " + value));
    }
}
