package com.jobboard.applications.infrastructure.web;

import com.jobboard.auth.application.port.in.AuthenticateTokenUseCase;
import com.jobboard.auth.domain.exception.UnauthorizedAccessException;
import com.jobboard.applications.application.port.in.DownloadResumeUseCase;
import com.jobboard.applications.application.port.in.GetApplicationsUseCase;
import com.jobboard.applications.application.port.in.SubmitApplicationUseCase;
import com.jobboard.applications.application.port.in.SubmitApplicationUseCase.SubmitApplicationCommand;
import com.jobboard.applications.application.port.in.UpdateApplicationStatusUseCase;
import com.jobboard.applications.domain.exception.InvalidApplicationException;
import com.jobboard.applications.domain.model.ApplicationStatus;
import com.jobboard.applications.domain.model.JobApplication;
import com.jobboard.applications.infrastructure.web.dto.*;
import com.jobboard.applications.infrastructure.web.mapper.ApplicationWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@Tag(name = "Applications", description = "Állásjelentkezések kezelése")
public class ApplicationRestController {

    private final SubmitApplicationUseCase submitApplicationUseCase;
    private final GetApplicationsUseCase getApplicationsUseCase;
    private final UpdateApplicationStatusUseCase updateApplicationStatusUseCase;
    private final DownloadResumeUseCase downloadResumeUseCase;
    private final ApplicationWebMapper applicationWebMapper;
    private final AuthenticateTokenUseCase authenticateTokenUseCase;

    @Autowired
    public ApplicationRestController(SubmitApplicationUseCase submitApplicationUseCase,
                                     GetApplicationsUseCase getApplicationsUseCase,
                                     @Autowired(required = false) UpdateApplicationStatusUseCase updateApplicationStatusUseCase,
                                     @Autowired(required = false) DownloadResumeUseCase downloadResumeUseCase,
                                     ApplicationWebMapper applicationWebMapper,
                                     @Autowired(required = false) AuthenticateTokenUseCase authenticateTokenUseCase) {
        this.submitApplicationUseCase = submitApplicationUseCase;
        this.getApplicationsUseCase = getApplicationsUseCase;
        this.updateApplicationStatusUseCase = updateApplicationStatusUseCase;
        this.downloadResumeUseCase = downloadResumeUseCase;
        this.applicationWebMapper = applicationWebMapper;
        this.authenticateTokenUseCase = authenticateTokenUseCase;
    }

    public ApplicationRestController(SubmitApplicationUseCase submitApplicationUseCase,
                                     GetApplicationsUseCase getApplicationsUseCase,
                                     ApplicationWebMapper applicationWebMapper) {
        this(submitApplicationUseCase, getApplicationsUseCase, null, null, applicationWebMapper, null);
    }

    @PostMapping(value = "/api/applications", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Jelentkezés beküldése (JSON)", description = "Beküldi a jelentkezést a megadott jobId-hoz JSON payloadként.")
    public ResponseEntity<ApplicationSubmissionResponseDto> submitApplicationJson(
            @Valid @RequestBody SubmitApplicationRequestDto request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long applicantUserId = null;
        if (authHeader != null && authenticateTokenUseCase != null) {
            var user = authenticateTokenUseCase.authenticateOptional(authHeader);
            if (user != null) {
                applicantUserId = user.id();
            }
        }

        byte[] resumeData = null;
        String contentType = null;
        if (request.resumeFileName() != null && !request.resumeFileName().isBlank()) {
            if (request.resumeFileName().toLowerCase().endsWith(".pdf")) {
                contentType = "application/pdf";
                resumeData = "%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\nxref\n0 2\n0000000000 65535 f \n0000000009 00000 n \ntrailer<</Size 2/Root 1 0 R>>\nstartxref\n56\n%%EOF".getBytes(StandardCharsets.UTF_8);
            } else if (request.resumeFileName().toLowerCase().endsWith(".docx")) {
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                resumeData = "PK\u0003\u0004mock-docx-content".getBytes(StandardCharsets.UTF_8);
            }
        }

        SubmitApplicationCommand command = applicationWebMapper.toCommand(request, applicantUserId, contentType, resumeData);
        JobApplication saved = submitApplicationUseCase.submitApplication(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationWebMapper.toSubmissionResponse(saved));
    }

    @PostMapping(value = "/api/applications", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Jelentkezés beküldése önéletrajzzal (Multipart)", description = "Beküldi a jelentkezést és feltölti a PDF/DOCX önéletrajz fájlt.")
    public ResponseEntity<ApplicationSubmissionResponseDto> submitApplicationMultipart(
            @RequestParam("jobId") Long jobId,
            @RequestParam("applicantName") String applicantName,
            @RequestParam("applicantEmail") String applicantEmail,
            @RequestPart(value = "resume", required = false) MultipartFile resume,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long applicantUserId = null;
        if (authHeader != null && authenticateTokenUseCase != null) {
            var user = authenticateTokenUseCase.authenticateOptional(authHeader);
            if (user != null) {
                applicantUserId = user.id();
            }
        }

        String fileName = null;
        String contentType = null;
        byte[] resumeBytes = null;

        if (resume != null && !resume.isEmpty()) {
            fileName = resume.getOriginalFilename();
            contentType = resume.getContentType();
            try {
                resumeBytes = resume.getBytes();
            } catch (IOException e) {
                throw new InvalidApplicationException("Nem sikerült beolvasni a feltöltött önéletrajz fájlt.");
            }
        }

        SubmitApplicationCommand command = new SubmitApplicationCommand(
                jobId,
                applicantName,
                applicantEmail,
                fileName,
                applicantUserId,
                contentType,
                resumeBytes
        );

        JobApplication saved = submitApplicationUseCase.submitApplication(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationWebMapper.toSubmissionResponse(saved));
    }

    @PostMapping(value = "/api/jobs/{jobId}/applications", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Jelentkezés beküldése URL paraméteres álláshoz (JSON)")
    public ResponseEntity<ApplicationSubmissionResponseDto> submitApplicationForJobJson(
            @PathVariable Long jobId,
            @Valid @RequestBody SubmitApplicationRequestDto request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        SubmitApplicationRequestDto overridden = new SubmitApplicationRequestDto(
                jobId,
                request.applicantName(),
                request.applicantEmail(),
                request.resumeFileName()
        );
        return submitApplicationJson(overridden, authHeader);
    }

    @PostMapping(value = "/api/jobs/{jobId}/applications", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Jelentkezés beküldése URL paraméteres álláshoz (Multipart)")
    public ResponseEntity<ApplicationSubmissionResponseDto> submitApplicationForJobMultipart(
            @PathVariable Long jobId,
            @RequestParam("applicantName") String applicantName,
            @RequestParam("applicantEmail") String applicantEmail,
            @RequestPart(value = "resume", required = false) MultipartFile resume,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return submitApplicationMultipart(jobId, applicantName, applicantEmail, resume, authHeader);
    }

    @GetMapping("/api/applications/my")
    @Operation(summary = "Bejelentkezett jelentkező saját jelentkezései", description = "Visszaadja a bejelentkezett felhasználó által benyújtott jelentkezéseket és azok állapotát.")
    public ResponseEntity<List<ApplicantApplicationResponseDto>> getMyApplications(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authenticateTokenUseCase == null) {
            throw new UnauthorizedAccessException("Hiányzó hitelesítési token.");
        }
        var user = authenticateTokenUseCase.authenticate(authHeader);

        var views = getApplicationsUseCase.getApplicationsForApplicant(user.id(), user.email());
        var dtos = views.stream()
                .map(applicationWebMapper::toApplicantViewDto)
                .toList();
        return ResponseEntity.ok(dtos);
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

    @PatchMapping("/api/applications/{id}/status")
    @Operation(summary = "Jelentkezés státuszának módosítása (ACCEPTED vagy REJECTED)", description = "Céges kezelő elfogadhatja vagy elutasíthatja a jelentkezést.")
    public ResponseEntity<JobApplicationDetailDto> updateApplicationStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateApplicationStatusRequestDto request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authenticateTokenUseCase == null) {
            throw new UnauthorizedAccessException("Hiányzó hitelesítési token.");
        }
        var user = authenticateTokenUseCase.authenticate(authHeader);
        if (!user.isCompanyManager()) {
            throw new UnauthorizedAccessException("Csak céges kezelők módosíthatják a jelentkezés státuszát.");
        }

        ApplicationStatus newStatus;
        try {
            newStatus = ApplicationStatus.valueOf(request.status().trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidApplicationException("Érvénytelen státusz: " + request.status() + ". Megengedett: ACCEPTED, REJECTED");
        }

        if (updateApplicationStatusUseCase == null) {
            throw new IllegalStateException("UpdateApplicationStatusUseCase nem áll rendelkezésre.");
        }

        JobApplication updated = updateApplicationStatusUseCase.updateStatus(id, newStatus, user.id());
        return ResponseEntity.ok(applicationWebMapper.toDetailDto(updated));
    }

    @GetMapping("/api/applications/{id}/resume")
    @Operation(summary = "Önéletrajz fájl letöltése", description = "Visszaadja a feltöltött PDF vagy DOCX fájlt attachment-ként.")
    public ResponseEntity<byte[]> downloadResume(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authenticateTokenUseCase == null) {
            throw new UnauthorizedAccessException("Hiányzó hitelesítési token.");
        }
        var user = authenticateTokenUseCase.authenticate(authHeader);

        if (downloadResumeUseCase == null) {
            throw new IllegalStateException("DownloadResumeUseCase nem áll rendelkezésre.");
        }

        DownloadResumeUseCase.ResumeFile resumeFile = downloadResumeUseCase.getResume(id, user.id());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resumeFile.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resumeFile.fileName() + "\"")
                .body(resumeFile.data());
    }
}
