package com.jobboard.jobs.domain.model;

import com.jobboard.jobs.domain.exception.InvalidJobDataException;
import java.time.LocalDate;
import java.util.ArrayList;
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

    public Job(Long id, String title, String company, JobType type, String location,
               String badge, String salary, String description, List<String> requirements,
               LocalDate postedAt) {
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
    }

    public static Job createNew(String title, String company, JobType type, String location,
                                String badge, String salary, String description,
                                List<String> requirements) {
        return new Job(null, title, company, type, location, badge, salary, description, requirements, LocalDate.now());
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
