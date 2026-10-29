package com.jobboard.jobs.infrastructure.persistence.entity;

import com.jobboard.jobs.domain.model.JobStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "jobs")
public class JobJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String badge;

    @Column(nullable = false)
    private String salary;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "job_requirements", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "requirement")
    private List<String> requirements = new ArrayList<>();

    @Column(name = "posted_at", nullable = false)
    private LocalDate postedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(32) default 'ACTIVE'")
    private JobStatus status = JobStatus.ACTIVE;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "job_skills", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    public JobJpaEntity() {}

    public JobJpaEntity(Long id, String title, String company, String type, String location,
                        String badge, String salary, String description, List<String> requirements,
                        LocalDate postedAt) {
        this(id, title, company, type, location, badge, salary, description, requirements, postedAt, JobStatus.ACTIVE, null, new ArrayList<>());
    }

    public JobJpaEntity(Long id, String title, String company, String type, String location,
                        String badge, String salary, String description, List<String> requirements,
                        LocalDate postedAt, JobStatus status, Long createdByUserId) {
        this(id, title, company, type, location, badge, salary, description, requirements, postedAt, status, createdByUserId, new ArrayList<>());
    }

    public JobJpaEntity(Long id, String title, String company, String type, String location,
                        String badge, String salary, String description, List<String> requirements,
                        LocalDate postedAt, JobStatus status, Long createdByUserId, List<String> skills) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.type = type;
        this.location = location;
        this.badge = badge;
        this.salary = salary;
        this.description = description;
        this.requirements = requirements != null ? new ArrayList<>(requirements) : new ArrayList<>();
        this.postedAt = postedAt;
        this.status = status != null ? status : JobStatus.ACTIVE;
        this.createdByUserId = createdByUserId;
        this.skills = skills != null ? new ArrayList<>(skills) : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<String> requirements) {
        this.requirements = requirements != null ? new ArrayList<>(requirements) : new ArrayList<>();
    }

    public LocalDate getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(LocalDate postedAt) {
        this.postedAt = postedAt;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status != null ? status : JobStatus.ACTIVE;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills != null ? new ArrayList<>(skills) : new ArrayList<>();
    }
}
