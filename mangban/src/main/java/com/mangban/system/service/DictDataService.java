package com.mangban.system.service;

import com.mangban.system.domain.dto.DictDataCreateRequest;
import com.mangban.system.domain.dto.DictDataUpdateRequest;
import com.mangban.system.domain.vo.DictDataVO;

import java.util.List;

public interface DictDataService {
    List<DictDataVO> list(String dictCode);

    DictDataVO create(DictDataCreateRequest request);

    DictDataVO update(Long id, DictDataUpdateRequest request);

    void delete(Long id);
}