package com.mangban.system.repository;

import com.mangban.system.domain.entity.SysLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SysLocationRepository extends JpaRepository<SysLocation, Long>,
        JpaSpecificationExecutor<SysLocation> {
    List<SysLocation> findByParentIdOrderBySortOrder(Long parentId);
    List<SysLocation> findByParentIdIsNullOrderBySortOrder();
    long countByParentIdAndIsDeleted(Long parentId, Integer isDeleted);
}