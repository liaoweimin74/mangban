package com.mangban.system.service;

import com.mangban.system.domain.dto.LoginRequest;
import com.mangban.system.domain.vo.LoginResponse;
import com.mangban.system.domain.vo.MenuTree;

import java.util.List;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    void logout(String token);

    LoginResponse refreshToken(String refreshToken);

    LoginResponse getCurrentUser(Long userId);

    List<MenuTree> getCurrentUserMenus(Long userId);
}