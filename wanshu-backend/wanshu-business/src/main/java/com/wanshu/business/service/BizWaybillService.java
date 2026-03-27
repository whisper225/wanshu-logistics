package com.wanshu.business.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.ResultCode;
import com.wanshu.model.entity.biz.BizWaybill;
import com.wanshu.business.mapper.BizWaybillMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BizWaybillService {

    private final BizWaybillMapper waybillMapper;

    public IPage<BizWaybill> page(int pageNum, int pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<BizWaybill> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(BizWaybill::getWaybillNumber, keyword)
                    .or().like(BizWaybill::getSenderName, keyword)
                    .or().like(BizWaybill::getReceiverName, keyword));
        }
        if (status != null) {
            wrapper.eq(BizWaybill::getStatus, status);
        }
        wrapper.orderByDesc(BizWaybill::getCreatedTime);
        return waybillMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public BizWaybill getById(Long id) {
        BizWaybill waybill = waybillMapper.selectById(id);
        if (waybill == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return waybill;
    }

    @Transactional
    public void create(BizWaybill waybill) {
        waybill.setWaybillNumber(generateWaybillNumber());
        if (waybill.getStatus() == null) {
            waybill.setStatus(0);
        }
        waybillMapper.insert(waybill);
    }

    @Transactional
    public void update(BizWaybill waybill) {
        waybillMapper.updateById(waybill);
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        BizWaybill update = new BizWaybill();
        update.setId(id);
        update.setStatus(status);
        waybillMapper.updateById(update);
    }

    @Transactional
    public void delete(Long id) {
        waybillMapper.deleteById(id);
    }

    private String generateWaybillNumber() {
        String prefix = "YD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return prefix + String.format("%04d", (int)(Math.random() * 10000));
    }
}
