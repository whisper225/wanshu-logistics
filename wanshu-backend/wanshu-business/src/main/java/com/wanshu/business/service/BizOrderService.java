package com.wanshu.business.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.ResultCode;
import com.wanshu.model.entity.biz.BizOrder;
import com.wanshu.business.mapper.BizOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BizOrderService {

    private final BizOrderMapper orderMapper;

    public IPage<BizOrder> page(int pageNum, int pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<BizOrder> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(BizOrder::getOrderNumber, keyword)
                    .or().like(BizOrder::getSenderName, keyword)
                    .or().like(BizOrder::getSenderPhone, keyword)
                    .or().like(BizOrder::getReceiverName, keyword)
                    .or().like(BizOrder::getReceiverPhone, keyword));
        }
        if (status != null) {
            wrapper.eq(BizOrder::getStatus, status);
        }
        wrapper.orderByDesc(BizOrder::getCreatedTime);
        return orderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public BizOrder getById(Long id) {
        BizOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return order;
    }

    @Transactional
    public void create(BizOrder order) {
        order.setOrderNumber(generateOrderNumber());
        if (order.getStatus() == null) {
            order.setStatus(0);
        }
        orderMapper.insert(order);
    }

    @Transactional
    public void update(BizOrder order) {
        orderMapper.updateById(order);
    }

    @Transactional
    public void cancel(Long id, String reason) {
        BizOrder order = getById(id);
        if (order.getStatus() >= 5) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        BizOrder update = new BizOrder();
        update.setId(id);
        update.setStatus(7);
        update.setCancelReason(reason);
        orderMapper.updateById(update);
    }

    @Transactional
    public void delete(Long id) {
        orderMapper.deleteById(id);
    }

    private String generateOrderNumber() {
        String prefix = "DD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return prefix + String.format("%04d", (int)(Math.random() * 10000));
    }
}
