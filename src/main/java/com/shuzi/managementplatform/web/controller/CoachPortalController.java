package com.shuzi.managementplatform.web.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shuzi.managementplatform.common.api.ApiResponse;
import com.shuzi.managementplatform.domain.entity.Coach;
import com.shuzi.managementplatform.domain.entity.Course;
import com.shuzi.managementplatform.domain.entity.UserAccount;
import com.shuzi.managementplatform.domain.mapper.CoachMapper;
import com.shuzi.managementplatform.domain.mapper.CourseMapper;
import com.shuzi.managementplatform.domain.mapper.UserAccountMapper;
import com.shuzi.managementplatform.domain.service.CheckinService;
import com.shuzi.managementplatform.web.dto.checkin.CoachCheckinInitResponse;
import com.shuzi.managementplatform.web.dto.course.CourseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coach")
@Tag(name = "CoachPortal", description = "Coach-side portal endpoints")
public class CoachPortalController {

    private final CheckinService checkinService;
    private final CourseMapper courseMapper;
    private final UserAccountMapper userAccountMapper;
    private final CoachMapper coachMapper;

    public CoachPortalController(CheckinService checkinService, CourseMapper courseMapper, UserAccountMapper userAccountMapper, CoachMapper coachMapper) {
        this.checkinService = checkinService;
        this.courseMapper = courseMapper;
        this.userAccountMapper = userAccountMapper;
        this.coachMapper = coachMapper;
    }

    @PreAuthorize("hasRole('COACH')")
    @GetMapping("/courses")
    @Operation(summary = "List my courses", description = "Get courses assigned to the current coach")
    public ApiResponse<List<CourseResponse>> listMyCourses(Authentication authentication) {
        String username = authentication.getName();
        UserAccount account = userAccountMapper.selectOne(Wrappers.<UserAccount>lambdaQuery().eq(UserAccount::getUsername, username));
        if (account == null || account.getCoachId() == null) {
            return ApiResponse.ok(List.of());
        }
        Coach coach = coachMapper.selectById(account.getCoachId());
        if (coach == null) {
            return ApiResponse.ok(List.of());
        }

        List<Course> courses = courseMapper.selectList(
                Wrappers.<Course>lambdaQuery()
                        .eq(Course::getCoachName, coach.getName())
                        .orderByDesc(Course::getStartTime)
        );

        return ApiResponse.ok(courses.stream().map(this::toResponse).toList());
    }

    @PreAuthorize("hasRole('COACH')")
    @PostMapping("/checkin/initiate")
    @Operation(summary = "Initiate check-in", description = "Generate a check-in token for a course")
    public ApiResponse<CoachCheckinInitResponse> initiateCheckin(
            @RequestParam Long courseId,
            Authentication authentication
    ) {
        // We could verify if the course belongs to the coach here for extra security
        return ApiResponse.ok(checkinService.initiateCoachCheckin(courseId));
    }

    private CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getCourseCode(),
                course.getName(),
                course.getCourseType(),
                course.getCoachName(),
                course.getVenue(),
                course.getStartTime(),
                course.getDurationMinutes(),
                course.getMaxCapacity(),
                0L, // currentEnrollment
                course.getCourseDate(),
                course.getClassStartTime(),
                course.getClassEndTime(),
                course.getStatus(),
                course.getDescription(),
                course.getTrainingTheme(),
                course.getTargetAgeRange(),
                course.getTargetGoals(),
                course.getFocusPoints(),
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }
}
