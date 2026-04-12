package com.shuzi.managementplatform.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shuzi.managementplatform.domain.entity.Course;
import com.shuzi.managementplatform.domain.mapper.CourseMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class CourseConflictChecker {

    private final CourseMapper courseMapper;

    public CourseConflictChecker(CourseMapper courseMapper) {
        this.courseMapper = courseMapper;
    }

    public List<Course> findConflicts(String coachName,
                                      LocalDate date,
                                      LocalTime start,
                                      LocalTime end,
                                      Long excludeId) {
        if (!StringUtils.hasText(coachName) || date == null || start == null || end == null) {
            return List.of();
        }
        return courseMapper.selectList(
                Wrappers.<Course>lambdaQuery()
                        .eq(Course::getCoachName, coachName.trim())
                        .eq(Course::getCourseDate, date)
                        .ne(excludeId != null, Course::getId, excludeId)
                        .isNotNull(Course::getClassStartTime)
                        .isNotNull(Course::getClassEndTime)
                        .lt(Course::getClassStartTime, end)
                        .gt(Course::getClassEndTime, start)
        );
    }
}

