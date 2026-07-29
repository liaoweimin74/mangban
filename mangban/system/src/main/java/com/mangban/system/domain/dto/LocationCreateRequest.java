package com.mangban.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LocationCreateRequest(
        Long parentId,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 20) String type,
        Integer sortOrder,
        @Size(max = 500) String remark) {
}