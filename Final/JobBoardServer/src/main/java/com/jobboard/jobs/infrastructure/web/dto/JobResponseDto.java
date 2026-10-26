package com.jobboard.jobs.infrastructure.web.dto;

import java.util.List;

/**
 * DTO matching the frontend Job interface:
 * id, title, company, type, location, badge, salary, description, requirements, postedAt, status, createdByUserId
 */
public record JobResponseDto(
        Long id,
        String title,
        String company,
        String type,
        String location,
        String badge,
        String salary,
        String description,
        List<String> requirements,
        String postedAt,
        String status,
        Long createdByUserId
) {
    /**
     * Backward-compatible 10-parameter constructor.
     */
    public JobResponseDto(
            Long id,
            String title,
            String company,
            String type,
            String location,
            String badge,
            String salary,
            String description,
            List<String> requirements,
            String postedAt
    ) {
        this(id, title, company, type, location, badge, salary, description, requirements, postedAt, "ACTIVE", null);
    }
}
