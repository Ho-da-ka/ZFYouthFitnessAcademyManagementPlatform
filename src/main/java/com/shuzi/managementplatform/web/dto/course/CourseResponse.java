package com.shuzi.managementplatform.web.dto.course;

import com.shuzi.managementplatform.domain.enums.CourseStatus;

import java.time.LocalDateTime;

public record CourseResponse(
        Long id,
        String courseCode,
        String name,
        String courseType,
        String coachName,
        String venue,
        LocalDateTime startTime,
        Integer durationMinutes,
        CourseStatus status,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
