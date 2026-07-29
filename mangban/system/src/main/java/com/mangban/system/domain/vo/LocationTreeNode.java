package com.mangban.system.domain.vo;

import java.util.List;

public record LocationTreeNode(
        Long id,
        Long parentId,
        String name,
        String code,
        String type,
        Integer sortOrder,
        String remark,
        List<LocationTreeNode> children) {
}