package com.shuzi.managementplatform.web.dto.parent;

import java.time.LocalDateTime;

public record ParentBookingResponse(
        Long id,
        Long studentId,
        String studentName,
        Long courseId,
        String courseName,
        String bookingStatus,
        Integer courseCapacity,
        String bookingRemark,
        String checkinStatus,
        LocalDateTime checkinTime,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

