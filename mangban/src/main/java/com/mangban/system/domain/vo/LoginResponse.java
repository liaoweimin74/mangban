package com.mangban.system.domain.vo;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        UserInfo user) {
}