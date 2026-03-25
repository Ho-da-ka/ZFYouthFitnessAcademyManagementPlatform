package com.shuzi.managementplatform.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuzi.managementplatform.common.model.BaseEntity;
import com.shuzi.managementplatform.domain.enums.CoachStatus;
import com.shuzi.managementplatform.domain.enums.Gender;

/**
 * Coach aggregate root mapped to table {@code coaches}.
 */
@TableName("coaches")
public class Coach extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("coach_code")
    private String coachCode;

    private String name;

    private Gender gender;

    private String phone;

    private String specialty;

    private CoachStatus status = CoachStatus.ACTIVE;

    private String remarks;

    public Long getId() {
        return id;
    }

    public String getCoachCode() {
        return coachCode;
    }

    public void setCoachCode(String coachCode) {
        this.coachCode = coachCode;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public CoachStatus getStatus() {
        return status;
    }

    public void setStatus(CoachStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
