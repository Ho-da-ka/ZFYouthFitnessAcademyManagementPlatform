package com.shuzi.managementplatform.common.api;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * Standard pagination response model converted from MyBatis-Plus page objects.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    /**
     * Convert MyBatis-Plus 1-based page metadata to frontend-friendly 0-based index.
     */
    public static <T> PageResponse<T> from(IPage<T> page) {
        return new PageResponse<>(
                page.getRecords(),
                (int) (page.getCurrent() - 1),
                (int) page.getSize(),
                page.getTotal(),
                (int) page.getPages()
        );
    }
}
