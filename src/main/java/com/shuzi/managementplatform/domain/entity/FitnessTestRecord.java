package com.shuzi.managementplatform.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuzi.managementplatform.common.model.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Fitness assessment record mapped to table {@code fitness_test_records}.
 */
@TableName("fitness_test_records")
public class FitnessTestRecord extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("student_id")
    private Long studentId;

    @TableField("student_name_snapshot")
    private String studentNameSnapshot;

    @TableField("test_date")
    private LocalDate testDate;

    @TableField("item_name")
    private String itemName;

    @TableField("test_value")
    private BigDecimal testValue;

    private String unit;

    private String comment;

    @TableField("deleted")
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public LocalDate getTestDate() {
        return testDate;
    }

    public void setTestDate(LocalDate testDate) {
        this.testDate = testDate;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public BigDecimal getTestValue() {
        return testValue;
    }

    public void setTestValue(BigDecimal testValue) {
        this.testValue = testValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getStudentNameSnapshot() {
        return studentNameSnapshot;
    }

    public void setStudentNameSnapshot(String studentNameSnapshot) {
        this.studentNameSnapshot = studentNameSnapshot;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
