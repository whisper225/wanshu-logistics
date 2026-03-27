package com.wanshu.system.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.ResultCode;
import com.wanshu.common.utils.RedisUtil;
import com.wanshu.model.entity.sys.SysRole;
import com.wanshu.model.entity.sys.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserService userService;
    private final SysRoleService roleService;
    private final RedisUtil redisUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final String CAPTCHA_PREFIX = "captcha:";

    public Map<String, Object> getCaptcha() {
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        // 生成简单的4位数字验证码
        String code = String.valueOf((int) (Math.random() * 9000 + 1000));
        redisUtil.setEx(CAPTCHA_PREFIX + captchaId, code, 300);

        Map<String, Object> result = new HashMap<>();
        result.put("captchaId", captchaId);
        // 返回SVG图片验证码
        String svg = generateCaptchaSvg(code);
        result.put("captchaImage", "data:image/svg+xml;base64," +
                java.util.Base64.getEncoder().encodeToString(svg.getBytes()));
        return result;
    }

    private String generateCaptchaSvg(String code) {
        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns='http://www.w3.org/2000/svg' width='120' height='40'>");
        svg.append("<rect width='120' height='40' fill='#f0f0f0'/>");
        // 干扰线
        for (int i = 0; i < 5; i++) {
            int x1 = (int) (Math.random() * 120);
            int y1 = (int) (Math.random() * 40);
            int x2 = (int) (Math.random() * 120);
            int y2 = (int) (Math.random() * 40);
            String color = String.format("#%02x%02x%02x",
                    (int) (Math.random() * 200), (int) (Math.random() * 200), (int) (Math.random() * 200));
            svg.append(String.format("<line x1='%d' y1='%d' x2='%d' y2='%d' stroke='%s'/>",
                    x1, y1, x2, y2, color));
        }
        // 文字
        for (int i = 0; i < code.length(); i++) {
            int x = 15 + i * 25;
            int y = 25 + (int) (Math.random() * 10 - 5);
            int rotate = (int) (Math.random() * 30 - 15);
            String color = String.format("#%02x%02x%02x",
                    (int) (Math.random() * 150), (int) (Math.random() * 150), (int) (Math.random() * 150));
            svg.append(String.format("<text x='%d' y='%d' fill='%s' font-size='24' font-weight='bold' " +
                    "transform='rotate(%d,%d,%d)'>%c</text>", x, y, color, rotate, x, y, code.charAt(i)));
        }
        svg.append("</svg>");
        return svg.toString();
    }

    public Map<String, Object> login(String username, String password, String captchaId, String captcha) {
        // 验证码校验
        if (captchaId != null && captcha != null) {
            Object cached = redisUtil.get(CAPTCHA_PREFIX + captchaId);
            redisUtil.delete(CAPTCHA_PREFIX + captchaId);
            if (cached == null || !captcha.equalsIgnoreCase(cached.toString())) {
                throw new BusinessException("验证码错误或已过期");
            }
        }

        // 用户校验
        SysUser user = userService.getByUsername(username);
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查角色的登录范围是否包含admin
        List<SysRole> roles = roleService.getRolesByUserId(user.getId());
        boolean canLoginAdmin = roles.stream().anyMatch(r -> {
            String scope = r.getLoginScope();
            return scope != null && scope.contains("admin");
        });
        if (!canLoginAdmin && !roles.isEmpty()) {
            throw new BusinessException("该账号无权登录管理端");
        }

        // Sa-Token 登录
        StpUtil.login(user.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        // 更新最后登录时间
        SysUser updateUser = new SysUser();
        updateUser.setId(user.getId());
        updateUser.setLastLoginTime(LocalDateTime.now());
        userService.update(updateUser);

        // 构建返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("token", tokenInfo.getTokenValue());
        result.put("userInfo", buildUserInfo(user, roles));
        return result;
    }

    public Map<String, Object> getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userService.getById(userId);
        List<SysRole> roles = roleService.getRolesByUserId(userId);
        return buildUserInfo(user, roles);
    }

    public void logout() {
        StpUtil.logout();
    }

    private Map<String, Object> buildUserInfo(SysUser user, List<SysRole> roles) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("name", user.getRealName());
        info.put("phone", user.getPhone());
        info.put("avatar", user.getAvatar());
        info.put("gender", user.getGender());
        info.put("organId", user.getOrganId());
        info.put("roles", roles.stream().map(SysRole::getRoleCode).toList());
        info.put("permissions", List.of("*"));
        return info;
    }
}
