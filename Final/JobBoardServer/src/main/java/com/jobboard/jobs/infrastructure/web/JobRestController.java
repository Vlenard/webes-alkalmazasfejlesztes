package com.jobboard.jobs.infrastructure.web;

import com.jobboard.auth.application.port.in.AuthenticateTokenUseCase;
import com.jobboard.auth.application.port.out.UserRepositoryPort;
import com.jobboard.auth.domain.exception.UnauthorizedAccessException;
import com.jobboard.jobs.application.port.in.CreateJobUseCase;
import com.jobboard.jobs.application.port.in.GetJobDetailUseCase;
import com.jobboard.jobs.application.port.in.GetJobsUseCase;
import com.jobboard.jobs.application.port.in.UpdateJobSkillsUseCase;
import com.jobboard.jobs.application.port.in.UpdateJobStatusUseCase;
import com.jobboard.jobs.domain.exception.InvalidJobDataException;
import com.jobboard.jobs.domain.model.Job;
import com.jobboard.jobs.domain.model.JobStatus;
import com.jobboard.jobs.domain.service.JobRecommendationMatcher;
import com.jobboard.jobs.infrastructure.web.dto.CreateJobRequestDto;
import com.jobboard.jobs.infrastructure.web.dto.JobResponseDto;
import com.jobboard.jobs.infrastructure.web.dto.UpdateJobSkillsRequestDto;
import com.jobboard.jobs.infrastructure.web.dto.UpdateJobStatusRequestDto;
import com.jobboard.jobs.infrastructure.web.mapper.JobWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Jobs", description = "Álláshirdetések kezelése (lekérdezés, keresés, szűrés, létrehozás, státuszmódosítás, skillek)")
public class JobRestController {

    private final GetJobsUseCase getJobsUseCase;
    private final GetJobDetailUseCase getJobDetailUseCase;
    private final CreateJobUseCase createJobUseCase;
    private final UpdateJobStatusUseCase updateJobStatusUseCase;
    private final JobWebMapper jobWebMapper;
    private final AuthenticateTokenUseCase authenticateTokenUseCase;
    private final UpdateJobSkillsUseCase updateJobSkillsUseCase;
    private final UserRepositoryPort userRepositoryPort;
    private final JobRecommendationMatcher jobRecommendationMatcher;

    @Autowired
    public JobRestController(GetJobsUseCase getJobsUseCase,
                             GetJobDetailUseCase getJobDetailUseCase,
                             CreateJobUseCase createJobUseCase,
                             @Autowired(required = false) UpdateJobStatusUseCase updateJobStatusUseCase,
                             JobWebMapper jobWebMapper,
                             @Autowired(required = false) AuthenticateTokenUseCase authenticateTokenUseCase,
                             @Autowired(required = false) UpdateJobSkillsUseCase updateJobSkillsUseCase,
                             @Autowired(required = false) UserRepositoryPort userRepositoryPort,
                             @Autowired(required = false) JobRecommendationMatcher jobRecommendationMatcher) {
        this.getJobsUseCase = getJobsUseCase;
        this.getJobDetailUseCase = getJobDetailUseCase;
        this.createJobUseCase = createJobUseCase;
        this.updateJobStatusUseCase = updateJobStatusUseCase;
        this.jobWebMapper = jobWebMapper;
        this.authenticateTokenUseCase = authenticateTokenUseCase;
        this.updateJobSkillsUseCase = updateJobSkillsUseCase;
        this.userRepositoryPort = userRepositoryPort;
        this.jobRecommendationMatcher = (jobRecommendationMatcher != null) ? jobRecommendationMatcher : new JobRecommendationMatcher();
    }

    public JobRestController(GetJobsUseCase getJobsUseCase,
                             GetJobDetailUseCase getJobDetailUseCase,
                             CreateJobUseCase createJobUseCase,
                             UpdateJobStatusUseCase updateJobStatusUseCase,
                             JobWebMapper jobWebMapper,
                             AuthenticateTokenUseCase authenticateTokenUseCase) {
        this(getJobsUseCase, getJobDetailUseCase, createJobUseCase, updateJobStatusUseCase, jobWebMapper, authenticateTokenUseCase, null, null, null);
    }

    public JobRestController(GetJobsUseCase getJobsUseCase,
                             GetJobDetailUseCase getJobDetailUseCase,
                             CreateJobUseCase createJobUseCase,
                             UpdateJobStatusUseCase updateJobStatusUseCase,
                             JobWebMapper jobWebMapper) {
        this(getJobsUseCase, getJobDetailUseCase, createJobUseCase, updateJobStatusUseCase, jobWebMapper, null, null, null, null);
    }

    @GetMapping
    @Operation(summary = "Állások listázása", description = "Lekéri a nyilvános, CSAK AKTÍV álláshirdetéseket, opcionálisan keresve vagy szűrve.")
    public ResponseEntity<List<JobResponseDto>> getJobs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String badge,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        List<Job> jobs = getJobsUseCase.getJobs(search, badge);

        if (authHeader != null && authenticateTokenUseCase != null) {
            var user = authenticateTokenUseCase.authenticateOptional(authHeader);
            if (user != null && user.isApplicant()) {
                List<String> applicantSkills = resolveApplicantSkills(user);
                final List<String> effectiveUserSkills = (applicantSkills != null) ? applicantSkills : List.of();

                record JobWithMatch(Job job, JobRecommendationMatcher.MatchResult matchResult) {}

                List<JobWithMatch> evaluatedJobs = jobs.stream()
                        .map(job -> new JobWithMatch(job, jobRecommendationMatcher.evaluate(job, effectiveUserSkills)))
                        .toList();

                if (!effectiveUserSkills.isEmpty()) {
                    Comparator<JobWithMatch> comparator = Comparator
                            .comparingInt((JobWithMatch jwm) -> jwm.matchResult().matchScore()).reversed()
                            .thenComparing(Comparator.comparingInt((JobWithMatch jwm) -> jwm.matchResult().matchedSkills().size()).reversed());

                    evaluatedJobs = evaluatedJobs.stream().sorted(comparator).toList();
                }

                List<JobResponseDto> response = evaluatedJobs.stream()
                        .map(jwm -> jobWebMapper.toResponseDto(jwm.job(), jwm.matchResult()))
                        .toList();
                return ResponseEntity.ok(response);
            }
        }

        List<JobResponseDto> response = jobs.stream()
                .map(jobWebMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @Operation(summary = "Céges kezelő saját hirdetései", description = "Visszaadja a bejelentkezett cég összes (aktív és lezárt) hirdetését.")
    public ResponseEntity<List<JobResponseDto>> getMyJobs(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
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
    public ResponseEntity<JobResponseDto> getJobById(
            @PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Job job = getJobDetailUseCase.getJobById(id);
        if (authHeader != null && authenticateTokenUseCase != null) {
            var user = authenticateTokenUseCase.authenticateOptional(authHeader);
            if (user != null && user.isApplicant()) {
                List<String> applicantSkills = resolveApplicantSkills(user);
                var matchResult = jobRecommendationMatcher.evaluate(job, applicantSkills != null ? applicantSkills : List.of());
                return ResponseEntity.ok(jobWebMapper.toResponseDto(job, matchResult));
            }
        }
        return ResponseEntity.ok(jobWebMapper.toResponseDto(job));
    }

    @PostMapping
    @Operation(summary = "Új álláshirdetés feladása")
    public ResponseEntity<JobResponseDto> createJob(
            @Valid @RequestBody CreateJobRequestDto request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
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
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
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

    @PatchMapping("/{id}/skills")
    @Operation(summary = "Álláshirdetés szükséges skilljeinek módosítása", description = "Csak a hirdetés létrehozója módosíthatja a szükséges skilleket.")
    public ResponseEntity<JobResponseDto> patchJobSkills(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobSkillsRequestDto request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        return handleUpdateJobSkills(id, request, authHeader);
    }

    @PutMapping("/{id}/skills")
    @Operation(summary = "Álláshirdetés szükséges skilljeinek felülírása", description = "Csak a hirdetés létrehozója módosíthatja a szükséges skilleket.")
    public ResponseEntity<JobResponseDto> putJobSkills(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobSkillsRequestDto request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        return handleUpdateJobSkills(id, request, authHeader);
    }

    private ResponseEntity<JobResponseDto> handleUpdateJobSkills(Long id, UpdateJobSkillsRequestDto request, String authHeader) {
        if (authHeader == null || authenticateTokenUseCase == null) {
            throw new UnauthorizedAccessException("Hiányzó hitelesítési token.");
        }
        var user = authenticateTokenUseCase.authenticate(authHeader);
        if (!user.isCompanyManager()) {
            throw new UnauthorizedAccessException("Csak céges kezelők módosíthatják a hirdetés skilljeit.");
        }
        if (updateJobSkillsUseCase == null) {
            throw new IllegalStateException("UpdateJobSkillsUseCase nem érhető el.");
        }
        List<String> skills = (request != null && request.skills() != null) ? request.skills() : List.of();
        Job updatedJob = updateJobSkillsUseCase.updateJobSkills(id, skills, user.id());
        return ResponseEntity.ok(jobWebMapper.toResponseDto(updatedJob));
    }

    private List<String> resolveApplicantSkills(AuthenticateTokenUseCase.AuthenticatedUser user) {
        if (user == null) {
            return List.of();
        }
        List<String> applicantSkills = user.skills();
        if (userRepositoryPort != null && user.id() != null) {
            var freshUser = userRepositoryPort.findById(user.id());
            if (freshUser.isPresent()) {
                applicantSkills = freshUser.get().getSkills();
            }
        }
        return applicantSkills != null ? applicantSkills : List.of();
    }
}

