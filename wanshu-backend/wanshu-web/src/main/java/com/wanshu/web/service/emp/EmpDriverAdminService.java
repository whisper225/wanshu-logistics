package com.wanshu.web.service.emp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanshu.base.mapper.BaseOrganMapper;
import com.wanshu.base.service.EmpDriverService;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.ResultCode;
import com.wanshu.model.entity.base.BaseOrgan;
import com.wanshu.model.entity.emp.EmpDriver;
import com.wanshu.model.entity.sys.SysUser;
import com.wanshu.system.service.SysUserService;
import com.wanshu.web.dto.emp.DriverVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 司机管理端仅查询列表与详情；车辆绑定见 {@link EmpDriverVehicleBindService}。
 * <p>
 * 列表以「拥有司机角色」的 {@link SysUser} 为准（与 init-admin-data 中 role_id=3 一致），
 * 再合并 {@link EmpDriver} 扩展信息；仅有用户表、尚未写入 emp_driver 的司机也会出现在列表中，
 * 便于在车辆管理等场景选择并绑定。
 * <p>
 * 所属机构以 {@link SysUser#getOrganId()} 为准（与 emp_driver.organ_id 由用户更新时同步一致）。
 */
@Service
@RequiredArgsConstructor
public class EmpDriverAdminService {

    /** 与 docs/sql/init-admin-data.sql 中 Driver 角色 id 一致 */
    private static final long ROLE_DRIVER_ID = 3L;

    private final EmpDriverService driverService;
    private final SysUserService userService;
    private final BaseOrganMapper baseOrganMapper;

    public Map<String, Object> page(int pageNum, int pageSize, Long organId, Integer workStatus, String keyword) {
        List<Long> roleDriverIds = userService.findUserIdsByRoleId(ROLE_DRIVER_ID);
        if (roleDriverIds.isEmpty()) {
            return buildPageMap(0L, pageNum, pageSize, Collections.emptyList());
        }
        Set<Long> candidateIds = new LinkedHashSet<>(roleDriverIds);
        if (StringUtils.hasText(keyword)) {
            List<Long> kwIds = userService.findIdsByKeyword(keyword);
            if (kwIds.isEmpty()) {
                return buildPageMap(0L, pageNum, pageSize, Collections.emptyList());
            }
            candidateIds.retainAll(new HashSet<>(kwIds));
        }
        if (candidateIds.isEmpty()) {
            return buildPageMap(0L, pageNum, pageSize, Collections.emptyList());
        }
        List<SysUser> users = userService.listByIds(candidateIds).stream()
                .filter(u -> u.getStatus() != null && u.getStatus() == 1)
                .collect(Collectors.toList());
        if (users.isEmpty()) {
            return buildPageMap(0L, pageNum, pageSize, Collections.emptyList());
        }
        List<Long> uids = users.stream().map(SysUser::getId).toList();
        Map<Long, EmpDriver> empByUserId = driverService.listByUserIds(uids).stream()
                .collect(Collectors.toMap(EmpDriver::getId, Function.identity(), (a, b) -> a));

        List<SysUser> filtered = new ArrayList<>();
        for (SysUser u : users) {
            EmpDriver ed = empByUserId.get(u.getId());
            if (organId != null) {
                if (!Objects.equals(u.getOrganId(), organId)) {
                    continue;
                }
            }
            if (workStatus != null) {
                if (ed == null || !Objects.equals(ed.getWorkStatus(), workStatus)) {
                    continue;
                }
            }
            filtered.add(u);
        }
        filtered.sort(Comparator.comparing((SysUser u) -> sortTime(empByUserId.get(u.getId()), u),
                Comparator.nullsLast(Comparator.reverseOrder())));

        long total = filtered.size();
        int from = Math.max(0, (pageNum - 1) * pageSize);
        if (from >= filtered.size()) {
            return buildPageMap(total, pageNum, pageSize, Collections.emptyList());
        }
        int to = Math.min(from + pageSize, filtered.size());
        List<SysUser> pageUsers = filtered.subList(from, to);

        Set<Long> organIdSet = new HashSet<>();
        for (SysUser u : pageUsers) {
            if (u.getOrganId() != null) {
                organIdSet.add(u.getOrganId());
            }
        }
        Map<Long, String> organNames = loadOrganNames(organIdSet);
        List<DriverVO> list = pageUsers.stream()
                .map(u -> toVo(empByUserId.get(u.getId()), u, organNames))
                .toList();
        return buildPageMap(total, pageNum, pageSize, list);
    }

    private static LocalDateTime sortTime(EmpDriver ed, SysUser u) {
        if (ed != null && ed.getCreatedTime() != null) {
            return ed.getCreatedTime();
        }
        return u.getCreatedTime();
    }

    public DriverVO getDetail(Long id) {
        EmpDriver d = driverService.getById(id);
        List<Long> roles = userService.getRoleIds(id);
        boolean isDriverRole = roles.contains(ROLE_DRIVER_ID);
        if (d == null && !isDriverRole) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        SysUser u = userService.getById(id);
        Set<Long> organIdSet = new HashSet<>();
        if (u.getOrganId() != null) {
            organIdSet.add(u.getOrganId());
        }
        Map<Long, String> organNames = loadOrganNames(organIdSet);
        return toVo(d, u, organNames);
    }

    private Map<Long, String> loadOrganNames(Set<Long> organIds) {
        if (organIds == null || organIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<BaseOrgan> organs = baseOrganMapper.selectList(
                new LambdaQueryWrapper<BaseOrgan>().in(BaseOrgan::getId, organIds));
        return organs.stream().collect(Collectors.toMap(BaseOrgan::getId, BaseOrgan::getOrganName, (a, b) -> a));
    }

    /**
     * @param d 可为 null（仅有 sys_user + 司机角色、尚未写入 emp_driver）
     */
    private DriverVO toVo(EmpDriver d, SysUser u, Map<Long, String> organNames) {
        DriverVO vo = new DriverVO();
        if (u == null) {
            return vo;
        }
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setRealName(u.getRealName());
        vo.setPhone(u.getPhone());
        vo.setOrganId(u.getOrganId());
        if (d != null) {
            vo.setVehicleTypes(d.getVehicleTypes());
            vo.setLicenseImage(d.getLicenseImage());
            vo.setWorkStatus(d.getWorkStatus());
            vo.setCreatedTime(d.getCreatedTime());
        } else {
            vo.setWorkStatus(null);
            vo.setCreatedTime(u.getCreatedTime());
        }
        Long oid = vo.getOrganId();
        if (oid != null && organNames != null) {
            vo.setOrganName(organNames.get(oid));
        }
        return vo;
    }

    private static Map<String, Object> buildPageMap(long total, int pageNum, int pageSize, List<DriverVO> list) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }
}
