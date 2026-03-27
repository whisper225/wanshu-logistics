package com.wanshu.dispatch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanshu.model.entity.dispatch.DispatchConfig;
import com.wanshu.dispatch.mapper.DispatchConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DispatchConfigService {

    private final DispatchConfigMapper configMapper;

    public List<DispatchConfig> listAll() {
        return configMapper.selectList(new LambdaQueryWrapper<DispatchConfig>()
                .orderByAsc(DispatchConfig::getConfigKey));
    }

    public DispatchConfig getByKey(String key) {
        return configMapper.selectOne(
                new LambdaQueryWrapper<DispatchConfig>().eq(DispatchConfig::getConfigKey, key));
    }

    @Transactional
    public void save(DispatchConfig config) {
        DispatchConfig existing = getByKey(config.getConfigKey());
        if (existing != null) {
            existing.setConfigValue(config.getConfigValue());
            existing.setDescription(config.getDescription());
            configMapper.updateById(existing);
        } else {
            configMapper.insert(config);
        }
    }

    @Transactional
    public void delete(Long id) {
        configMapper.deleteById(id);
    }
}
