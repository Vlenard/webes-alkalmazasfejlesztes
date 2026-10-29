package com.jobboard.jobs.infrastructure.web.mapper;

import com.jobboard.jobs.application.port.in.CreateJobUseCase.CreateJobCommand;
import com.jobboard.jobs.domain.model.Job;
import com.jobboard.jobs.domain.model.JobType;
import com.jobboard.jobs.domain.service.JobRecommendationMatcher.MatchResult;
import com.jobboard.jobs.infrastructure.web.dto.CreateJobRequestDto;
import com.jobboard.jobs.infrastructure.web.dto.JobResponseDto;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class JobWebMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public JobResponseDto toResponseDto(Job job) {
        return toResponseDto(job, null);
    }

    public JobResponseDto toResponseDto(Job job, MatchResult matchResult) {
        if (job == null) {
            return null;
        }
        Integer matchScore = matchResult != null ? matchResult.matchScore() : null;
        List<String> matchedSkills = matchResult != null ? matchResult.matchedSkills() : null;
        List<String> missingSkills = matchResult != null ? matchResult.missingSkills() : null;
        Boolean recommended = matchResult != null ? matchResult.recommended() : null;

        return new JobResponseDto(
                job.getId(),
                job.getTitle(),
                job.getCompany(),
                job.getType() != null ? job.getType().getDisplayName() : null,
                job.getLocation(),
                job.getBadge(),
                job.getSalary(),
                job.getDescription(),
                job.getRequirements(),
                job.getPostedAt() != null ? job.getPostedAt().format(DATE_FORMATTER) : null,
                job.getStatus() != null ? job.getStatus().name() : "ACTIVE",
                job.getCreatedByUserId(),
                job.getSkills(),
                matchScore,
                matchedSkills,
                missingSkills,
                recommended
        );
    }

    public CreateJobCommand toCommand(CreateJobRequestDto dto) {
        return toCommand(dto, null);
    }

    public CreateJobCommand toCommand(CreateJobRequestDto dto, Long createdByUserId) {
        if (dto == null) {
            return null;
        }
        return new CreateJobCommand(
                dto.title(),
                dto.company(),
                JobType.fromString(dto.type()),
                dto.location(),
                dto.badge(),
                dto.salary(),
                dto.description(),
                dto.requirements(),
                createdByUserId,
                dto.skills()
        );
    }
}
