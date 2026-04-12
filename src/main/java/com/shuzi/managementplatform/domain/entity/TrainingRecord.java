package com.shuzi.managementplatform.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuzi.managementplatform.common.model.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Training record entity mapped to table {@code training_records}.
 */
@TableName("training_records")
public class TrainingRecord extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("student_id")
    private Long studentId;

    @TableField("course_id")
    private Long courseId;

    @TableField("training_date")
    private LocalDate trainingDate;

    @TableField("training_content")
    private String trainingContent;

    @TableField("duration_minutes")
    private Integer durationMinutes;

    @TableField("intensity_level")
    private String intensityLevel;

    @TableField("performance_summary")
    private String performanceSummary;

    @TableField("highlight_note")
    private String highlightNote;

    @TableField("improvement_note")
    private String improvementNote;

    @TableField("parent_action")
    private String parentAction;

    @TableField("next_step_suggestion")
    private String nextStepSuggestion;

    @TableField("coach_comment")
    private String coachComment;

    @TableField("ai_summary")
    private String aiSummary;

    @TableField("parent_read_at")
    private LocalDateTime parentReadAt;

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }

    public String getTrainingContent() {
        return trainingContent;
    }

    public void setTrainingContent(String trainingContent) {
        this.trainingContent = trainingContent;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getIntensityLevel() {
        return intensityLevel;
    }

    public void setIntensityLevel(String intensityLevel) {
        this.intensityLevel = intensityLevel;
    }

    public String getPerformanceSummary() {
        return performanceSummary;
    }

    public void setPerformanceSummary(String performanceSummary) {
        this.performanceSummary = performanceSummary;
    }

    public String getHighlightNote() {
        return highlightNote;
    }

    public void setHighlightNote(String highlightNote) {
        this.highlightNote = highlightNote;
    }

    public String getImprovementNote() {
        return improvementNote;
    }

    public void setImprovementNote(String improvementNote) {
        this.improvementNote = improvementNote;
    }

    public String getParentAction() {
        return parentAction;
    }

    public void setParentAction(String parentAction) {
        this.parentAction = parentAction;
    }

    public String getNextStepSuggestion() {
        return nextStepSuggestion;
    }

    public void setNextStepSuggestion(String nextStepSuggestion) {
        this.nextStepSuggestion = nextStepSuggestion;
    }

    public String getCoachComment() {
        return coachComment;
    }

    public void setCoachComment(String coachComment) {
        this.coachComment = coachComment;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public void setAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
    }

    public LocalDateTime getParentReadAt() {
        return parentReadAt;
    }

    public void setParentReadAt(LocalDateTime parentReadAt) {
        this.parentReadAt = parentReadAt;
    }
}
