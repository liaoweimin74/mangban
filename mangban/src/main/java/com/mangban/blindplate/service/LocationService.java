package com.mangban.blindplate.service;

import com.mangban.blindplate.domain.dto.LocationCreateRequest;
import com.mangban.blindplate.domain.dto.LocationUpdateRequest;
import com.mangban.blindplate.domain.vo.LocationTreeNode;

import java.util.List;

public interface LocationService {
    List<LocationTreeNode> tree();
    LocationTreeNode create(LocationCreateRequest request);
    LocationTreeNode update(Long id, LocationUpdateRequest request);
    void delete(Long id);
}