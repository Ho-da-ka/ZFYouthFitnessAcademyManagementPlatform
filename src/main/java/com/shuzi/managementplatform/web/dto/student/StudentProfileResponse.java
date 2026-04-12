package com.shuzi.managementplatform.web.dto.student;

public record StudentProfileResponse(StudentResponse student, StudentStats stats) {
    public record StudentStats(long attendanceTotal, long attendancePresent,
                               double attendanceRate, long fitnessTestCount, long trainingRecordCount) {}
}
