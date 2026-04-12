package com.shuzi.managementplatform.web.dto.course;

import java.time.LocalDate;
import java.util.List;

public record CourseAttendanceStatsResponse(
        long totalSessions,
        double avgAttendanceRate,
        List<DailyAttendance> trend
) {
    public record DailyAttendance(
            LocalDate date,
            long present,
            long total
    ) {
    }
}

