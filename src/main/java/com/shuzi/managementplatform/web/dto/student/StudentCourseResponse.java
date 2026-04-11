package com.shuzi.managementplatform.web.dto.student;

import com.shuzi.managementplatform.domain.enums.CourseStatus;

import java.time.LocalDateTime;

public record StudentCourseResponse(
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
        String bookingStatus,
        String checkinStatus
) {
}
