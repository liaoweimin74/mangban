package com.mangban.blindplate.controller;

import com.mangban.common.domain.PageResult;
import com.mangban.common.domain.R;
import com.mangban.blindplate.domain.dto.*;
import com.mangban.blindplate.domain.vo.IsolationPointVO;
import com.mangban.blindplate.service.IsolationPointService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/isolation-points")
public class IsolationPointController {
    private final IsolationPointService isolationPointService;

    public IsolationPointController(IsolationPointService isolationPointService) {
        this.isolationPointService = isolationPointService;
    }

    @GetMapping
    public R<PageResult<IsolationPointVO>> list(
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) Long plantId,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String medium,
            @RequestParam(required = false) String hazardLevel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String occupyStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(isolationPointService.list(unitId, plantId, code, name, medium,
                hazardLevel, status, occupyStatus, page, size));
    }

    @GetMapping("/{id}")
    public R<IsolationPointVO> getById(@PathVariable Long id) {
        return R.ok(isolationPointService.getById(id));
    }

    @PostMapping
    public R<IsolationPointVO> create(@Valid @RequestBody IsolationPointCreateRequest request) {
        return R.ok(isolationPointService.create(request));
    }

    @PutMapping("/{id}")
    public R<IsolationPointVO> update(@PathVariable Long id, @Valid @RequestBody IsolationPointUpdateRequest request) {
        return R.ok(isolationPointService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        isolationPointService.delete(id);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    public R<IsolationPointVO> updateStatus(@PathVariable Long id,
                                             @Valid @RequestBody IsolationPointStatusRequest request) {
        return R.ok(isolationPointService.updateStatus(id, request));
    }

    @PutMapping("/{id}/occupy")
    public R<IsolationPointVO> updateOccupy(@PathVariable Long id,
                                              @Valid @RequestBody IsolationPointOccupyRequest request) {
        return R.ok(isolationPointService.updateOccupy(id, request));
    }
}