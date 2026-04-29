package com.gavin.shipping.dictionary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record DictionaryItemResponse(
        Long id,
        String dictType,
        String label,
        String value,
        Integer sort,
        Boolean enabled,
        String remark,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt
) {
}
