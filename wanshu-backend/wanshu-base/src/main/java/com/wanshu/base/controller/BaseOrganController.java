package com.wanshu.base.controller;

import com.wanshu.common.result.R;
import com.wanshu.model.entity.base.BaseOrgan;
import com.wanshu.model.entity.base.BaseOrganScope;
import com.wanshu.base.service.BaseOrganService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "机构管理")
@RestController
@RequestMapping("/api/base/organ")
@RequiredArgsConstructor
public class BaseOrganController {

    private final BaseOrganService organService;

    @Operation(summary = "机构树")
    @GetMapping("/tree")
    public R<List<Map<String, Object>>> tree(@RequestParam(required = false) String keyword) {
        return R.ok(organService.tree(keyword));
    }

    @Operation(summary = "机构分页列表（扁平，含上级机构名称）")
    @GetMapping("/page")
    public R<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return R.ok(organService.page(pageNum, pageSize, keyword));
    }

    @Operation(summary = "机构详情（含 hasChildren，id/parentId 为字符串）")
    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        return R.ok(organService.detailMap(id));
    }

    @Operation(summary = "创建机构")
    @PostMapping
    public R<Void> create(@RequestBody BaseOrgan organ) {
        organService.create(organ);
        return R.ok();
    }

    @Operation(summary = "更新机构")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody BaseOrgan organ) {
        organ.setId(id);
        organService.update(organ);
        return R.ok();
    }

    @Operation(summary = "删除机构")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        organService.delete(id);
        return R.ok();
    }

    @Operation(summary = "获取机构作业范围")
    @GetMapping("/{id}/scopes")
    public R<List<BaseOrganScope>> getScopes(@PathVariable Long id) {
        return R.ok(organService.getScopes(id));
    }

    @Operation(summary = "更新机构作业范围")
    @PutMapping("/{id}/scopes")
    public R<Void> updateScopes(@PathVariable Long id, @RequestBody List<BaseOrganScope> scopes) {
        organService.updateScopes(id, scopes);
        return R.ok();
    }
}
