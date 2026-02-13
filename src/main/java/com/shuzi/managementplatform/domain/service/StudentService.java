package com.shuzi.managementplatform.domain.service;

import com.shuzi.managementplatform.common.exception.BusinessException;
import com.shuzi.managementplatform.common.exception.ResourceNotFoundException;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.enums.StudentStatus;
import com.shuzi.managementplatform.domain.repository.StudentRepository;
import com.shuzi.managementplatform.web.dto.student.StudentCreateRequest;
import com.shuzi.managementplatform.web.dto.student.StudentResponse;
import com.shuzi.managementplatform.web.dto.student.StudentUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional
    public StudentResponse create(StudentCreateRequest request) {
        if (studentRepository.existsByStudentNo(request.studentNo())) {
            throw new BusinessException(HttpStatus.CONFLICT, "studentNo already exists");
        }
        Student student = new Student();
        student.setStudentNo(request.studentNo());
        student.setName(request.name());
        student.setGender(request.gender());
        student.setBirthDate(request.birthDate());
        student.setGuardianName(request.guardianName());
        student.setGuardianPhone(request.guardianPhone());
        student.setStatus(request.status() == null ? StudentStatus.ACTIVE : request.status());
        student.setRemarks(request.remarks());
        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse update(Long id, StudentUpdateRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("student not found: " + id));
        student.setName(request.name());
        student.setGender(request.gender());
        student.setBirthDate(request.birthDate());
        student.setGuardianName(request.guardianName());
        student.setGuardianPhone(request.guardianPhone());
        student.setStatus(request.status());
        student.setRemarks(request.remarks());
        return toResponse(studentRepository.save(student));
    }

    @Transactional(readOnly = true)
    public StudentResponse getById(Long id) {
        return toResponse(studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("student not found: " + id)));
    }

    @Transactional(readOnly = true)
    public Page<StudentResponse> page(String name, StudentStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String normalizedName = name == null ? null : name.trim();
        Page<Student> result;

        if (normalizedName != null && !normalizedName.isEmpty() && status != null) {
            result = studentRepository.findByNameContainingIgnoreCaseAndStatus(normalizedName, status, pageable);
        } else if (normalizedName != null && !normalizedName.isEmpty()) {
            result = studentRepository.findByNameContainingIgnoreCase(normalizedName, pageable);
        } else if (status != null) {
            result = studentRepository.findByStatus(status, pageable);
        } else {
            result = studentRepository.findAll(pageable);
        }

        List<StudentResponse> content = result.stream().map(this::toResponse).toList();
        return new PageImpl<>(content, pageable, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Student getEntityById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("student not found: " + id));
    }

    private StudentResponse toResponse(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getStudentNo(),
                student.getName(),
                student.getGender(),
                student.getBirthDate(),
                student.getGuardianName(),
                student.getGuardianPhone(),
                student.getStatus(),
                student.getRemarks(),
                student.getCreatedAt(),
                student.getUpdatedAt()
        );
    }
}
