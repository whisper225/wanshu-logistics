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
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpCourierService {

    private final EmpCourierMapper courierMapper;
    private final EmpCourierScopeMapper courierScopeMapper;

    public IPage<EmpCourier> page(int pageNum, int pageSize, Long organId, Integer workStatus,
                                    String keyword, List<Long> userIdsMatchingKeyword) {
        LambdaQueryWrapper<EmpCourier> wrapper = new LambdaQueryWrapper<>();
        if (organId != null) {
            wrapper.eq(EmpCourier::getOrganId, organId);
        }
        if (workStatus != null) {
            wrapper.eq(EmpCourier::getWorkStatus, workStatus);
        }
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> {
                if (userIdsMatchingKeyword != null && !userIdsMatchingKeyword.isEmpty()) {
                    w.and(w2 -> w2.in(EmpCourier::getId, userIdsMatchingKeyword).or().like(EmpCourier::getEmployeeNo, kw));
                } else {
                    w.like(EmpCourier::getEmployeeNo, kw);
                }
            });
        }
        wrapper.orderByDesc(EmpCourier::getCreatedTime);
        return courierMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public EmpCourier getById(Long id) {
        return courierMapper.selectById(id);
    }

    /** 按用户主键批量查询扩展行（主键与 sys_user.id 一致） */
    public List<EmpCourier> listByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return courierMapper.selectList(new LambdaQueryWrapper<EmpCourier>().in(EmpCourier::getId, userIds));
    }

    @Transactional
    public void create(EmpCourier courier) {
        courierMapper.insert(courier);
    }

    @Transactional
    public void update(EmpCourier courier) {
        courierMapper.updateById(courier);
    }

    /**
     * 删除快递员扩展行前先清理作业范围，避免孤儿数据。
     */
    @Transactional
    public void delete(Long id) {
        deleteScopesByCourierId(id);
        courierMapper.deleteById(id);
    }

    public void deleteScopesByCourierId(Long courierId) {
        courierScopeMapper.delete(
                new LambdaQueryWrapper<EmpCourierScope>().eq(EmpCourierScope::getCourierId, courierId));
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
