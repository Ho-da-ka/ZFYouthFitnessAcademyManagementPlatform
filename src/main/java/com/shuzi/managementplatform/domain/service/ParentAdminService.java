package com.shuzi.managementplatform.domain.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuzi.managementplatform.common.exception.BusinessException;
import com.shuzi.managementplatform.common.exception.ResourceNotFoundException;
import com.shuzi.managementplatform.domain.entity.ParentAccount;
import com.shuzi.managementplatform.domain.entity.ParentStudentRelation;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.entity.UserAccount;
import com.shuzi.managementplatform.domain.enums.ParentBindingType;
import com.shuzi.managementplatform.domain.mapper.ParentAccountMapper;
import com.shuzi.managementplatform.domain.mapper.ParentStudentRelationMapper;
import com.shuzi.managementplatform.domain.mapper.StudentMapper;
import com.shuzi.managementplatform.domain.mapper.UserAccountMapper;
import com.shuzi.managementplatform.web.dto.parentadmin.ParentAdminBoundStudentResponse;
import com.shuzi.managementplatform.web.dto.parentadmin.ParentAdminDetailResponse;
import com.shuzi.managementplatform.web.dto.parentadmin.ParentAdminListItemResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ParentAdminService {

    private static final String AUTO_UNBIND_MESSAGE = "该关系由学员监护人手机号自动生成，请修改学员档案中的监护人手机号后再解除";

    private final ParentAccountMapper parentAccountMapper;
    private final ParentStudentRelationMapper parentStudentRelationMapper;
    private final StudentMapper studentMapper;
    private final UserAccountMapper userAccountMapper;

    public ParentAdminService(
            ParentAccountMapper parentAccountMapper,
            ParentStudentRelationMapper parentStudentRelationMapper,
            StudentMapper studentMapper,
            UserAccountMapper userAccountMapper
    ) {
        this.parentAccountMapper = parentAccountMapper;
        this.parentStudentRelationMapper = parentStudentRelationMapper;
        this.studentMapper = studentMapper;
        this.userAccountMapper = userAccountMapper;
    }

    @Transactional(readOnly = true)
    public IPage<ParentAdminListItemResponse> page(String keyword, String studentName, int page, int size) {
        List<ParentAccount> accounts = parentAccountMapper.selectList(
                Wrappers.<ParentAccount>lambdaQuery().orderByDesc(ParentAccount::getId)
        );
        if (accounts.isEmpty()) {
            return emptyPage(page, size);
        }

        Map<Long, UserAccount> loginMap = buildLoginMap(accounts);
        Map<Long, List<ParentStudentRelation>> relationMap = buildRelationMap(
                accounts.stream().map(ParentAccount::getId).toList()
        );
        Map<Long, Student> studentMap = buildStudentMap(relationMap.values().stream().flatMap(List::stream).toList());

        List<ParentAdminListItemResponse> rows = accounts.stream()
                .filter(account -> matchesKeyword(account, loginMap.get(account.getUserAccountId()), keyword))
                .filter(account -> matchesStudentName(relationMap.getOrDefault(account.getId(), List.of()), studentMap, studentName))
                .map(account -> toListItem(account, loginMap.get(account.getUserAccountId()), relationMap.getOrDefault(account.getId(), List.of()), studentMap))
                .toList();

        Page<ParentAdminListItemResponse> responsePage = new Page<>(page + 1L, size, rows.size());
        if (rows.isEmpty()) {
            responsePage.setRecords(List.of());
            return responsePage;
        }

        int safeSize = Math.max(size, 1);
        int fromIndex = Math.min(page * safeSize, rows.size());
        int toIndex = Math.min(fromIndex + safeSize, rows.size());
        responsePage.setRecords(rows.subList(fromIndex, toIndex));
        return responsePage;
    }

    @Transactional(readOnly = true)
    public ParentAdminDetailResponse getDetail(Long id) {
        ParentAccount account = parentAccountMapper.selectById(id);
        if (account == null) {
            throw new ResourceNotFoundException("parent account not found: " + id);
        }

        UserAccount login = account.getUserAccountId() == null ? null : userAccountMapper.selectById(account.getUserAccountId());
        List<ParentStudentRelation> relations = parentStudentRelationMapper.selectList(
                Wrappers.<ParentStudentRelation>lambdaQuery()
                        .eq(ParentStudentRelation::getParentAccountId, id)
                        .orderByAsc(ParentStudentRelation::getId)
        );
        Map<Long, Student> studentMap = buildStudentMap(relations);
        List<ParentAdminBoundStudentResponse> students = relations.stream()
                .map(relation -> toBoundStudent(relation, studentMap.get(relation.getStudentId())))
                .toList();

        return new ParentAdminDetailResponse(
                account.getId(),
                account.getDisplayName(),
                account.getPhone(),
                login == null ? null : login.getUsername(),
                login == null ? null : login.getLastLoginAt(),
                account.getUpdatedAt(),
                students
        );
    }

    @Transactional
    public void addManualBinding(Long parentId, Long studentId) {
        requireParent(parentId);
        requireStudent(studentId);

        ParentStudentRelation relation = parentStudentRelationMapper.selectOne(
                Wrappers.<ParentStudentRelation>lambdaQuery()
                        .eq(ParentStudentRelation::getParentAccountId, parentId)
                        .eq(ParentStudentRelation::getStudentId, studentId)
        );
        if (relation == null) {
            ParentStudentRelation created = new ParentStudentRelation();
            created.setParentAccountId(parentId);
            created.setStudentId(studentId);
            created.setBindingType(ParentBindingType.MANUAL);
            parentStudentRelationMapper.insert(created);
            return;
        }

        ParentBindingType merged = relation.getBindingType().mergeManual();
        if (merged != relation.getBindingType()) {
            relation.setBindingType(merged);
            parentStudentRelationMapper.updateById(relation);
        }
    }

    @Transactional
    public void removeManualBinding(Long parentId, Long studentId) {
        ParentStudentRelation relation = parentStudentRelationMapper.selectOne(
                Wrappers.<ParentStudentRelation>lambdaQuery()
                        .eq(ParentStudentRelation::getParentAccountId, parentId)
                        .eq(ParentStudentRelation::getStudentId, studentId)
        );
        if (relation == null) {
            throw new ResourceNotFoundException("parent binding not found");
        }
        if (relation.getBindingType() == ParentBindingType.AUTO) {
            throw new BusinessException(HttpStatus.CONFLICT, AUTO_UNBIND_MESSAGE);
        }

        ParentBindingType downgraded = relation.getBindingType().removeManual();
        if (downgraded == null) {
            parentStudentRelationMapper.deleteById(relation.getId());
            return;
        }
        if (downgraded != relation.getBindingType()) {
            relation.setBindingType(downgraded);
            parentStudentRelationMapper.updateById(relation);
        }
    }

    private Page<ParentAdminListItemResponse> emptyPage(int page, int size) {
        Page<ParentAdminListItemResponse> responsePage = new Page<>(page + 1L, size, 0);
        responsePage.setRecords(List.of());
        return responsePage;
    }

    private Map<Long, UserAccount> buildLoginMap(List<ParentAccount> accounts) {
        List<Long> loginIds = accounts.stream()
                .map(ParentAccount::getUserAccountId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (loginIds.isEmpty()) {
            return Map.of();
        }
        return userAccountMapper.selectBatchIds(loginIds).stream()
                .collect(Collectors.toMap(UserAccount::getId, login -> login));
    }

    private Map<Long, List<ParentStudentRelation>> buildRelationMap(List<Long> parentIds) {
        if (parentIds.isEmpty()) {
            return Map.of();
        }
        return parentStudentRelationMapper.selectList(
                Wrappers.<ParentStudentRelation>lambdaQuery()
                        .in(ParentStudentRelation::getParentAccountId, parentIds)
                        .orderByAsc(ParentStudentRelation::getId)
        ).stream().collect(Collectors.groupingBy(
                ParentStudentRelation::getParentAccountId,
                LinkedHashMap::new,
                Collectors.toList()
        ));
    }

    private Map<Long, Student> buildStudentMap(List<ParentStudentRelation> relations) {
        List<Long> studentIds = relations.stream()
                .map(ParentStudentRelation::getStudentId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (studentIds.isEmpty()) {
            return Map.of();
        }
        return studentMapper.selectBatchIds(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, student -> student));
    }

    private ParentAdminListItemResponse toListItem(
            ParentAccount account,
            UserAccount login,
            List<ParentStudentRelation> relations,
            Map<Long, Student> studentMap
    ) {
        List<String> studentNames = relations.stream()
                .map(ParentStudentRelation::getStudentId)
                .map(studentMap::get)
                .filter(student -> student != null && StringUtils.hasText(student.getName()))
                .map(Student::getName)
                .distinct()
                .toList();

        return new ParentAdminListItemResponse(
                account.getId(),
                account.getDisplayName(),
                account.getPhone(),
                login == null ? null : login.getUsername(),
                login == null ? null : login.getLastLoginAt(),
                account.getUpdatedAt(),
                studentNames.size(),
                studentNames
        );
    }

    private ParentAdminBoundStudentResponse toBoundStudent(ParentStudentRelation relation, Student student) {
        return new ParentAdminBoundStudentResponse(
                relation.getStudentId(),
                student == null ? null : student.getStudentNo(),
                student == null ? null : student.getName(),
                student == null ? null : student.getGuardianPhone(),
                relation.getBindingType() == null ? null : relation.getBindingType().name()
        );
    }

    private boolean matchesKeyword(ParentAccount account, UserAccount login, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
        return containsIgnoreCase(account.getDisplayName(), normalizedKeyword)
                || containsIgnoreCase(account.getPhone(), normalizedKeyword)
                || containsIgnoreCase(login == null ? null : login.getUsername(), normalizedKeyword);
    }

    private boolean matchesStudentName(List<ParentStudentRelation> relations, Map<Long, Student> studentMap, String studentName) {
        if (!StringUtils.hasText(studentName)) {
            return true;
        }
        String normalizedStudentName = studentName.trim().toLowerCase(Locale.ROOT);
        for (ParentStudentRelation relation : relations) {
            Student student = studentMap.get(relation.getStudentId());
            if (student != null && containsIgnoreCase(student.getName(), normalizedStudentName)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return StringUtils.hasText(source) && source.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private ParentAccount requireParent(Long parentId) {
        ParentAccount account = parentAccountMapper.selectById(parentId);
        if (account == null) {
            throw new ResourceNotFoundException("parent account not found: " + parentId);
        }
        return account;
    }

    private Student requireStudent(Long studentId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new ResourceNotFoundException("student not found: " + studentId);
        }
        return student;
    }
}
