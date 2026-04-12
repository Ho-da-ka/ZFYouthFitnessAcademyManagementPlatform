package com.shuzi.managementplatform.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuzi.managementplatform.common.exception.ResourceNotFoundException;
import com.shuzi.managementplatform.domain.entity.AttendanceRecord;
import com.shuzi.managementplatform.domain.entity.StageEvaluation;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.enums.AttendanceStatus;
import com.shuzi.managementplatform.domain.mapper.AttendanceRecordMapper;
import com.shuzi.managementplatform.domain.mapper.FitnessTestRecordMapper;
import com.shuzi.managementplatform.domain.mapper.StageEvaluationMapper;
import com.shuzi.managementplatform.domain.mapper.TrainingRecordMapper;
import com.shuzi.managementplatform.web.dto.evaluation.StageEvaluationCreateRequest;
import com.shuzi.managementplatform.web.dto.evaluation.StageEvaluationResponse;
import com.shuzi.managementplatform.web.dto.evaluation.StageEvaluationUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class StageEvaluationService {

    private final StageEvaluationMapper stageEvaluationMapper;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final TrainingRecordMapper trainingRecordMapper;
    private final FitnessTestRecordMapper fitnessTestRecordMapper;
    private final StudentService studentService;
    private final GeneratedContentService generatedContentService;
    private final CareAlertService careAlertService;

    public StageEvaluationService(
            StageEvaluationMapper stageEvaluationMapper,
            AttendanceRecordMapper attendanceRecordMapper,
            TrainingRecordMapper trainingRecordMapper,
            FitnessTestRecordMapper fitnessTestRecordMapper,
            StudentService studentService,
            GeneratedContentService generatedContentService,
            CareAlertService careAlertService
    ) {
        this.stageEvaluationMapper = stageEvaluationMapper;
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.trainingRecordMapper = trainingRecordMapper;
        this.fitnessTestRecordMapper = fitnessTestRecordMapper;
        this.studentService = studentService;
        this.generatedContentService = generatedContentService;
        this.careAlertService = careAlertService;
    }

    @Transactional
    public StageEvaluationResponse create(StageEvaluationCreateRequest request) {
        Student student = studentService.getEntityById(request.studentId());
        StageEvaluation entity = new StageEvaluation();
        apply(entity, request.studentId(), request.cycleName(), request.periodStart(), request.periodEnd(),
                request.trainingSummary(), request.fitnessSummary(), request.coachEvaluation(), request.nextStagePlan());
        entity.setAttendanceRate(calculateAttendanceRate(request.studentId(), request.periodStart(), request.periodEnd()));
        entity.setAiInterpretation(generatedContentService.generateStageInterpretation(
                entity.getTrainingSummary(),
                entity.getFitnessSummary(),
                entity.getCoachEvaluation(),
                entity.getNextStagePlan()
        ));
        entity.setParentReport(generatedContentService.generateParentReport(
                student.getName(),
                entity.getCycleName(),
                entity.getAttendanceRate().doubleValue(),
                entity.getFitnessSummary(),
                entity.getCoachEvaluation(),
                entity.getNextStagePlan()
        ));
        stageEvaluationMapper.insert(entity);
        careAlertService.refreshStudentAlerts(request.studentId());
        return toResponse(entity, student);
    }

    @Transactional
    public StageEvaluationResponse update(Long id, StageEvaluationUpdateRequest request) {
        StageEvaluation entity = stageEvaluationMapper.selectById(id);
        if (entity == null) {
            throw new ResourceNotFoundException("stage evaluation not found: " + id);
        }
        Student student = studentService.getEntityById(request.studentId());
        apply(entity, request.studentId(), request.cycleName(), request.periodStart(), request.periodEnd(),
                request.trainingSummary(), request.fitnessSummary(), request.coachEvaluation(), request.nextStagePlan());
        entity.setAttendanceRate(calculateAttendanceRate(request.studentId(), request.periodStart(), request.periodEnd()));
        entity.setAiInterpretation(generatedContentService.generateStageInterpretation(
                entity.getTrainingSummary(),
                entity.getFitnessSummary(),
                entity.getCoachEvaluation(),
                entity.getNextStagePlan()
        ));
        entity.setParentReport(generatedContentService.generateParentReport(
                student.getName(),
                entity.getCycleName(),
                entity.getAttendanceRate().doubleValue(),
                entity.getFitnessSummary(),
                entity.getCoachEvaluation(),
                entity.getNextStagePlan()
        ));
        stageEvaluationMapper.updateById(entity);
        careAlertService.refreshStudentAlerts(request.studentId());
        return toResponse(entity, student);
    }

    @Transactional(readOnly = true)
    public StageEvaluationResponse getById(Long id) {
        StageEvaluation entity = stageEvaluationMapper.selectById(id);
        if (entity == null) {
            throw new ResourceNotFoundException("stage evaluation not found: " + id);
        }
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public IPage<StageEvaluationResponse> page(Long studentId, int page, int size) {
        Page<StageEvaluation> pageRequest = new Page<>(page + 1L, size);
        LambdaQueryWrapper<StageEvaluation> query = Wrappers.<StageEvaluation>lambdaQuery()
                .orderByDesc(StageEvaluation::getPeriodEnd, StageEvaluation::getId);
        if (studentId != null) {
            query.eq(StageEvaluation::getStudentId, studentId);
        }
        Page<StageEvaluation> result = stageEvaluationMapper.selectPage(pageRequest, query);
        Page<StageEvaluationResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(result.getRecords().stream().map(this::toResponse).toList());
        return responsePage;
    }

    private void apply(
            StageEvaluation entity,
            Long studentId,
            String cycleName,
            java.time.LocalDate periodStart,
            java.time.LocalDate periodEnd,
            String trainingSummary,
            String fitnessSummary,
            String coachEvaluation,
            String nextStagePlan
    ) {
        entity.setStudentId(studentId);
        entity.setCycleName(cycleName);
        entity.setPeriodStart(periodStart);
        entity.setPeriodEnd(periodEnd);
        entity.setTrainingSummary(trainingSummary);
        entity.setFitnessSummary(fitnessSummary);
        entity.setCoachEvaluation(coachEvaluation);
        entity.setNextStagePlan(nextStagePlan);
    }

    private BigDecimal calculateAttendanceRate(Long studentId, java.time.LocalDate periodStart, java.time.LocalDate periodEnd) {
        long totalAttendance = attendanceRecordMapper.selectCount(
                Wrappers.<AttendanceRecord>lambdaQuery()
                        .eq(AttendanceRecord::getStudentId, studentId)
                        .between(AttendanceRecord::getAttendanceDate, periodStart, periodEnd)
        );
        long effectiveAttendance = attendanceRecordMapper.selectCount(
                Wrappers.<AttendanceRecord>lambdaQuery()
                        .eq(AttendanceRecord::getStudentId, studentId)
                        .between(AttendanceRecord::getAttendanceDate, periodStart, periodEnd)
                        .in(AttendanceRecord::getStatus, AttendanceStatus.PRESENT, AttendanceStatus.LATE)
        );
        if (totalAttendance == 0) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf((double) effectiveAttendance / totalAttendance)
                .setScale(4, RoundingMode.HALF_UP);
    }

    private StageEvaluationResponse toResponse(StageEvaluation entity) {
        Student student = studentService.getEntityById(entity.getStudentId());
        return toResponse(entity, student);
    }

    private StageEvaluationResponse toResponse(StageEvaluation entity, Student student) {
        return new StageEvaluationResponse(
                entity.getId(),
                entity.getStudentId(),
                student.getName(),
                entity.getCycleName(),
                entity.getPeriodStart(),
                entity.getPeriodEnd(),
                entity.getAttendanceRate() == null ? 0.0 : entity.getAttendanceRate().doubleValue(),
                entity.getTrainingSummary(),
                entity.getFitnessSummary(),
                entity.getCoachEvaluation(),
                entity.getNextStagePlan(),
                entity.getAiInterpretation(),
                entity.getParentReport(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
