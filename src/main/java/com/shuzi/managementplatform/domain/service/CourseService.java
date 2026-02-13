package com.shuzi.managementplatform.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuzi.managementplatform.common.exception.BusinessException;
import com.shuzi.managementplatform.common.exception.ResourceNotFoundException;
import com.shuzi.managementplatform.domain.entity.Course;
import com.shuzi.managementplatform.domain.enums.CourseStatus;
import com.shuzi.managementplatform.domain.mapper.CourseMapper;
import com.shuzi.managementplatform.web.dto.course.CourseCreateRequest;
import com.shuzi.managementplatform.web.dto.course.CourseResponse;
import com.shuzi.managementplatform.web.dto.course.CourseUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Course domain service for CRUD and filtered pagination.
 */
@Service
public class CourseService {

    private final CourseMapper courseMapper;

    public CourseService(CourseMapper courseMapper) {
        this.courseMapper = courseMapper;
    }

    @Transactional
    public CourseResponse create(CourseCreateRequest request) {
        // Business key uniqueness check.
        Long count = courseMapper.selectCount(
                Wrappers.<Course>lambdaQuery().eq(Course::getCourseCode, request.courseCode())
        );
        if (count != null && count > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "courseCode already exists");
        }
        Course course = new Course();
        course.setCourseCode(request.courseCode());
        course.setName(request.name());
        course.setCourseType(request.courseType());
        course.setCoachName(request.coachName());
        course.setVenue(request.venue());
        course.setStartTime(request.startTime());
        course.setDurationMinutes(request.durationMinutes());
        course.setStatus(request.status() == null ? CourseStatus.PLANNED : request.status());
        course.setDescription(request.description());
        courseMapper.insert(course);
        return toResponse(course);
    }

    @Transactional
    public CourseResponse update(Long id, CourseUpdateRequest request) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new ResourceNotFoundException("course not found: " + id);
        }
        course.setName(request.name());
        course.setCourseType(request.courseType());
        course.setCoachName(request.coachName());
        course.setVenue(request.venue());
        course.setStartTime(request.startTime());
        course.setDurationMinutes(request.durationMinutes());
        course.setStatus(request.status());
        course.setDescription(request.description());
        courseMapper.updateById(course);
        return toResponse(course);
    }

    @Transactional(readOnly = true)
    public CourseResponse getById(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new ResourceNotFoundException("course not found: " + id);
        }
        return toResponse(course);
    }

    @Transactional(readOnly = true)
    public IPage<CourseResponse> page(String name, CourseStatus status, int page, int size) {
        // MyBatis-Plus page index starts from 1; API contract uses 0-based index.
        Page<Course> pageRequest = new Page<>(page + 1L, size);
        LambdaQueryWrapper<Course> query = Wrappers.<Course>lambdaQuery();

        if (StringUtils.hasText(name)) {
            query.like(Course::getName, name.trim());
        }
        if (status != null) {
            query.eq(Course::getStatus, status);
        }
        query.orderByDesc(Course::getId);

        Page<Course> result = courseMapper.selectPage(pageRequest, query);
        List<CourseResponse> records = result.getRecords().stream().map(this::toResponse).toList();

        Page<CourseResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(records);
        return responsePage;
    }

    @Transactional(readOnly = true)
    public Course getEntityById(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new ResourceNotFoundException("course not found: " + id);
        }
        return course;
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
                course.getStatus(),
                course.getDescription(),
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }
}
