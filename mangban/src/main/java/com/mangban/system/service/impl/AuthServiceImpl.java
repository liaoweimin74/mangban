package com.mangban.system.service.impl;

import com.mangban.common.constant.GlobalConstant;
import com.mangban.common.exception.BusinessException;
import com.mangban.framework.security.jwt.JwtTokenProvider;
import com.mangban.framework.redis.RedisCache;
import com.mangban.system.domain.dto.LoginRequest;
import com.mangban.system.domain.entity.*;
import com.mangban.system.domain.vo.LoginResponse;
import com.mangban.system.domain.vo.MenuTree;
import com.mangban.system.domain.vo.UserInfo;
import com.mangban.system.repository.*;
import com.mangban.system.service.AuthService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {
    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final SysMenuRepository menuRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final SysRoleMenuRepository roleMenuRepository;
    private final SysOrganizationRepository orgRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(SysUserRepository userRepository, SysRoleRepository roleRepository,
                           SysMenuRepository menuRepository, SysUserRoleRepository userRoleRepository,
                           SysRoleMenuRepository roleMenuRepository, SysOrganizationRepository orgRepository,
                           JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.menuRepository = menuRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleMenuRepository = roleMenuRepository;
        this.orgRepository = orgRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        SysUser user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getUsername());

        UserInfo userInfo = buildUserInfo(user);
        return new LoginResponse(accessToken, refreshToken, userInfo);
    }

    @Override
    public void logout(String token) {
        // No-op: Redis disabled
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("Refresh Token 无效或已过期");
        }
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getUsername());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getUsername());

        UserInfo userInfo = buildUserInfo(user);
        return new LoginResponse(newAccessToken, newRefreshToken, userInfo);
    }

    @Override
    public LoginResponse getCurrentUser(Long userId) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        UserInfo userInfo = buildUserInfo(user);
        return new LoginResponse(null, null, userInfo);
    }

    @Override
    public List<MenuTree> getCurrentUserMenus(Long userId) {
        List<SysUserRole> userRoles = userRoleRepository.findByUserId(userId);
        Set<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());

        // Admin gets all menus
        boolean isAdmin = roleRepository.findAllById(roleIds).stream()
                .anyMatch(r -> "admin".equals(r.getRoleCode()));

        List<SysMenu> allMenus;
        if (isAdmin) {
            allMenus = menuRepository.findByParentIdIsNullOrderBySortOrder();
        } else {
            Set<Long> menuIds = roleMenuRepository.findByRoleIdIn(roleIds).stream()
                    .map(SysRoleMenu::getMenuId)
                    .collect(Collectors.toSet());
            allMenus = menuRepository.findAllById(menuIds).stream()
                    .filter(m -> m.getParentId() == null)
                    .sorted(Comparator.comparingInt(SysMenu::getSortOrder))
                    .collect(Collectors.toList());
        }

        return buildMenuTree(allMenus);
    }

    private UserInfo buildUserInfo(SysUser user) {
        List<SysUserRole> userRoles = userRoleRepository.findByUserId(user.getId());
        Set<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
        List<SysRole> roles = roleRepository.findAllById(roleIds);

        Set<Long> menuIds = roleMenuRepository.findByRoleIdIn(roleIds).stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toSet());
        Set<String> permissions = menuRepository.findAllById(menuIds).stream()
                .map(SysMenu::getPermission)
                .filter(p -> p != null && !p.isEmpty())
                .collect(Collectors.toSet());

        String orgName = null;
        if (user.getOrgId() != null) {
            orgName = orgRepository.findById(user.getOrgId())
                    .map(SysOrganization::getOrgName).orElse(null);
        }

        return new UserInfo(
                user.getId(), user.getUsername(), user.getNickname(),
                user.getEmail(), user.getPhone(), user.getAvatar(),
                user.getOrgId(), orgName,
                roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList()),
                permissions);
    }

    private List<MenuTree> buildMenuTree(List<SysMenu> menus) {
        return menus.stream()
                .filter(m -> m.getStatus() == 1 && m.getIsDeleted() == 0)
                .map(this::toMenuTree)
                .collect(Collectors.toList());
    }

    private MenuTree toMenuTree(SysMenu menu) {
        List<SysMenu> children = menuRepository.findByParentIdOrderBySortOrder(menu.getId());
        List<MenuTree> childTrees = children.stream()
                .filter(c -> c.getStatus() == 1 && c.getIsDeleted() == 0)
                .map(this::toMenuTree)
                .collect(Collectors.toList());

        return new MenuTree(
                menu.getId(), menu.getParentId(), menu.getMenuName(),
                menu.getMenuType(), menu.getPath(), menu.getComponent(),
                menu.getPermission(), menu.getIcon(), menu.getSortOrder(),
                childTrees.isEmpty() ? null : childTrees);
    }
}