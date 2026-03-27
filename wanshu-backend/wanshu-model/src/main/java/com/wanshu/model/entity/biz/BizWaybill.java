package com.wanshu.model.entity.biz;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanshu.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 运单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_waybill")
public class BizWaybill extends BaseEntity {

    private String waybillNumber;
    private Long orderId;
    private String senderName;
    private String senderPhone;
    private String senderAddress;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String goodsName;
    private BigDecimal weight;
    private BigDecimal volume;
    private BigDecimal freight;
    private Long sendOrganId;
    private Long receiveOrganId;
    private Integer status;
    private String signImage;
    private LocalDateTime signTime;
}
