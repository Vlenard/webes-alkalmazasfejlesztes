package com.jobboard.jobs.infrastructure.web.dto;

import java.util.List;

/**
 * DTO matching the frontend Job interface exactly:
 * id, title, company, type, location, badge, salary, description, requirements, postedAt
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
        String postedAt
) {}
