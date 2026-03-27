package com.wanshu.base.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.ResultCode;
import com.wanshu.model.entity.base.BaseOrgan;
import com.wanshu.model.entity.base.BaseOrganScope;
import com.wanshu.base.mapper.BaseOrganMapper;
import com.wanshu.base.mapper.BaseOrganScopeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BaseOrganService {

    private final BaseOrganMapper organMapper;
    private final BaseOrganScopeMapper organScopeMapper;

    public List<Map<String, Object>> tree(String keyword) {
        LambdaQueryWrapper<BaseOrgan> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(BaseOrgan::getOrganName, keyword);
        }
        wrapper.orderByAsc(BaseOrgan::getSortOrder);
        List<BaseOrgan> organs = organMapper.selectList(wrapper);
        return buildTree(organs, 0L);
    }

    private List<Map<String, Object>> buildTree(List<BaseOrgan> organs, Long parentId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (BaseOrgan organ : organs) {
            if (parentId.equals(organ.getParentId())) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", organ.getId());
                node.put("parentId", organ.getParentId());
                node.put("name", organ.getOrganName());
                node.put("type", organ.getOrganType());
                node.put("provinceId", organ.getProvinceId());
                node.put("cityId", organ.getCityId());
                node.put("countyId", organ.getCountyId());
                node.put("address", organ.getAddress());
                node.put("longitude", organ.getLongitude());
                node.put("latitude", organ.getLatitude());
                node.put("managerName", organ.getManagerName());
                node.put("managerPhone", organ.getManagerPhone());
                node.put("contactName", organ.getContactName());
                node.put("contactPhone", organ.getContactPhone());
                node.put("status", organ.getStatus());
                List<Map<String, Object>> children = buildTree(organs, organ.getId());
                if (!children.isEmpty()) {
                    node.put("children", children);
                }
                result.add(node);
            }
        }
        return result;
    }

    public BaseOrgan getById(Long id) {
        BaseOrgan organ = organMapper.selectById(id);
        if (organ == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return organ;
    }

    @Transactional
    public void create(BaseOrgan organ) {
        organMapper.insert(organ);
    }

    @Transactional
    public void update(BaseOrgan organ) {
        organMapper.updateById(organ);
    }

    @Transactional
    public void delete(Long id) {
        LambdaQueryWrapper<BaseOrgan> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(BaseOrgan::getParentId, id);
        if (organMapper.selectCount(childWrapper) > 0) {
            throw new BusinessException("存在下级机构，不能删除");
        }
        organMapper.deleteById(id);
    }

    public List<BaseOrganScope> getScopes(Long organId) {
        return organScopeMapper.selectList(
                new LambdaQueryWrapper<BaseOrganScope>().eq(BaseOrganScope::getOrganId, organId));
    }

    @Transactional
    public void updateScopes(Long organId, List<BaseOrganScope> scopes) {
        organScopeMapper.delete(
                new LambdaQueryWrapper<BaseOrganScope>().eq(BaseOrganScope::getOrganId, organId));
        if (scopes != null) {
            for (BaseOrganScope scope : scopes) {
                scope.setOrganId(organId);
                organScopeMapper.insert(scope);
            }
        }
    }
}
