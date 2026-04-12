package com.shuzi.managementplatform.web.dto.student;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record StudentFitnessTrendResponse(String testItem, String unit, List<DataPoint> records) {
    public record DataPoint(LocalDate testDate, BigDecimal value) {}
}
