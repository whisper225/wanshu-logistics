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

    @Operation(summary = "发送短信验证码（司机端等）")
    @PostMapping("/sms/send")
    public R<Void> sendSms(@RequestBody Map<String, String> body) {
        authService.sendSmsCode(body.get("phone"));
        return R.ok();
    }

    @Operation(summary = "短信验证码登录（司机端）")
    @PostMapping("/sms/login")
    public R<Map<String, Object>> smsLogin(@RequestBody Map<String, String> body) {
        return R.ok(authService.smsLogin(body.get("phone"), body.get("code")));
    }

    @Operation(summary = "微信小程序登录（user=用户端 courier=快递员端 driver=司机端）")
    @PostMapping("/wx/login")
    public R<Map<String, Object>> wxLogin(@RequestBody Map<String, String> body) {
        return R.ok(authService.wxMiniLogin(body.get("code"), body.get("role")));
    }
}
