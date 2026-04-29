package com.gavin.shipping.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateRoleRequest(
        @NotBlank(message = "角色名称不能为空")
        @Size(max = 30, message = "角色名称长度不能超过30")
        String roleName,

        @Size(max = 255, message = "角色说明长度不能超过255")
        String description,

        List<String> permissions
) {
}
