package com.mangban.blindplate.repository;

import com.mangban.blindplate.domain.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long>,
        JpaSpecificationExecutor<Location> {
    List<Location> findByParentIdOrderBySortOrder(Long parentId);
    List<Location> findByParentIdIsNullOrderBySortOrder();
    long countByParentIdAndIsDeleted(Long parentId, Integer isDeleted);
}