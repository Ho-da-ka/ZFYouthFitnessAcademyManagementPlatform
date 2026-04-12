package com.shuzi.managementplatform.web.dto.student;

public record StudentAttendanceStatsResponse(String month, long present, long total, double rate) {}
