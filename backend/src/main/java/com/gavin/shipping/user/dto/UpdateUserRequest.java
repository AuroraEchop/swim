package com.gavin.shipping.user.dto;

import com.gavin.shipping.domain.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank(message = "真实姓名不能为空")
        @Size(min = 2, max = 30, message = "真实姓名长度应为2-30")
        String realName,

        @Size(max = 30, message = "联系电话长度不能超过30")
        String phone,

        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱长度不能超过100")
        String email,

        @NotNull(message = "角色不能为空")
        Long roleId,

        @NotNull(message = "用户状态不能为空")
        UserStatus status
) {
}
