package com.mangban.blindplate.repository;

import com.mangban.blindplate.domain.entity.SysLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SysLocationRepository extends JpaRepository<SysLocation, Long>,
        JpaSpecificationExecutor<SysLocation> {
    List<SysLocation> findByParentIdOrderBySortOrder(Long parentId);
    List<SysLocation> findByParentIdIsNullOrderBySortOrder();
    long countByParentIdAndIsDeleted(Long parentId, Integer isDeleted);
}