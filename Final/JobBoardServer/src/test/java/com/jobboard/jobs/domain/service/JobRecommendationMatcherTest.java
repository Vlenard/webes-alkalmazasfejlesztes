package com.jobboard.jobs.domain.service;

import com.jobboard.jobs.domain.model.Job;
import com.jobboard.jobs.domain.model.JobType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class JobRecommendationMatcherTest {

    private JobRecommendationMatcher matcher;

    @BeforeEach
    void setUp() {
        matcher = new JobRecommendationMatcher();
    }

    @Test
    @DisplayName("calculateSkillSimilarity: azonos skillek 1.0 egyezést adnak")
    void shouldReturn100PercentForIdenticalSkills() {
        double similarity = matcher.calculateSkillSimilarity("React", "React");
        assertThat(similarity).isEqualTo(1.0);
    }

    @Test
    @DisplayName("calculateSkillSimilarity: 'React' és 'React ismeret' 1.0 egyezést ad a stop-word szűrés miatt")
    void shouldReturn100PercentForReactAndReactIsmeret() {
        double sim1 = matcher.calculateSkillSimilarity("React", "React ismeret");
        double sim2 = matcher.calculateSkillSimilarity("React ismeret", "React");

        assertThat(sim1).isEqualTo(1.0);
        assertThat(sim2).isEqualTo(1.0);
    }

    @Test
    @DisplayName("calculateSkillSimilarity: Stop-words és ékezetek kiszűrése (pl. 'szintű', 'tudás', 'tapasztalat')")
    void shouldFilterStopWordsAndAccents() {
        double similarity = matcher.calculateSkillSimilarity("Haladó Java tudás", "Java tapasztalat");
        assertThat(similarity).isEqualTo(1.0);
    }

    @Test
    @DisplayName("calculateSkillSimilarity: Minden specifikált stop-word ('ismerete', 'tudás', 'tapasztalat', 'gyakorlat', 'junior', 'senior') 1.0 egyezést ad a mag-skillel")
    void shouldReturn100PercentForIndividualStopWordsWithAccentsAndCase() {
        // Specifikált stop-wordök: 'ismerete', 'tudás', 'tapasztalat', 'gyakorlat', 'junior', 'senior'
        assertThat(matcher.calculateSkillSimilarity("React ismerete", "React")).isEqualTo(1.0);
        assertThat(matcher.calculateSkillSimilarity("React", "REACT ISMERETE")).isEqualTo(1.0);
        assertThat(matcher.calculateSkillSimilarity("React tudás", "React")).isEqualTo(1.0);
        assertThat(matcher.calculateSkillSimilarity("React", "React Tudás")).isEqualTo(1.0);
        assertThat(matcher.calculateSkillSimilarity("React tapasztalat", "React")).isEqualTo(1.0);
        assertThat(matcher.calculateSkillSimilarity("React gyakorlat", "React")).isEqualTo(1.0);
        assertThat(matcher.calculateSkillSimilarity("Junior React", "React")).isEqualTo(1.0);
        assertThat(matcher.calculateSkillSimilarity("Senior React", "React")).isEqualTo(1.0);
        assertThat(matcher.calculateSkillSimilarity("React", "SENIOR REACT")).isEqualTo(1.0);

        // További stop-wordök: 'alapok', 'haladó', 'medior', 'knowledge', 'experience', 'skill', 'skills', 'szintű'
        assertThat(matcher.calculateSkillSimilarity("React alapok", "React")).isEqualTo(1.0);
        assertThat(matcher.calculateSkillSimilarity("Haladó React", "React")).isEqualTo(1.0);
        assertThat(matcher.calculateSkillSimilarity("Medior React", "React")).isEqualTo(1.0);
        assertThat(matcher.calculateSkillSimilarity("React knowledge", "React")).isEqualTo(1.0);
        assertThat(matcher.calculateSkillSimilarity("React experience", "React")).isEqualTo(1.0);
        assertThat(matcher.calculateSkillSimilarity("React skill", "React")).isEqualTo(1.0);
        assertThat(matcher.calculateSkillSimilarity("React skills", "React")).isEqualTo(1.0);
        assertThat(matcher.calculateSkillSimilarity("React szintű", "React")).isEqualTo(1.0);

        // Több stop-word és írásjel kombinációja kis/nagybetűkkel és ékezetekkel
        assertThat(matcher.calculateSkillSimilarity("  Senior React gyakorlat (haladó szintű, tudás)  ", "react")).isEqualTo(1.0);
    }

    @Test
    @DisplayName("calculateSkillSimilarity: Részhalmaz tokenek esetén 0.85 hasonlóság ('React' vs 'React.js', 'Spring' vs 'Spring Boot') mindkét irányban")
    void shouldReturn85PercentForSubsetTokens() {
        double reactSim1 = matcher.calculateSkillSimilarity("React", "React.js");
        double reactSim2 = matcher.calculateSkillSimilarity("React.js", "React");
        double springSim1 = matcher.calculateSkillSimilarity("Spring", "Spring Boot");
        double springSim2 = matcher.calculateSkillSimilarity("Spring Boot", "Spring");
        double dockerSim1 = matcher.calculateSkillSimilarity("Docker", "Docker Compose");
        double dockerSim2 = matcher.calculateSkillSimilarity("Docker Compose", "Docker");

        assertThat(reactSim1).isEqualTo(0.85);
        assertThat(reactSim2).isEqualTo(0.85);
        assertThat(springSim1).isEqualTo(0.85);
        assertThat(springSim2).isEqualTo(0.85);
        assertThat(dockerSim1).isEqualTo(0.85);
        assertThat(dockerSim2).isEqualTo(0.85);
    }

    @Test
    @DisplayName("calculateSkillSimilarity: Jaccard hasonlóság >= 0.5 esetén visszaadja a Jaccard értéket, alatta 0.0")
    void shouldReturnJaccardSimilarityWhenGreaterOrEqualToHalf() {
        // Core tokens: user: [aws, cloud, devops], job: [aws, cloud, kubernetes]
        // Intersection = 2 (aws, cloud), Union = 4 (aws, cloud, devops, kubernetes) -> 2/4 = 0.5
        double similarity = matcher.calculateSkillSimilarity("AWS Cloud DevOps", "AWS Cloud Kubernetes");
        assertThat(similarity).isCloseTo(0.5, within(0.001));

        // Core tokens: user: [angular, typescript, rxjs], job: [react, typescript, rxjs]
        // Intersection = 2 (typescript, rxjs), Union = 4 -> 2/4 = 0.5
        double angularReactSim = matcher.calculateSkillSimilarity("Angular TypeScript RxJS", "React TypeScript RxJS");
        assertThat(angularReactSim).isCloseTo(0.5, within(0.001));

        // Core tokens: user: [angular, typescript, rxjs], job: [vue, typescript, rxjs, graphql]
        // Intersection = 2, Union = 5 -> 2/5 = 0.4 < 0.5 -> 0.0
        double lowJaccard = matcher.calculateSkillSimilarity("Angular TypeScript RxJS", "Vue TypeScript RxJS GraphQL");
        assertThat(lowJaccard).isEqualTo(0.0);

        // Core tokens: user: [python, ai], job: [python, web, backend, database]
        // Intersection = 1 (python), Union = 5 -> 1/5 = 0.2 < 0.5 -> 0.0
        double lowSimilarity = matcher.calculateSkillSimilarity("Python AI", "Python Web Backend Database");
        assertThat(lowSimilarity).isEqualTo(0.0);
    }

    @Test
    @DisplayName("calculateSkillSimilarity: Teljesen különböző vagy üres skillek 0.0 pontot kapnak")
    void shouldReturnZeroForDifferentOrEmptySkills() {
        assertThat(matcher.calculateSkillSimilarity("React", "PostgreSQL")).isEqualTo(0.0);
        assertThat(matcher.calculateSkillSimilarity("Java", "Python")).isEqualTo(0.0);
        assertThat(matcher.calculateSkillSimilarity("Docker", "Figma")).isEqualTo(0.0);
        assertThat(matcher.calculateSkillSimilarity(null, "Java")).isEqualTo(0.0);
        assertThat(matcher.calculateSkillSimilarity("Java", null)).isEqualTo(0.0);
        assertThat(matcher.calculateSkillSimilarity("", "  ")).isEqualTo(0.0);
    }

    @Test
    @DisplayName("evaluate: Teljesen nem passzoló skillek esetén 0% matchScore, üres matchedSkills és az összes hiányzó skill")
    void shouldReturnZeroMatchScoreForCompletelyNonMatchingSkills() {
        Job job = Job.createNew(
                "Frontend Fejlesztő", "Code Zrt", JobType.FULL_TIME, "Budapest", "Frontend", "1.2M", "Leírás",
                List.of("Req1"), 2L, List.of("React", "TypeScript", "Next.js")
        );

        JobRecommendationMatcher.MatchResult result = matcher.evaluate(job, List.of("Python", "Django", "PostgreSQL"));

        assertThat(result.matchScore()).isEqualTo(0);
        assertThat(result.matchedSkills()).isEmpty();
        assertThat(result.missingSkills()).containsExactly("React", "TypeScript", "Next.js");
        assertThat(result.recommended()).isFalse();
    }

    @Test
    @DisplayName("evaluate: Részhalmaz (0.85) egyezés eléri a 0.6-os küszöböt és bekerül a matchedSkills közé")
    void shouldIncludeSubsetMatchInMatchedSkillsAndCalculateScore() {
        Job job = Job.createNew(
                "Backend Fejlesztő", "Server Kft", JobType.FULL_TIME, "Budapest", "Java", "1.3M", "Desc",
                List.of("Req"), 3L, List.of("Spring Boot", "PostgreSQL")
        );

        // "Spring Boot" vs "Spring" -> 0.85 >= 0.6 -> matched
        // "PostgreSQL" vs user skills ("Spring", "Docker") -> 0.0 < 0.6 -> missing
        // Score: Math.round((0.85 + 0.0) / 2 * 100) = Math.round(42.5) = 43%
        JobRecommendationMatcher.MatchResult result = matcher.evaluate(job, List.of("Spring", "Docker"));

        assertThat(result.matchScore()).isEqualTo(43);
        assertThat(result.matchedSkills()).containsExactly("Spring Boot");
        assertThat(result.missingSkills()).containsExactly("PostgreSQL");
        assertThat(result.recommended()).isTrue();
    }

    @Test
    @DisplayName("evaluate: Ha a felhasználónak nincs skillje, 0 matchScore és hiányzó effektív skillek az eredmény")
    void shouldReturnZeroScoreWhenUserSkillsEmpty() {
        Job job = Job.createNew(
                "Java Fejlesztő", "Tech Kft", JobType.FULL_TIME, "BP", "Java", "1M", "Desc",
                List.of("Követelmény"), 1L, List.of("Java", "Spring Boot")
        );

        JobRecommendationMatcher.MatchResult result = matcher.evaluate(job, List.of());

        assertThat(result.matchScore()).isEqualTo(0);
        assertThat(result.matchedSkills()).isEmpty();
        assertThat(result.missingSkills()).containsExactly("Java", "Spring Boot");
        assertThat(result.recommended()).isFalse();
    }

    @Test
    @DisplayName("evaluate: Ha az állásnak nincs külön skill listája, a badge-et használja fallbackként")
    void shouldFallbackToJobBadgeWhenJobSkillsEmpty() {
        Job job = Job.createNew(
                "Java Fejlesztő", "Tech Kft", JobType.FULL_TIME, "BP", "Java", "1M", "Desc",
                List.of("Követelmény"), 1L
        );

        JobRecommendationMatcher.MatchResult result = matcher.evaluate(job, List.of("Java ismeret"));

        assertThat(result.matchScore()).isEqualTo(100);
        assertThat(result.matchedSkills()).containsExactly("Java");
        assertThat(result.missingSkills()).isEmpty();
        assertThat(result.recommended()).isTrue();
    }

    @Test
    @DisplayName("evaluate: Pontos és részleges egyezések kombinációja és a 0.6 küszöb vizsgálata")
    void shouldCorrectlyScoreAndRecommendCandidate() {
        Job job = Job.createNew(
                "Frontend Fejlesztő", "Code Zrt", JobType.FULL_TIME, "Budapest", "Frontend", "1.2M", "Leírás",
                List.of("Req1"), 2L, List.of("React", "TypeScript", "Tailwind CSS")
        );

        // User skillek: "React ismeret" -> 1.0 (match), "TypeScript" -> 1.0 (match), hiányzik: "Tailwind CSS" (0.0)
        // totalSim = 2.0 / 3 -> 66.67% -> 67%
        JobRecommendationMatcher.MatchResult result = matcher.evaluate(job, List.of("React ismeret", "TypeScript"));

        assertThat(result.matchScore()).isEqualTo(67);
        assertThat(result.matchedSkills()).containsExactly("React", "TypeScript");
        assertThat(result.missingSkills()).containsExactly("Tailwind CSS");
        assertThat(result.recommended()).isTrue();
    }
}
