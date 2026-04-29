package com.gavin.shipping.common;

import java.util.List;

public record PageResult<T>(
        List<T> records,
        int page,
        int pageSize,
        long total,
        long totalPages
) {

    public static <T> PageResult<T> of(List<T> records, int page, int pageSize, long total) {
        long totalPages = total == 0 ? 0 : (long) Math.ceil((double) total / pageSize);
        return new PageResult<>(records, page, pageSize, total, totalPages);
    }
}
