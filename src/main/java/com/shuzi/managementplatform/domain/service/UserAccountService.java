package com.shuzi.managementplatform.domain.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shuzi.managementplatform.common.exception.BusinessException;
import com.shuzi.managementplatform.domain.entity.Coach;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.entity.UserAccount;
import com.shuzi.managementplatform.domain.enums.CoachStatus;
import com.shuzi.managementplatform.domain.enums.StudentStatus;
import com.shuzi.managementplatform.domain.mapper.CoachMapper;
import com.shuzi.managementplatform.domain.mapper.StudentMapper;
import com.shuzi.managementplatform.domain.mapper.UserAccountMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * User account maintenance service for auth user lifecycle and profile binding.
 */
@Service
public class UserAccountService {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_DISABLED = "DISABLED";

    private final UserAccountMapper userAccountMapper;
    private final CoachMapper coachMapper;
    private final StudentMapper studentMapper;
    private final PasswordEncoder passwordEncoder;
    private static final Map<String, String> SYSTEM_INITIAL_PASSWORDS = Map.of(
            "admin", "Admin@123",
            "coach", "Coach@123",
            "student", "Student@123",
            "parent", "Parent@123"
    );

    public UserAccountService(
            UserAccountMapper userAccountMapper,
            CoachMapper coachMapper,
            StudentMapper studentMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.userAccountMapper = userAccountMapper;
        this.coachMapper = coachMapper;
        this.studentMapper = studentMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void initializeDefaultAccounts() {
        ensureSystemAccount("admin", "Admin@123", "ADMIN");
        ensureSystemAccount("coach", "Coach@123", "COACH");
        ensureSystemAccount("student", "Student@123", "STUDENT");
        ensureSystemAccount("parent", "Parent@123", "PARENT");
    }

    @Transactional
    public void syncExistingCoachAndStudentAccounts() {
        List<Coach> coaches = coachMapper.selectList(Wrappers.<Coach>lambdaQuery());
        for (Coach coach : coaches) {
            upsertCoachAccount(coach);
        }

        List<Student> students = studentMapper.selectList(Wrappers.<Student>lambdaQuery());
        for (Student student : students) {
            upsertStudentAccount(student);
        }
    }

    @Transactional
    public void upsertCoachAccount(Coach coach) {
        UserAccount account = userAccountMapper.selectOne(
                Wrappers.<UserAccount>lambdaQuery().eq(UserAccount::getCoachId, coach.getId())
        );
        if (account == null) {
            account = new UserAccount();
            account.setCoachId(coach.getId());
            account.setRole("COACH");
            account.setUsername(generateCoachUsername(coach.getCoachCode(), coach.getId()));
            account.setPasswordHash(passwordEncoder.encode(buildCoachInitialPassword(coach.getCoachCode())));
        }

        account.setStatus(toAccountStatus(coach.getStatus()));
        account.setRole("COACH");
        account.setUsername(resolveUniqueUsername(account.getUsername(), account.getId(), "coach_" + coach.getCoachCode()));

        if (account.getId() == null) {
            userAccountMapper.insert(account);
        } else {
            userAccountMapper.updateById(account);
        }
    }

    @Transactional
    public void upsertStudentAccount(Student student) {
        UserAccount account = userAccountMapper.selectOne(
                Wrappers.<UserAccount>lambdaQuery().eq(UserAccount::getStudentId, student.getId())
        );
        if (account == null) {
            account = new UserAccount();
            account.setStudentId(student.getId());
            account.setRole("STUDENT");
            account.setUsername(generateStudentUsername(student.getStudentNo(), student.getId()));
            account.setPasswordHash(passwordEncoder.encode(buildStudentInitialPassword(student.getStudentNo())));
        }

        account.setStatus(toAccountStatus(student.getStatus()));
        account.setRole("STUDENT");
        account.setUsername(resolveUniqueUsername(account.getUsername(), account.getId(), "student_" + student.getStudentNo()));

        if (account.getId() == null) {
            userAccountMapper.insert(account);
        } else {
            userAccountMapper.updateById(account);
        }
    }

    @Transactional
    public void deleteByCoachId(Long coachId) {
        userAccountMapper.delete(Wrappers.<UserAccount>lambdaQuery().eq(UserAccount::getCoachId, coachId));
    }

    @Transactional
    public void deleteByStudentId(Long studentId) {
        userAccountMapper.delete(Wrappers.<UserAccount>lambdaQuery().eq(UserAccount::getStudentId, studentId));
    }

    @Transactional
    public void markLoginSuccess(String username) {
        if (!StringUtils.hasText(username)) {
            return;
        }
        UserAccount account = userAccountMapper.selectOne(
                Wrappers.<UserAccount>lambdaQuery().eq(UserAccount::getUsername, username.trim())
        );
        if (account == null) {
            return;
        }
        account.setLastLoginAt(LocalDateTime.now());
        userAccountMapper.updateById(account);
    }

    @Transactional
    public void changePasswordBySelf(String username, String oldPassword, String newPassword) {
        UserAccount account = getByUsername(username);
        if (!passwordEncoder.matches(oldPassword, account.getPasswordHash())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "旧密码不正确");
        }
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        userAccountMapper.updateById(account);
    }

    @Transactional
    public void adminSetPassword(String username, String newPassword) {
        UserAccount account = getByUsername(username);
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        userAccountMapper.updateById(account);
    }

    @Transactional
    public String adminResetInitialPassword(String username) {
        UserAccount account = getByUsername(username);
        String initialPassword = resolveInitialPassword(account);
        account.setPasswordHash(passwordEncoder.encode(initialPassword));
        userAccountMapper.updateById(account);
        return initialPassword;
    }

    public String buildCoachInitialPassword(String coachCode) {
        return coachCode.trim() + "@123";
    }

    public String buildStudentInitialPassword(String studentNo) {
        return studentNo.trim() + "@123";
    }

    private void ensureSystemAccount(String username, String rawPassword, String role) {
        UserAccount account = userAccountMapper.selectOne(
                Wrappers.<UserAccount>lambdaQuery().eq(UserAccount::getUsername, username)
        );
        if (account == null) {
            UserAccount newAccount = new UserAccount();
            newAccount.setUsername(username);
            newAccount.setPasswordHash(passwordEncoder.encode(rawPassword));
            newAccount.setRole(role);
            newAccount.setStatus(STATUS_ACTIVE);
            userAccountMapper.insert(newAccount);
            return;
        }

        boolean changed = false;
        if (!role.equals(account.getRole())) {
            account.setRole(role);
            changed = true;
        }
        if (!STATUS_ACTIVE.equals(account.getStatus())) {
            account.setStatus(STATUS_ACTIVE);
            changed = true;
        }
        if (changed) {
            userAccountMapper.updateById(account);
        }
    }

    private String toAccountStatus(CoachStatus status) {
        return status == CoachStatus.ACTIVE ? STATUS_ACTIVE : STATUS_DISABLED;
    }

    private String toAccountStatus(StudentStatus status) {
        return status == StudentStatus.ACTIVE ? STATUS_ACTIVE : STATUS_DISABLED;
    }

    private String generateCoachUsername(String coachCode, Long coachId) {
        return resolveUniqueUsername("coach_" + coachCode, null, "coach_" + coachId);
    }

    private String generateStudentUsername(String studentNo, Long studentId) {
        return resolveUniqueUsername("student_" + studentNo, null, "student_" + studentId);
    }

    private String resolveUniqueUsername(String preferred, Long selfId, String fallbackSuffix) {
        String candidate = normalizeUsername(preferred);
        UserAccount conflict = findByUsername(candidate);
        if (conflict == null || (selfId != null && selfId.equals(conflict.getId()))) {
            return candidate;
        }

        String fallback = normalizeUsername(candidate + "_" + fallbackSuffix);
        UserAccount secondConflict = findByUsername(fallback);
        if (secondConflict == null || (selfId != null && selfId.equals(secondConflict.getId()))) {
            return fallback;
        }

        return normalizeUsername(fallback + "_" + System.currentTimeMillis());
    }

    private UserAccount findByUsername(String username) {
        return userAccountMapper.selectOne(
                Wrappers.<UserAccount>lambdaQuery().eq(UserAccount::getUsername, username)
        );
    }

    private UserAccount getByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "username is required");
        }
        UserAccount account = findByUsername(username.trim().toLowerCase(Locale.ROOT));
        if (account == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "user account not found");
        }
        return account;
    }

    private String resolveInitialPassword(UserAccount account) {
        if (account.getCoachId() != null) {
            Coach coach = coachMapper.selectById(account.getCoachId());
            if (coach == null || !StringUtils.hasText(coach.getCoachCode())) {
                throw new BusinessException(HttpStatus.CONFLICT, "关联教练档案不存在，无法重置初始密码");
            }
            return buildCoachInitialPassword(coach.getCoachCode());
        }
        if (account.getStudentId() != null) {
            Student student = studentMapper.selectById(account.getStudentId());
            if (student == null || !StringUtils.hasText(student.getStudentNo())) {
                throw new BusinessException(HttpStatus.CONFLICT, "关联学员档案不存在，无法重置初始密码");
            }
            return buildStudentInitialPassword(student.getStudentNo());
        }
        String systemPassword = SYSTEM_INITIAL_PASSWORDS.get(account.getUsername());
        if (systemPassword != null) {
            return systemPassword;
        }
        return "Reset@" + (100000 + ThreadLocalRandom.current().nextInt(900000));
    }

    private String normalizeUsername(String source) {
        return source == null ? "" : source.trim().toLowerCase(Locale.ROOT).replace(" ", "");
    }
}
