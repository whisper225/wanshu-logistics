package com.wanshu.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.wanshu.common.result.R;
import com.wanshu.web.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "工作台")
@RestController
@RequestMapping("/api/system/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "工作台统计数据")
    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(dashboardService.buildStats(userId));
    }
}
