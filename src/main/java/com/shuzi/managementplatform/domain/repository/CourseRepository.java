package com.shuzi.managementplatform.domain.repository;

import com.shuzi.managementplatform.domain.entity.Course;
import com.shuzi.managementplatform.domain.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);

    boolean existsByCourseCode(String courseCode);

    Page<Course> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Course> findByStatus(CourseStatus status, Pageable pageable);

    Page<Course> findByNameContainingIgnoreCaseAndStatus(String name, CourseStatus status, Pageable pageable);
}
