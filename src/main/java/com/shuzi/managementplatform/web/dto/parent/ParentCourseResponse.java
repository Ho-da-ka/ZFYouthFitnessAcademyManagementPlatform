package com.shuzi.managementplatform.web.dto.parent;

import com.shuzi.managementplatform.domain.enums.CourseStatus;

import java.time.LocalDateTime;

public record ParentCourseResponse(
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
        Integer capacity,
        Long bookedCount,
        Long availableCount
) {
}

