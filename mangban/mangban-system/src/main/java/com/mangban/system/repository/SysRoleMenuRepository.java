package com.mangban.system.repository;

import com.mangban.system.domain.entity.SysRoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenu, Long> {
    List<SysRoleMenu> findByRoleId(Long roleId);

    List<SysRoleMenu> findByRoleIdIn(Set<Long> roleIds);

    void deleteByRoleId(Long roleId);
}