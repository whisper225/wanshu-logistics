package com.wanshu.system.controller;

import com.wanshu.common.result.R;
import com.wanshu.system.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "获取验证码")
    @GetMapping("/captcha")
    public R<Map<String, Object>> captcha() {
        return R.ok(authService.getCaptcha());
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String username = params.get("account");
        String password = params.get("password");
        String captchaId = params.get("captchaId");
        String captcha = params.get("captcha");
        return R.ok(authService.login(username, password, captchaId, captcha));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/userInfo")
    public R<Map<String, Object>> getUserInfo() {
        return R.ok(authService.getUserInfo());
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }
}
