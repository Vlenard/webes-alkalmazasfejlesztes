package com.jobboard;

import com.jobboard.jobs.domain.service.JobRecommendationMatcher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JobBoardServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobBoardServerApplication.class, args);
    }

    @Bean
    public JobRecommendationMatcher jobRecommendationMatcher() {
        return new JobRecommendationMatcher();
    }
}
