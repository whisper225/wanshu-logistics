package com.wanshu.web.service.emp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 司机管理端仅查询列表与详情；车辆绑定见 {@link EmpDriverVehicleBindService}。
 */
@Service
@RequiredArgsConstructor
public class EmpDriverAdminService {

    private final EmpDriverService driverService;
    private final SysUserService userService;
    private final BaseOrganMapper baseOrganMapper;

    public Map<String, Object> page(int pageNum, int pageSize, Long organId, Integer workStatus, String keyword) {
        List<Long> userIds = userService.findIdsByKeyword(StringUtils.hasText(keyword) ? keyword : "");
        IPage<EmpDriver> page = driverService.page(pageNum, pageSize, organId, workStatus, keyword, userIds);
        List<EmpDriver> records = page.getRecords();
        if (records.isEmpty()) {
            return buildPageMap(page, Collections.emptyList());
        }
        List<Long> ids = records.stream().map(EmpDriver::getId).toList();
        Map<Long, SysUser> userMap = userService.listByIds(ids).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity(), (a, b) -> a));
        Map<Long, String> organNames = loadOrganNames(records.stream()
                .map(EmpDriver::getOrganId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        List<DriverVO> list = records.stream()
                .map(d -> toVo(d, userMap.get(d.getId()), organNames))
                .toList();
        return buildPageMap(page, list);
    }

    public DriverVO getDetail(Long id) {
        EmpDriver d = driverService.getById(id);
        if (d == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        SysUser u = userService.getById(id);
        Map<Long, String> organNames = d.getOrganId() != null
                ? loadOrganNames(Set.of(d.getOrganId()))
                : Collections.emptyMap();
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

    private DriverVO toVo(EmpDriver d, SysUser u, Map<Long, String> organNames) {
        DriverVO vo = new DriverVO();
        vo.setId(d.getId());
        vo.setOrganId(d.getOrganId());
        vo.setVehicleTypes(d.getVehicleTypes());
        vo.setLicenseImage(d.getLicenseImage());
        vo.setWorkStatus(d.getWorkStatus());
        vo.setCreatedTime(d.getCreatedTime());
        if (d.getOrganId() != null && organNames != null) {
            vo.setOrganName(organNames.get(d.getOrganId()));
        }
        if (u != null) {
            vo.setUsername(u.getUsername());
            vo.setRealName(u.getRealName());
            vo.setPhone(u.getPhone());
        }
        return vo;
    }

    private static Map<String, Object> buildPageMap(IPage<?> page, List<DriverVO> list) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return result;
    }
}
