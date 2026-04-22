package com.shuzi.managementplatform.domain.service;

import com.shuzi.managementplatform.domain.entity.ParentAccount;
import com.shuzi.managementplatform.domain.entity.ParentStudentRelation;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.entity.UserAccount;
import com.shuzi.managementplatform.domain.enums.ParentBindingType;
import com.shuzi.managementplatform.domain.mapper.ParentAccountMapper;
import com.shuzi.managementplatform.domain.mapper.ParentStudentRelationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParentAccountSyncServiceTest {

    @Mock
    private ParentAccountMapper parentAccountMapper;
    @Mock
    private ParentStudentRelationMapper parentStudentRelationMapper;
    @Mock
    private UserAccountService userAccountService;

    @InjectMocks
    private ParentAccountSyncService parentAccountSyncService;

    @Test
    void syncStudentGuardianBindingShouldCreateParentAccountAndAutoRelation() {
        Student student = new Student();
        ReflectionTestUtils.setField(student, "id", 7L);
        student.setGuardianName("Parent Li");
        student.setGuardianPhone("13800138000");

        UserAccount parentLogin = new UserAccount();
        ReflectionTestUtils.setField(parentLogin, "id", 19L);
        parentLogin.setUsername("13800138000");
        parentLogin.setRole("PARENT");

        when(parentAccountMapper.selectOne(any())).thenReturn(null);
        when(userAccountService.upsertParentAccount("13800138000", "Parent Li")).thenReturn(parentLogin);
        when(parentAccountMapper.insert(any(ParentAccount.class))).thenAnswer(invocation -> {
            ParentAccount account = invocation.getArgument(0);
            ReflectionTestUtils.setField(account, "id", 31L);
            return 1;
        });
        when(parentStudentRelationMapper.selectOne(any())).thenReturn(null);

        parentAccountSyncService.syncStudentGuardianBinding(student, null);

        verify(userAccountService).upsertParentAccount("13800138000", "Parent Li");
        verify(parentStudentRelationMapper).insert(argThat((ParentStudentRelation relation) ->
                relation.getParentAccountId().equals(31L)
                        && relation.getStudentId().equals(7L)
                        && relation.getBindingType() == ParentBindingType.AUTO
        ));
    }

    @Test
    void syncStudentGuardianBindingShouldDowngradeOldAutoManualRelationAndCreateNewAutoRelation() {
        Student student = new Student();
        ReflectionTestUtils.setField(student, "id", 8L);
        student.setGuardianName("Parent Wang");
        student.setGuardianPhone("13900139000");

        ParentAccount oldAccount = new ParentAccount();
        ReflectionTestUtils.setField(oldAccount, "id", 41L);
        oldAccount.setPhone("13700137000");

        ParentStudentRelation oldRelation = new ParentStudentRelation();
        ReflectionTestUtils.setField(oldRelation, "id", 51L);
        oldRelation.setParentAccountId(41L);
        oldRelation.setStudentId(8L);
        oldRelation.setBindingType(ParentBindingType.AUTO_MANUAL);

        UserAccount newParentLogin = new UserAccount();
        ReflectionTestUtils.setField(newParentLogin, "id", 61L);
        newParentLogin.setUsername("13900139000");
        newParentLogin.setRole("PARENT");

        when(parentAccountMapper.selectOne(any()))
                .thenReturn(oldAccount)
                .thenReturn(null);
        when(parentStudentRelationMapper.selectOne(any()))
                .thenReturn(oldRelation)
                .thenReturn(null);
        when(userAccountService.upsertParentAccount("13900139000", "Parent Wang")).thenReturn(newParentLogin);
        when(parentAccountMapper.insert(any(ParentAccount.class))).thenAnswer(invocation -> {
            ParentAccount account = invocation.getArgument(0);
            ReflectionTestUtils.setField(account, "id", 71L);
            return 1;
        });

        parentAccountSyncService.syncStudentGuardianBinding(student, "13700137000");

        verify(parentStudentRelationMapper).updateById(argThat((ParentStudentRelation relation) ->
                relation.getId().equals(51L) && relation.getBindingType() == ParentBindingType.MANUAL
        ));
        verify(parentStudentRelationMapper).insert(argThat((ParentStudentRelation relation) ->
                relation.getParentAccountId().equals(71L)
                        && relation.getStudentId().equals(8L)
                        && relation.getBindingType() == ParentBindingType.AUTO
        ));
    }
}
