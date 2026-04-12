package com.shuzi.managementplatform.domain.service;

import com.shuzi.managementplatform.common.exception.BusinessException;
import com.shuzi.managementplatform.common.exception.ResourceNotFoundException;
import com.shuzi.managementplatform.domain.entity.FitnessTestRecord;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.mapper.FitnessTestRecordMapper;
import com.shuzi.managementplatform.web.dto.fitness.FitnessTestCreateRequest;
import com.shuzi.managementplatform.web.dto.fitness.FitnessTestUpdateRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FitnessTestServiceTest {

    @Mock
    private FitnessTestRecordMapper fitnessTestRecordMapper;
    @Mock
    private StudentService studentService;

    @InjectMocks
    private FitnessTestService fitnessTestService;

    private Student student;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setStudentNo("S001");
        student.setName("学员A");
    }

    @Test
    void createShouldPersistRecord() {
        when(studentService.getEntityById(1L)).thenReturn(student);

        fitnessTestService.create(new FitnessTestCreateRequest(
                1L, LocalDate.now(), "50米跑", BigDecimal.valueOf(8.9), "秒", "良好"));

        verify(fitnessTestRecordMapper, times(1)).insert(ArgumentMatchers.any(FitnessTestRecord.class));
    }

    @Test
    void updateShouldModifyRecordFields() {
        FitnessTestRecord record = new FitnessTestRecord();
        record.setStudentId(1L);
        record.setItemName("旧项目");
        record.setTestDate(LocalDate.now().minusDays(1));
        record.setTestValue(BigDecimal.valueOf(10));
        record.setUnit("次");
        when(fitnessTestRecordMapper.selectById(9L)).thenReturn(record);
        when(studentService.findNullableById(1L)).thenReturn(student);

        fitnessTestService.update(9L, new FitnessTestUpdateRequest(
                LocalDate.now(), "立定跳远", BigDecimal.valueOf(210), "cm", "进步"));

        Assertions.assertEquals("立定跳远", record.getItemName());
        Assertions.assertEquals(BigDecimal.valueOf(210), record.getTestValue());
        verify(fitnessTestRecordMapper, times(1)).updateById(record);
    }

    @Test
    void createShouldRejectFutureDate() {
        Assertions.assertThrows(BusinessException.class, () ->
                fitnessTestService.create(new FitnessTestCreateRequest(
                        1L, LocalDate.now().plusDays(1), "50米跑", BigDecimal.valueOf(8.8), "秒", null)));
    }

    @Test
    void deleteShouldThrowWhenRecordNotFound() {
        when(fitnessTestRecordMapper.selectById(999L)).thenReturn(null);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> fitnessTestService.delete(999L));
    }
}

