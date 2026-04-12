package com.shuzi.managementplatform.web.controller;

import com.shuzi.managementplatform.common.api.ApiResponse;
import com.shuzi.managementplatform.common.api.PageResponse;
import com.shuzi.managementplatform.domain.enums.StudentStatus;
import com.shuzi.managementplatform.domain.service.StudentService;
import com.shuzi.managementplatform.web.dto.student.StudentAttendanceStatsResponse;
import com.shuzi.managementplatform.web.dto.student.StudentCreateRequest;
import com.shuzi.managementplatform.web.dto.student.StudentFitnessTrendResponse;
import com.shuzi.managementplatform.web.dto.student.StudentImportResult;
import com.shuzi.managementplatform.web.dto.student.StudentProfileResponse;
import com.shuzi.managementplatform.web.dto.student.StudentResponse;
import com.shuzi.managementplatform.web.dto.student.StudentUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Student management REST endpoints.
 */
@RestController
@RequestMapping("/api/v1/students")
@Tag(name = "Student", description = "Student profile management endpoints")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Create student", description = "Create a new student profile")
    public ApiResponse<StudentResponse> create(@Valid @RequestBody StudentCreateRequest request) {
        return ApiResponse.ok("student created", studentService.create(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update student", description = "Update student profile by ID")
    public ApiResponse<StudentResponse> update(@PathVariable Long id, @Valid @RequestBody StudentUpdateRequest request) {
        return ApiResponse.ok("student updated", studentService.update(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete student", description = "Delete student profile by ID when it is not referenced by business records")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ApiResponse.ok("student deleted", null);
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping("/{id}")
    @Operation(summary = "Get student detail", description = "Fetch student profile by ID")
    public ApiResponse<StudentResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(studentService.getById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping
    @Operation(summary = "List students", description = "Paginated query by optional name and status")
    public ApiResponse<PageResponse<StudentResponse>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) StudentStatus status
    ) {
        return ApiResponse.ok(PageResponse.from(studentService.page(name, status, page, size)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping("/{id}/profile")
    @Operation(summary = "Get student profile with stats")
    public ApiResponse<StudentProfileResponse> getProfile(@PathVariable Long id) {
        return ApiResponse.ok(studentService.getProfile(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping("/{id}/attendance-stats")
    @Operation(summary = "Get student monthly attendance stats")
    public ApiResponse<java.util.List<StudentAttendanceStatsResponse>> getAttendanceStats(@PathVariable Long id) {
        return ApiResponse.ok(studentService.getAttendanceStats(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping("/{id}/fitness-trends")
    @Operation(summary = "Get student fitness trends by item")
    public ApiResponse<java.util.List<StudentFitnessTrendResponse>> getFitnessTrends(@PathVariable Long id) {
        return ApiResponse.ok(studentService.getFitnessTrends(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import")
    @Operation(summary = "Import students from Excel")
    public ApiResponse<StudentImportResult> importStudents(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok("students imported", studentService.importFromExcel(file));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/import-template")
    @Operation(summary = "Download student import template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=student-import-template.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("students");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("studentNo");
            header.createCell(1).setCellValue("name");
            header.createCell(2).setCellValue("gender(MALE/FEMALE)");
            header.createCell(3).setCellValue("birthDate(yyyy-MM-dd)");
            header.createCell(4).setCellValue("guardianName");
            header.createCell(5).setCellValue("guardianPhone");
            header.createCell(6).setCellValue("status(ACTIVE/INACTIVE)");
            header.createCell(7).setCellValue("remarks");

            workbook.write(response.getOutputStream());
            response.flushBuffer();
        }
    }
}
