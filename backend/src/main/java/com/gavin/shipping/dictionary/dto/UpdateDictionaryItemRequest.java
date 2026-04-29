package com.gavin.shipping.dictionary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDictionaryItemRequest(
        @NotBlank(message = "显示名称不能为空")
        @Size(max = 50, message = "显示名称长度不能超过50")
        String label,

        @NotBlank(message = "字典值不能为空")
        @Size(max = 50, message = "字典值长度不能超过50")
        String value,

        Integer sort,

        Boolean enabled,

        @Size(max = 255, message = "备注长度不能超过255")
        String remark
) {
}
