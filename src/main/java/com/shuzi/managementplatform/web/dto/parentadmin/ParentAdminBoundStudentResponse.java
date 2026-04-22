package com.shuzi.managementplatform.web.dto.parentadmin;

public record ParentAdminBoundStudentResponse(
        Long studentId,
        String studentNo,
        String studentName,
        String guardianPhone,
        String bindingType
) {
}
