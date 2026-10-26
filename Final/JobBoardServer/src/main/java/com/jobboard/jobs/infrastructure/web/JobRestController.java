package com.jobboard.jobs.infrastructure.web;

import com.jobboard.auth.application.port.in.AuthenticateTokenUseCase;
import com.jobboard.auth.domain.exception.UnauthorizedAccessException;
import com.jobboard.jobs.application.port.in.CreateJobUseCase;
import com.jobboard.jobs.application.port.in.GetJobDetailUseCase;
import com.jobboard.jobs.application.port.in.GetJobsUseCase;
import com.jobboard.jobs.application.port.in.UpdateJobStatusUseCase;
import com.jobboard.jobs.domain.exception.InvalidJobDataException;
import com.jobboard.jobs.domain.model.Job;
import com.jobboard.jobs.domain.model.JobStatus;
import com.jobboard.jobs.infrastructure.web.dto.CreateJobRequestDto;
import com.jobboard.jobs.infrastructure.web.dto.JobResponseDto;
import com.jobboard.jobs.infrastructure.web.dto.UpdateJobStatusRequestDto;
import com.jobboard.jobs.infrastructure.web.mapper.JobWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Jobs", description = "Álláshirdetések kezelése (lekérdezés, keresés, szűrés, létrehozás, státuszmódosítás)")
public class JobRestController {

    private final GetJobsUseCase getJobsUseCase;
    private final GetJobDetailUseCase getJobDetailUseCase;
    private final CreateJobUseCase createJobUseCase;
    private final UpdateJobStatusUseCase updateJobStatusUseCase;
    private final JobWebMapper jobWebMapper;
    private final AuthenticateTokenUseCase authenticateTokenUseCase;

    @Autowired
    public JobRestController(GetJobsUseCase getJobsUseCase,
                             GetJobDetailUseCase getJobDetailUseCase,
                             CreateJobUseCase createJobUseCase,
                             @Autowired(required = false) UpdateJobStatusUseCase updateJobStatusUseCase,
                             JobWebMapper jobWebMapper,
                             @Autowired(required = false) AuthenticateTokenUseCase authenticateTokenUseCase) {
        this.getJobsUseCase = getJobsUseCase;
        this.getJobDetailUseCase = getJobDetailUseCase;
        this.createJobUseCase = createJobUseCase;
        this.updateJobStatusUseCase = updateJobStatusUseCase;
        this.jobWebMapper = jobWebMapper;
        this.authenticateTokenUseCase = authenticateTokenUseCase;
    }

    public JobRestController(GetJobsUseCase getJobsUseCase,
                             GetJobDetailUseCase getJobDetailUseCase,
                             CreateJobUseCase createJobUseCase,
                             UpdateJobStatusUseCase updateJobStatusUseCase,
                             JobWebMapper jobWebMapper) {
        this(getJobsUseCase, getJobDetailUseCase, createJobUseCase, updateJobStatusUseCase, jobWebMapper, null);
    }

    @GetMapping
    @Operation(summary = "Állások listázása", description = "Lekéri a nyilvános, CSAK AKTÍV álláshirdetéseket, opcionálisan keresve vagy szűrve.")
    public ResponseEntity<List<JobResponseDto>> getJobs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String badge) {
        List<Job> jobs = getJobsUseCase.getJobs(search, badge);
        List<JobResponseDto> response = jobs.stream()
                .map(jobWebMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @Operation(summary = "Céges kezelő saját hirdetései", description = "Visszaadja a bejelentkezett cég összes (aktív és lezárt) hirdetését.")
    public ResponseEntity<List<JobResponseDto>> getMyJobs(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authenticateTokenUseCase == null) {
            throw new UnauthorizedAccessException("Hiányzó hitelesítési token.");
        }
        var user = authenticateTokenUseCase.authenticate(authHeader);
        if (!user.isCompanyManager()) {
            throw new UnauthorizedAccessException("Csak céges kezelők érhetik el a saját hirdetéseiket.");
        }

        List<Job> jobs = getJobsUseCase.getJobsByCreator(user.id(), user.companyName());
        List<JobResponseDto> response = jobs.stream()
                .map(jobWebMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id:[0-9]+}")
    @Operation(summary = "Egy álláshirdetés lekérése ID alapján")
    public ResponseEntity<JobResponseDto> getJobById(@PathVariable Long id) {
        Job job = getJobDetailUseCase.getJobById(id);
        return ResponseEntity.ok(jobWebMapper.toResponseDto(job));
    }

    @PostMapping
    @Operation(summary = "Új álláshirdetés feladása")
    public ResponseEntity<JobResponseDto> createJob(
            @Valid @RequestBody CreateJobRequestDto request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long createdByUserId = null;
        if (authHeader != null && authenticateTokenUseCase != null) {
            var user = authenticateTokenUseCase.authenticate(authHeader);
            if (!user.isCompanyManager()) {
                throw new UnauthorizedAccessException("Csak céges kezelők hozhatnak létre új hirdetést.");
            }
            createdByUserId = user.id();
        }

        Job createdJob = createJobUseCase.createJob(jobWebMapper.toCommand(request, createdByUserId));
        JobResponseDto response = jobWebMapper.toResponseDto(createdJob);
        return ResponseEntity.created(URI.create("/api/jobs/" + createdJob.getId()))
                .body(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Álláshirdetés státuszának módosítása (ACTIVE vagy CLOSED)", description = "Csak a hirdetés létrehozója zárhatja le vagy nyithatja újra.")
    public ResponseEntity<JobResponseDto> updateJobStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobStatusRequestDto request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authenticateTokenUseCase == null) {
            throw new UnauthorizedAccessException("Hiányzó hitelesítési token.");
        }
        var user = authenticateTokenUseCase.authenticate(authHeader);
        if (!user.isCompanyManager()) {
            throw new UnauthorizedAccessException("Csak céges kezelők módosíthatják a hirdetés státuszát.");
        }

        JobStatus newStatus;
        try {
            newStatus = JobStatus.valueOf(request.status().trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidJobDataException("Érvénytelen hirdetés státusz: " + request.status() + ". Megengedett: ACTIVE, CLOSED");
        }

        Job updatedJob = updateJobStatusUseCase.updateJobStatus(id, newStatus, user.id());
        return ResponseEntity.ok(jobWebMapper.toResponseDto(updatedJob));
    }
}
