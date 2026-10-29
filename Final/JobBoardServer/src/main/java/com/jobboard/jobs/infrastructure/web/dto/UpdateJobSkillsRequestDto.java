package com.jobboard.jobs.infrastructure.web.dto;

import java.util.List;

public record UpdateJobSkillsRequestDto(
        List<String> skills
) {
    public UpdateJobSkillsRequestDto() {
        this(List.of());
    }

    public UpdateJobSkillsRequestDto {
        skills = skills != null ? List.copyOf(skills) : List.of();
    }
}
