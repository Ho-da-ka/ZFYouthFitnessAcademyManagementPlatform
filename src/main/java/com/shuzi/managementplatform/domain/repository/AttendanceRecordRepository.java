package com.shuzi.managementplatform.domain.repository;

import com.shuzi.managementplatform.domain.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    @Query("""
            select a from AttendanceRecord a
            where (:studentId is null or a.student.id = :studentId)
              and (:courseId is null or a.course.id = :courseId)
              and (:startDate is null or a.attendanceDate >= :startDate)
              and (:endDate is null or a.attendanceDate <= :endDate)
            order by a.attendanceDate desc, a.id desc
            """)
    List<AttendanceRecord> search(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
