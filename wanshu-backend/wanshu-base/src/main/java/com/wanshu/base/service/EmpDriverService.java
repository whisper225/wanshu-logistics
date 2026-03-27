package com.wanshu.base.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanshu.model.entity.emp.EmpDriver;
import com.wanshu.base.mapper.EmpDriverMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmpDriverService {

    private final EmpDriverMapper driverMapper;

    public IPage<EmpDriver> page(int pageNum, int pageSize, Long organId, Integer workStatus) {
        LambdaQueryWrapper<EmpDriver> wrapper = new LambdaQueryWrapper<>();
        if (organId != null) {
            wrapper.eq(EmpDriver::getOrganId, organId);
        }
        if (workStatus != null) {
            wrapper.eq(EmpDriver::getWorkStatus, workStatus);
        }
        wrapper.orderByDesc(EmpDriver::getCreatedTime);
        return driverMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public EmpDriver getById(Long id) {
        return driverMapper.selectById(id);
    }

    @Transactional
    public void create(EmpDriver driver) {
        driverMapper.insert(driver);
    }

    @Transactional
    public void update(EmpDriver driver) {
        driverMapper.updateById(driver);
    }

    @Transactional
    public void delete(Long id) {
        driverMapper.deleteById(id);
    }
}
