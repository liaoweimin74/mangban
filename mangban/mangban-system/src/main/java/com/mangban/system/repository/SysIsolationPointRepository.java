package com.mangban.system.repository;

import com.mangban.system.domain.entity.SysIsolationPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SysIsolationPointRepository extends JpaRepository<SysIsolationPoint, Long>,
        JpaSpecificationExecutor<SysIsolationPoint> {
    Optional<SysIsolationPoint> findByCode(String code);
    long countByUnitIdAndIsDeleted(Long unitId, Integer isDeleted);
}