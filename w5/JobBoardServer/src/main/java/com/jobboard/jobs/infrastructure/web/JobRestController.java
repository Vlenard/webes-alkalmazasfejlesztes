package com.jobboard.jobs.infrastructure.web;

import com.jobboard.jobs.application.port.in.CreateJobUseCase;
import com.jobboard.jobs.application.port.in.GetJobDetailUseCase;
import com.jobboard.jobs.application.port.in.GetJobsUseCase;
import com.jobboard.jobs.domain.model.Job;
import com.jobboard.jobs.infrastructure.web.dto.CreateJobRequestDto;
import com.jobboard.jobs.infrastructure.web.dto.JobResponseDto;
import com.jobboard.jobs.infrastructure.web.mapper.JobWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Jobs", description = "Álláshirdetések kezelése (lekérdezés, keresés, szűrés, létrehozás)")
public class JobRestController {

    private final GetJobsUseCase getJobsUseCase;
    private final GetJobDetailUseCase getJobDetailUseCase;
    private final CreateJobUseCase createJobUseCase;
    private final JobWebMapper jobWebMapper;

    public JobRestController(GetJobsUseCase getJobsUseCase,
                             GetJobDetailUseCase getJobDetailUseCase,
                             CreateJobUseCase createJobUseCase,
                             JobWebMapper jobWebMapper) {
        this.getJobsUseCase = getJobsUseCase;
        this.getJobDetailUseCase = getJobDetailUseCase;
        this.createJobUseCase = createJobUseCase;
        this.jobWebMapper = jobWebMapper;
    }

    @GetMapping
    @Operation(summary = "Állások listázása", description = "Lekéri az összes álláshirdetést, opcionálisan kulcsszó (search) és technológia (badge) alapján szűrve.")
    public ResponseEntity<List<JobResponseDto>> getJobs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String badge) {
        List<Job> jobs = getJobsUseCase.getJobs(search, badge);
        List<JobResponseDto> response = jobs.stream()
                .map(jobWebMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Egy álláshirdetés lekérése ID alapján")
    public ResponseEntity<JobResponseDto> getJobById(@PathVariable Long id) {
        Job job = getJobDetailUseCase.getJobById(id);
        return ResponseEntity.ok(jobWebMapper.toResponseDto(job));
    }

    @PostMapping
    @Operation(summary = "Új álláshirdetés feladása")
    public ResponseEntity<JobResponseDto> createJob(@Valid @RequestBody CreateJobRequestDto request) {
        Job createdJob = createJobUseCase.createJob(jobWebMapper.toCommand(request));
        JobResponseDto response = jobWebMapper.toResponseDto(createdJob);
        return ResponseEntity.created(URI.create("/api/jobs/" + createdJob.getId()))
                .body(response);
    }
}
