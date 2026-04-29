package com.gavin.shipping.domain;

public record UserAccount(
        Long id,
        String username,
        String password,
        String realName,
        String roleCode,
        UserStatus status
) {
}
