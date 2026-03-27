package com.wanshu.dispatch.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wanshu.common.result.R;
import com.wanshu.model.entity.dispatch.DispatchLine;
import com.wanshu.model.entity.dispatch.DispatchTrip;
import com.wanshu.dispatch.service.DispatchLineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "线路管理")
@RestController
@RequestMapping("/api/dispatch/line")
@RequiredArgsConstructor
public class DispatchLineController {

    private final DispatchLineService lineService;

    @Operation(summary = "线路分页列表")
    @GetMapping("/page")
    public R<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        IPage<DispatchLine> page = lineService.page(pageNum, pageSize, keyword, status);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return R.ok(result);
    }

    @Operation(summary = "线路详情")
    @GetMapping("/{id}")
    public R<DispatchLine> detail(@PathVariable Long id) {
        return R.ok(lineService.getById(id));
    }

    @Operation(summary = "创建线路")
    @PostMapping
    public R<Void> create(@RequestBody DispatchLine line) {
        lineService.create(line);
        return R.ok();
    }

    @Operation(summary = "更新线路")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody DispatchLine line) {
        line.setId(id);
        lineService.update(line);
        return R.ok();
    }

    @Operation(summary = "删除线路")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        lineService.delete(id);
        return R.ok();
    }

    // ========== 班次 ==========

    @Operation(summary = "获取线路班次列表")
    @GetMapping("/{id}/trips")
    public R<List<DispatchTrip>> getTrips(@PathVariable Long id) {
        return R.ok(lineService.getTrips(id));
    }

    @Operation(summary = "创建班次")
    @PostMapping("/{id}/trips")
    public R<Void> createTrip(@PathVariable Long id, @RequestBody DispatchTrip trip) {
        trip.setLineId(id);
        lineService.createTrip(trip);
        return R.ok();
    }

    @Operation(summary = "更新班次")
    @PutMapping("/trips/{tripId}")
    public R<Void> updateTrip(@PathVariable Long tripId, @RequestBody DispatchTrip trip) {
        trip.setId(tripId);
        lineService.updateTrip(trip);
        return R.ok();
    }

    @Operation(summary = "删除班次")
    @DeleteMapping("/trips/{tripId}")
    public R<Void> deleteTrip(@PathVariable Long tripId) {
        lineService.deleteTrip(tripId);
        return R.ok();
    }
}
