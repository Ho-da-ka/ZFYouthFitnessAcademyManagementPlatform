package com.shuzi.managementplatform.domain.repository;

import com.shuzi.managementplatform.domain.entity.FitnessTestRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FitnessTestRecordRepository extends JpaRepository<FitnessTestRecord, Long> {
    List<FitnessTestRecord> findByStudentIdOrderByTestDateDescIdDesc(Long studentId);
}
