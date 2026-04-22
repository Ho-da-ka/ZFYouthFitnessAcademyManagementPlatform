package com.shuzi.managementplatform.domain.service;

import com.shuzi.managementplatform.common.exception.BusinessException;
import com.shuzi.managementplatform.domain.entity.ParentAccount;
import com.shuzi.managementplatform.domain.entity.ParentStudentRelation;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.entity.UserAccount;
import com.shuzi.managementplatform.domain.enums.ParentBindingType;
import com.shuzi.managementplatform.domain.mapper.ParentAccountMapper;
import com.shuzi.managementplatform.domain.mapper.ParentStudentRelationMapper;
import com.shuzi.managementplatform.domain.mapper.StudentMapper;
import com.shuzi.managementplatform.domain.mapper.UserAccountMapper;
import com.shuzi.managementplatform.web.dto.parentadmin.ParentAdminDetailResponse;
import com.shuzi.managementplatform.web.dto.parentadmin.ParentAdminListItemResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParentAdminServiceTest {

    @Mock
    private ParentAccountMapper parentAccountMapper;
    @Mock
    private ParentStudentRelationMapper parentStudentRelationMapper;
    @Mock
    private StudentMapper studentMapper;
    @Mock
    private UserAccountMapper userAccountMapper;

    @InjectMocks
    private ParentAdminService parentAdminService;

    @Test
    void pageShouldExposeLoginAndBoundStudentSummary() {
        ParentAccount account = new ParentAccount();
        ReflectionTestUtils.setField(account, "id", 31L);
        ReflectionTestUtils.setField(account, "updatedAt", LocalDateTime.of(2026, 4, 22, 9, 30));
        account.setUserAccountId(41L);
        account.setDisplayName("Parent Li");
        account.setPhone("13800138000");

        UserAccount login = new UserAccount();
        ReflectionTestUtils.setField(login, "id", 41L);
        login.setUsername("13800138000");
        login.setLastLoginAt(LocalDateTime.of(2026, 4, 22, 8, 0));

        ParentStudentRelation relation = new ParentStudentRelation();
        relation.setParentAccountId(31L);
        relation.setStudentId(51L);
        relation.setBindingType(ParentBindingType.AUTO);

        Student student = new Student();
        ReflectionTestUtils.setField(student, "id", 51L);
        student.setName("Li Lei");

        when(parentAccountMapper.selectList(any())).thenReturn(List.of(account));
        when(parentStudentRelationMapper.selectList(any())).thenReturn(List.of(relation));
        when(studentMapper.selectBatchIds(List.of(51L))).thenReturn(List.of(student));
        when(userAccountMapper.selectBatchIds(List.of(41L))).thenReturn(List.of(login));

        IPage<ParentAdminListItemResponse> page = parentAdminService.page("13800138000", null, 0, 10);

        Assertions.assertEquals(1, page.getRecords().size());
        ParentAdminListItemResponse row = page.getRecords().get(0);
        Assertions.assertEquals("Parent Li", row.displayName());
        Assertions.assertEquals("13800138000", row.username());
        Assertions.assertEquals(1, row.studentCount());
        Assertions.assertEquals(List.of("Li Lei"), row.studentNames());
    }

    @Test
    void getDetailShouldExposeBindingTypes() {
        ParentAccount account = new ParentAccount();
        ReflectionTestUtils.setField(account, "id", 32L);
        ReflectionTestUtils.setField(account, "updatedAt", LocalDateTime.of(2026, 4, 22, 10, 0));
        account.setUserAccountId(42L);
        account.setDisplayName("Parent Wang");
        account.setPhone("13900139000");

        UserAccount login = new UserAccount();
        ReflectionTestUtils.setField(login, "id", 42L);
        login.setUsername("13900139000");
        login.setLastLoginAt(LocalDateTime.of(2026, 4, 22, 7, 45));

        ParentStudentRelation relation = new ParentStudentRelation();
        relation.setParentAccountId(32L);
        relation.setStudentId(52L);
        relation.setBindingType(ParentBindingType.AUTO_MANUAL);

        Student student = new Student();
        ReflectionTestUtils.setField(student, "id", 52L);
        student.setStudentNo("S1052");
        student.setName("Wang Xiao");
        student.setGuardianPhone("13900139000");

        when(parentAccountMapper.selectById(32L)).thenReturn(account);
        when(userAccountMapper.selectById(42L)).thenReturn(login);
        when(parentStudentRelationMapper.selectList(any())).thenReturn(List.of(relation));
        when(studentMapper.selectBatchIds(List.of(52L))).thenReturn(List.of(student));

        ParentAdminDetailResponse detail = parentAdminService.getDetail(32L);

        Assertions.assertEquals("Parent Wang", detail.displayName());
        Assertions.assertEquals(1, detail.students().size());
        Assertions.assertEquals("AUTO_MANUAL", detail.students().get(0).bindingType());
        Assertions.assertEquals("Wang Xiao", detail.students().get(0).studentName());
    }

    @Test
    void addManualBindingShouldMergeWithAutoRelation() {
        ParentAccount account = new ParentAccount();
        ReflectionTestUtils.setField(account, "id", 31L);

        Student student = new Student();
        ReflectionTestUtils.setField(student, "id", 51L);

        ParentStudentRelation relation = new ParentStudentRelation();
        ReflectionTestUtils.setField(relation, "id", 61L);
        relation.setParentAccountId(31L);
        relation.setStudentId(51L);
        relation.setBindingType(ParentBindingType.AUTO);

        when(parentAccountMapper.selectById(31L)).thenReturn(account);
        when(studentMapper.selectById(51L)).thenReturn(student);
        when(parentStudentRelationMapper.selectOne(any())).thenReturn(relation);

        parentAdminService.addManualBinding(31L, 51L);

        verify(parentStudentRelationMapper).updateById(argThat((ParentStudentRelation updated) ->
                updated.getId().equals(61L) && updated.getBindingType() == ParentBindingType.AUTO_MANUAL
        ));
    }

    @Test
    void removeManualBindingShouldRejectPureAutoRelation() {
        ParentStudentRelation relation = new ParentStudentRelation();
        ReflectionTestUtils.setField(relation, "id", 71L);
        relation.setParentAccountId(31L);
        relation.setStudentId(51L);
        relation.setBindingType(ParentBindingType.AUTO);

        when(parentStudentRelationMapper.selectOne(any())).thenReturn(relation);

        BusinessException error = Assertions.assertThrows(BusinessException.class,
                () -> parentAdminService.removeManualBinding(31L, 51L));

        Assertions.assertEquals("该关系由学员监护人手机号自动生成，请修改学员档案中的监护人手机号后再解除", error.getMessage());
    }
}
