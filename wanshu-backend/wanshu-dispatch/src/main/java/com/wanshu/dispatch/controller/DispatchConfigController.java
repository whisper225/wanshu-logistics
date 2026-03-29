package com.wanshu.dispatch.controller;

import com.wanshu.common.result.R;
import com.wanshu.model.entity.dispatch.DispatchConfig;
import com.wanshu.dispatch.service.DispatchConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "调度配置")
@RestController
@RequestMapping("/api/dispatch/config")
@RequiredArgsConstructor
public class DispatchConfigController {

    private final DispatchConfigService configService;

    @Operation(summary = "获取所有配置")
    @GetMapping
    public R<List<DispatchConfig>> list() {
        return R.ok(configService.listAll());
    }

    @Operation(summary = "全局调度与成本配置（路线类型成本 + 调度规则，organ_id 为空）")
    @GetMapping("/global")
    public R<DispatchConfig> global() {
        return R.ok(configService.getGlobal());
    }

    @Operation(summary = "保存全局调度与成本配置")
    @PutMapping("/global")
    public R<Void> saveGlobal(@RequestBody DispatchConfig config) {
        configService.saveGlobal(config);
        return R.ok();
    }

    @Operation(summary = "保存配置")
    @PostMapping
    public R<Void> save(@RequestBody DispatchConfig config) {
        configService.save(config);
        return R.ok();
    }

    @Operation(summary = "删除配置")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        configService.delete(id);
        return R.ok();
    }
}
