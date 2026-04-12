package com.shuzi.managementplatform.domain.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shuzi.managementplatform.common.exception.BusinessException;
import com.shuzi.managementplatform.common.exception.ResourceNotFoundException;
import com.shuzi.managementplatform.domain.entity.AttendanceRecord;
import com.shuzi.managementplatform.domain.entity.Course;
import com.shuzi.managementplatform.domain.entity.CourseBooking;
import com.shuzi.managementplatform.domain.entity.FitnessTestRecord;
import com.shuzi.managementplatform.domain.entity.InAppMessage;
import com.shuzi.managementplatform.domain.entity.ParentAccount;
import com.shuzi.managementplatform.domain.entity.ParentStudentRelation;
import com.shuzi.managementplatform.domain.entity.StageEvaluation;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.entity.TrainingRecord;
import com.shuzi.managementplatform.domain.entity.UserAccount;
import com.shuzi.managementplatform.domain.enums.AttendanceStatus;
import com.shuzi.managementplatform.domain.enums.CourseStatus;
import com.shuzi.managementplatform.domain.enums.StudentStatus;
import com.shuzi.managementplatform.domain.mapper.AttendanceRecordMapper;
import com.shuzi.managementplatform.domain.mapper.CourseBookingMapper;
import com.shuzi.managementplatform.domain.mapper.CourseMapper;
import com.shuzi.managementplatform.domain.mapper.FitnessTestRecordMapper;
import com.shuzi.managementplatform.domain.mapper.InAppMessageMapper;
import com.shuzi.managementplatform.domain.mapper.ParentAccountMapper;
import com.shuzi.managementplatform.domain.mapper.ParentStudentRelationMapper;
import com.shuzi.managementplatform.domain.mapper.StudentMapper;
import com.shuzi.managementplatform.domain.mapper.StageEvaluationMapper;
import com.shuzi.managementplatform.domain.mapper.TrainingRecordMapper;
import com.shuzi.managementplatform.domain.mapper.UserAccountMapper;
import com.shuzi.managementplatform.web.dto.parent.ParentBookingCreateRequest;
import com.shuzi.managementplatform.web.dto.parent.ParentBookingResponse;
import com.shuzi.managementplatform.web.dto.parent.ParentCheckinCreateRequest;
import com.shuzi.managementplatform.web.dto.parent.ParentCheckinResponse;
import com.shuzi.managementplatform.web.dto.parent.ParentChildResponse;
import com.shuzi.managementplatform.web.dto.parent.ParentCourseResponse;
import com.shuzi.managementplatform.web.dto.parent.ParentFitnessResponse;
import com.shuzi.managementplatform.web.dto.parent.ParentGrowthOverviewResponse;
import com.shuzi.managementplatform.web.dto.parent.ParentMessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Parent-side portal service for children, courses, booking, check-in and messages.
 */
@Service
public class ParentPortalService {

    private static final String ROLE_PARENT = "PARENT";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String BOOKING_STATUS_BOOKED = "BOOKED";
    private static final String BOOKING_STATUS_CANCELED = "CANCELED";
    private static final String CHECKIN_STATUS_PENDING = "PENDING";
    private static final String CHECKIN_STATUS_CHECKED_IN = "CHECKED_IN";
    private static final int DEFAULT_COURSE_CAPACITY = 20;

    private final UserAccountMapper userAccountMapper;
    private final ParentAccountMapper parentAccountMapper;
    private final ParentStudentRelationMapper parentStudentRelationMapper;
    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;
    private final CourseBookingMapper courseBookingMapper;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final FitnessTestRecordMapper fitnessTestRecordMapper;
    private final InAppMessageMapper inAppMessageMapper;
    private final TrainingRecordMapper trainingRecordMapper;
    private final StageEvaluationMapper stageEvaluationMapper;
    private final CareAlertService careAlertService;

    public ParentPortalService(
            UserAccountMapper userAccountMapper,
            ParentAccountMapper parentAccountMapper,
            ParentStudentRelationMapper parentStudentRelationMapper,
            StudentMapper studentMapper,
            CourseMapper courseMapper,
            CourseBookingMapper courseBookingMapper,
            AttendanceRecordMapper attendanceRecordMapper,
            FitnessTestRecordMapper fitnessTestRecordMapper,
            InAppMessageMapper inAppMessageMapper,
            TrainingRecordMapper trainingRecordMapper,
            StageEvaluationMapper stageEvaluationMapper,
            CareAlertService careAlertService
    ) {
        this.userAccountMapper = userAccountMapper;
        this.parentAccountMapper = parentAccountMapper;
        this.parentStudentRelationMapper = parentStudentRelationMapper;
        this.studentMapper = studentMapper;
        this.courseMapper = courseMapper;
        this.courseBookingMapper = courseBookingMapper;
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.fitnessTestRecordMapper = fitnessTestRecordMapper;
        this.inAppMessageMapper = inAppMessageMapper;
        this.trainingRecordMapper = trainingRecordMapper;
        this.stageEvaluationMapper = stageEvaluationMapper;
        this.careAlertService = careAlertService;
    }

    @Transactional
    public List<ParentChildResponse> listChildren(String username) {
        ParentAccount parentAccount = resolveParentAccount(username);
        List<Student> students = listBoundStudents(parentAccount.getId());
        return students.stream().map(this::toChildResponse).toList();
    }

    @Transactional
    public List<ParentCourseResponse> listCourses(String username) {
        resolveParentAccount(username);
        List<Course> courses = courseMapper.selectList(
                Wrappers.<Course>lambdaQuery()
                        .in(Course::getStatus, List.of(CourseStatus.PLANNED, CourseStatus.ONGOING))
                        .orderByAsc(Course::getStartTime, Course::getId)
        );
        return courses.stream()
                .map(course -> {
                    long bookedCount = countBooked(course.getId());
                    long available = Math.max(0L, DEFAULT_COURSE_CAPACITY - bookedCount);
                    return new ParentCourseResponse(
                            course.getId(),
                            course.getCourseCode(),
                            course.getName(),
                            course.getCourseType(),
                            course.getCoachName(),
                            course.getVenue(),
                            course.getStartTime(),
                            course.getDurationMinutes(),
                            course.getStatus(),
                            course.getDescription(),
                            DEFAULT_COURSE_CAPACITY,
                            bookedCount,
                            available
                    );
                })
                .toList();
    }

    @Transactional
    public List<ParentBookingResponse> listBookings(String username) {
        ParentAccount parentAccount = resolveParentAccount(username);
        List<CourseBooking> bookings = courseBookingMapper.selectList(
                Wrappers.<CourseBooking>lambdaQuery()
                        .eq(CourseBooking::getParentAccountId, parentAccount.getId())
                        .orderByDesc(CourseBooking::getId)
        );
        Map<Long, Student> studentMap = studentMap(bookings.stream().map(CourseBooking::getStudentId).toList());
        Map<Long, Course> courseMap = courseMap(bookings.stream().map(CourseBooking::getCourseId).toList());
        return bookings.stream().map(booking -> toBookingResponse(booking, studentMap, courseMap)).toList();
    }

    @Transactional
    public ParentBookingResponse createBooking(String username, ParentBookingCreateRequest request) {
        ParentAccount parentAccount = resolveParentAccount(username);
        assertStudentBound(parentAccount.getId(), request.studentId());

        Student student = studentMapper.selectById(request.studentId());
        if (student == null) {
            throw new ResourceNotFoundException("student not found: " + request.studentId());
        }
        Course course = courseMapper.selectById(request.courseId());
        if (course == null) {
            throw new ResourceNotFoundException("course not found: " + request.courseId());
        }
        if (course.getStatus() != CourseStatus.PLANNED && course.getStatus() != CourseStatus.ONGOING) {
            throw new BusinessException(HttpStatus.CONFLICT, "课程当前状态不允许预约");
        }

        Long duplicated = courseBookingMapper.selectCount(
                Wrappers.<CourseBooking>lambdaQuery()
                        .eq(CourseBooking::getParentAccountId, parentAccount.getId())
                        .eq(CourseBooking::getStudentId, request.studentId())
                        .eq(CourseBooking::getCourseId, request.courseId())
                        .eq(CourseBooking::getBookingStatus, BOOKING_STATUS_BOOKED)
        );
        if (duplicated != null && duplicated > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "该学员已预约此课程");
        }

        long bookedCount = countBooked(request.courseId());
        if (bookedCount >= DEFAULT_COURSE_CAPACITY) {
            throw new BusinessException(HttpStatus.CONFLICT, "课程名额已满，请选择其他课程");
        }

        CourseBooking booking = new CourseBooking();
        booking.setParentAccountId(parentAccount.getId());
        booking.setStudentId(request.studentId());
        booking.setCourseId(request.courseId());
        booking.setBookingStatus(BOOKING_STATUS_BOOKED);
        booking.setCourseCapacity(DEFAULT_COURSE_CAPACITY);
        booking.setBookingRemark(trimNullable(request.remark()));
        booking.setCheckinStatus(CHECKIN_STATUS_PENDING);
        courseBookingMapper.insert(booking);

        createMessage(
                parentAccount.getId(),
                "课程预约成功",
                "已为学员“" + student.getName() + "”预约课程“" + course.getName() + "”。",
                "BOOKING"
        );
        return toBookingResponse(
                booking,
                Map.of(student.getId(), student),
                Map.of(course.getId(), course)
        );
    }

    @Transactional
    public ParentBookingResponse cancelBooking(String username, Long bookingId) {
        ParentAccount parentAccount = resolveParentAccount(username);
        CourseBooking booking = getBookingByParent(parentAccount.getId(), bookingId);
        if (BOOKING_STATUS_CANCELED.equals(booking.getBookingStatus())) {
            return toBookingResponse(booking, studentMap(List.of(booking.getStudentId())), courseMap(List.of(booking.getCourseId())));
        }

        booking.setBookingStatus(BOOKING_STATUS_CANCELED);
        booking.setCheckinStatus(CHECKIN_STATUS_PENDING);
        booking.setCheckinTime(null);
        courseBookingMapper.updateById(booking);

        Student student = studentMapper.selectById(booking.getStudentId());
        Course course = courseMapper.selectById(booking.getCourseId());
        if (student != null && course != null) {
            createMessage(
                    parentAccount.getId(),
                    "课程预约已取消",
                    "已取消学员“" + student.getName() + "”的课程“" + course.getName() + "”预约。",
                    "BOOKING"
            );
        }

        return toBookingResponse(
                booking,
                studentMap(List.of(booking.getStudentId())),
                courseMap(List.of(booking.getCourseId()))
        );
    }

    @Transactional
    public List<ParentCheckinResponse> listCheckins(String username) {
        ParentAccount parentAccount = resolveParentAccount(username);
        List<Long> studentIds = listBoundStudentIds(parentAccount.getId());
        if (studentIds.isEmpty()) {
            return List.of();
        }

        List<AttendanceRecord> records = attendanceRecordMapper.selectList(
                Wrappers.<AttendanceRecord>lambdaQuery()
                        .in(AttendanceRecord::getStudentId, studentIds)
                        .orderByDesc(AttendanceRecord::getAttendanceDate, AttendanceRecord::getId)
        );
        Map<Long, Student> studentMap = studentMap(records.stream().map(AttendanceRecord::getStudentId).toList());
        Map<Long, Course> courseMap = courseMap(records.stream().map(AttendanceRecord::getCourseId).toList());
        Map<String, Long> bookingMap = bookingIdMap(parentAccount.getId(), records);

        return records.stream().map(record -> new ParentCheckinResponse(
                record.getId(),
                bookingMap.get(buildAttendKey(record.getStudentId(), record.getCourseId(), record.getAttendanceDate())),
                record.getStudentId(),
                studentMap.get(record.getStudentId()) == null ? "-" : studentMap.get(record.getStudentId()).getName(),
                record.getCourseId(),
                courseMap.get(record.getCourseId()) == null ? "-" : courseMap.get(record.getCourseId()).getName(),
                record.getAttendanceDate(),
                record.getStatus(),
                record.getNote(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        )).toList();
    }

    @Transactional
    public ParentCheckinResponse createCheckin(String username, ParentCheckinCreateRequest request) {
        ParentAccount parentAccount = resolveParentAccount(username);
        CourseBooking booking = getBookingByParent(parentAccount.getId(), request.bookingId());
        if (!BOOKING_STATUS_BOOKED.equals(booking.getBookingStatus())) {
            throw new BusinessException(HttpStatus.CONFLICT, "仅可对有效预约记录执行签到");
        }

        LocalDate attendanceDate = request.attendanceDate() == null ? LocalDate.now() : request.attendanceDate();
        Long exists = attendanceRecordMapper.selectCount(
                Wrappers.<AttendanceRecord>lambdaQuery()
                        .eq(AttendanceRecord::getStudentId, booking.getStudentId())
                        .eq(AttendanceRecord::getCourseId, booking.getCourseId())
                        .eq(AttendanceRecord::getAttendanceDate, attendanceDate)
        );
        if (exists != null && exists > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "该学员在所选日期已存在签到记录");
        }

        AttendanceRecord attendance = new AttendanceRecord();
        attendance.setStudentId(booking.getStudentId());
        attendance.setCourseId(booking.getCourseId());
        attendance.setAttendanceDate(attendanceDate);
        attendance.setStatus(AttendanceStatus.PRESENT);
        attendance.setNote(trimNullable(request.note()) == null ? "家长小程序签到" : trimNullable(request.note()));
        attendanceRecordMapper.insert(attendance);
        careAlertService.refreshStudentAlerts(booking.getStudentId());

        booking.setCheckinStatus(CHECKIN_STATUS_CHECKED_IN);
        booking.setCheckinTime(LocalDateTime.now());
        courseBookingMapper.updateById(booking);

        Student student = studentMapper.selectById(booking.getStudentId());
        Course course = courseMapper.selectById(booking.getCourseId());
        if (student != null && course != null) {
            createMessage(
                    parentAccount.getId(),
                    "签到成功",
                    "学员“" + student.getName() + "”已完成课程“" + course.getName() + "”签到。",
                    "CHECKIN"
            );
        }

        return new ParentCheckinResponse(
                attendance.getId(),
                booking.getId(),
                attendance.getStudentId(),
                student == null ? "-" : student.getName(),
                attendance.getCourseId(),
                course == null ? "-" : course.getName(),
                attendance.getAttendanceDate(),
                attendance.getStatus(),
                attendance.getNote(),
                attendance.getCreatedAt(),
                attendance.getUpdatedAt()
        );
    }

    @Transactional
    public List<ParentFitnessResponse> listFitnessTests(String username, Long studentId) {
        ParentAccount parentAccount = resolveParentAccount(username);
        List<Long> studentIds = listBoundStudentIds(parentAccount.getId());
        if (studentIds.isEmpty()) {
            return List.of();
        }
        if (studentId != null && !studentIds.contains(studentId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "无权查看该学员体测记录");
        }

        var query = Wrappers.<FitnessTestRecord>lambdaQuery();
        if (studentId != null) {
            query.eq(FitnessTestRecord::getStudentId, studentId);
        } else {
            query.in(FitnessTestRecord::getStudentId, studentIds);
        }
        query.orderByDesc(FitnessTestRecord::getTestDate, FitnessTestRecord::getId);

        List<FitnessTestRecord> records = fitnessTestRecordMapper.selectList(query);
        Map<Long, Student> studentMap = studentMap(records.stream().map(FitnessTestRecord::getStudentId).toList());
        return records.stream().map(record -> new ParentFitnessResponse(
                record.getId(),
                record.getStudentId(),
                studentMap.get(record.getStudentId()) == null ? record.getStudentNameSnapshot() : studentMap.get(record.getStudentId()).getName(),
                record.getTestDate(),
                record.getItemName(),
                record.getTestValue(),
                record.getUnit(),
                record.getComment(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        )).toList();
    }

    @Transactional
    public List<ParentMessageResponse> listMessages(String username) {
        ParentAccount parentAccount = resolveParentAccount(username);
        List<InAppMessage> messages = inAppMessageMapper.selectList(
                Wrappers.<InAppMessage>lambdaQuery()
                        .eq(InAppMessage::getParentAccountId, parentAccount.getId())
                        .orderByDesc(InAppMessage::getId)
        );
        return messages.stream().map(this::toMessageResponse).toList();
    }

    @Transactional
    public ParentGrowthOverviewResponse getGrowthOverview(String username, Long studentId) {
        ParentAccount parentAccount = resolveParentAccount(username);
        assertStudentBound(parentAccount.getId(), studentId);

        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new ResourceNotFoundException("student not found: " + studentId);
        }

        List<TrainingRecord> feedback = trainingRecordMapper.selectList(
                Wrappers.<TrainingRecord>lambdaQuery()
                        .eq(TrainingRecord::getStudentId, studentId)
                        .orderByDesc(TrainingRecord::getTrainingDate, TrainingRecord::getId)
                        .last("limit 5")
        );
        StageEvaluation latestEvaluation = stageEvaluationMapper.selectOne(
                Wrappers.<StageEvaluation>lambdaQuery()
                        .eq(StageEvaluation::getStudentId, studentId)
                        .orderByDesc(StageEvaluation::getPeriodEnd, StageEvaluation::getId)
                        .last("limit 1")
        );

        return new ParentGrowthOverviewResponse(
                student.getId(),
                student.getName(),
                student.getGoalFocus(),
                student.getTrainingTags(),
                student.getRiskNotes(),
                student.getGoalStartDate(),
                student.getGoalEndDate(),
                feedback.stream().map(this::toTrainingFeedbackItem).toList(),
                latestEvaluation == null ? null : toGrowthEvaluation(latestEvaluation)
        );
    }

    @Transactional
    public ParentMessageResponse markMessageRead(String username, Long messageId) {
        ParentAccount parentAccount = resolveParentAccount(username);
        InAppMessage message = inAppMessageMapper.selectOne(
                Wrappers.<InAppMessage>lambdaQuery()
                        .eq(InAppMessage::getId, messageId)
                        .eq(InAppMessage::getParentAccountId, parentAccount.getId())
        );
        if (message == null) {
            throw new ResourceNotFoundException("message not found: " + messageId);
        }
        if (!Integer.valueOf(1).equals(message.getIsRead())) {
            message.setIsRead(1);
            message.setReadAt(LocalDateTime.now());
            inAppMessageMapper.updateById(message);
        }
        return toMessageResponse(message);
    }

    private ParentAccount resolveParentAccount(String username) {
        if (!StringUtils.hasText(username)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "未登录或登录状态已失效");
        }
        String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);
        UserAccount userAccount = userAccountMapper.selectOne(
                Wrappers.<UserAccount>lambdaQuery().eq(UserAccount::getUsername, normalizedUsername)
        );
        if (userAccount == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "登录账号不存在");
        }
        if (!ROLE_PARENT.equalsIgnoreCase(userAccount.getRole())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "当前账号不是家长角色");
        }

        ParentAccount parentAccount = parentAccountMapper.selectOne(
                Wrappers.<ParentAccount>lambdaQuery().eq(ParentAccount::getUserAccountId, userAccount.getId())
        );
        if (parentAccount == null) {
            parentAccount = new ParentAccount();
            parentAccount.setUserAccountId(userAccount.getId());
            parentAccount.setDisplayName("家长-" + userAccount.getUsername());
            parentAccount.setPhone(extractPhoneFromUsername(userAccount.getUsername()));
            parentAccount.setStatus(STATUS_ACTIVE);
            parentAccountMapper.insert(parentAccount);
        }

        ensureDefaultStudentBinding(parentAccount, normalizedUsername);
        return parentAccount;
    }

    private void ensureDefaultStudentBinding(ParentAccount parentAccount, String username) {
        Long relationCount = parentStudentRelationMapper.selectCount(
                Wrappers.<ParentStudentRelation>lambdaQuery()
                        .eq(ParentStudentRelation::getParentAccountId, parentAccount.getId())
        );
        if (relationCount != null && relationCount > 0) {
            return;
        }

        List<Student> candidates = Collections.emptyList();
        if (StringUtils.hasText(parentAccount.getPhone())) {
            candidates = studentMapper.selectList(
                    Wrappers.<Student>lambdaQuery()
                            .eq(Student::getGuardianPhone, parentAccount.getPhone())
                            .eq(Student::getStatus, StudentStatus.ACTIVE)
                            .orderByAsc(Student::getId)
            );
        }
        if (candidates.isEmpty() && "parent".equals(username)) {
            candidates = studentMapper.selectList(
                    Wrappers.<Student>lambdaQuery()
                            .eq(Student::getStatus, StudentStatus.ACTIVE)
                            .orderByAsc(Student::getId)
            );
        }
        for (Student student : candidates) {
            ParentStudentRelation relation = new ParentStudentRelation();
            relation.setParentAccountId(parentAccount.getId());
            relation.setStudentId(student.getId());
            parentStudentRelationMapper.insert(relation);
        }
    }

    private List<Long> listBoundStudentIds(Long parentAccountId) {
        List<ParentStudentRelation> relations = parentStudentRelationMapper.selectList(
                Wrappers.<ParentStudentRelation>lambdaQuery()
                        .eq(ParentStudentRelation::getParentAccountId, parentAccountId)
                        .orderByAsc(ParentStudentRelation::getId)
        );
        return relations.stream()
                .map(ParentStudentRelation::getStudentId)
                .distinct()
                .toList();
    }

    private List<Student> listBoundStudents(Long parentAccountId) {
        List<Long> studentIds = listBoundStudentIds(parentAccountId);
        if (studentIds.isEmpty()) {
            return List.of();
        }
        return studentMapper.selectList(
                Wrappers.<Student>lambdaQuery()
                        .in(Student::getId, studentIds)
                        .orderByAsc(Student::getId)
        );
    }

    private void assertStudentBound(Long parentAccountId, Long studentId) {
        Long count = parentStudentRelationMapper.selectCount(
                Wrappers.<ParentStudentRelation>lambdaQuery()
                        .eq(ParentStudentRelation::getParentAccountId, parentAccountId)
                        .eq(ParentStudentRelation::getStudentId, studentId)
        );
        if (count == null || count == 0) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "无权操作该学员");
        }
    }

    private long countBooked(Long courseId) {
        Long count = courseBookingMapper.selectCount(
                Wrappers.<CourseBooking>lambdaQuery()
                        .eq(CourseBooking::getCourseId, courseId)
                        .eq(CourseBooking::getBookingStatus, BOOKING_STATUS_BOOKED)
        );
        return count == null ? 0L : count;
    }

    private Map<Long, Student> studentMap(Collection<Long> studentIds) {
        Set<Long> ids = studentIds.stream().filter(id -> id != null && id > 0).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Student> students = studentMapper.selectBatchIds(ids);
        Map<Long, Student> map = new HashMap<>();
        for (Student student : students) {
            map.put(student.getId(), student);
        }
        return map;
    }

    private Map<Long, Course> courseMap(Collection<Long> courseIds) {
        Set<Long> ids = courseIds.stream().filter(id -> id != null && id > 0).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Course> courses = courseMapper.selectBatchIds(ids);
        Map<Long, Course> map = new HashMap<>();
        for (Course course : courses) {
            map.put(course.getId(), course);
        }
        return map;
    }

    private ParentBookingResponse toBookingResponse(CourseBooking booking, Map<Long, Student> studentMap, Map<Long, Course> courseMap) {
        Student student = studentMap.get(booking.getStudentId());
        Course course = courseMap.get(booking.getCourseId());
        return new ParentBookingResponse(
                booking.getId(),
                booking.getStudentId(),
                student == null ? "-" : student.getName(),
                booking.getCourseId(),
                course == null ? "-" : course.getName(),
                booking.getBookingStatus(),
                booking.getCourseCapacity(),
                booking.getBookingRemark(),
                booking.getCheckinStatus(),
                booking.getCheckinTime(),
                booking.getCreatedAt(),
                booking.getUpdatedAt()
        );
    }

    private ParentChildResponse toChildResponse(Student student) {
        return new ParentChildResponse(
                student.getId(),
                student.getStudentNo(),
                student.getName(),
                student.getGender(),
                student.getBirthDate(),
                student.getGuardianName(),
                student.getGuardianPhone(),
                student.getStatus(),
                student.getCreatedAt(),
                student.getUpdatedAt()
        );
    }

    private ParentMessageResponse toMessageResponse(InAppMessage message) {
        return new ParentMessageResponse(
                message.getId(),
                message.getTitle(),
                message.getContent(),
                message.getMsgType(),
                Integer.valueOf(1).equals(message.getIsRead()),
                message.getReadAt(),
                message.getCreatedAt()
        );
    }

    private ParentGrowthOverviewResponse.TrainingFeedbackItem toTrainingFeedbackItem(TrainingRecord record) {
        return new ParentGrowthOverviewResponse.TrainingFeedbackItem(
                record.getId(),
                record.getTrainingDate(),
                record.getTrainingContent(),
                record.getHighlightNote(),
                record.getImprovementNote(),
                record.getParentAction(),
                record.getNextStepSuggestion(),
                record.getAiSummary(),
                record.getParentReadAt()
        );
    }

    private ParentGrowthOverviewResponse.GrowthEvaluation toGrowthEvaluation(StageEvaluation evaluation) {
        return new ParentGrowthOverviewResponse.GrowthEvaluation(
                evaluation.getCycleName(),
                evaluation.getAttendanceRate() == null ? 0.0 : evaluation.getAttendanceRate().doubleValue(),
                evaluation.getFitnessSummary(),
                evaluation.getCoachEvaluation(),
                evaluation.getNextStagePlan(),
                evaluation.getParentReport()
        );
    }

    private String trimNullable(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.trim();
    }

    private String extractPhoneFromUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        String source = username.trim().toLowerCase(Locale.ROOT);
        if (source.startsWith("parent_")) {
            String candidate = source.substring("parent_".length());
            if (candidate.matches("\\d{11}")) {
                return candidate;
            }
        }
        return null;
    }

    private CourseBooking getBookingByParent(Long parentAccountId, Long bookingId) {
        CourseBooking booking = courseBookingMapper.selectOne(
                Wrappers.<CourseBooking>lambdaQuery()
                        .eq(CourseBooking::getId, bookingId)
                        .eq(CourseBooking::getParentAccountId, parentAccountId)
        );
        if (booking == null) {
            throw new ResourceNotFoundException("booking not found: " + bookingId);
        }
        return booking;
    }

    private Map<String, Long> bookingIdMap(Long parentAccountId, List<AttendanceRecord> attendanceRecords) {
        if (attendanceRecords.isEmpty()) {
            return Map.of();
        }
        Set<Long> studentIds = attendanceRecords.stream().map(AttendanceRecord::getStudentId).collect(Collectors.toSet());
        Set<Long> courseIds = attendanceRecords.stream().map(AttendanceRecord::getCourseId).collect(Collectors.toSet());
        List<CourseBooking> bookings = courseBookingMapper.selectList(
                Wrappers.<CourseBooking>lambdaQuery()
                        .eq(CourseBooking::getParentAccountId, parentAccountId)
                        .in(CourseBooking::getStudentId, studentIds)
                        .in(CourseBooking::getCourseId, courseIds)
        );

        Map<String, Long> map = new HashMap<>();
        for (CourseBooking booking : bookings) {
            map.put(buildAttendKey(booking.getStudentId(), booking.getCourseId(), null), booking.getId());
        }
        return map;
    }

    private String buildAttendKey(Long studentId, Long courseId, LocalDate ignoredDate) {
        return studentId + ":" + courseId;
    }

    private void createMessage(Long parentAccountId, String title, String content, String msgType) {
        InAppMessage message = new InAppMessage();
        message.setParentAccountId(parentAccountId);
        message.setTitle(title);
        message.setContent(content);
        message.setMsgType(msgType);
        message.setIsRead(0);
        inAppMessageMapper.insert(message);
    }
}
