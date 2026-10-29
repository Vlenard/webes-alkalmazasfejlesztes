package com.jobboard.jobs.infrastructure.web.dto;

import java.util.List;

/**
 * DTO matching the frontend Job interface:
 * id, title, company, type, location, badge, salary, description, requirements, postedAt, status, createdByUserId,
 * skills, matchScore, matchedSkills, missingSkills, recommended
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
        Long createdByUserId,
        List<String> skills,
        Integer matchScore,
        List<String> matchedSkills,
        List<String> missingSkills,
        Boolean recommended
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
        this(id, title, company, type, location, badge, salary, description, requirements, postedAt, "ACTIVE", null, List.of(), null, null, null, null);
    }

    /**
     * Backward-compatible 12-parameter constructor.
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
            String postedAt,
            String status,
            Long createdByUserId
    ) {
        this(id, title, company, type, location, badge, salary, description, requirements, postedAt, status, createdByUserId, List.of(), null, null, null, null);
    }

    /**
     * Backward-compatible 13-parameter constructor with skills.
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
            String postedAt,
            String status,
            Long createdByUserId,
            List<String> skills
    ) {
        this(id, title, company, type, location, badge, salary, description, requirements, postedAt, status, createdByUserId, skills, null, null, null, null);
    }

    public JobResponseDto {
        requirements = requirements != null ? List.copyOf(requirements) : List.of();
        skills = skills != null ? List.copyOf(skills) : List.of();
        matchedSkills = matchedSkills != null ? List.copyOf(matchedSkills) : null;
        missingSkills = missingSkills != null ? List.copyOf(missingSkills) : null;
    }
}
