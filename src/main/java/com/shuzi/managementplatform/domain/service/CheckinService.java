package com.shuzi.managementplatform.domain.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shuzi.managementplatform.common.exception.BusinessException;
import com.shuzi.managementplatform.common.exception.ResourceNotFoundException;
import com.shuzi.managementplatform.domain.entity.AttendanceRecord;
import com.shuzi.managementplatform.domain.entity.Course;
import com.shuzi.managementplatform.domain.entity.CourseBooking;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.entity.UserAccount;
import com.shuzi.managementplatform.domain.enums.AttendanceStatus;
import com.shuzi.managementplatform.domain.enums.StudentStatus;
import com.shuzi.managementplatform.domain.mapper.AttendanceRecordMapper;
import com.shuzi.managementplatform.domain.mapper.CourseBookingMapper;
import com.shuzi.managementplatform.domain.mapper.CourseMapper;
import com.shuzi.managementplatform.domain.mapper.StudentMapper;
import com.shuzi.managementplatform.domain.mapper.UserAccountMapper;
import com.shuzi.managementplatform.web.dto.attendance.AttendanceResponse;
import com.shuzi.managementplatform.web.dto.checkin.CoachCheckinInitResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

@Service
public class CheckinService {

    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_CHECKIN = "checkin";
    private static final String CLAIM_COURSE_ID = "courseId";
    private static final long TOKEN_EXPIRE_SECONDS = 600; // 10 minutes

    private final SecretKey signingKey;
    private final CourseMapper courseMapper;
    private final StudentMapper studentMapper;
    private final UserAccountMapper userAccountMapper;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final CourseBookingMapper courseBookingMapper;
    private final CareAlertService careAlertService;

    public CheckinService(
            @Value("${security.jwt.secret}") String secret,
            CourseMapper courseMapper,
            StudentMapper studentMapper,
            UserAccountMapper userAccountMapper,
            AttendanceRecordMapper attendanceRecordMapper,
            CourseBookingMapper courseBookingMapper,
            CareAlertService careAlertService
    ) {
        this.signingKey = buildSigningKey(secret);
        this.courseMapper = courseMapper;
        this.studentMapper = studentMapper;
        this.userAccountMapper = userAccountMapper;
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.courseBookingMapper = courseBookingMapper;
        this.careAlertService = careAlertService;
    }

    @Transactional(readOnly = true)
    public CoachCheckinInitResponse initiateCoachCheckin(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new ResourceNotFoundException("Course not found: " + courseId);
        }

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(TOKEN_EXPIRE_SECONDS);

        String token = Jwts.builder()
                .claim(CLAIM_TYPE, TYPE_CHECKIN)
                .claim(CLAIM_COURSE_ID, courseId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();

        return new CoachCheckinInitResponse(
                token,
                courseId,
                course.getName(),
                LocalDateTime.ofInstant(expiresAt, ZoneId.systemDefault())
        );
    }

    @Transactional
    public AttendanceResponse studentScanCheckin(String token, String username) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "无效或过期的签到二维码");
        }

        String type = claims.get(CLAIM_TYPE, String.class);
        if (!TYPE_CHECKIN.equals(type)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "错误的二维码类型");
        }

        Long courseId = claims.get(CLAIM_COURSE_ID, Long.class);
        Student student = resolveStudent(username);

        return performCheckin(student, courseId);
    }

    private AttendanceResponse performCheckin(Student student, Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new ResourceNotFoundException("Course not found: " + courseId);
        }

        LocalDate today = LocalDate.now();

        // Check for existing attendance today
        Long exists = attendanceRecordMapper.selectCount(
                Wrappers.<AttendanceRecord>lambdaQuery()
                        .eq(AttendanceRecord::getStudentId, student.getId())
                        .eq(AttendanceRecord::getCourseId, courseId)
                        .eq(AttendanceRecord::getAttendanceDate, today)
        );
        if (exists != null && exists > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "您今天已经签过到了");
        }

        // Create Attendance Record
        AttendanceRecord record = new AttendanceRecord();
        record.setStudentId(student.getId());
        record.setCourseId(courseId);
        record.setAttendanceDate(today);
        record.setStatus(AttendanceStatus.PRESENT);
        record.setNote("学生扫码签到");
        attendanceRecordMapper.insert(record);

        // Update Course Booking if exists
        CourseBooking booking = courseBookingMapper.selectOne(
                Wrappers.<CourseBooking>lambdaQuery()
                        .eq(CourseBooking::getStudentId, student.getId())
                        .eq(CourseBooking::getCourseId, courseId)
                        .eq(CourseBooking::getBookingStatus, "BOOKED")
                        .orderByDesc(CourseBooking::getId)
                        .last("limit 1")
        );
        if (booking != null) {
            booking.setCheckinStatus("CHECKED_IN");
            booking.setCheckinTime(LocalDateTime.now());
            courseBookingMapper.updateById(booking);
        }

        careAlertService.refreshStudentAlerts(student.getId());

        return new AttendanceResponse(
                record.getId(),
                student.getId(),
                student.getName(),
                course.getId(),
                course.getName(),
                record.getAttendanceDate(),
                record.getStatus(),
                record.getNote(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

    private Student resolveStudent(String username) {
        if (!StringUtils.hasText(username)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        UserAccount account = userAccountMapper.selectOne(
                Wrappers.<UserAccount>lambdaQuery().eq(UserAccount::getUsername, username.toLowerCase(Locale.ROOT))
        );
        if (account == null || !"STUDENT".equalsIgnoreCase(account.getRole())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "仅学生可进行扫码签到");
        }
        Student student = studentMapper.selectById(account.getStudentId());
        if (student == null || student.getStatus() != StudentStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "学员状态异常");
        }
        return student;
    }

    private SecretKey buildSigningKey(String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            try {
                bytes = MessageDigest.getInstance("SHA-256").digest(bytes);
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to build signing key", ex);
            }
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}
