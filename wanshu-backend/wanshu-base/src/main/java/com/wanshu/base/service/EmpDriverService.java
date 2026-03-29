package com.wanshu.base.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanshu.model.entity.emp.EmpDriver;
import com.wanshu.base.mapper.EmpDriverMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpDriverService {

    private final EmpDriverMapper driverMapper;

    public IPage<EmpDriver> page(int pageNum, int pageSize, Long organId, Integer workStatus,
                                 String keyword, List<Long> userIdsMatchingKeyword) {
        LambdaQueryWrapper<EmpDriver> wrapper = new LambdaQueryWrapper<>();
        if (organId != null) {
            wrapper.eq(EmpDriver::getOrganId, organId);
        }
        if (workStatus != null) {
            wrapper.eq(EmpDriver::getWorkStatus, workStatus);
        }
        if (StringUtils.hasText(keyword)) {
            if (userIdsMatchingKeyword == null || userIdsMatchingKeyword.isEmpty()) {
                wrapper.apply("1=0");
            } else {
                wrapper.in(EmpDriver::getId, userIdsMatchingKeyword);
            }
        }
        wrapper.orderByDesc(EmpDriver::getCreatedTime);
        return driverMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public EmpDriver getById(Long id) {
        return driverMapper.selectById(id);
    }

    /** 按用户主键批量查询扩展行（主键与 sys_user.id 一致） */
    public List<EmpDriver> listByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return driverMapper.selectList(new LambdaQueryWrapper<EmpDriver>().in(EmpDriver::getId, userIds));
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
