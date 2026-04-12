package com.shuzi.managementplatform.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuzi.managementplatform.common.model.BaseEntity;
import com.shuzi.managementplatform.domain.enums.Gender;
import com.shuzi.managementplatform.domain.enums.StudentStatus;

import java.time.LocalDate;

/**
 * Student aggregate root mapped to table {@code students}.
 */
@TableName("students")
public class Student extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("student_no")
    private String studentNo;

    private String name;

    private Gender gender;

    @TableField("birth_date")
    private LocalDate birthDate;

    @TableField("guardian_name")
    private String guardianName;

    @TableField("guardian_phone")
    private String guardianPhone;

    private StudentStatus status = StudentStatus.ACTIVE;

    private String remarks;

    @TableField("goal_focus")
    private String goalFocus;

    @TableField("training_tags")
    private String trainingTags;

    @TableField("risk_notes")
    private String riskNotes;

    @TableField("goal_start_date")
    private LocalDate goalStartDate;

    @TableField("goal_end_date")
    private LocalDate goalEndDate;

    public Long getId() {
        return id;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public String getGuardianPhone() {
        return guardianPhone;
    }

    public void setGuardianPhone(String guardianPhone) {
        this.guardianPhone = guardianPhone;
    }

    public StudentStatus getStatus() {
        return status;
    }

    public void setStatus(StudentStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getGoalFocus() {
        return goalFocus;
    }

    public void setGoalFocus(String goalFocus) {
        this.goalFocus = goalFocus;
    }

    public String getTrainingTags() {
        return trainingTags;
    }

    public void setTrainingTags(String trainingTags) {
        this.trainingTags = trainingTags;
    }

    public String getRiskNotes() {
        return riskNotes;
    }

    public void setRiskNotes(String riskNotes) {
        this.riskNotes = riskNotes;
    }

    public LocalDate getGoalStartDate() {
        return goalStartDate;
    }

    public void setGoalStartDate(LocalDate goalStartDate) {
        this.goalStartDate = goalStartDate;
    }

    public LocalDate getGoalEndDate() {
        return goalEndDate;
    }

    public void setGoalEndDate(LocalDate goalEndDate) {
        this.goalEndDate = goalEndDate;
    }
}
