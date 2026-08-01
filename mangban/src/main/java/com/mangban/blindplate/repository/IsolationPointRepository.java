package com.mangban.blindplate.repository;

import com.mangban.blindplate.domain.entity.IsolationPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface IsolationPointRepository extends JpaRepository<IsolationPoint, Long>,
        JpaSpecificationExecutor<IsolationPoint> {
    Optional<IsolationPoint> findByCode(String code);
    long countByUnitIdAndIsDeleted(Long unitId, Integer isDeleted);
}