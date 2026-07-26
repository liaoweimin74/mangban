package com.mangban.system.service;

import com.mangban.system.domain.vo.MenuTree;

import java.util.List;

public interface MenuService {
    List<MenuTree> tree();

    MenuTree create(com.mangban.system.domain.dto.MenuCreateRequest request);

    MenuTree update(Long id, com.mangban.system.domain.dto.MenuUpdateRequest request);

    void delete(Long id);
}