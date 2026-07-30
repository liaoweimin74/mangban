package com.mangban.system.service;

import com.mangban.common.domain.PageResult;
import com.mangban.system.domain.dto.DictTypeCreateRequest;
import com.mangban.system.domain.dto.DictTypeQueryRequest;
import com.mangban.system.domain.dto.DictTypeUpdateRequest;
import com.mangban.system.domain.vo.DictTypeVO;

public interface DictTypeService {
    PageResult<DictTypeVO> list(DictTypeQueryRequest query);

    DictTypeVO create(DictTypeCreateRequest request);

    DictTypeVO update(Long id, DictTypeUpdateRequest request);

    void delete(Long id);
}