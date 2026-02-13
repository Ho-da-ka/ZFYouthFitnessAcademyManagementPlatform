package com.shuzi.managementplatform.domain.service;

import com.shuzi.managementplatform.common.exception.BusinessException;
import com.shuzi.managementplatform.common.exception.ResourceNotFoundException;
import com.shuzi.managementplatform.domain.entity.Course;
import com.shuzi.managementplatform.domain.enums.CourseStatus;
import com.shuzi.managementplatform.domain.repository.CourseRepository;
import com.shuzi.managementplatform.web.dto.course.CourseCreateRequest;
import com.shuzi.managementplatform.web.dto.course.CourseResponse;
import com.shuzi.managementplatform.web.dto.course.CourseUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional
    public CourseResponse create(CourseCreateRequest request) {
        if (courseRepository.existsByCourseCode(request.courseCode())) {
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
        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse update(Long id, CourseUpdateRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("course not found: " + id));
        course.setName(request.name());
        course.setCourseType(request.courseType());
        course.setCoachName(request.coachName());
        course.setVenue(request.venue());
        course.setStartTime(request.startTime());
        course.setDurationMinutes(request.durationMinutes());
        course.setStatus(request.status());
        course.setDescription(request.description());
        return toResponse(courseRepository.save(course));
    }

    @Transactional(readOnly = true)
    public CourseResponse getById(Long id) {
        return toResponse(courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("course not found: " + id)));
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> page(String name, CourseStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String normalizedName = name == null ? null : name.trim();
        Page<Course> result;

        if (normalizedName != null && !normalizedName.isEmpty() && status != null) {
            result = courseRepository.findByNameContainingIgnoreCaseAndStatus(normalizedName, status, pageable);
        } else if (normalizedName != null && !normalizedName.isEmpty()) {
            result = courseRepository.findByNameContainingIgnoreCase(normalizedName, pageable);
        } else if (status != null) {
            result = courseRepository.findByStatus(status, pageable);
        } else {
            result = courseRepository.findAll(pageable);
        }

        List<CourseResponse> content = result.stream().map(this::toResponse).toList();
        return new PageImpl<>(content, pageable, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Course getEntityById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("course not found: " + id));
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
