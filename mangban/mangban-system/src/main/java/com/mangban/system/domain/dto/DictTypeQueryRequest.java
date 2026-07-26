package com.mangban.system.domain.dto;

public record DictTypeQueryRequest(
        String dictName,
        String dictCode,
        Integer status,
        Integer page,
        Integer size) {
}