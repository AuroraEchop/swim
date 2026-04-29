package com.gavin.shipping.role.dto;

public record RoleResponse(
        Long id,
        String roleName,
        String roleCode,
        String description,
        Boolean builtIn
) {
}
