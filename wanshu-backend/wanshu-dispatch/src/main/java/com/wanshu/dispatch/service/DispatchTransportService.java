package com.wanshu.dispatch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanshu.model.entity.dispatch.DispatchTransportTask;
import com.wanshu.model.entity.dispatch.DispatchReturnRegister;
import com.wanshu.dispatch.mapper.DispatchTransportTaskMapper;
import com.wanshu.dispatch.mapper.DispatchReturnRegisterMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class DispatchTransportService {

    private final DispatchTransportTaskMapper taskMapper;
    private final DispatchReturnRegisterMapper returnRegisterMapper;

    public IPage<DispatchTransportTask> page(int pageNum, int pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<DispatchTransportTask> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(DispatchTransportTask::getTaskNumber, keyword);
        }
        if (status != null) {
            wrapper.eq(DispatchTransportTask::getStatus, status);
        }
        wrapper.orderByDesc(DispatchTransportTask::getCreatedTime);
        return taskMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public DispatchTransportTask getById(Long id) {
        return taskMapper.selectById(id);
    }

    @Transactional
    public void create(DispatchTransportTask task) {
        task.setTaskNumber("YS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int)(Math.random() * 10000)));
        if (task.getStatus() == null) {
            task.setStatus(0);
        }
        taskMapper.insert(task);
    }

    @Transactional
    public void update(DispatchTransportTask task) {
        taskMapper.updateById(task);
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        DispatchTransportTask update = new DispatchTransportTask();
        update.setId(id);
        update.setStatus(status);
        taskMapper.updateById(update);
    }

    @Transactional
    public void delete(Long id) {
        taskMapper.deleteById(id);
    }

    // ========== 回车登记 ==========

    public IPage<DispatchReturnRegister> pageReturnRegisters(int pageNum, int pageSize) {
        LambdaQueryWrapper<DispatchReturnRegister> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DispatchReturnRegister::getCreatedTime);
        return returnRegisterMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Transactional
    public void createReturnRegister(DispatchReturnRegister register) {
        returnRegisterMapper.insert(register);
    }
}
