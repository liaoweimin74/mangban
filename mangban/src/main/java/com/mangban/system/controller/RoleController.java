package com.mangban.system.controller;

import com.mangban.common.domain.PageResult;
import com.mangban.common.domain.R;
import com.mangban.system.domain.dto.RoleCreateRequest;
import com.mangban.system.domain.dto.RoleQueryRequest;
import com.mangban.system.domain.dto.RoleUpdateRequest;
import com.mangban.system.domain.vo.RoleVO;
import com.mangban.system.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public R<PageResult<RoleVO>> list(RoleQueryRequest query) {
        return R.ok(roleService.list(query));
    }

    @PostMapping
    public R<RoleVO> create(@Valid @RequestBody RoleCreateRequest request) {
        return R.ok(roleService.create(request));
    }

    @PutMapping("/{id}")
    public R<RoleVO> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return R.ok(roleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return R.ok();
    }

    @GetMapping("/{id}/menus")
    public R<List<Long>> getRoleMenus(@PathVariable Long id) {
        return R.ok(roleService.getRoleMenus(id));
    }

    @PutMapping("/{id}/menus")
    public R<Void> assignMenus(@PathVariable Long id, @RequestBody MenuIdsRequest request) {
        roleService.assignMenus(id, request.menuIds());
        return R.ok();
    }

    public record MenuIdsRequest(Long[] menuIds) {}
}