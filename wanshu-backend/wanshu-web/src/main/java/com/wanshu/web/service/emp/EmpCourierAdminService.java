package com.wanshu.web.service.emp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 快递员管理端仅查询列表与详情；作业范围写入见 {@link EmpCourierService}。
 */
@Service
@RequiredArgsConstructor
public class EmpCourierAdminService {

    private final EmpCourierService courierService;
    private final SysUserService userService;
    private final BaseOrganMapper baseOrganMapper;

    public Map<String, Object> page(int pageNum, int pageSize, Long organId, Integer workStatus, String keyword) {
        List<Long> userIds = userService.findIdsByKeyword(StringUtils.hasText(keyword) ? keyword : "");
        IPage<EmpCourier> page = courierService.page(pageNum, pageSize, organId, workStatus, keyword, userIds);
        List<EmpCourier> records = page.getRecords();
        if (records.isEmpty()) {
            return buildPageMap(page, Collections.emptyList());
        }
        List<Long> ids = records.stream().map(EmpCourier::getId).toList();
        Map<Long, SysUser> userMap = userService.listByIds(ids).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity(), (a, b) -> a));
        Map<Long, String> organNames = loadOrganNames(records.stream()
                .map(EmpCourier::getOrganId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        List<CourierVO> list = records.stream()
                .map(c -> toVo(c, userMap.get(c.getId()), organNames))
                .toList();
        return buildPageMap(page, list);
    }

    public CourierVO getDetail(Long id) {
        EmpCourier c = courierService.getById(id);
        if (c == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        SysUser u = userService.getById(id);
        Map<Long, String> organNames = c.getOrganId() != null
                ? loadOrganNames(Set.of(c.getOrganId()))
                : Collections.emptyMap();
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
        vo.setId(c.getId());
        vo.setEmployeeNo(c.getEmployeeNo());
        vo.setOrganId(c.getOrganId());
        vo.setWorkStatus(c.getWorkStatus());
        vo.setCreatedTime(c.getCreatedTime());
        if (c.getOrganId() != null && organNames != null) {
            vo.setOrganName(organNames.get(c.getOrganId()));
        }
        if (u != null) {
            vo.setUsername(u.getUsername());
            vo.setRealName(u.getRealName());
            vo.setPhone(u.getPhone());
        }
        return vo;
    }

    private static Map<String, Object> buildPageMap(IPage<?> page, List<CourierVO> list) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return result;
    }
}
