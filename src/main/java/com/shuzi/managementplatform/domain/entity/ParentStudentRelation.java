package com.shuzi.managementplatform.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuzi.managementplatform.common.model.BaseEntity;
import com.shuzi.managementplatform.domain.enums.ParentBindingType;

/**
 * Many-to-many relation between parent account and student profile.
 */
@TableName("parent_student_relations")
public class ParentStudentRelation extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("parent_account_id")
    private Long parentAccountId;

    @TableField("student_id")
    private Long studentId;

    @TableField("binding_type")
    private ParentBindingType bindingType = ParentBindingType.AUTO;

    public Long getId() {
        return id;
    }

    public Long getParentAccountId() {
        return parentAccountId;
    }

    public void setParentAccountId(Long parentAccountId) {
        this.parentAccountId = parentAccountId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public ParentBindingType getBindingType() {
        return bindingType;
    }

    public void setBindingType(ParentBindingType bindingType) {
        this.bindingType = bindingType;
    }
}

