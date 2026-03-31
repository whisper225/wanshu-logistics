package com.wanshu.model.entity.biz;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wanshu.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_order")
public class BizOrder extends BaseEntity {

    private String orderNumber;
    private String senderName;
    private String senderPhone;
    private Long senderProvinceId;
    private Long senderCityId;
    private Long senderCountyId;
    private String senderAddress;
    private String receiverName;
    private String receiverPhone;
    private Long receiverProvinceId;
    private Long receiverCityId;
    private Long receiverCountyId;
    private String receiverAddress;
    private String goodsName;
    private String goodsType;
    private BigDecimal weight;
    private BigDecimal volume;
    private Integer paymentMethod;
    private BigDecimal estimatedFee;
    private BigDecimal actualFee;
    @JsonFormat(pattern = "yyyy-M-d H:mm:ss")
    private LocalDateTime pickupTime;
    private Integer privacyFlag;
    private Integer status;
    private String cancelReason;
    private Long userId;
    private Integer source;
}
