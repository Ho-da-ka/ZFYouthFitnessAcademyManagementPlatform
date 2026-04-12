package com.shuzi.managementplatform.domain.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuzi.managementplatform.common.exception.ResourceNotFoundException;
import com.shuzi.managementplatform.domain.entity.FitnessTestRecord;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.mapper.FitnessTestRecordMapper;
import com.shuzi.managementplatform.web.dto.fitness.FitnessTestCreateRequest;
import com.shuzi.managementplatform.web.dto.fitness.FitnessTestResponse;
import com.shuzi.managementplatform.web.dto.fitness.FitnessTestUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * Fitness test service for recording and querying student assessment timelines.
 */
@Service
public class FitnessTestService {

    private final FitnessTestRecordMapper fitnessTestRecordMapper;
    private final StudentService studentService;

    public FitnessTestService(FitnessTestRecordMapper fitnessTestRecordMapper, StudentService studentService) {
        this.fitnessTestRecordMapper = fitnessTestRecordMapper;
        this.studentService = studentService;
    }

    @Transactional
    public FitnessTestResponse create(FitnessTestCreateRequest request) {
        Student student = studentService.getEntityById(request.studentId());

        FitnessTestRecord record = new FitnessTestRecord();
        record.setStudentId(student.getId());
        record.setTestDate(request.testDate());
        record.setItemName(request.itemName());
        record.setTestValue(request.testValue());
        record.setUnit(request.unit());
        record.setComment(request.comment());
        record.setStudentNameSnapshot(student.getName());
        fitnessTestRecordMapper.insert(record);
        return toResponse(record, student);
    }

    @Transactional
    public FitnessTestResponse update(Long id, FitnessTestUpdateRequest request) {
        FitnessTestRecord record = fitnessTestRecordMapper.selectById(id);
        if (record == null) {
            throw new ResourceNotFoundException("fitness test record not found: " + id);
        }
        record.setTestDate(request.testDate());
        record.setItemName(request.itemName());
        record.setTestValue(request.testValue());
        record.setUnit(request.unit());
        record.setComment(request.comment());
        fitnessTestRecordMapper.updateById(record);
        return toResponse(record);
    }

    @Transactional
    public void delete(Long id) {
        FitnessTestRecord record = fitnessTestRecordMapper.selectById(id);
        if (record == null) {
            throw new ResourceNotFoundException("fitness test record not found: " + id);
        }
        fitnessTestRecordMapper.deleteById(id);
    }

    @Transactional(readOnly = true)
    public IPage<FitnessTestResponse> page(Long studentId, int page, int size) {
        Page<FitnessTestRecord> pageRequest = new Page<>(page + 1L, size);
        var query = Wrappers.<FitnessTestRecord>lambdaQuery()
                .orderByDesc(FitnessTestRecord::getTestDate, FitnessTestRecord::getId);
        if (studentId != null) {
            query.eq(FitnessTestRecord::getStudentId, studentId);
        }
        Page<FitnessTestRecord> result = fitnessTestRecordMapper.selectPage(pageRequest, query);
        Page<FitnessTestResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(result.getRecords().stream().map(this::toResponse).toList());
        return responsePage;
    }

    private FitnessTestResponse toResponse(FitnessTestRecord record) {
        Student student = null;
        if (record.getStudentId() != null) {
            student = studentService.findNullableById(record.getStudentId());
        }
        if (student != null) {
            return toResponse(record, student);
        }
        return new FitnessTestResponse(
                record.getId(),
                record.getStudentId(),
                record.getStudentNameSnapshot(),
                record.getTestDate(),
                record.getItemName(),
                record.getTestValue(),
                record.getUnit(),
                record.getComment(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

    private FitnessTestResponse toResponse(FitnessTestRecord record, Student student) {
        return new FitnessTestResponse(
                record.getId(),
                student.getId(),
                student.getName(),
                record.getTestDate(),
                record.getItemName(),
                record.getTestValue(),
                record.getUnit(),
                record.getComment(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }
}
