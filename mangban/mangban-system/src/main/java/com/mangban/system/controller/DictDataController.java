package com.mangban.system.controller;

import com.mangban.common.domain.R;
import com.mangban.system.domain.dto.DictDataCreateRequest;
import com.mangban.system.domain.dto.DictDataUpdateRequest;
import com.mangban.system.domain.vo.DictDataVO;
import com.mangban.system.service.DictDataService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dict-data")
public class DictDataController {
    private final DictDataService dictDataService;

    public DictDataController(DictDataService dictDataService) {
        this.dictDataService = dictDataService;
    }

    @GetMapping("/{dictCode}")
    public R<List<DictDataVO>> list(@PathVariable String dictCode) {
        return R.ok(dictDataService.list(dictCode));
    }

    @PostMapping
    public R<DictDataVO> create(@Valid @RequestBody DictDataCreateRequest request) {
        return R.ok(dictDataService.create(request));
    }

    @PutMapping("/{id}")
    public R<DictDataVO> update(@PathVariable Long id, @Valid @RequestBody DictDataUpdateRequest request) {
        return R.ok(dictDataService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        dictDataService.delete(id);
        return R.ok();
    }
}