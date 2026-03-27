package com.wanshu.base.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanshu.model.entity.emp.EmpCourier;
import com.wanshu.model.entity.emp.EmpCourierScope;
import com.wanshu.base.mapper.EmpCourierMapper;
import com.wanshu.base.mapper.EmpCourierScopeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpCourierService {

    private final EmpCourierMapper courierMapper;
    private final EmpCourierScopeMapper courierScopeMapper;

    public IPage<EmpCourier> page(int pageNum, int pageSize, Long organId, Integer workStatus) {
        LambdaQueryWrapper<EmpCourier> wrapper = new LambdaQueryWrapper<>();
        if (organId != null) {
            wrapper.eq(EmpCourier::getOrganId, organId);
        }
        if (workStatus != null) {
            wrapper.eq(EmpCourier::getWorkStatus, workStatus);
        }
        wrapper.orderByDesc(EmpCourier::getCreatedTime);
        return courierMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public EmpCourier getById(Long id) {
        return courierMapper.selectById(id);
    }

    @Transactional
    public void create(EmpCourier courier) {
        courierMapper.insert(courier);
    }

    @Transactional
    public void update(EmpCourier courier) {
        courierMapper.updateById(courier);
    }

    @Transactional
    public void delete(Long id) {
        courierMapper.deleteById(id);
    }

    public List<EmpCourierScope> getScopes(Long courierId) {
        return courierScopeMapper.selectList(
                new LambdaQueryWrapper<EmpCourierScope>().eq(EmpCourierScope::getCourierId, courierId));
    }

    @Transactional
    public void updateScopes(Long courierId, List<EmpCourierScope> scopes) {
        courierScopeMapper.delete(
                new LambdaQueryWrapper<EmpCourierScope>().eq(EmpCourierScope::getCourierId, courierId));
        if (scopes != null) {
            for (EmpCourierScope scope : scopes) {
                scope.setCourierId(courierId);
                courierScopeMapper.insert(scope);
            }
        }
    }
}
