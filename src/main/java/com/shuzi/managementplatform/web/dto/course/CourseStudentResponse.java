package com.shuzi.managementplatform.web.dto.course;

public record CourseStudentResponse(
        Long studentId,
        String studentName,
        String gender,
        long attendanceTotal,
        long attendancePresent,
        double attendanceRate
) {
}

