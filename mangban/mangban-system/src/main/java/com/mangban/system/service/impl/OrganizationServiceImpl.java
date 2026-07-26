package com.mangban.system.service.impl;

import com.mangban.common.constant.GlobalConstant;
import com.mangban.common.exception.BusinessException;
import com.mangban.system.domain.dto.OrganizationCreateRequest;
import com.mangban.system.domain.dto.OrganizationUpdateRequest;
import com.mangban.system.domain.entity.SysOrganization;
import com.mangban.system.domain.vo.TreeNode;
import com.mangban.system.repository.SysOrganizationRepository;
import com.mangban.system.repository.SysUserRepository;
import com.mangban.system.service.OrganizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrganizationServiceImpl implements OrganizationService {
    private final SysOrganizationRepository orgRepository;
    private final SysUserRepository userRepository;

    public OrganizationServiceImpl(SysOrganizationRepository orgRepository, SysUserRepository userRepository) {
        this.orgRepository = orgRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<TreeNode> tree() {
        List<SysOrganization> roots = orgRepository.findByParentIdIsNullOrderBySortOrder();
        return roots.stream()
                .filter(o -> o.getIsDeleted() == 0)
                .map(this::toTreeNode)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TreeNode create(OrganizationCreateRequest request) {
        SysOrganization org = new SysOrganization();
        org.setParentId(request.parentId());
        org.setOrgName(request.orgName());
        org.setOrgCode(request.orgCode());
        org.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        org.setStatus(request.status() != null ? request.status() : 1);
        org = orgRepository.save(org);
        return toTreeNode(org);
    }

    @Override
    @Transactional
    public TreeNode update(Long id, OrganizationUpdateRequest request) {
        SysOrganization org = orgRepository.findById(id)
                .orElseThrow(() -> new BusinessException("组织机构不存在"));
        if (StringUtils.hasText(request.orgName())) org.setOrgName(request.orgName());
        if (StringUtils.hasText(request.orgCode())) org.setOrgCode(request.orgCode());
        if (request.sortOrder() != null) org.setSortOrder(request.sortOrder());
        if (request.status() != null) org.setStatus(request.status());
        org = orgRepository.save(org);
        return toTreeNode(org);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!orgRepository.existsById(id)) {
            throw new BusinessException("组织机构不存在");
        }
        if (orgRepository.countByParentId(id) > 0) {
            throw new BusinessException("存在子节点，无法删除");
        }
        if (userRepository.countByOrgId(id) > 0) {
            throw new BusinessException("该机构下存在用户，无法删除");
        }
        SysOrganization org = orgRepository.findById(id).orElseThrow();
        org.setIsDeleted(GlobalConstant.DELETED_YES);
        orgRepository.save(org);
    }

    private TreeNode toTreeNode(SysOrganization org) {
        List<SysOrganization> children = orgRepository.findByParentIdOrderBySortOrder(org.getId());
        List<TreeNode> childNodes = children.stream()
                .filter(c -> c.getIsDeleted() == 0)
                .map(this::toTreeNode)
                .collect(Collectors.toList());
        return new TreeNode(org.getId(), org.getParentId(), org.getOrgName(),
                org.getOrgCode(), org.getSortOrder(), org.getStatus(),
                childNodes.isEmpty() ? null : childNodes);
    }
}