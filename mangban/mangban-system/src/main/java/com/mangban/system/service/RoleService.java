package com.mangban.system.service;

import com.mangban.common.domain.PageResult;
import com.mangban.system.domain.dto.*;

import java.util.List;

public interface RoleService {
    PageResult<com.mangban.system.domain.vo.RoleVO> list(RoleQueryRequest query);

    com.mangban.system.domain.vo.RoleVO create(RoleCreateRequest request);

    com.mangban.system.domain.vo.RoleVO update(Long id, RoleUpdateRequest request);

    void delete(Long id);

    List<Long> getRoleMenus(Long roleId);

    void assignMenus(Long roleId, Long[] menuIds);
}