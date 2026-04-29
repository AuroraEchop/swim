package com.gavin.shipping.auth.dto;

public record LoginResponse(
        String loginToken,
        String tokenType,
        LoginUser user
) {
}
