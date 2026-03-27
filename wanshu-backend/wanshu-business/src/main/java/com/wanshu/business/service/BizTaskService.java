package com.wanshu.business.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanshu.model.entity.biz.BizDeliveryTask;
import com.wanshu.model.entity.biz.BizPickupTask;
import com.wanshu.business.mapper.BizDeliveryTaskMapper;
import com.wanshu.business.mapper.BizPickupTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BizTaskService {

    private final BizPickupTaskMapper pickupTaskMapper;
    private final BizDeliveryTaskMapper deliveryTaskMapper;

    // ========== 取件作业 ==========

    public IPage<BizPickupTask> pagePickupTasks(int pageNum, int pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<BizPickupTask> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(BizPickupTask::getTaskNumber, keyword);
        }
        if (status != null) {
            wrapper.eq(BizPickupTask::getStatus, status);
        }
        wrapper.orderByDesc(BizPickupTask::getCreatedTime);
        return pickupTaskMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public BizPickupTask getPickupTaskById(Long id) {
        return pickupTaskMapper.selectById(id);
    }

    @Transactional
    public void createPickupTask(BizPickupTask task) {
        task.setTaskNumber("QJ" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int)(Math.random() * 10000)));
        pickupTaskMapper.insert(task);
    }

    @Transactional
    public void assignPickupTask(Long taskId, Long courierId) {
        BizPickupTask update = new BizPickupTask();
        update.setId(taskId);
        update.setCourierId(courierId);
        update.setAssignTime(LocalDateTime.now());
        update.setStatus(1);
        pickupTaskMapper.updateById(update);
    }

    @Transactional
    public void cancelPickupTask(Long taskId, String reason) {
        BizPickupTask update = new BizPickupTask();
        update.setId(taskId);
        update.setStatus(3);
        update.setCancelReason(reason);
        pickupTaskMapper.updateById(update);
    }

    // ========== 派件作业 ==========

    public IPage<BizDeliveryTask> pageDeliveryTasks(int pageNum, int pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<BizDeliveryTask> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(BizDeliveryTask::getTaskNumber, keyword);
        }
        if (status != null) {
            wrapper.eq(BizDeliveryTask::getStatus, status);
        }
        wrapper.orderByDesc(BizDeliveryTask::getCreatedTime);
        return deliveryTaskMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public BizDeliveryTask getDeliveryTaskById(Long id) {
        return deliveryTaskMapper.selectById(id);
    }

    @Transactional
    public void createDeliveryTask(BizDeliveryTask task) {
        task.setTaskNumber("PJ" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int)(Math.random() * 10000)));
        deliveryTaskMapper.insert(task);
    }

    @Transactional
    public void assignDeliveryTask(Long taskId, Long courierId) {
        BizDeliveryTask update = new BizDeliveryTask();
        update.setId(taskId);
        update.setCourierId(courierId);
        update.setAssignTime(LocalDateTime.now());
        update.setStatus(1);
        deliveryTaskMapper.updateById(update);
    }
}
