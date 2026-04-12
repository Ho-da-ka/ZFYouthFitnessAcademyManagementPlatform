package com.shuzi.managementplatform.web.dto.statistics;

public record CoachWorkloadResponse(Long coachId, String coachName, long courseCount, long trainingCount) {}
