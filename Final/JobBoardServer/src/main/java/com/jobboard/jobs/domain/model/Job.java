package com.jobboard.jobs.domain.model;

import com.jobboard.jobs.domain.exception.InvalidJobDataException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Domain entity representing a Job Listing.
 * Core domain logic isolated from infrastructure and frameworks.
 */
public class Job {
    private final Long id;
    private final String title;
    private final String company;
    private final JobType type;
    private final String location;
    private final String badge;
    private final String salary;
    private final String description;
    private final List<String> requirements;
    private final LocalDate postedAt;
    private final JobStatus status;
    private final Long createdByUserId;

    /**
     * Backward-compatible 10-parameter constructor (default status: ACTIVE, createdByUserId: null).
     */
    public Job(Long id, String title, String company, JobType type, String location,
               String badge, String salary, String description, List<String> requirements,
               LocalDate postedAt) {
        this(id, title, company, type, location, badge, salary, description, requirements, postedAt, JobStatus.ACTIVE, null);
    }

    /**
     * Canonical constructor initializing all fields of the Job aggregate.
     */
    public Job(Long id, String title, String company, JobType type, String location,
               String badge, String salary, String description, List<String> requirements,
               LocalDate postedAt, JobStatus status, Long createdByUserId) {
        validate(title, company, type, location, badge, salary, description);
        this.id = id;
        this.title = title.trim();
        this.company = company.trim();
        this.type = type;
        this.location = location.trim();
        this.badge = badge.trim();
        this.salary = salary.trim();
        this.description = description.trim();
        this.requirements = requirements != null ? List.copyOf(requirements) : Collections.emptyList();
        this.postedAt = postedAt != null ? postedAt : LocalDate.now();
        this.status = status != null ? status : JobStatus.ACTIVE;
        this.createdByUserId = createdByUserId;
    }

    public static Job createNew(String title, String company, JobType type, String location,
                                String badge, String salary, String description,
                                List<String> requirements) {
        return createNew(title, company, type, location, badge, salary, description, requirements, null);
    }

    public static Job createNew(String title, String company, JobType type, String location,
                                String badge, String salary, String description,
                                List<String> requirements, Long createdByUserId) {
        return new Job(null, title, company, type, location, badge, salary, description,
                requirements, LocalDate.now(), JobStatus.ACTIVE, createdByUserId);
    }

    private static void validate(String title, String company, JobType type, String location,
                                 String badge, String salary, String description) {
        if (title == null || title.isBlank()) {
            throw new InvalidJobDataException("A pozíció megnevezése nem lehet üres.");
        }
        if (company == null || company.isBlank()) {
            throw new InvalidJobDataException("A cég neve nem lehet üres.");
        }
        if (type == null) {
            throw new InvalidJobDataException("A foglalkoztatási típus megadása kötelező.");
        }
        if (location == null || location.isBlank()) {
            throw new InvalidJobDataException("A helyszín megadása kötelező.");
        }
        if (badge == null || badge.isBlank()) {
            throw new InvalidJobDataException("A technológia / badge megadása kötelező.");
        }
        if (salary == null || salary.isBlank()) {
            throw new InvalidJobDataException("A bérsáv megadása kötelező.");
        }
        if (description == null || description.isBlank()) {
            throw new InvalidJobDataException("A leírás megadása kötelező.");
        }
    }

    public boolean matchesSearch(String searchQuery) {
        if (searchQuery == null || searchQuery.isBlank()) {
            return true;
        }
        String q = searchQuery.trim().toLowerCase();
        return this.title.toLowerCase().contains(q) || this.company.toLowerCase().contains(q);
    }

    public boolean matchesBadge(String badgeFilter) {
        if (badgeFilter == null || badgeFilter.isBlank()) {
            return true;
        }
        return this.badge.equalsIgnoreCase(badgeFilter.trim());
    }

    public boolean isActive() {
        return this.status == JobStatus.ACTIVE;
    }

    public boolean isClosed() {
        return this.status == JobStatus.CLOSED;
    }

    public Job withStatusChangedBy(JobStatus newStatus, Long requestingUserId) {
        if (newStatus == null) {
            throw new InvalidJobDataException("A státusz megadása kötelező.");
        }
        if (this.createdByUserId != null && !this.createdByUserId.equals(requestingUserId)) {
            throw new InvalidJobDataException("Csak a hirdetés létrehozója módosíthatja vagy nyithatja újra a hirdetést.");
        }
        return new Job(this.id, this.title, this.company, this.type, this.location, this.badge,
                this.salary, this.description, this.requirements, this.postedAt, newStatus, this.createdByUserId);
    }

    public Job withCreatedByUserId(Long createdByUserId) {
        return new Job(this.id, this.title, this.company, this.type, this.location, this.badge,
                this.salary, this.description, this.requirements, this.postedAt, this.status, createdByUserId);
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCompany() {
        return company;
    }

    public JobType getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public String getBadge() {
        return badge;
    }

    public String getSalary() {
        return salary;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public LocalDate getPostedAt() {
        return postedAt;
    }

    public JobStatus getStatus() {
        return status;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return Objects.equals(id, job.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
