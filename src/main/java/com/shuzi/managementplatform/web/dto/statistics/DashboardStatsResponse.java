package com.shuzi.managementplatform.web.dto.statistics;

import java.util.List;

public record DashboardStatsResponse(
        StudentStats students,
        CourseStats courses,
        CoachStats coaches,
        AttendanceStats attendance,
        long recentFitnessCount,
        long recentTrainingCount
) {
    public record StudentStats(long total, long active, long inactive, long graduated) {}
    public record CourseStats(long total, long planned, long ongoing, long completed, long cancelled) {}
    public record CoachStats(long total, long active, long inactive) {}
    public record AttendanceStats(long thisMonthTotal, long thisMonthPresent, double thisMonthRate,
                                  List<DailyAttendance> last7Days) {}
    public record DailyAttendance(String date, long present, long total) {}
}
