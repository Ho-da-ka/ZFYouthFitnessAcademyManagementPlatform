package com.shuzi.managementplatform.web.dto.student;

import java.util.List;

public record StudentImportResult(
        int successCount,
        int failCount,
        List<String> errors
) {
}

