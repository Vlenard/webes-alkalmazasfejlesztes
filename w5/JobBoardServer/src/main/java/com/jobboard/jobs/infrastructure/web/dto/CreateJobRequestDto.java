package com.jobboard.jobs.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateJobRequestDto(
        @NotBlank(message = "A pozíció megnevezése nem lehet üres.")
        String title,

        @NotBlank(message = "A cég neve nem lehet üres.")
        String company,

        @NotBlank(message = "A foglalkoztatási típus nem lehet üres (Full-time, Part-time, Contract, Remote).")
        String type,

        @NotBlank(message = "A helyszín nem lehet üres.")
        String location,

        @NotBlank(message = "A technológia / badge nem lehet üres.")
        String badge,

        @NotBlank(message = "A bérsáv nem lehet üres.")
        String salary,

        @NotBlank(message = "A leírás nem lehet üres.")
        String description,

        @NotEmpty(message = "Legalább egy követelmény megadása szükséges.")
        List<String> requirements
) {}
