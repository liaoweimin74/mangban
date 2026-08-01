package com.mangban.blindplate.service.impl;

import com.mangban.common.constant.GlobalConstant;
import com.mangban.common.exception.BusinessException;
import com.mangban.blindplate.domain.dto.LocationCreateRequest;
import com.mangban.blindplate.domain.dto.LocationUpdateRequest;
import com.mangban.blindplate.domain.entity.Location;
import com.mangban.blindplate.domain.vo.LocationTreeNode;
import com.mangban.blindplate.repository.LocationRepository;
import com.mangban.blindplate.service.LocationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationServiceImpl implements LocationService {
    private final LocationRepository locationRepository;

    public LocationServiceImpl(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    public List<LocationTreeNode> tree() {
        List<Location> roots = locationRepository.findByParentIdIsNullOrderBySortOrder();
        return roots.stream()
                .filter(l -> l.getIsDeleted() == 0)
                .map(this::toTreeNode)
                .collect(Collectors.toList());
    }

    private LocationTreeNode toTreeNode(Location location) {
        List<Location> children = locationRepository.findByParentIdOrderBySortOrder(location.getId());
        List<LocationTreeNode> childNodes = children.stream()
                .filter(c -> c.getIsDeleted() == 0)
                .map(this::toTreeNode)
                .collect(Collectors.toList());
        return new LocationTreeNode(
                location.getId(), location.getParentId(),
                location.getName(), location.getCode(), location.getType(),
                location.getSortOrder(), location.getRemark(),
                childNodes.isEmpty() ? null : childNodes);
    }

    private void validateParentType(String childType, Long parentId) {
        if (parentId == null) {
            if (!"FACTORY".equals(childType)) {
                throw new BusinessException("根节点必须是工厂类型");
            }
            return;
        }
        Location parent = locationRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException("父节点不存在"));
        if ("FACTORY".equals(childType)) {
            throw new BusinessException("工厂节点必须为根节点");
        }
        if ("PLANT".equals(childType) && !"FACTORY".equals(parent.getType())) {
            throw new BusinessException("装置必须挂在工厂下");
        }
        if ("UNIT".equals(childType) && !"PLANT".equals(parent.getType())) {
            throw new BusinessException("单元必须挂在装置下");
        }
    }

    @Override
    @Transactional
    public LocationTreeNode create(LocationCreateRequest request) {
        validateParentType(request.type(), request.parentId());
        Location location = new Location();
        location.setParentId(request.parentId());
        location.setName(request.name());
        location.setCode(request.code());
        location.setType(request.type());
        location.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        location.setRemark(request.remark());
        location = locationRepository.save(location);
        return toTreeNode(location);
    }

    @Override
    @Transactional
    public LocationTreeNode update(Long id, LocationUpdateRequest request) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("位置节点不存在"));
        if (StringUtils.hasText(request.name())) location.setName(request.name());
        if (StringUtils.hasText(request.code())) location.setCode(request.code());
        if (request.sortOrder() != null) location.setSortOrder(request.sortOrder());
        if (StringUtils.hasText(request.remark())) location.setRemark(request.remark());
        location = locationRepository.save(location);
        return toTreeNode(location);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new BusinessException("位置节点不存在");
        }
        if (locationRepository.countByParentIdAndIsDeleted(id, 0) > 0) {
            throw new BusinessException("存在子节点，无法删除");
        }
        Location location = locationRepository.findById(id).orElseThrow();
        location.setIsDeleted(GlobalConstant.DELETED_YES);
        locationRepository.save(location);
    }
}