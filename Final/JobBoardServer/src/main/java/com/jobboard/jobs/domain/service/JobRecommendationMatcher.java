package com.jobboard.jobs.domain.service;

import com.jobboard.jobs.domain.model.Job;
import java.text.Normalizer;
import java.util.*;

/**
 * Pure Java Domain Service implementing a hybrid skill recommendation and matching engine.
 * No framework or persistence dependencies.
 */
public class JobRecommendationMatcher {

    private static final Set<String> STOP_WORDS = Set.of(
            "ismeret", "ismerete", "tudas", "tapasztalat", "alapok",
            "gyakorlat", "halado", "junior", "senior", "medior",
            "knowledge", "experience", "skill", "skills", "szintu"
    );

    public record MatchResult(
            int matchScore,
            List<String> matchedSkills,
            List<String> missingSkills,
            boolean recommended
    ) {}

    public JobRecommendationMatcher() {}

    /**
     * Evaluates a Job listing against a candidate's skills.
     *
     * @param job candidate Job entity
     * @param userSkills list of candidate skills
     * @return MatchResult containing match score (0-100), matched/missing skills, and recommended flag
     */
    public MatchResult evaluate(Job job, List<String> userSkills) {
        if (job == null) {
            return new MatchResult(0, List.of(), List.of(), false);
        }

        List<String> effectiveJobSkills;
        if (job.getSkills() != null && !job.getSkills().isEmpty()) {
            effectiveJobSkills = job.getSkills();
        } else if (job.getBadge() != null && !job.getBadge().isBlank()) {
            effectiveJobSkills = List.of(job.getBadge());
        } else {
            effectiveJobSkills = Collections.emptyList();
        }

        if (effectiveJobSkills.isEmpty()) {
            return new MatchResult(0, List.of(), List.of(), false);
        }

        if (userSkills == null || userSkills.isEmpty()) {
            return new MatchResult(0, List.of(), List.copyOf(effectiveJobSkills), false);
        }

        List<String> validUserSkills = userSkills.stream()
                .filter(s -> s != null && !s.isBlank())
                .toList();

        if (validUserSkills.isEmpty()) {
            return new MatchResult(0, List.of(), List.copyOf(effectiveJobSkills), false);
        }

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();
        double totalSim = 0.0;

        for (String jobSkill : effectiveJobSkills) {
            double maxSim = 0.0;
            for (String userSkill : validUserSkills) {
                double sim = calculateSkillSimilarity(userSkill, jobSkill);
                if (sim > maxSim) {
                    maxSim = sim;
                }
            }
            totalSim += maxSim;
            if (maxSim >= 0.6) {
                matchedSkills.add(jobSkill);
            } else {
                missingSkills.add(jobSkill);
            }
        }

        int matchScore = (int) Math.round((totalSim / effectiveJobSkills.size()) * 100.0);
        matchScore = Math.max(0, Math.min(100, matchScore));
        boolean recommended = matchScore >= 40 || !matchedSkills.isEmpty();

        return new MatchResult(matchScore, List.copyOf(matchedSkills), List.copyOf(missingSkills), recommended);
    }

    /**
     * Calculates hybrid similarity score between a candidate skill and a job skill.
     *
     * @param userSkill applicant's skill string
     * @param jobSkill required job skill string
     * @return similarity between 0.0 and 1.0
     */
    public double calculateSkillSimilarity(String userSkill, String jobSkill) {
        if (userSkill == null || jobSkill == null) {
            return 0.0;
        }

        String cleanedUser = cleanString(userSkill);
        String cleanedJob = cleanString(jobSkill);

        if (cleanedUser.isBlank() || cleanedJob.isBlank()) {
            return 0.0;
        }

        List<String> coreTokensUser = extractCoreTokens(cleanedUser);
        List<String> coreTokensJob = extractCoreTokens(cleanedJob);

        String coreUser = String.join(" ", coreTokensUser);
        String coreJob = String.join(" ", coreTokensJob);

        // Rule 1: cleaned strings match or non-blank core expressions match (1.0)
        if (cleanedUser.equals(cleanedJob)) {
            return 1.0;
        }
        if (!coreUser.isBlank() && coreUser.equals(coreJob)) {
            return 1.0;
        }

        Set<String> setTokensUser = new HashSet<>(coreTokensUser);
        Set<String> setTokensJob = new HashSet<>(coreTokensJob);

        // Rule 2: one core token set is a complete subset of the other (0.85)
        if (!setTokensUser.isEmpty() && !setTokensJob.isEmpty()) {
            if (setTokensJob.containsAll(setTokensUser) || setTokensUser.containsAll(setTokensJob)) {
                return 0.85;
            }
        }

        // Rule 3: Jaccard similarity of core tokens (intersection / union) >= 0.5
        if (setTokensUser.isEmpty() || setTokensJob.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(setTokensUser);
        intersection.retainAll(setTokensJob);

        Set<String> union = new HashSet<>(setTokensUser);
        union.addAll(setTokensJob);

        if (union.isEmpty()) {
            return 0.0;
        }

        double jaccard = (double) intersection.size() / union.size();
        return jaccard >= 0.5 ? jaccard : 0.0;
    }

    public static MatchResult evaluateJob(Job job, List<String> userSkills) {
        return new JobRecommendationMatcher().evaluate(job, userSkills);
    }

    public static double computeSimilarity(String userSkill, String jobSkill) {
        return new JobRecommendationMatcher().calculateSkillSimilarity(userSkill, jobSkill);
    }

    private String cleanString(String text) {
        if (text == null) {
            return "";
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[.,/\\-_()]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private List<String> extractCoreTokens(String cleanedText) {
        if (cleanedText.isBlank()) {
            return Collections.emptyList();
        }
        String[] tokens = cleanedText.split("\\s+");
        List<String> result = new ArrayList<>();
        for (String token : tokens) {
            if (!token.isBlank() && !STOP_WORDS.contains(token)) {
                result.add(token);
            }
        }
        return result;
    }
}
