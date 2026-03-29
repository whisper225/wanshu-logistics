package com.wanshu.web.controller.emp;

import com.wanshu.base.service.EmpCourierService;
import com.wanshu.common.result.R;
import com.wanshu.model.entity.emp.EmpCourierScope;
import com.wanshu.web.dto.emp.CourierVO;
import com.wanshu.web.service.emp.EmpCourierAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 快递员管理：仅查询与作业范围维护；不提供增删改账号。
 */
@Tag(name = "快递员管理")
@RestController
@RequestMapping("/api/emp/courier")
@RequiredArgsConstructor
public class EmpCourierController {

    private final EmpCourierAdminService courierAdminService;
    private final EmpCourierService courierService;

    @Operation(summary = "快递员分页列表")
    @GetMapping("/page")
    public R<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long organId,
            @RequestParam(required = false) Integer workStatus,
            @RequestParam(required = false) String keyword) {
        return R.ok(courierAdminService.page(pageNum, pageSize, organId, workStatus, keyword));
    }

    @Operation(summary = "快递员详情")
    @GetMapping("/{id}")
    public R<CourierVO> detail(@PathVariable Long id) {
        return R.ok(courierAdminService.getDetail(id));
    }

    @Operation(summary = "获取快递员作业范围")
    @GetMapping("/{id}/scopes")
    public R<List<EmpCourierScope>> getScopes(@PathVariable Long id) {
        return R.ok(courierService.getScopes(id));
    }

    @Operation(summary = "更新快递员作业范围")
    @PutMapping("/{id}/scopes")
    public R<Void> updateScopes(@PathVariable Long id, @RequestBody List<EmpCourierScope> scopes) {
        courierService.updateScopes(id, scopes);
        return R.ok();
    }
}
