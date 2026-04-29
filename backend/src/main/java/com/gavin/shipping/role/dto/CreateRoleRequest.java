package com.gavin.shipping.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateRoleRequest(
        @NotBlank(message = "角色名称不能为空")
        @Size(max = 30, message = "角色名称长度不能超过30")
        String roleName,

        @NotBlank(message = "角色编码不能为空")
        @Size(max = 30, message = "角色编码长度不能超过30")
        String roleCode,

        @Size(max = 255, message = "角色说明长度不能超过255")
        String description,

        List<String> permissions
) {
}
