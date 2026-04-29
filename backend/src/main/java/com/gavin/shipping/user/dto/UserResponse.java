package com.gavin.shipping.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gavin.shipping.domain.UserStatus;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String realName,
        String phone,
        String email,
        Long roleId,
        String roleName,
        String roleCode,
        UserStatus status,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt
) {
}
