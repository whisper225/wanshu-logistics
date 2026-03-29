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
     * 全局调度与成本配置（organ_id 为空的唯一一条；不存在则初始化）
     */
    public DispatchConfig getGlobal() {
        DispatchConfig one = configMapper.selectOne(
                new LambdaQueryWrapper<DispatchConfig>()
                        .isNull(DispatchConfig::getOrganId)
                        .last("LIMIT 1"));
        if (one == null) {
            DispatchConfig init = new DispatchConfig();
            init.setLatestDispatchHour(1);
            init.setMaxAssignTime(0);
            init.setPriorityFirst(1);
            init.setPrioritySecond(2);
            init.setOrganId(null);
            configMapper.insert(init);
            return configMapper.selectById(init.getId());
        }
        return one;
    }

    @Transactional
    public void saveGlobal(DispatchConfig full) {
        DispatchConfig existing = getGlobal();
        existing.setLatestDispatchHour(full.getLatestDispatchHour());
        existing.setMaxAssignTime(full.getMaxAssignTime());
        existing.setPriorityFirst(full.getPriorityFirst());
        existing.setPrioritySecond(full.getPrioritySecond());
        existing.setCostPerKmType1(full.getCostPerKmType1());
        existing.setCostPerKmType2(full.getCostPerKmType2());
        existing.setCostPerKmType3(full.getCostPerKmType3());
        existing.setUpdatedTime(LocalDateTime.now());
        configMapper.updateById(existing);
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
                existing.setCostPerKmType1(config.getCostPerKmType1());
                existing.setCostPerKmType2(config.getCostPerKmType2());
                existing.setCostPerKmType3(config.getCostPerKmType3());
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
