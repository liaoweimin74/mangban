package com.mangban.system.domain.vo;

import java.time.LocalDateTime;

public record IsolationPointVO(
        Long id,
        Long unitId,
        String unitName,
        String plantName,
        String factoryName,
        String code,
        String name,
        String medium,
        String pressureRating,
        String temperatureRating,
        String hazardLevel,
        String pointType,
        String blindSpec,
        String equipmentTag,
        String pipelineNo,
        String status,
        String occupyStatus,
        String remark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}