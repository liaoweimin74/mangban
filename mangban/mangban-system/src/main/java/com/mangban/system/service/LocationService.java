package com.mangban.system.service;

import com.mangban.system.domain.dto.LocationCreateRequest;
import com.mangban.system.domain.dto.LocationUpdateRequest;
import com.mangban.system.domain.vo.LocationTreeNode;

import java.util.List;

public interface LocationService {
    List<LocationTreeNode> tree();
    LocationTreeNode create(LocationCreateRequest request);
    LocationTreeNode update(Long id, LocationUpdateRequest request);
    void delete(Long id);
}