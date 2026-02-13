package com.shuzi.managementplatform.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuzi.managementplatform.common.exception.BusinessException;
import com.shuzi.managementplatform.common.exception.ResourceNotFoundException;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.enums.StudentStatus;
import com.shuzi.managementplatform.domain.mapper.StudentMapper;
import com.shuzi.managementplatform.web.dto.student.StudentCreateRequest;
import com.shuzi.managementplatform.web.dto.student.StudentResponse;
import com.shuzi.managementplatform.web.dto.student.StudentUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class StudentService {

    private final StudentMapper studentMapper;

    public StudentService(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    @Transactional
    public StudentResponse create(StudentCreateRequest request) {
        Long count = studentMapper.selectCount(
                Wrappers.<Student>lambdaQuery().eq(Student::getStudentNo, request.studentNo())
        );
        if (count != null && count > 0) {
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
        studentMapper.insert(student);
        return toResponse(student);
    }

    @Transactional
    public StudentResponse update(Long id, StudentUpdateRequest request) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new ResourceNotFoundException("student not found: " + id);
        }
        student.setName(request.name());
        student.setGender(request.gender());
        student.setBirthDate(request.birthDate());
        student.setGuardianName(request.guardianName());
        student.setGuardianPhone(request.guardianPhone());
        student.setStatus(request.status());
        student.setRemarks(request.remarks());
        studentMapper.updateById(student);
        return toResponse(student);
    }

    @Transactional(readOnly = true)
    public StudentResponse getById(Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new ResourceNotFoundException("student not found: " + id);
        }
        return toResponse(student);
    }

    @Transactional(readOnly = true)
    public IPage<StudentResponse> page(String name, StudentStatus status, int page, int size) {
        Page<Student> pageRequest = new Page<>(page + 1L, size);
        LambdaQueryWrapper<Student> query = Wrappers.<Student>lambdaQuery();

        if (StringUtils.hasText(name)) {
            query.like(Student::getName, name.trim());
        }
        if (status != null) {
            query.eq(Student::getStatus, status);
        }
        query.orderByDesc(Student::getId);

        Page<Student> result = studentMapper.selectPage(pageRequest, query);
        List<StudentResponse> records = result.getRecords().stream().map(this::toResponse).toList();

        Page<StudentResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(records);
        return responsePage;
    }

    @Transactional(readOnly = true)
    public Student getEntityById(Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new ResourceNotFoundException("student not found: " + id);
        }
        return student;
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
