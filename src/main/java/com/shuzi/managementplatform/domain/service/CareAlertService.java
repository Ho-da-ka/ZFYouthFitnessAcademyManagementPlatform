package com.shuzi.managementplatform.domain.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shuzi.managementplatform.domain.entity.AttendanceRecord;
import com.shuzi.managementplatform.domain.entity.CareAlert;
import com.shuzi.managementplatform.domain.entity.FitnessTestRecord;
import com.shuzi.managementplatform.domain.entity.StageEvaluation;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.enums.AttendanceStatus;
import com.shuzi.managementplatform.domain.mapper.AttendanceRecordMapper;
import com.shuzi.managementplatform.domain.mapper.CareAlertMapper;
import com.shuzi.managementplatform.domain.mapper.FitnessTestRecordMapper;
import com.shuzi.managementplatform.domain.mapper.StageEvaluationMapper;
import com.shuzi.managementplatform.web.dto.alert.CareAlertResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class CareAlertService {

    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_RESOLVED = "RESOLVED";
    private static final String TYPE_ABSENCE_STREAK = "ABSENCE_STREAK";
    private static final String TYPE_FITNESS_REGRESSION = "FITNESS_REGRESSION";
    private static final String TYPE_EVALUATION_OVERDUE = "EVALUATION_OVERDUE";

    private final CareAlertMapper careAlertMapper;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final FitnessTestRecordMapper fitnessTestRecordMapper;
    private final StageEvaluationMapper stageEvaluationMapper;
    private final StudentService studentService;

    public CareAlertService(
            CareAlertMapper careAlertMapper,
            AttendanceRecordMapper attendanceRecordMapper,
            FitnessTestRecordMapper fitnessTestRecordMapper,
            StageEvaluationMapper stageEvaluationMapper,
            StudentService studentService
    ) {
        this.careAlertMapper = careAlertMapper;
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.fitnessTestRecordMapper = fitnessTestRecordMapper;
        this.stageEvaluationMapper = stageEvaluationMapper;
        this.studentService = studentService;
    }

    @Transactional
    public void refreshStudentAlerts(Long studentId) {
        if (studentId == null) {
            return;
        }
        Student student = studentService.getEntityById(studentId);
        LocalDate today = LocalDate.now();
        Map<String, AlertDraft> activeAlerts = evaluateActiveAlerts(studentId, student.getGoalEndDate(), today);
        Map<String, CareAlert> latestAlerts = latestAlertsByType(studentId);

        for (AlertDraft draft : activeAlerts.values()) {
            CareAlert existing = latestAlerts.get(draft.type());
            if (existing == null) {
                careAlertMapper.insert(toEntity(studentId, draft, STATUS_OPEN, null));
                continue;
            }
            if (!STATUS_OPEN.equals(existing.getStatus())) {
                existing.setAlertTitle(draft.title());
                existing.setAlertContent(draft.content());
                existing.setStatus(STATUS_OPEN);
                existing.setTriggeredAt(draft.triggeredAt());
                existing.setResolvedAt(null);
                careAlertMapper.updateById(existing);
            }
        }

        for (CareAlert existing : latestAlerts.values()) {
            if (STATUS_OPEN.equals(existing.getStatus()) && !activeAlerts.containsKey(existing.getAlertType())) {
                existing.setStatus(STATUS_RESOLVED);
                existing.setResolvedAt(LocalDateTime.now());
                careAlertMapper.updateById(existing);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<CareAlertResponse> listAlerts(Long studentId, String status, Integer limit) {
        String normalizedStatus = StringUtils.hasText(status) ? status.trim().toUpperCase(Locale.ROOT) : null;
        var query = Wrappers.<CareAlert>lambdaQuery()
                .orderByAsc(CareAlert::getStatus)
                .orderByDesc(CareAlert::getTriggeredAt, CareAlert::getId);
        if (studentId != null) {
            query.eq(CareAlert::getStudentId, studentId);
        }
        if (normalizedStatus != null) {
            query.eq(CareAlert::getStatus, normalizedStatus);
        }
        if (limit != null && limit > 0) {
            query.last("LIMIT " + limit);
        }
        return careAlertMapper.selectList(query).stream().map(this::toResponse).toList();
    }

    private Map<String, AlertDraft> evaluateActiveAlerts(Long studentId, LocalDate goalEndDate, LocalDate today) {
        Map<String, AlertDraft> drafts = new LinkedHashMap<>();
        LocalDateTime triggeredAt = today.atStartOfDay();
        if (hasAbsenceStreak(studentId)) {
            drafts.put(TYPE_ABSENCE_STREAK, new AlertDraft(
                    TYPE_ABSENCE_STREAK,
                    "连续缺勤提醒",
                    "最近两次考勤均为缺勤，建议尽快联系家长确认原因并安排补课跟进。",
                    triggeredAt
            ));
        }
        if (hasFitnessRegression(studentId)) {
            drafts.put(TYPE_FITNESS_REGRESSION, new AlertDraft(
                    TYPE_FITNESS_REGRESSION,
                    "体测回落提醒",
                    "近期关键体测较上一次出现回落，建议复核训练负荷并安排针对性巩固。",
                    triggeredAt
            ));
        }
        if (hasOverdueEvaluation(studentId, goalEndDate, today)) {
            drafts.put(TYPE_EVALUATION_OVERDUE, new AlertDraft(
                    TYPE_EVALUATION_OVERDUE,
                    "阶段评估逾期提醒",
                    "当前目标周期已结束但仍未完成阶段评估，建议补录评估单并同步家长结果说明。",
                    triggeredAt
            ));
        }
        return drafts;
    }

    private Map<String, CareAlert> latestAlertsByType(Long studentId) {
        List<CareAlert> alerts = careAlertMapper.selectList(Wrappers.<CareAlert>lambdaQuery()
                .eq(CareAlert::getStudentId, studentId)
                .orderByDesc(CareAlert::getTriggeredAt, CareAlert::getId));
        Map<String, CareAlert> latestByType = new LinkedHashMap<>();
        for (CareAlert alert : alerts) {
            latestByType.putIfAbsent(alert.getAlertType(), alert);
        }
        return latestByType;
    }

    private boolean hasAbsenceStreak(Long studentId) {
        List<AttendanceRecord> records = attendanceRecordMapper.selectList(
                Wrappers.<AttendanceRecord>lambdaQuery()
                        .eq(AttendanceRecord::getStudentId, studentId)
                        .orderByDesc(AttendanceRecord::getAttendanceDate, AttendanceRecord::getId)
                        .last("LIMIT 2")
        );
        return records.size() >= 2
                && records.get(0).getStatus() == AttendanceStatus.ABSENT
                && records.get(1).getStatus() == AttendanceStatus.ABSENT;
    }

    private boolean hasFitnessRegression(Long studentId) {
        List<FitnessTestRecord> records = fitnessTestRecordMapper.selectList(
                Wrappers.<FitnessTestRecord>lambdaQuery()
                        .eq(FitnessTestRecord::getStudentId, studentId)
                        .orderByDesc(FitnessTestRecord::getTestDate, FitnessTestRecord::getId)
                        .last("LIMIT 12")
        );
        Map<String, List<FitnessTestRecord>> grouped = new LinkedHashMap<>();
        for (FitnessTestRecord record : records) {
            if (record.getTestValue() == null || !StringUtils.hasText(record.getItemName())) {
                continue;
            }
            grouped.computeIfAbsent(alertKey(record), ignored -> new ArrayList<>()).add(record);
        }
        for (List<FitnessTestRecord> itemRecords : grouped.values()) {
            if (itemRecords.size() < 2) {
                continue;
            }
            FitnessTestRecord latest = itemRecords.get(0);
            FitnessTestRecord previous = itemRecords.get(1);
            if (isRegression(latest, previous)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasOverdueEvaluation(Long studentId, LocalDate goalEndDate, LocalDate today) {
        if (goalEndDate == null || !goalEndDate.isBefore(today)) {
            return false;
        }
        Long count = stageEvaluationMapper.selectCount(
                Wrappers.<StageEvaluation>lambdaQuery()
                        .eq(StageEvaluation::getStudentId, studentId)
                        .ge(StageEvaluation::getPeriodEnd, goalEndDate)
        );
        return count == null || count == 0;
    }

    private boolean isRegression(FitnessTestRecord latest, FitnessTestRecord previous) {
        BigDecimal latestValue = latest.getTestValue();
        BigDecimal previousValue = previous.getTestValue();
        if (latestValue == null || previousValue == null) {
            return false;
        }
        int compare = latestValue.compareTo(previousValue);
        if (compare == 0) {
            return false;
        }
        return isLowerBetter(latest) ? compare > 0 : compare < 0;
    }

    private boolean isLowerBetter(FitnessTestRecord record) {
        String unit = normalize(record.getUnit());
        if (List.of("秒", "s", "sec", "secs", "second", "seconds", "分钟", "min", "mins", "minute", "minutes")
                .contains(unit)) {
            return true;
        }
        String itemName = normalize(record.getItemName());
        return itemName.contains("跑") || itemName.contains("计时");
    }

    private String alertKey(FitnessTestRecord record) {
        return normalize(record.getItemName()) + "|" + normalize(record.getUnit());
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private CareAlert toEntity(Long studentId, AlertDraft draft, String status, LocalDateTime resolvedAt) {
        CareAlert alert = new CareAlert();
        alert.setStudentId(studentId);
        alert.setAlertType(draft.type());
        alert.setAlertTitle(draft.title());
        alert.setAlertContent(draft.content());
        alert.setStatus(status);
        alert.setTriggeredAt(draft.triggeredAt());
        alert.setResolvedAt(resolvedAt);
        return alert;
    }

    private CareAlertResponse toResponse(CareAlert alert) {
        Student student = studentService.findNullableById(alert.getStudentId());
        return new CareAlertResponse(
                alert.getId(),
                alert.getStudentId(),
                student == null ? null : student.getName(),
                alert.getAlertType(),
                alert.getAlertTitle(),
                alert.getAlertContent(),
                alert.getStatus(),
                alert.getTriggeredAt(),
                alert.getResolvedAt(),
                alert.getCreatedAt(),
                alert.getUpdatedAt()
        );
    }

    private record AlertDraft(
            String type,
            String title,
            String content,
            LocalDateTime triggeredAt
    ) {
    }
}
