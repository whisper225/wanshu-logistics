package com.wanshu.system.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.utils.RedisUtil;
import com.wanshu.model.entity.sys.SysRole;
import com.wanshu.model.entity.sys.SysUser;
import com.wanshu.system.config.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserService userService;
    private final SysRoleService roleService;
    private final RedisUtil redisUtil;
    private final PasswordEncoder passwordEncoder;

    private static final String CAPTCHA_PREFIX = "captcha:";

    private static final long CAPTCHA_TTL_SECONDS = 300;

    /** Redis 不可用时进程内缓存（开发环境兜底，生产请保证 Redis 可用） */
    private final ConcurrentHashMap<String, CaptchaMem> captchaMemory = new ConcurrentHashMap<>();

    /** 与 CommonConstant.SMS_CODE_KEY_PREFIX 一致 */
    private static final String SMS_LOGIN_PREFIX = "sms:code:";

    @Value("${wanshu.auth.wx-mock-user-id:0}")
    private long wxMockUserId;

    private static final class CaptchaMem {
        final String code;
        final long expireAt;

        CaptchaMem(String code, long expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }
    }

    /** 写入验证码：优先 Redis，失败则内存（避免本地未启 Redis 时 /api/auth/captcha 直接 500） */
    private void storeCaptcha(String captchaId, String code) {
        try {
            redisUtil.setEx(CAPTCHA_PREFIX + captchaId, code, CAPTCHA_TTL_SECONDS);
        } catch (Exception e) {
            log.warn("Redis 不可用，验证码使用进程内缓存（建议开发环境启动 localhost:6379）: {}", e.getMessage());
            captchaMemory.put(captchaId, new CaptchaMem(code, System.currentTimeMillis() + CAPTCHA_TTL_SECONDS * 1000));
        }
    }

    /** 校验并删除验证码：先 Redis，再内存 */
    private String consumeCaptcha(String captchaId) {
        try {
            Object cached = redisUtil.get(CAPTCHA_PREFIX + captchaId);
            redisUtil.delete(CAPTCHA_PREFIX + captchaId);
            if (cached != null) {
                return cached.toString();
            }
        } catch (Exception e) {
            log.warn("Redis 读取验证码失败，尝试进程内缓存: {}", e.getMessage());
        }
        CaptchaMem mem = captchaMemory.remove(captchaId);
        if (mem != null && mem.expireAt >= System.currentTimeMillis()) {
            return mem.code;
        }
        return null;
    }

    public Map<String, Object> getCaptcha() {
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        // 生成简单的4位数字验证码
        String code = String.valueOf((int) (Math.random() * 9000 + 1000));
        storeCaptcha(captchaId, code);

        Map<String, Object> result = new HashMap<>();
        result.put("captchaId", captchaId);
        // 返回SVG图片验证码
        String svg = generateCaptchaSvg(code);
        result.put("captchaImage", "data:image/svg+xml;base64," +
                java.util.Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8)));
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
            String cached = consumeCaptcha(captchaId);
            if (cached == null || !captcha.equalsIgnoreCase(cached)) {
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

    /**
     * 发送短信验证码（演示：写入 Redis，未接真实短信网关）
     */
    public void sendSmsCode(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() < 11) {
            throw new BusinessException("请输入有效手机号");
        }
        String code = String.valueOf((int) (Math.random() * 900000 + 100000));
        redisUtil.setEx(SMS_LOGIN_PREFIX + phone, code, 300);
    }

    /**
     * 短信验证码登录（司机端）：需账号角色登录范围包含 driver
     */
    public Map<String, Object> smsLogin(String phone, String code) {
        if (!StringUtils.hasText(phone) || !StringUtils.hasText(code)) {
            throw new BusinessException("手机号和验证码不能为空");
        }
        Object cached = redisUtil.get(SMS_LOGIN_PREFIX + phone);
        redisUtil.delete(SMS_LOGIN_PREFIX + phone);
        if (cached == null || !code.equals(cached.toString())) {
            throw new BusinessException("验证码错误或已过期");
        }
        SysUser user = userService.getByPhone(phone);
        if (user == null) {
            throw new BusinessException("该手机号未注册");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        List<SysRole> roles = roleService.getRolesByUserId(user.getId());
        boolean canDriver = roles.stream().anyMatch(r -> {
            String scope = r.getLoginScope();
            return scope != null && scope.contains("driver");
        });
        if (!canDriver) {
            throw new BusinessException("该账号无权登录司机端（角色需包含 driver 登录范围）");
        }
        StpUtil.login(user.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        Map<String, Object> result = new HashMap<>();
        result.put("token", tokenInfo.getTokenValue());
        result.put("userInfo", buildUserInfo(user, roles));
        return result;
    }

    /**
     * 微信小程序登录（演示：未调用 wx.code2session；当配置 wanshu.auth.wx-mock-user-id 为大于 0 的 sys_user.id 时模拟登录）
     */
    public Map<String, Object> wxMiniLogin(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException("code不能为空");
        }
        if (wxMockUserId <= 0) {
            throw new BusinessException("未配置小程序模拟登录：请在配置中设置 wanshu.auth.wx-mock-user-id 为有效 sys_user.id");
        }
        SysUser user = userService.getById(wxMockUserId);
        if (user == null) {
            throw new BusinessException("wx-mock-user-id 对应的用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        List<SysRole> roles = roleService.getRolesByUserId(user.getId());
        StpUtil.login(user.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        Map<String, Object> result = new HashMap<>();
        result.put("token", tokenInfo.getTokenValue());
        result.put("userInfo", buildUserInfo(user, roles));
        result.put("mockNote", "演示登录：已忽略微信 code，使用 wanshu.auth.wx-mock-user-id");
        return result;
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
