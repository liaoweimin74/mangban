package com.mangban.blindplate.service;

import com.mangban.common.domain.PageResult;
import com.mangban.blindplate.domain.dto.*;
import com.mangban.blindplate.domain.vo.IsolationPointVO;

public interface IsolationPointService {
    PageResult<IsolationPointVO> list(Long unitId, Long plantId, String code, String name,
                                      String medium, String hazardLevel, String status,
                                      String occupyStatus, int page, int size);
    IsolationPointVO getById(Long id);
    IsolationPointVO create(IsolationPointCreateRequest request);
    IsolationPointVO update(Long id, IsolationPointUpdateRequest request);
    void delete(Long id);
    IsolationPointVO updateStatus(Long id, IsolationPointStatusRequest request);
    IsolationPointVO updateOccupy(Long id, IsolationPointOccupyRequest request);
}