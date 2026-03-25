package com.shuzi.managementplatform.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shuzi.managementplatform.common.exception.ResourceNotFoundException;
import com.shuzi.managementplatform.domain.entity.Course;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.entity.TrainingRecord;
import com.shuzi.managementplatform.domain.mapper.TrainingRecordMapper;
import com.shuzi.managementplatform.web.dto.training.TrainingRecordCreateRequest;
import com.shuzi.managementplatform.web.dto.training.TrainingRecordResponse;
import com.shuzi.managementplatform.web.dto.training.TrainingRecordUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * Training record service for recording and querying student training sessions.
 */
@Service
public class TrainingRecordService {

    private final TrainingRecordMapper trainingRecordMapper;
    private final StudentService studentService;
    private final CourseService courseService;

    public TrainingRecordService(
            TrainingRecordMapper trainingRecordMapper,
            StudentService studentService,
            CourseService courseService
    ) {
        this.trainingRecordMapper = trainingRecordMapper;
        this.studentService = studentService;
        this.courseService = courseService;
    }

    @Transactional
    public TrainingRecordResponse create(TrainingRecordCreateRequest request) {
        Student student = studentService.getEntityById(request.studentId());
        Course course = courseService.getEntityById(request.courseId());

        TrainingRecord record = new TrainingRecord();
        apply(record, request.studentId(), request.courseId(), request.trainingDate(), request.trainingContent(),
                request.durationMinutes(), request.intensityLevel(), request.performanceSummary(), request.coachComment());
        trainingRecordMapper.insert(record);
        return toResponse(record, student, course);
    }

    @Transactional
    public TrainingRecordResponse update(Long id, TrainingRecordUpdateRequest request) {
        TrainingRecord record = trainingRecordMapper.selectById(id);
        if (record == null) {
            throw new ResourceNotFoundException("training record not found: " + id);
        }

        Student student = studentService.getEntityById(request.studentId());
        Course course = courseService.getEntityById(request.courseId());

        apply(record, request.studentId(), request.courseId(), request.trainingDate(), request.trainingContent(),
                request.durationMinutes(), request.intensityLevel(), request.performanceSummary(), request.coachComment());
        trainingRecordMapper.updateById(record);
        return toResponse(record, student, course);
    }

    @Transactional(readOnly = true)
    public TrainingRecordResponse getById(Long id) {
        TrainingRecord record = trainingRecordMapper.selectById(id);
        if (record == null) {
            throw new ResourceNotFoundException("training record not found: " + id);
        }
        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public List<TrainingRecordResponse> search(Long studentId, Long courseId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<TrainingRecord> query = Wrappers.<TrainingRecord>lambdaQuery();
        if (studentId != null) {
            query.eq(TrainingRecord::getStudentId, studentId);
        }
        if (courseId != null) {
            query.eq(TrainingRecord::getCourseId, courseId);
        }
        if (startDate != null) {
            query.ge(TrainingRecord::getTrainingDate, startDate);
        }
        if (endDate != null) {
            query.le(TrainingRecord::getTrainingDate, endDate);
        }
        query.orderByDesc(TrainingRecord::getTrainingDate, TrainingRecord::getId);

        return trainingRecordMapper.selectList(query)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void apply(
            TrainingRecord record,
            Long studentId,
            Long courseId,
            LocalDate trainingDate,
            String trainingContent,
            Integer durationMinutes,
            String intensityLevel,
            String performanceSummary,
            String coachComment
    ) {
        record.setStudentId(studentId);
        record.setCourseId(courseId);
        record.setTrainingDate(trainingDate);
        record.setTrainingContent(trainingContent.trim());
        record.setDurationMinutes(durationMinutes);
        record.setIntensityLevel(normalize(intensityLevel));
        record.setPerformanceSummary(normalize(performanceSummary));
        record.setCoachComment(normalize(coachComment));
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private TrainingRecordResponse toResponse(TrainingRecord record) {
        Student student = studentService.getEntityById(record.getStudentId());
        Course course = courseService.getEntityById(record.getCourseId());
        return toResponse(record, student, course);
    }

    private TrainingRecordResponse toResponse(TrainingRecord record, Student student, Course course) {
        return new TrainingRecordResponse(
                record.getId(),
                student.getId(),
                student.getName(),
                course.getId(),
                course.getName(),
                record.getTrainingDate(),
                record.getTrainingContent(),
                record.getDurationMinutes(),
                record.getIntensityLevel(),
                record.getPerformanceSummary(),
                record.getCoachComment(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }
}
