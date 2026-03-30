package com.wanshu.web.controller.open;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanshu.business.mapper.BizOrderMapper;
import com.wanshu.business.mapper.BizWaybillMapper;
import com.wanshu.model.entity.biz.BizOrder;
import com.wanshu.model.entity.biz.BizWaybill;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

/**
 * 开放接口（无需用户登录，固定 API Key 鉴权）
 * <p>
 * 专供 Coze 工作流回调使用，不对小程序前端暴露。
 * 鉴权方式（任选其一，值须与 wanshu.open.api-key 或环境变量 WANSHU_OPEN_API_KEY 完全一致）：
 * <ul>
 *   <li>请求头 {@code X-Api-Key: &lt;密钥&gt;}</li>
 *   <li>请求头 {@code Authorization: Bearer &lt;密钥&gt;}（Coze HTTP 节点常用）</li>
 *   <li>查询参数 {@code apiKey=} 或 {@code token=}</li>
 * </ul>
 */
@Slf4j
@Tag(name = "开放接口（Coze 工作流回调）")
@RestController
@RequestMapping("/api/open")
@RequiredArgsConstructor
public class OpenWaybillController {

    private final BizWaybillMapper waybillMapper;
    private final BizOrderMapper orderMapper;

    @Value("${wanshu.open.api-key:}")
    private String configuredApiKey;

    /**
     * 按运单号或订单号查询基础状态，供 Coze 智能客服工作流调用。
     *
     * <p>Coze HTTP 节点配置：
     * <pre>
     * GET https://你的域名/api/open/waybill/query?number=YD20240101001234
     * Header 任选其一：
     *   X-Api-Key: &lt;与 wanshu.open.api-key 相同&gt;
     *   或 Authorization: Bearer &lt;同上&gt;
     * 或在 URL 上带 apiKey=&lt;同上&gt;（部分 Coze 节点仅支持 Query）
     * </pre>
     *
     * <p>响应示例（找到运单）：
     * <pre>
     * {
     *   "found": true,
     *   "waybillNumber": "YD20240101001234",
     *   "status": 2,
     *   "statusText": "运输中",
     *   "senderName": "张三",
     *   "senderAddress": "北京市朝阳区xxx",
     *   "receiverName": "李四",
     *   "receiverAddress": "上海市浦东新区xxx",
     *   "goodsName": "电子产品",
     *   "freight": 25.00
     * }
     * </pre>
     *
     * <p>响应示例（未找到）：
     * <pre>{"found": false}</pre>
     */
    @Operation(summary = "按单号查询运单状态（Coze 工作流回调专用）")
    @GetMapping("/waybill/query")
    public Map<String, Object> queryWaybill(
            @RequestHeader(value = "X-Api-Key", required = false) String xApiKey,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam String number,
            @RequestParam(value = "apiKey", required = false) String apiKeyQuery,
            @RequestParam(value = "token", required = false) String tokenQuery) {

        String apiKey = resolveClientApiKey(xApiKey, authorization, apiKeyQuery, tokenQuery);
        validateApiKey(apiKey);

        if (!StringUtils.hasText(number)) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("found", false);
            return empty;
        }

        String num = number.trim();

        // 优先按运单号查（YD 开头）
        BizWaybill waybill = findWaybillByNumber(num);
        if (waybill != null) {
            return buildWaybillResult(waybill);
        }

        // 再按订单号查，找到订单后再关联运单
        BizOrder order = findOrderByNumber(num);
        if (order != null) {
            BizWaybill wb = findWaybillByOrderId(order.getId());
            if (wb != null) {
                return buildWaybillResult(wb);
            }
            // 订单存在但运单未生成（待揽件）
            return buildOrderResult(order);
        }

        Map<String, Object> notFound = new HashMap<>();
        notFound.put("found", false);
        return notFound;
    }

    // ─────────────────────────── private ───────────────────────────

    /**
     * 从 Coze HTTP 节点可能使用的多种位置解析密钥（与配置项逐字相等即通过）。
     */
    private String resolveClientApiKey(
            String xApiKey,
            String authorization,
            String apiKeyQuery,
            String tokenQuery) {
        if (StringUtils.hasText(xApiKey)) {
            return xApiKey.trim();
        }
        if (StringUtils.hasText(apiKeyQuery)) {
            return apiKeyQuery.trim();
        }
        if (StringUtils.hasText(tokenQuery)) {
            return tokenQuery.trim();
        }
        if (StringUtils.hasText(authorization)) {
            String a = authorization.trim();
            if (a.length() > 7 && a.regionMatches(true, 0, "Bearer ", 0, 7)) {
                return a.substring(7).trim();
            }
            return a;
        }
        return null;
    }

    private void validateApiKey(String apiKey) {
        if (!StringUtils.hasText(configuredApiKey)) {
            log.warn("wanshu.open.api-key 未配置，开放接口拒绝所有请求");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "开放接口未启用");
        }
        if (!StringUtils.hasText(apiKey) || !configuredApiKey.equals(apiKey)) {
            log.warn("开放接口 API Key 校验失败（请确认 Coze 中配置的密钥与服务器 wanshu.open.api-key / WANSHU_OPEN_API_KEY 一致；支持 X-Api-Key、Authorization Bearer、?apiKey=、?token=）");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "API Key 无效");
        }
    }

    private BizWaybill findWaybillByNumber(String number) {
        return waybillMapper.selectOne(
                new LambdaQueryWrapper<BizWaybill>()
                        .eq(BizWaybill::getWaybillNumber, number)
                        .last("LIMIT 1"));
    }

    private BizOrder findOrderByNumber(String number) {
        return orderMapper.selectOne(
                new LambdaQueryWrapper<BizOrder>()
                        .eq(BizOrder::getOrderNumber, number)
                        .last("LIMIT 1"));
    }

    private BizWaybill findWaybillByOrderId(Long orderId) {
        return waybillMapper.selectOne(
                new LambdaQueryWrapper<BizWaybill>()
                        .eq(BizWaybill::getOrderId, orderId)
                        .last("LIMIT 1"));
    }

    private Map<String, Object> buildWaybillResult(BizWaybill wb) {
        Map<String, Object> result = new HashMap<>();
        result.put("found", true);
        result.put("waybillNumber", wb.getWaybillNumber());
        result.put("status", wb.getStatus());
        result.put("statusText", waybillStatusText(wb.getStatus()));
        result.put("senderName", nullSafe(wb.getSenderName()));
        result.put("senderAddress", nullSafe(wb.getSenderAddress()));
        result.put("receiverName", nullSafe(wb.getReceiverName()));
        result.put("receiverAddress", nullSafe(wb.getReceiverAddress()));
        result.put("goodsName", nullSafe(wb.getGoodsName()));
        result.put("freight", wb.getFreight());
        return result;
    }

    /**
     * 订单存在但运单尚未生成（揽件前），用订单信息拼结果。
     */
    private Map<String, Object> buildOrderResult(BizOrder order) {
        Map<String, Object> result = new HashMap<>();
        result.put("found", true);
        result.put("waybillNumber", nullSafe(order.getOrderNumber()));
        result.put("status", order.getStatus());
        result.put("statusText", orderStatusText(order.getStatus()));
        result.put("senderName", nullSafe(order.getSenderName()));
        result.put("senderAddress", nullSafe(order.getSenderAddress()));
        result.put("receiverName", nullSafe(order.getReceiverName()));
        result.put("receiverAddress", nullSafe(order.getReceiverAddress()));
        result.put("goodsName", nullSafe(order.getGoodsName()));
        result.put("freight", order.getEstimatedFee());
        return result;
    }

    private String waybillStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待揽收";
            case 1 -> "已揽收";
            case 2 -> "运输中";
            case 3 -> "派送中";
            case 4 -> "已签收";
            case 5 -> "已拒收";
            default -> "未知";
        };
    }

    private String orderStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "已下单，等待快递员接单";
            case 1 -> "快递员已接单，即将上门取件";
            case 2 -> "已揽收";
            case 3 -> "运输中";
            case 4 -> "派送中";
            case 5 -> "已签收";
            case 6 -> "已拒收";
            case 7 -> "已取消";
            default -> "未知";
        };
    }

    private String nullSafe(String s) {
        return s != null ? s : "";
    }
}
