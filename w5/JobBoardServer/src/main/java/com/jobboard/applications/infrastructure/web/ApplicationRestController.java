package com.jobboard.applications.infrastructure.web;

import com.jobboard.applications.application.port.in.GetApplicationsUseCase;
import com.jobboard.applications.application.port.in.SubmitApplicationUseCase;
import com.jobboard.applications.application.port.in.SubmitApplicationUseCase.SubmitApplicationCommand;
import com.jobboard.applications.domain.model.JobApplication;
import com.jobboard.applications.infrastructure.web.dto.ApplicationSubmissionResponseDto;
import com.jobboard.applications.infrastructure.web.dto.JobApplicationDetailDto;
import com.jobboard.applications.infrastructure.web.dto.SubmitApplicationRequestDto;
import com.jobboard.applications.infrastructure.web.mapper.ApplicationWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Applications", description = "Állásjelentkezések kezelése")
public class ApplicationRestController {

    private final SubmitApplicationUseCase submitApplicationUseCase;
    private final GetApplicationsUseCase getApplicationsUseCase;
    private final ApplicationWebMapper applicationWebMapper;

    public ApplicationRestController(SubmitApplicationUseCase submitApplicationUseCase,
                                     GetApplicationsUseCase getApplicationsUseCase,
                                     ApplicationWebMapper applicationWebMapper) {
        this.submitApplicationUseCase = submitApplicationUseCase;
        this.getApplicationsUseCase = getApplicationsUseCase;
        this.applicationWebMapper = applicationWebMapper;
    }

    @PostMapping("/api/applications")
    @Operation(summary = "Jelentkezés beküldése", description = "Beküldi a jelentkezést a megadott jobId-hoz, illeszkedve a frontendhez.")
    public ResponseEntity<ApplicationSubmissionResponseDto> submitApplication(
            @Valid @RequestBody SubmitApplicationRequestDto request) {
        JobApplication saved = submitApplicationUseCase.submitApplication(applicationWebMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationWebMapper.toSubmissionResponse(saved));
    }

    @PostMapping("/api/jobs/{jobId}/applications")
    @Operation(summary = "Jelentkezés beküldése URL paraméteres álláshoz")
    public ResponseEntity<ApplicationSubmissionResponseDto> submitApplicationForJob(
            @PathVariable Long jobId,
            @Valid @RequestBody SubmitApplicationRequestDto request) {
        // Enforce URL path jobId
        SubmitApplicationCommand command = new SubmitApplicationCommand(
                jobId,
                request.applicantName(),
                request.applicantEmail(),
                request.resumeFileName()
        );
        JobApplication saved = submitApplicationUseCase.submitApplication(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationWebMapper.toSubmissionResponse(saved));
    }

    @GetMapping("/api/jobs/{jobId}/applications")
    @Operation(summary = "Egy adott álláshoz tartozó jelentkezések lekérdezése")
    public ResponseEntity<List<JobApplicationDetailDto>> getApplicationsByJob(@PathVariable Long jobId) {
        List<JobApplication> list = getApplicationsUseCase.getApplicationsByJobId(jobId);
        List<JobApplicationDetailDto> dtos = list.stream()
                .map(applicationWebMapper::toDetailDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/api/applications")
    @Operation(summary = "Összes leadott jelentkezés lekérdezése")
    public ResponseEntity<List<GetApplicationsUseCase.GetAllApplicationsResult>> getAllApplications() {
        return ResponseEntity.ok(getApplicationsUseCase.getAllApplications());
    }
}
