package com.shuzi.managementplatform.web.dto.parent;

import java.time.LocalDateTime;

public record ParentMessageResponse(
        Long id,
        String title,
        String content,
        String msgType,
        boolean read,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
}

