package com.wanshu.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.ResultCode;
import com.wanshu.base.service.EmpCourierService;
import com.wanshu.base.service.EmpDriverService;
import com.wanshu.model.entity.emp.EmpCourier;
import com.wanshu.model.entity.emp.EmpDriver;
import com.wanshu.model.entity.sys.SysUser;
import com.wanshu.model.entity.sys.SysUserRole;
import com.wanshu.system.mapper.SysUserMapper;
import com.wanshu.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final EmpDriverService empDriverService;
    private final EmpCourierService empCourierService;

    public IPage<SysUser> page(int pageNum, int pageSize, String keyword, Integer status, Long organId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getPhone, keyword));
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        if (organId != null) {
            wrapper.eq(SysUser::getOrganId, organId);
        }
        wrapper.orderByDesc(SysUser::getCreatedTime);
        return userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public SysUser getById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return user;
    }

    public SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    public SysUser getByPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return null;
        }
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getPhone, phone);
        return userMapper.selectOne(wrapper);
    }

    /**
     * 按微信 openid 查找用户
     */
    public SysUser getByWxOpenid(String openid) {
        if (!StringUtils.hasText(openid)) {
            return null;
        }
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getWxOpenid, openid);
        return userMapper.selectOne(wrapper);
    }

    /**
     * 微信用户首次登录自动注册：创建最小化 SysUser 记录（无密码、无角色）。
     * password 列 NOT NULL，微信用户无需密码登录，使用固定占位符填充。
     */
    @Transactional
    public SysUser registerWxUser(String openid, String unionid) {
        SysUser user = new SysUser();
        user.setWxOpenid(openid);
        user.setWxUnionid(unionid);
        user.setUsername("wx_" + openid.substring(0, Math.min(8, openid.length())));
        user.setRealName("微信用户");
        user.setPassword("WX_NO_PASSWORD");
        user.setStatus(1);
        userMapper.insert(user);
        return user;
    }

    /**
     * 关键词匹配用户名、真实姓名、手机号，返回用户 id 列表（用于员工分页与扩展表联查）。
     */
    public List<Long> findIdsByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }
        String kw = keyword.trim();
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(q -> q.like(SysUser::getUsername, kw)
                .or().like(SysUser::getRealName, kw)
                .or().like(SysUser::getPhone, kw));
        return userMapper.selectList(wrapper).stream().map(SysUser::getId).toList();
    }

    /**
     * 拥有指定角色的用户 ID（去重）。与 init-admin-data 中角色 id 一致：3 = DRIVER。
     */
    public List<Long> findUserIdsByRoleId(Long roleId) {
        if (roleId == null) {
            return List.of();
        }
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getRoleId, roleId);
        return userRoleMapper.selectList(wrapper).stream()
                .map(SysUserRole::getUserId)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<SysUser> listByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, ids));
    }

    @Transactional
    public void create(SysUser user) {
        SysUser existing = getByUsername(user.getUsername());
        if (existing != null) {
            throw new BusinessException(ResultCode.DATA_EXIST);
        }
        userMapper.insert(user);
        syncEmpDriverOrganFromUser(user.getId(), user.getOrganId());
        syncEmpCourierOrganFromUser(user.getId(), user.getOrganId());
    }

    @Transactional
    public void update(SysUser user) {
        userMapper.updateById(user);
        syncEmpDriverOrganFromUser(user.getId(), user.getOrganId());
        syncEmpCourierOrganFromUser(user.getId(), user.getOrganId());
    }

    /**
     * 司机扩展表主键与 sys_user.id 一致；用户机构变更时同步 emp_driver.organ_id。
     */
    private void syncEmpDriverOrganFromUser(Long userId, Long organId) {
        if (userId == null) {
            return;
        }
        EmpDriver driver = empDriverService.getById(userId);
        if (driver == null) {
            return;
        }
        if (Objects.equals(driver.getOrganId(), organId)) {
            return;
        }
        driver.setOrganId(organId);
        empDriverService.update(driver);
    }

    /**
     * 快递员扩展表主键与 sys_user.id 一致；用户机构变更时同步 emp_courier.organ_id。
     */
    private void syncEmpCourierOrganFromUser(Long userId, Long organId) {
        if (userId == null) {
            return;
        }
        EmpCourier courier = empCourierService.getById(userId);
        if (courier == null) {
            return;
        }
        if (Objects.equals(courier.getOrganId(), organId)) {
            return;
        }
        courier.setOrganId(organId);
        empCourierService.update(courier);
    }

    @Transactional
    public void delete(Long id) {
        userMapper.deleteById(id);
    }

    public List<Long> getRoleIds(Long userId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        return userRoleMapper.selectList(wrapper).stream()
                .map(SysUserRole::getRoleId)
                .toList();
    }

    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        userRoleMapper.delete(wrapper);

        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
    }
}
