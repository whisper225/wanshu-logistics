package com.wanshu.web.service.emp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanshu.base.mapper.BaseOrganMapper;
import com.wanshu.base.service.EmpCourierService;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.ResultCode;
import com.wanshu.model.entity.base.BaseOrgan;
import com.wanshu.model.entity.emp.EmpCourier;
import com.wanshu.model.entity.sys.SysUser;
import com.wanshu.system.service.SysUserService;
import com.wanshu.web.dto.emp.CourierVO;
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
 * 快递员管理端仅查询列表与详情；作业范围写入见 {@link EmpCourierService}。
 * <p>
 * 列表以「拥有快递员角色」且已存在 {@link EmpCourier} 扩展行为准（与 init-admin-data 中 role_id=2 一致）。
 * 所属机构以 {@link SysUser#getOrganId()} 为准（与 emp_courier.organ_id 由用户更新时同步一致）。
 */
@Service
@RequiredArgsConstructor
public class EmpCourierAdminService {

    /** 与 docs/sql/init-admin-data.sql 中 Courier 角色 id 一致 */
    private static final long ROLE_COURIER_ID = 2L;

    private final EmpCourierService courierService;
    private final SysUserService userService;
    private final BaseOrganMapper baseOrganMapper;

    public Map<String, Object> page(int pageNum, int pageSize, Long organId, Integer workStatus, String keyword) {
        List<Long> roleCourierIds = userService.findUserIdsByRoleId(ROLE_COURIER_ID);
        if (roleCourierIds.isEmpty()) {
            return buildPageMap(0L, pageNum, pageSize, Collections.emptyList());
        }
        Set<Long> candidateIds = new LinkedHashSet<>(roleCourierIds);
        if (StringUtils.hasText(keyword)) {
            List<Long> kwIds = userService.findIdsByKeyword(keyword);
            Set<Long> kwSet = new HashSet<>(kwIds);
            List<SysUser> allRoleUsers = userService.listByIds(candidateIds);
            Set<Long> matched = new HashSet<>();
            List<Long> allUids = allRoleUsers.stream().map(SysUser::getId).toList();
            Map<Long, EmpCourier> empMap = courierService.listByUserIds(allUids).stream()
                    .collect(Collectors.toMap(EmpCourier::getId, Function.identity(), (a, b) -> a));
            String kw = keyword.trim();
            for (SysUser u : allRoleUsers) {
                EmpCourier c = empMap.get(u.getId());
                if (c == null) {
                    continue;
                }
                boolean userMatch = kwSet.contains(u.getId());
                boolean empNoMatch = c.getEmployeeNo() != null && c.getEmployeeNo().contains(kw);
                if (userMatch || empNoMatch) {
                    matched.add(u.getId());
                }
            }
            candidateIds.retainAll(matched);
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
        Map<Long, EmpCourier> empByUserId = courierService.listByUserIds(uids).stream()
                .collect(Collectors.toMap(EmpCourier::getId, Function.identity(), (a, b) -> a));

        List<SysUser> filtered = new ArrayList<>();
        for (SysUser u : users) {
            EmpCourier c = empByUserId.get(u.getId());
            if (c == null) {
                continue;
            }
            if (organId != null) {
                if (!Objects.equals(u.getOrganId(), organId)) {
                    continue;
                }
            }
            if (workStatus != null) {
                if (!Objects.equals(c.getWorkStatus(), workStatus)) {
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
        List<CourierVO> list = pageUsers.stream()
                .map(u -> toVo(empByUserId.get(u.getId()), u, organNames))
                .toList();
        return buildPageMap(total, pageNum, pageSize, list);
    }

    private static LocalDateTime sortTime(EmpCourier c, SysUser u) {
        if (c != null && c.getCreatedTime() != null) {
            return c.getCreatedTime();
        }
        return u.getCreatedTime();
    }

    public CourierVO getDetail(Long id) {
        EmpCourier c = courierService.getById(id);
        if (c == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        SysUser u = userService.getById(id);
        Set<Long> organIdSet = new HashSet<>();
        if (u.getOrganId() != null) {
            organIdSet.add(u.getOrganId());
        }
        Map<Long, String> organNames = loadOrganNames(organIdSet);
        return toVo(c, u, organNames);
    }

    private Map<Long, String> loadOrganNames(Set<Long> organIds) {
        if (organIds == null || organIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<BaseOrgan> organs = baseOrganMapper.selectList(
                new LambdaQueryWrapper<BaseOrgan>().in(BaseOrgan::getId, organIds));
        return organs.stream().collect(Collectors.toMap(BaseOrgan::getId, BaseOrgan::getOrganName, (a, b) -> a));
    }

    private CourierVO toVo(EmpCourier c, SysUser u, Map<Long, String> organNames) {
        CourierVO vo = new CourierVO();
        if (c == null || u == null) {
            return vo;
        }
        vo.setId(c.getId());
        vo.setEmployeeNo(c.getEmployeeNo());
        vo.setWorkStatus(c.getWorkStatus());
        vo.setCreatedTime(c.getCreatedTime());
        vo.setUsername(u.getUsername());
        vo.setRealName(u.getRealName());
        vo.setPhone(u.getPhone());
        vo.setOrganId(u.getOrganId());
        Long oid = vo.getOrganId();
        if (oid != null && organNames != null) {
            vo.setOrganName(organNames.get(oid));
        }
        return vo;
    }

    private static Map<String, Object> buildPageMap(long total, int pageNum, int pageSize, List<CourierVO> list) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }
}
