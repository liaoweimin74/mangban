package com.mangban.blindplate.controller;

import com.mangban.common.domain.R;
import com.mangban.blindplate.domain.dto.LocationCreateRequest;
import com.mangban.blindplate.domain.dto.LocationUpdateRequest;
import com.mangban.blindplate.domain.vo.LocationTreeNode;
import com.mangban.blindplate.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/tree")
    public R<List<LocationTreeNode>> tree() {
        return R.ok(locationService.tree());
    }

    @PostMapping
    public R<LocationTreeNode> create(@Valid @RequestBody LocationCreateRequest request) {
        return R.ok(locationService.create(request));
    }

    @PutMapping("/{id}")
    public R<LocationTreeNode> update(@PathVariable Long id, @Valid @RequestBody LocationUpdateRequest request) {
        return R.ok(locationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        locationService.delete(id);
        return R.ok();
    }
}