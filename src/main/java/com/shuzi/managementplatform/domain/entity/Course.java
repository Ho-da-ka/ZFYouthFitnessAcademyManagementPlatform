package com.shuzi.managementplatform.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuzi.managementplatform.common.model.BaseEntity;
import com.shuzi.managementplatform.domain.enums.CourseStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Course aggregate root mapped to table {@code courses}.
 */
@TableName("courses")
public class Course extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("course_code")
    private String courseCode;

    private String name;

    @TableField("course_type")
    private String courseType;

    @TableField("coach_name")
    private String coachName;

    private String venue;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("duration_minutes")
    private Integer durationMinutes;

    @TableField("max_capacity")
    private Integer maxCapacity;

    @TableField("course_date")
    private LocalDate courseDate;

    @TableField("class_start_time")
    private LocalTime classStartTime;

    @TableField("class_end_time")
    private LocalTime classEndTime;

    private CourseStatus status = CourseStatus.PLANNED;

    private String description;

    @TableField("training_theme")
    private String trainingTheme;

    @TableField("target_age_range")
    private String targetAgeRange;

    @TableField("target_goals")
    private String targetGoals;

    @TableField("focus_points")
    private String focusPoints;

    public Long getId() {
        return id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public String getCoachName() {
        return coachName;
    }

    public void setCoachName(String coachName) {
        this.coachName = coachName;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public LocalDate getCourseDate() {
        return courseDate;
    }

    public void setCourseDate(LocalDate courseDate) {
        this.courseDate = courseDate;
    }

    public LocalTime getClassStartTime() {
        return classStartTime;
    }

    public void setClassStartTime(LocalTime classStartTime) {
        this.classStartTime = classStartTime;
    }

    public LocalTime getClassEndTime() {
        return classEndTime;
    }

    public void setClassEndTime(LocalTime classEndTime) {
        this.classEndTime = classEndTime;
    }

    public CourseStatus getStatus() {
        return status;
    }

    public void setStatus(CourseStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTrainingTheme() {
        return trainingTheme;
    }

    public void setTrainingTheme(String trainingTheme) {
        this.trainingTheme = trainingTheme;
    }

    public String getTargetAgeRange() {
        return targetAgeRange;
    }

    public void setTargetAgeRange(String targetAgeRange) {
        this.targetAgeRange = targetAgeRange;
    }

    public String getTargetGoals() {
        return targetGoals;
    }

    public void setTargetGoals(String targetGoals) {
        this.targetGoals = targetGoals;
    }

    public String getFocusPoints() {
        return focusPoints;
    }

    public void setFocusPoints(String focusPoints) {
        this.focusPoints = focusPoints;
    }
}
