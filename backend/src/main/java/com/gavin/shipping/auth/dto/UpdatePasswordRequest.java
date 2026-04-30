package com.gavin.shipping.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePasswordRequest(
        @NotBlank(message = "旧密码不能为空")
        @Size(min = 6, max = 30, message = "旧密码长度应为6-30")
        String oldPassword,

        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 30, message = "新密码长度应为6-30")
        String newPassword
) {
}
