package com.jobboard.applications.application.port.in;

import com.jobboard.applications.domain.model.ApplicationStatus;
import com.jobboard.applications.domain.model.JobApplication;

/**
 * Inbound port for an employer to accept or reject an application.
 */
public interface UpdateApplicationStatusUseCase {

    JobApplication updateStatus(Long applicationId, ApplicationStatus newStatus, Long employerUserId);
}
