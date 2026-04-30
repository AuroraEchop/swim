package com.gavin.shipping.auth.dto;

import java.util.List;

public record CurrentUserResponse(
        Long id,
        String username,
        String realName,
        String roleCode,
        List<String> permissions
) {
}
