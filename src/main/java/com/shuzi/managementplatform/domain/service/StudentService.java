package com.shuzi.managementplatform.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuzi.managementplatform.common.exception.BusinessException;
import com.shuzi.managementplatform.common.exception.ResourceNotFoundException;
import com.shuzi.managementplatform.domain.entity.AttendanceRecord;
import com.shuzi.managementplatform.domain.entity.FitnessTestRecord;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.entity.TrainingRecord;
import com.shuzi.managementplatform.domain.enums.Gender;
import com.shuzi.managementplatform.domain.enums.StudentStatus;
import com.shuzi.managementplatform.domain.mapper.AttendanceRecordMapper;
import com.shuzi.managementplatform.domain.mapper.FitnessTestRecordMapper;
import com.shuzi.managementplatform.domain.mapper.StudentMapper;
import com.shuzi.managementplatform.domain.mapper.TrainingRecordMapper;
import com.shuzi.managementplatform.web.dto.student.StudentAttendanceStatsResponse;
import com.shuzi.managementplatform.web.dto.student.StudentCreateRequest;
import com.shuzi.managementplatform.web.dto.student.StudentFitnessTrendResponse;
import com.shuzi.managementplatform.web.dto.student.StudentImportResult;
import com.shuzi.managementplatform.web.dto.student.StudentProfileResponse;
import com.shuzi.managementplatform.web.dto.student.StudentResponse;
import com.shuzi.managementplatform.web.dto.student.StudentUpdateRequest;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.shuzi.managementplatform.domain.enums.AttendanceStatus;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Student domain service for CRUD and filtered pagination.
 */
@Service
public class StudentService {

    private final StudentMapper studentMapper;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final FitnessTestRecordMapper fitnessTestRecordMapper;
    private final TrainingRecordMapper trainingRecordMapper;
    private final UserAccountService userAccountService;

    public StudentService(
            StudentMapper studentMapper,
            AttendanceRecordMapper attendanceRecordMapper,
            FitnessTestRecordMapper fitnessTestRecordMapper,
            TrainingRecordMapper trainingRecordMapper,
            UserAccountService userAccountService
    ) {
        this.studentMapper = studentMapper;
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.fitnessTestRecordMapper = fitnessTestRecordMapper;
        this.trainingRecordMapper = trainingRecordMapper;
        this.userAccountService = userAccountService;
    }

    @Transactional
    public StudentResponse create(StudentCreateRequest request) {
        // Business key uniqueness check.
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
        student.setGoalFocus(request.goalFocus());
        student.setTrainingTags(request.trainingTags());
        student.setRiskNotes(request.riskNotes());
        student.setGoalStartDate(request.goalStartDate());
        student.setGoalEndDate(request.goalEndDate());
        studentMapper.insert(student);
        userAccountService.upsertStudentAccount(student);
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
        student.setGoalFocus(request.goalFocus());
        student.setTrainingTags(request.trainingTags());
        student.setRiskNotes(request.riskNotes());
        student.setGoalStartDate(request.goalStartDate());
        student.setGoalEndDate(request.goalEndDate());
        studentMapper.updateById(student);
        userAccountService.upsertStudentAccount(student);
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

    @Transactional
    public void delete(Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new ResourceNotFoundException("student not found: " + id);
        }

        Long attendanceCount = attendanceRecordMapper.selectCount(
                Wrappers.<AttendanceRecord>lambdaQuery()
                        .eq(AttendanceRecord::getStudentId, id)
        );
        if (attendanceCount != null && attendanceCount > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "该学员存在考勤记录，无法删除，请先清理关联数据");
        }

        // Fitness records are soft-deleted and detached from student on student removal.
        fitnessTestRecordMapper.update(
                null,
                Wrappers.<FitnessTestRecord>lambdaUpdate()
                        .eq(FitnessTestRecord::getStudentId, id)
                        .set(FitnessTestRecord::getStudentNameSnapshot, student.getName())
                        .set(FitnessTestRecord::getStudentId, null)
                        .set(FitnessTestRecord::getDeleted, 1)
        );

        Long trainingCount = trainingRecordMapper.selectCount(
                Wrappers.<TrainingRecord>lambdaQuery()
                        .eq(TrainingRecord::getStudentId, id)
        );
        if (trainingCount != null && trainingCount > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "该学员存在训练记录，无法删除，请先清理关联数据");
        }

        userAccountService.deleteByStudentId(id);
        studentMapper.deleteById(id);
    }

    @Transactional(readOnly = true)
    public IPage<StudentResponse> page(String name, StudentStatus status, int page, int size) {
        // MyBatis-Plus page index starts from 1; API contract uses 0-based index.
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

    @Transactional(readOnly = true)
    public Student findNullableById(Long id) {
        return studentMapper.selectById(id);
    }

    @Transactional
    public StudentImportResult importFromExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "import file is empty");
        }

        int successCount = 0;
        List<String> errors = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                return new StudentImportResult(0, 0, List.of());
            }

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                String studentNo = normalize(formatter.formatCellValue(row.getCell(0)));
                String name = normalize(formatter.formatCellValue(row.getCell(1)));
                String genderText = normalize(formatter.formatCellValue(row.getCell(2)));
                String birthDateText = normalize(formatter.formatCellValue(row.getCell(3)));
                String guardianName = normalize(formatter.formatCellValue(row.getCell(4)));
                String guardianPhone = normalize(formatter.formatCellValue(row.getCell(5)));
                String statusText = normalize(formatter.formatCellValue(row.getCell(6)));
                String remarks = normalize(formatter.formatCellValue(row.getCell(7)));

                if (!StringUtils.hasText(name)) {
                    errors.add("第 " + (rowIndex + 1) + " 行：姓名不能为空");
                    continue;
                }

                Gender gender = parseGender(genderText);
                if (gender == null) {
                    errors.add("第 " + (rowIndex + 1) + " 行：性别无效，仅支持 MALE/FEMALE 或 男/女");
                    continue;
                }

                LocalDate birthDate;
                try {
                    birthDate = LocalDate.parse(birthDateText);
                } catch (DateTimeParseException ex) {
                    errors.add("第 " + (rowIndex + 1) + " 行：出生日期格式应为 yyyy-MM-dd");
                    continue;
                }

                StudentStatus status = parseStatus(statusText);
                if (!StringUtils.hasText(studentNo)) {
                    studentNo = buildImportStudentNo(rowIndex);
                }

                try {
                    create(new StudentCreateRequest(
                            studentNo,
                            name,
                            gender,
                            birthDate,
                            guardianName,
                            guardianPhone,
                            status,
                            remarks,
                            null,
                            null,
                            null,
                            null,
                            null
                    ));
                    successCount += 1;
                } catch (Exception ex) {
                    errors.add("第 " + (rowIndex + 1) + " 行：" + ex.getMessage());
                }
            }
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "failed to parse import file: " + ex.getMessage());
        }

        return new StudentImportResult(successCount, errors.size(), errors);
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
                student.getGoalFocus(),
                student.getTrainingTags(),
                student.getRiskNotes(),
                student.getGoalStartDate(),
                student.getGoalEndDate(),
                student.getCreatedAt(),
                student.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public StudentProfileResponse getProfile(Long id) {
        Student student = getEntityById(id);
        long attendanceTotal = attendanceRecordMapper.selectCount(
                Wrappers.<AttendanceRecord>lambdaQuery().eq(AttendanceRecord::getStudentId, id));
        long attendancePresent = attendanceRecordMapper.selectCount(
                Wrappers.<AttendanceRecord>lambdaQuery().eq(AttendanceRecord::getStudentId, id)
                        .in(AttendanceRecord::getStatus, AttendanceStatus.PRESENT, AttendanceStatus.LATE));
        double rate = attendanceTotal == 0 ? 0.0 :
                Math.round((double) attendancePresent / attendanceTotal * 1000.0) / 1000.0;
        long fitnessCount = fitnessTestRecordMapper.selectCount(
                Wrappers.<FitnessTestRecord>lambdaQuery().eq(FitnessTestRecord::getStudentId, id));
        long trainingCount = trainingRecordMapper.selectCount(
                Wrappers.<TrainingRecord>lambdaQuery().eq(TrainingRecord::getStudentId, id));
        return new StudentProfileResponse(toResponse(student),
                new StudentProfileResponse.StudentStats(attendanceTotal, attendancePresent, rate, fitnessCount, trainingCount));
    }

    @Transactional(readOnly = true)
    public List<StudentAttendanceStatsResponse> getAttendanceStats(Long id) {
        getEntityById(id);
        List<AttendanceRecord> records = attendanceRecordMapper.selectList(
                Wrappers.<AttendanceRecord>lambdaQuery().eq(AttendanceRecord::getStudentId, id)
                        .orderByAsc(AttendanceRecord::getAttendanceDate));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, long[]> monthMap = new LinkedHashMap<>();
        for (AttendanceRecord r : records) {
            String month = r.getAttendanceDate().format(fmt);
            monthMap.computeIfAbsent(month, k -> new long[]{0, 0});
            monthMap.get(month)[0]++;
            if (r.getStatus() == AttendanceStatus.PRESENT || r.getStatus() == AttendanceStatus.LATE) {
                monthMap.get(month)[1]++;
            }
        }
        List<StudentAttendanceStatsResponse> result = new ArrayList<>();
        monthMap.forEach((month, counts) -> {
            double rate = counts[0] == 0 ? 0.0 : Math.round((double) counts[1] / counts[0] * 1000.0) / 1000.0;
            result.add(new StudentAttendanceStatsResponse(month, counts[1], counts[0], rate));
        });
        return result;
    }

    @Transactional(readOnly = true)
    public List<StudentFitnessTrendResponse> getFitnessTrends(Long id) {
        getEntityById(id);
        List<FitnessTestRecord> records = fitnessTestRecordMapper.selectList(
                Wrappers.<FitnessTestRecord>lambdaQuery().eq(FitnessTestRecord::getStudentId, id)
                        .orderByAsc(FitnessTestRecord::getTestDate));
        Map<String, List<StudentFitnessTrendResponse.DataPoint>> itemMap = new LinkedHashMap<>();
        Map<String, String> unitMap = new LinkedHashMap<>();
        for (FitnessTestRecord r : records) {
            itemMap.computeIfAbsent(r.getItemName(), k -> new ArrayList<>())
                    .add(new StudentFitnessTrendResponse.DataPoint(r.getTestDate(), r.getTestValue()));
            unitMap.putIfAbsent(r.getItemName(), r.getUnit());
        }
        List<StudentFitnessTrendResponse> result = new ArrayList<>();
        itemMap.forEach((item, points) -> result.add(
                new StudentFitnessTrendResponse(item, unitMap.get(item), points)));
        return result;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private Gender parseGender(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        if ("男".equals(raw) || "MALE".equalsIgnoreCase(raw)) {
            return Gender.MALE;
        }
        if ("女".equals(raw) || "FEMALE".equalsIgnoreCase(raw)) {
            return Gender.FEMALE;
        }
        return null;
    }

    private StudentStatus parseStatus(String raw) {
        if (!StringUtils.hasText(raw)) {
            return StudentStatus.ACTIVE;
        }
        if ("在训".equals(raw) || "ACTIVE".equalsIgnoreCase(raw)) {
            return StudentStatus.ACTIVE;
        }
        if ("停训".equals(raw) || "INACTIVE".equalsIgnoreCase(raw)) {
            return StudentStatus.INACTIVE;
        }
        return StudentStatus.ACTIVE;
    }

    private String buildImportStudentNo(int rowIndex) {
        return "IMP" + System.currentTimeMillis() + rowIndex;
    }
}
