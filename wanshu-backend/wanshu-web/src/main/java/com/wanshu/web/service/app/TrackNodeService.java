package com.wanshu.web.service.app;

import com.wanshu.base.mapper.BaseOrganMapper;
import com.wanshu.model.entity.base.BaseOrgan;
import com.wanshu.model.entity.sys.SysUser;
import com.wanshu.model.entity.track.TrackNode;
import com.wanshu.system.mapper.SysUserMapper;
import com.wanshu.web.repository.TrackNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流跟踪节点写入服务。
 * <p>
 * 在以下关键节点调用：
 * <ul>
 *   <li>揽收 ({@code completePickup})</li>
 *   <li>司机出库 ({@code confirmDeparture})</li>
 *   <li>司机入库 ({@code confirmArrival})</li>
 *   <li>派件分配 ({@code createDeliveryTask})</li>
 *   <li>签收 ({@code signDelivery})</li>
 *   <li>拒收 ({@code rejectDelivery})</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrackNodeService {

    private final TrackNodeRepository trackNodeRepository;
    private final SysUserMapper sysUserMapper;
    private final BaseOrganMapper organMapper;

    // =====================================================================
    // 揽收节点
    // =====================================================================

    /**
     * 写入"已揽收"跟踪节点。
     *
     * @param waybillId 运单 ID
     * @param courierId 操作快递员 ID（= sys_user.id）
     */
    public void recordPickup(Long waybillId, Long courierId) {
        String courierInfo = buildPersonInfo(courierId);
        save(waybillId, 1, "快件已揽收",
                "快递员：" + courierInfo, null);
    }

    // =====================================================================
    // 运输节点
    // =====================================================================

    /**
     * 写入"已从【XX】发出"跟踪节点（司机出库）。
     *
     * @param waybillId   运单 ID
     * @param startOrganId 出发机构 ID
     */
    public void recordDeparture(Long waybillId, Long startOrganId) {
        String organName = getOrganName(startOrganId);
        String title = "快件已从【" + (organName != null ? organName : "出发站点") + "】发出";
        save(waybillId, 2, title, null, organName);
    }

    /**
     * 写入"已到达【XX】"跟踪节点（司机入库）。
     *
     * @param waybillId    运单 ID
     * @param arrivedOrganId 到达机构 ID
     */
    public void recordArrival(Long waybillId, Long arrivedOrganId) {
        String organName = getOrganName(arrivedOrganId);
        String title = "快件已到达【" + (organName != null ? organName : "中转站点") + "】";
        save(waybillId, 3, title, null, organName);
    }

    // =====================================================================
    // 派送节点
    // =====================================================================

    /**
     * 写入"正在派送中"跟踪节点（派件任务分配）。
     *
     * @param waybillId  运单 ID
     * @param courierId  负责派件的快递员 ID（= sys_user.id），可为 null（未分配）
     */
    public void recordDelivering(Long waybillId, Long courierId) {
        String content = courierId != null
                ? "快递员：" + buildPersonInfo(courierId)
                : null;
        save(waybillId, 4, "快件正在派送中", content, null);
    }

    /**
     * 写入"已签收"跟踪节点。
     *
     * @param waybillId 运单 ID
     * @param courierId 操作快递员 ID
     */
    public void recordSigned(Long waybillId, Long courierId) {
        String courierInfo = buildPersonInfo(courierId);
        save(waybillId, 5, "快件已签收",
                "快递员：" + courierInfo, null);
    }

    /**
     * 写入"已拒收"跟踪节点。
     *
     * @param waybillId 运单 ID
     * @param courierId 操作快递员 ID
     * @param reason    拒收原因（可为 null）
     */
    public void recordRejected(Long waybillId, Long courierId, String reason) {
        String courierInfo = buildPersonInfo(courierId);
        String content = "快递员：" + courierInfo
                + (reason != null && !reason.isBlank() ? "，原因：" + reason : "");
        save(waybillId, 6, "快件已拒收", content, null);
    }

    // =====================================================================
    // 查询
    // =====================================================================

    /**
     * 按运单 ID 查询所有跟踪节点（时间降序）。
     */
    public List<TrackNode> listByWaybillId(Long waybillId) {
        return trackNodeRepository.findByWaybillIdOrderByTimeDesc(waybillId);
    }

    // =====================================================================
    // 私有辅助
    // =====================================================================

    private void save(Long waybillId, int status, String title, String content, String organName) {
        try {
            TrackNode node = new TrackNode();
            node.setWaybillId(waybillId);
            node.setStatus(status);
            node.setTitle(title);
            node.setContent(content);
            node.setOrganName(organName);
            node.setTime(LocalDateTime.now());
            trackNodeRepository.save(node);
            log.info("[跟踪节点] 写入成功，waybillId={}，status={}，title={}", waybillId, status, title);
        } catch (Exception e) {
            log.error("[跟踪节点] 写入失败，waybillId={}，原因：{}", waybillId, e.getMessage(), e);
        }
    }

    /** 查询用户姓名及电话，格式为"张三，电话：133xxxx"；查不到则返回"未知"。 */
    private String buildPersonInfo(Long userId) {
        if (userId == null) {
            return "未知";
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return "未知";
        }
        String name = user.getRealName() != null ? user.getRealName() : user.getUsername();
        String phone = user.getPhone() != null ? user.getPhone() : "";
        return name + (phone.isBlank() ? "" : "，电话：" + phone);
    }

    /** 查询机构名称，查不到返回 null。 */
    private String getOrganName(Long organId) {
        if (organId == null) {
            return null;
        }
        BaseOrgan organ = organMapper.selectById(organId);
        return organ != null ? organ.getOrganName() : null;
    }
}
