package com.wanshu.base.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.ResultCode;
import com.wanshu.model.entity.base.BaseOrgan;
import com.wanshu.model.entity.base.BaseOrganScope;
import com.wanshu.base.mapper.BaseOrganMapper;
import com.wanshu.base.mapper.BaseOrganScopeMapper;
import com.wanshu.common.event.OrganGraphDeleteEvent;
import com.wanshu.common.event.OrganGraphSnapshot;
import com.wanshu.common.event.OrganGraphUpsertEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 扁平分页（含上级机构名称），供管理端列表。
     */
    public Map<String, Object> page(int pageNum, int pageSize, String keyword) {
        Page<BaseOrgan> p = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BaseOrgan> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(q -> q.like(BaseOrgan::getOrganName, keyword)
                    .or().like(BaseOrgan::getManagerName, keyword)
                    .or().like(BaseOrgan::getManagerPhone, keyword));
        }
        w.orderByAsc(BaseOrgan::getSortOrder).orderByAsc(BaseOrgan::getId);
        IPage<BaseOrgan> result = organMapper.selectPage(p, w);
        Set<Long> parentIds = result.getRecords().stream()
                .map(BaseOrgan::getParentId)
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .collect(Collectors.toSet());
        Map<Long, String> parentNames = new HashMap<>();
        if (!parentIds.isEmpty()) {
            List<BaseOrgan> parents = organMapper.selectList(
                    new LambdaQueryWrapper<BaseOrgan>().in(BaseOrgan::getId, parentIds));
            for (BaseOrgan po : parents) {
                parentNames.put(po.getId(), po.getOrganName());
            }
            for (Long pid : parentIds) {
                if (!parentNames.containsKey(pid)) {
                    BaseOrgan one = organMapper.selectById(pid);
                    if (one != null) {
                        parentNames.put(pid, one.getOrganName());
                    }
                }
            }
        }
        Set<Long> rowIds = result.getRecords().stream().map(BaseOrgan::getId).collect(Collectors.toSet());
        Set<Long> idsWithChildren = Collections.emptySet();
        if (!rowIds.isEmpty()) {
            List<BaseOrgan> anyChild = organMapper.selectList(
                    new LambdaQueryWrapper<BaseOrgan>().in(BaseOrgan::getParentId, rowIds));
            idsWithChildren = anyChild.stream().map(BaseOrgan::getParentId).collect(Collectors.toSet());
        }
        final Set<Long> parentIdsHavingChildren = idsWithChildren;
        List<Map<String, Object>> list = new ArrayList<>();
        for (BaseOrgan o : result.getRecords()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", String.valueOf(o.getId()));
            row.put("parentId", o.getParentId() == null || o.getParentId() == 0
                    ? "0" : String.valueOf(o.getParentId()));
            Long pid = o.getParentId();
            if (pid == null || pid == 0) {
                row.put("parentName", "—");
            } else {
                row.put("parentName", parentNames.getOrDefault(pid, "(" + pid + ")"));
            }
            row.put("organName", o.getOrganName());
            row.put("organType", o.getOrganType());
            row.put("provinceId", o.getProvinceId());
            row.put("cityId", o.getCityId());
            row.put("countyId", o.getCountyId());
            row.put("address", o.getAddress());
            row.put("longitude", o.getLongitude());
            row.put("latitude", o.getLatitude());
            row.put("managerName", o.getManagerName());
            row.put("managerPhone", o.getManagerPhone());
            row.put("contactName", o.getContactName());
            row.put("contactPhone", o.getContactPhone());
            row.put("sortOrder", o.getSortOrder());
            row.put("status", o.getStatus());
            row.put("hasChildren", parentIdsHavingChildren.contains(o.getId()));
            list.add(row);
        }
        Map<String, Object> out = new HashMap<>();
        out.put("list", list);
        out.put("total", result.getTotal());
        out.put("pageNum", result.getCurrent());
        out.put("pageSize", result.getSize());
        return out;
    }

    public List<Map<String, Object>> tree(String keyword) {
        LambdaQueryWrapper<BaseOrgan> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(BaseOrgan::getOrganName, keyword);
        }
        wrapper.orderByAsc(BaseOrgan::getSortOrder);
        List<BaseOrgan> organs = organMapper.selectList(wrapper);
        return buildTree(organs, 0L);
    }

    private static Long normalizeParentId(Long pid) {
        return (pid == null || pid == 0L) ? 0L : pid;
    }

    private List<Map<String, Object>> buildTree(List<BaseOrgan> organs, Long parentId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (BaseOrgan organ : organs) {
            if (parentId.equals(normalizeParentId(organ.getParentId()))) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", String.valueOf(organ.getId()));
                node.put("parentId", organ.getParentId() == null || organ.getParentId() == 0
                        ? "0" : String.valueOf(organ.getParentId()));
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

    /**
     * 详情（与分页一致：id/parentId 为字符串；附带 hasChildren 供前端禁用删除）
     */
    public Map<String, Object> detailMap(Long id) {
        BaseOrgan o = getById(id);
        long childCount = organMapper.selectCount(
                new LambdaQueryWrapper<BaseOrgan>().eq(BaseOrgan::getParentId, id));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", String.valueOf(o.getId()));
        m.put("parentId", o.getParentId() == null || o.getParentId() == 0
                ? "0" : String.valueOf(o.getParentId()));
        m.put("organName", o.getOrganName());
        m.put("organType", o.getOrganType());
        m.put("provinceId", o.getProvinceId());
        m.put("cityId", o.getCityId());
        m.put("countyId", o.getCountyId());
        m.put("address", o.getAddress());
        m.put("longitude", o.getLongitude());
        m.put("latitude", o.getLatitude());
        m.put("managerName", o.getManagerName());
        m.put("managerPhone", o.getManagerPhone());
        m.put("contactName", o.getContactName());
        m.put("contactPhone", o.getContactPhone());
        m.put("sortOrder", o.getSortOrder());
        m.put("status", o.getStatus());
        m.put("deleted", o.getDeleted());
        m.put("createdTime", o.getCreatedTime());
        m.put("updatedTime", o.getUpdatedTime());
        m.put("hasChildren", childCount > 0);
        return m;
    }

    @Transactional
    public void create(BaseOrgan organ) {
        organMapper.insert(organ);
        BaseOrgan saved = organMapper.selectById(organ.getId());
        if (saved != null) {
            eventPublisher.publishEvent(new OrganGraphUpsertEvent(toSnapshot(saved)));
        }
    }

    @Transactional
    public void update(BaseOrgan organ) {
        organMapper.updateById(organ);
        BaseOrgan saved = organMapper.selectById(organ.getId());
        if (saved != null) {
            eventPublisher.publishEvent(new OrganGraphUpsertEvent(toSnapshot(saved)));
        }
    }

    @Transactional
    public void delete(Long id) {
        LambdaQueryWrapper<BaseOrgan> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(BaseOrgan::getParentId, id);
        if (organMapper.selectCount(childWrapper) > 0) {
            throw new BusinessException("存在下级机构，不能删除");
        }
        organMapper.deleteById(id);
        eventPublisher.publishEvent(new OrganGraphDeleteEvent(id));
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

    private static OrganGraphSnapshot toSnapshot(BaseOrgan o) {
        return new OrganGraphSnapshot(
                o.getId(),
                o.getParentId(),
                o.getOrganName(),
                o.getOrganType(),
                o.getProvinceId(),
                o.getCityId(),
                o.getCountyId(),
                o.getAddress(),
                o.getLongitude(),
                o.getLatitude(),
                o.getManagerName(),
                o.getManagerPhone(),
                o.getContactPhone(),
                o.getStatus());
    }
}
