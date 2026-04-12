package com.shuzi.managementplatform.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuzi.managementplatform.common.model.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@TableName("stage_evaluations")
public class StageEvaluation extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("student_id")
    private Long studentId;

    @TableField("cycle_name")
    private String cycleName;

    @TableField("period_start")
    private LocalDate periodStart;

    @TableField("period_end")
    private LocalDate periodEnd;

    @TableField("attendance_rate")
    private BigDecimal attendanceRate;

    @TableField("training_summary")
    private String trainingSummary;

    @TableField("fitness_summary")
    private String fitnessSummary;

    @TableField("coach_evaluation")
    private String coachEvaluation;

    @TableField("next_stage_plan")
    private String nextStagePlan;

    @TableField("ai_interpretation")
    private String aiInterpretation;

    @TableField("parent_report")
    private String parentReport;

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getCycleName() {
        return cycleName;
    }

    public void setCycleName(String cycleName) {
        this.cycleName = cycleName;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public BigDecimal getAttendanceRate() {
        return attendanceRate;
    }

    public void setAttendanceRate(BigDecimal attendanceRate) {
        this.attendanceRate = attendanceRate;
    }

    public String getTrainingSummary() {
        return trainingSummary;
    }

    public void setTrainingSummary(String trainingSummary) {
        this.trainingSummary = trainingSummary;
    }

    public String getFitnessSummary() {
        return fitnessSummary;
    }

    public void setFitnessSummary(String fitnessSummary) {
        this.fitnessSummary = fitnessSummary;
    }

    public String getCoachEvaluation() {
        return coachEvaluation;
    }

    public void setCoachEvaluation(String coachEvaluation) {
        this.coachEvaluation = coachEvaluation;
    }

    public String getNextStagePlan() {
        return nextStagePlan;
    }

    public void setNextStagePlan(String nextStagePlan) {
        this.nextStagePlan = nextStagePlan;
    }

    public String getAiInterpretation() {
        return aiInterpretation;
    }

    public void setAiInterpretation(String aiInterpretation) {
        this.aiInterpretation = aiInterpretation;
    }

    public String getParentReport() {
        return parentReport;
    }

    public void setParentReport(String parentReport) {
        this.parentReport = parentReport;
    }
}
