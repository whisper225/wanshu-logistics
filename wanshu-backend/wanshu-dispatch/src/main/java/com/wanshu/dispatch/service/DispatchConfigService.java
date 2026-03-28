package com.wanshu.dispatch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanshu.model.entity.dispatch.DispatchConfig;
import com.wanshu.dispatch.mapper.DispatchConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DispatchConfigService {

    private final DispatchConfigMapper configMapper;

    public List<DispatchConfig> listAll() {
        return configMapper.selectList(new LambdaQueryWrapper<DispatchConfig>()
                .orderByAsc(DispatchConfig::getId));
    }

    /**
     * 根据机构ID查询调度配置
     */
    public DispatchConfig getByOrganId(Long organId) {
        return configMapper.selectOne(
                new LambdaQueryWrapper<DispatchConfig>().eq(DispatchConfig::getOrganId, organId));
    }

    @Transactional
    public void save(DispatchConfig config) {
        if (config.getId() != null) {
            config.setUpdatedTime(LocalDateTime.now());
            configMapper.updateById(config);
        } else {
            // 按机构ID查询是否已有配置
            DispatchConfig existing = config.getOrganId() != null ? getByOrganId(config.getOrganId()) : null;
            if (existing != null) {
                existing.setLatestDispatchHour(config.getLatestDispatchHour());
                existing.setMaxAssignTime(config.getMaxAssignTime());
                existing.setPriorityFirst(config.getPriorityFirst());
                existing.setPrioritySecond(config.getPrioritySecond());
                existing.setUpdatedTime(LocalDateTime.now());
                configMapper.updateById(existing);
            } else {
                configMapper.insert(config);
            }
        }
    }

    @Transactional
    public void delete(Long id) {
        configMapper.deleteById(id);
    }
}
