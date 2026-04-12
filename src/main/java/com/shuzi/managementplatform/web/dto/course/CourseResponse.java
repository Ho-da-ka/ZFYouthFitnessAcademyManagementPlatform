package com.shuzi.managementplatform.web.dto.course;

import com.shuzi.managementplatform.domain.enums.CourseStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record CourseResponse(
        Long id,
        String courseCode,
        String name,
        String courseType,
        String coachName,
        String venue,
        LocalDateTime startTime,
        Integer durationMinutes,
        Integer maxCapacity,
        Long currentEnrollment,
        LocalDate courseDate,
        LocalTime classStartTime,
        LocalTime classEndTime,
        CourseStatus status,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
