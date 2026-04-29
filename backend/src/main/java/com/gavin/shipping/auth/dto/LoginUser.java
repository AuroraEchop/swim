package com.gavin.shipping.auth.dto;

public record LoginUser(
        Long id,
        String username,
        String realName,
        String roleCode
) {
}
