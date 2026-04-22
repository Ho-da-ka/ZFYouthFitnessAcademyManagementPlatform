package com.shuzi.managementplatform.domain.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shuzi.managementplatform.domain.entity.ParentAccount;
import com.shuzi.managementplatform.domain.entity.ParentStudentRelation;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.entity.UserAccount;
import com.shuzi.managementplatform.domain.enums.ParentBindingType;
import com.shuzi.managementplatform.domain.mapper.ParentAccountMapper;
import com.shuzi.managementplatform.domain.mapper.ParentStudentRelationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ParentAccountSyncService {

    private static final String STATUS_ACTIVE = "ACTIVE";

    private final ParentAccountMapper parentAccountMapper;
    private final ParentStudentRelationMapper parentStudentRelationMapper;
    private final UserAccountService userAccountService;

    public ParentAccountSyncService(
            ParentAccountMapper parentAccountMapper,
            ParentStudentRelationMapper parentStudentRelationMapper,
            UserAccountService userAccountService
    ) {
        this.parentAccountMapper = parentAccountMapper;
        this.parentStudentRelationMapper = parentStudentRelationMapper;
        this.userAccountService = userAccountService;
    }

    @Transactional
    public void syncStudentGuardianBinding(Student student, String previousGuardianPhone) {
        if (student == null || student.getId() == null) {
            return;
        }
        String currentPhone = normalizePhone(student.getGuardianPhone());
        String oldPhone = normalizePhone(previousGuardianPhone);

        if (StringUtils.hasText(oldPhone) && !oldPhone.equals(currentPhone)) {
            removeAutomaticBinding(student.getId(), oldPhone);
        }
        if (!StringUtils.hasText(currentPhone)) {
            return;
        }

        ParentAccount parentAccount = ensureParentAccount(currentPhone, student.getGuardianName());
        ParentStudentRelation relation = parentStudentRelationMapper.selectOne(
                Wrappers.<ParentStudentRelation>lambdaQuery()
                        .eq(ParentStudentRelation::getParentAccountId, parentAccount.getId())
                        .eq(ParentStudentRelation::getStudentId, student.getId())
        );
        if (relation == null) {
            ParentStudentRelation created = new ParentStudentRelation();
            created.setParentAccountId(parentAccount.getId());
            created.setStudentId(student.getId());
            created.setBindingType(ParentBindingType.AUTO);
            parentStudentRelationMapper.insert(created);
            return;
        }

        ParentBindingType merged = relation.getBindingType().mergeAutomatic();
        if (merged != relation.getBindingType()) {
            relation.setBindingType(merged);
            parentStudentRelationMapper.updateById(relation);
        }
    }

    private ParentAccount ensureParentAccount(String phone, String displayName) {
        ParentAccount account = parentAccountMapper.selectOne(
                Wrappers.<ParentAccount>lambdaQuery().eq(ParentAccount::getPhone, phone)
        );
        if (account != null) {
            if (StringUtils.hasText(displayName) && !displayName.trim().equals(account.getDisplayName())) {
                account.setDisplayName(displayName.trim());
                parentAccountMapper.updateById(account);
            }
            return account;
        }

        UserAccount login = userAccountService.upsertParentAccount(phone, displayName);
        ParentAccount created = new ParentAccount();
        created.setUserAccountId(login.getId());
        created.setDisplayName(StringUtils.hasText(displayName) ? displayName.trim() : phone);
        created.setPhone(phone);
        created.setStatus(STATUS_ACTIVE);
        parentAccountMapper.insert(created);
        return created;
    }

    private void removeAutomaticBinding(Long studentId, String oldPhone) {
        ParentAccount parentAccount = parentAccountMapper.selectOne(
                Wrappers.<ParentAccount>lambdaQuery().eq(ParentAccount::getPhone, oldPhone)
        );
        if (parentAccount == null) {
            return;
        }
        ParentStudentRelation relation = parentStudentRelationMapper.selectOne(
                Wrappers.<ParentStudentRelation>lambdaQuery()
                        .eq(ParentStudentRelation::getParentAccountId, parentAccount.getId())
                        .eq(ParentStudentRelation::getStudentId, studentId)
        );
        if (relation == null || relation.getBindingType() == null) {
            return;
        }
        ParentBindingType downgraded = relation.getBindingType().removeAutomatic();
        if (downgraded == relation.getBindingType()) {
            return;
        }
        if (downgraded == null) {
            parentStudentRelationMapper.deleteById(relation.getId());
            return;
        }
        relation.setBindingType(downgraded);
        parentStudentRelationMapper.updateById(relation);
    }

    private String normalizePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return null;
        }
        return phone.trim().replace(" ", "");
    }
}
