package com.gavin.shipping.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 30, message = "用户名长度必须为3-30")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 30, message = "密码长度必须为6-30")
        String password
) {
}
