package com.mangban.system.domain.dto;

import jakarta.validation.constraints.Size;

public record LocationUpdateRequest(
        Long parentId,
        @Size(max = 100) String name,
        @Size(max = 50) String code,
        Integer sortOrder,
        @Size(max = 500) String remark) {
}