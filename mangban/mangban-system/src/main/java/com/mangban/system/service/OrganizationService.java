package com.mangban.system.service;

import java.util.List;

public interface OrganizationService {
    List<com.mangban.system.domain.vo.TreeNode> tree();

    com.mangban.system.domain.vo.TreeNode create(com.mangban.system.domain.dto.OrganizationCreateRequest request);

    com.mangban.system.domain.vo.TreeNode update(Long id, com.mangban.system.domain.dto.OrganizationUpdateRequest request);

    void delete(Long id);
}