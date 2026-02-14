package com.shuzi.managementplatform.domain.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shuzi.managementplatform.domain.entity.FitnessTestRecord;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.mapper.FitnessTestRecordMapper;
import com.shuzi.managementplatform.web.dto.fitness.FitnessTestCreateRequest;
import com.shuzi.managementplatform.web.dto.fitness.FitnessTestResponse;
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
        fitnessTestRecordMapper.insert(record);
        return toResponse(record, student);
    }

    @Transactional(readOnly = true)
    public List<FitnessTestResponse> listByStudent(Long studentId) {
        var query = Wrappers.<FitnessTestRecord>lambdaQuery()
                .orderByDesc(FitnessTestRecord::getTestDate, FitnessTestRecord::getId);
        if (studentId != null) {
            query.eq(FitnessTestRecord::getStudentId, studentId);
        }
        return fitnessTestRecordMapper.selectList(query)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private FitnessTestResponse toResponse(FitnessTestRecord record) {
        Student student = studentService.getEntityById(record.getStudentId());
        return toResponse(record, student);
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
