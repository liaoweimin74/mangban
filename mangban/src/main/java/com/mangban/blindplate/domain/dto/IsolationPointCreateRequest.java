package com.mangban.blindplate.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IsolationPointCreateRequest(
        @NotNull Long unitId,
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 50) String medium,
        @Size(max = 50) String pressureRating,
        @Size(max = 50) String temperatureRating,
        @Size(max = 20) String hazardLevel,
        @Size(max = 50) String pointType,
        @Size(max = 100) String blindSpec,
        @Size(max = 50) String equipmentTag,
        @Size(max = 50) String pipelineNo,
        @Size(max = 500) String remark) {
}