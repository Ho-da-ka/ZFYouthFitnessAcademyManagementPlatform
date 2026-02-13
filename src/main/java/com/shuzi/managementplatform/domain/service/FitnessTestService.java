package com.shuzi.managementplatform.domain.service;

import com.shuzi.managementplatform.domain.entity.FitnessTestRecord;
import com.shuzi.managementplatform.domain.repository.FitnessTestRecordRepository;
import com.shuzi.managementplatform.web.dto.fitness.FitnessTestCreateRequest;
import com.shuzi.managementplatform.web.dto.fitness.FitnessTestResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FitnessTestService {

    private final FitnessTestRecordRepository fitnessTestRecordRepository;
    private final StudentService studentService;

    public FitnessTestService(FitnessTestRecordRepository fitnessTestRecordRepository, StudentService studentService) {
        this.fitnessTestRecordRepository = fitnessTestRecordRepository;
        this.studentService = studentService;
    }

    @Transactional
    public FitnessTestResponse create(FitnessTestCreateRequest request) {
        FitnessTestRecord record = new FitnessTestRecord();
        record.setStudent(studentService.getEntityById(request.studentId()));
        record.setTestDate(request.testDate());
        record.setItemName(request.itemName());
        record.setTestValue(request.testValue());
        record.setUnit(request.unit());
        record.setComment(request.comment());
        return toResponse(fitnessTestRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public List<FitnessTestResponse> listByStudent(Long studentId) {
        return fitnessTestRecordRepository.findByStudentIdOrderByTestDateDescIdDesc(studentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private FitnessTestResponse toResponse(FitnessTestRecord record) {
        return new FitnessTestResponse(
                record.getId(),
                record.getStudent().getId(),
                record.getStudent().getName(),
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
