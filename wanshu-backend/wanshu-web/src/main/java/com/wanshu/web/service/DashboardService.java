package com.wanshu.web.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanshu.common.enums.OrganType;
import com.wanshu.model.entity.base.BaseOrgan;
import com.wanshu.model.entity.biz.BizDeliveryTask;
import com.wanshu.model.entity.biz.BizOrder;
import com.wanshu.model.entity.biz.BizPickupTask;
import com.wanshu.model.entity.dispatch.DispatchTransportTask;
import com.wanshu.model.entity.sys.SysUser;
import com.wanshu.base.mapper.BaseOrganMapper;
import com.wanshu.business.mapper.BizDeliveryTaskMapper;
import com.wanshu.business.mapper.BizOrderMapper;
import com.wanshu.business.mapper.BizPickupTaskMapper;
import com.wanshu.dispatch.mapper.DispatchTransportTaskMapper;
import com.wanshu.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SysUserMapper sysUserMapper;
    private final BaseOrganMapper baseOrganMapper;
    private final BizOrderMapper bizOrderMapper;
    private final BizPickupTaskMapper bizPickupTaskMapper;
    private final BizDeliveryTaskMapper bizDeliveryTaskMapper;
    private final DispatchTransportTaskMapper dispatchTransportTaskMapper;

    public Map<String, Object> buildStats(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        Map<String, Object> root = new HashMap<>();
        if (user == null || user.getOrganId() == null) {
            root.put("organizationInfo", defaultOrgInfo());
            fillAggregates(root, LocalDateTime.now().toLocalDate().atStartOfDay(),
                    LocalDateTime.now().toLocalDate().plusDays(1).atStartOfDay());
            return root;
        }
        BaseOrgan organ = baseOrganMapper.selectById(user.getOrganId());
        Map<String, Object> orgInfo = new HashMap<>();
        if (organ != null) {
            orgInfo.put("name", organ.getOrganName());
            orgInfo.put("type", organTypeDisplay(organ.getOrganType()));
            long subCount = baseOrganMapper.selectCount(new LambdaQueryWrapper<BaseOrgan>()
                    .eq(BaseOrgan::getParentId, organ.getId()));
            orgInfo.put("subOrganizationCount", subCount);
            long empCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getOrganId, organ.getId()));
            orgInfo.put("employeeCount", empCount);
        } else {
            orgInfo.putAll(defaultOrgInfo());
        }
        root.put("organizationInfo", orgInfo);

        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();
        fillAggregates(root, dayStart, dayEnd);
        return root;
    }

    private Map<String, Object> defaultOrgInfo() {
        Map<String, Object> m = new HashMap<>();
        m.put("name", "—");
        m.put("type", "—");
        m.put("subOrganizationCount", 0);
        m.put("employeeCount", 0);
        return m;
    }

    private String organTypeDisplay(Integer code) {
        OrganType t = OrganType.getByCode(code);
        return t != null ? t.getDesc() : (code == null ? "—" : String.valueOf(code));
    }

    private void fillAggregates(Map<String, Object> root, LocalDateTime dayStart, LocalDateTime dayEnd) {
        LambdaQueryWrapper<BizOrder> todayOrders = new LambdaQueryWrapper<BizOrder>()
                .ge(BizOrder::getCreatedTime, dayStart)
                .lt(BizOrder::getCreatedTime, dayEnd);
        List<BizOrder> ordersToday = bizOrderMapper.selectList(todayOrders);
        double orderAmount = ordersToday.stream()
                .mapToDouble(o -> feeOrZero(o.getActualFee(), o.getEstimatedFee()))
                .sum();

        Map<String, Object> todayData = new HashMap<>();
        todayData.put("orderAmount", Math.round(orderAmount * 100.0) / 100.0);
        todayData.put("orderCount", ordersToday.size());
        long transportToday = dispatchTransportTaskMapper.selectCount(new LambdaQueryWrapper<DispatchTransportTask>()
                .ge(DispatchTransportTask::getCreatedTime, dayStart)
                .lt(DispatchTransportTask::getCreatedTime, dayEnd));
        todayData.put("transportTaskCount", transportToday);
        root.put("todayData", todayData);

        root.put("pickupTasks", countPickupBuckets(dayStart, dayEnd));
        root.put("transportTasks", countTransportBuckets(dayStart, dayEnd));
        root.put("deliveryTasks", countDeliveryBuckets(dayStart, dayEnd));

        root.put("recentOrders", recentOrderSeries());
        root.put("orderDistribution", orderDistributionByProvince());
    }

    private double feeOrZero(BigDecimal actual, BigDecimal estimated) {
        if (actual != null) {
            return actual.doubleValue();
        }
        if (estimated != null) {
            return estimated.doubleValue();
        }
        return 0;
    }

    private Map<String, Integer> countPickupBuckets(LocalDateTime start, LocalDateTime end) {
        List<BizPickupTask> list = bizPickupTaskMapper.selectList(new LambdaQueryWrapper<BizPickupTask>()
                .ge(BizPickupTask::getCreatedTime, start)
                .lt(BizPickupTask::getCreatedTime, end));
        Map<String, Integer> m = new HashMap<>();
        m.put("unassigned", (int) list.stream().filter(t -> t.getStatus() != null && t.getStatus() == 0).count());
        m.put("assigned", (int) list.stream().filter(t -> t.getStatus() != null && t.getStatus() == 1).count());
        m.put("completed", (int) list.stream().filter(t -> t.getStatus() != null && t.getStatus() == 2).count());
        return m;
    }

    private Map<String, Integer> countTransportBuckets(LocalDateTime start, LocalDateTime end) {
        List<DispatchTransportTask> list = dispatchTransportTaskMapper.selectList(
                new LambdaQueryWrapper<DispatchTransportTask>()
                        .ge(DispatchTransportTask::getCreatedTime, start)
                        .lt(DispatchTransportTask::getCreatedTime, end));
        Map<String, Integer> m = new HashMap<>();
        m.put("pending", (int) list.stream().filter(t -> t.getStatus() != null && t.getStatus() == 0).count());
        m.put("inProgress", (int) list.stream().filter(t -> t.getStatus() != null && t.getStatus() == 1).count());
        m.put("completed", (int) list.stream().filter(t -> t.getStatus() != null && t.getStatus() >= 2).count());
        return m;
    }

    private Map<String, Integer> countDeliveryBuckets(LocalDateTime start, LocalDateTime end) {
        List<BizDeliveryTask> list = bizDeliveryTaskMapper.selectList(new LambdaQueryWrapper<BizDeliveryTask>()
                .ge(BizDeliveryTask::getCreatedTime, start)
                .lt(BizDeliveryTask::getCreatedTime, end));
        Map<String, Integer> m = new HashMap<>();
        m.put("unassigned", (int) list.stream().filter(t -> t.getStatus() != null && t.getStatus() == 0).count());
        m.put("assigned", (int) list.stream().filter(t -> t.getStatus() != null && t.getStatus() == 1).count());
        m.put("completed", (int) list.stream().filter(t -> t.getStatus() != null && t.getStatus() >= 3).count());
        return m;
    }

    private Map<String, Object> recentOrderSeries() {
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            LocalDateTime s = d.atStartOfDay();
            LocalDateTime e = d.plusDays(1).atStartOfDay();
            long c = bizOrderMapper.selectCount(new LambdaQueryWrapper<BizOrder>()
                    .ge(BizOrder::getCreatedTime, s)
                    .lt(BizOrder::getCreatedTime, e));
            dates.add(String.format("%02d-%02d", d.getMonthValue(), d.getDayOfMonth()));
            counts.add((int) c);
        }
        Map<String, Object> series = new HashMap<>();
        series.put("dates", dates);
        series.put("counts", counts);
        return series;
    }

    private List<Map<String, Object>> orderDistributionByProvince() {
        List<BizOrder> all = bizOrderMapper.selectList(new LambdaQueryWrapper<BizOrder>().last("LIMIT 5000"));
        Map<Long, Long> grouped = all.stream()
                .filter(o -> o.getSenderProvinceId() != null)
                .collect(Collectors.groupingBy(BizOrder::getSenderProvinceId, Collectors.counting()));
        return grouped.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(34)
                .map(e -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("province", "省ID:" + e.getKey());
                    row.put("count", e.getValue().intValue());
                    return row;
                })
                .collect(Collectors.toList());
    }
}
