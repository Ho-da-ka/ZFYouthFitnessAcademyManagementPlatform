package com.shuzi.managementplatform.domain.repository;

import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.enums.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentNo(String studentNo);

    boolean existsByStudentNo(String studentNo);

    Page<Student> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Student> findByStatus(StudentStatus status, Pageable pageable);

    Page<Student> findByNameContainingIgnoreCaseAndStatus(String name, StudentStatus status, Pageable pageable);
}
