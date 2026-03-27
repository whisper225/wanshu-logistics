package com.wanshu.dispatch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.ResultCode;
import com.wanshu.model.entity.dispatch.DispatchLine;
import com.wanshu.model.entity.dispatch.DispatchTrip;
import com.wanshu.dispatch.mapper.DispatchLineMapper;
import com.wanshu.dispatch.mapper.DispatchTripMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DispatchLineService {

    private final DispatchLineMapper lineMapper;
    private final DispatchTripMapper tripMapper;

    public IPage<DispatchLine> page(int pageNum, int pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<DispatchLine> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(DispatchLine::getLineName, keyword)
                    .or().like(DispatchLine::getLineNumber, keyword);
        }
        if (status != null) {
            wrapper.eq(DispatchLine::getStatus, status);
        }
        wrapper.orderByDesc(DispatchLine::getCreatedTime);
        return lineMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public DispatchLine getById(Long id) {
        DispatchLine line = lineMapper.selectById(id);
        if (line == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return line;
    }

    @Transactional
    public void create(DispatchLine line) {
        lineMapper.insert(line);
    }

    @Transactional
    public void update(DispatchLine line) {
        lineMapper.updateById(line);
    }

    @Transactional
    public void delete(Long id) {
        LambdaQueryWrapper<DispatchTrip> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DispatchTrip::getLineId, id);
        if (tripMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该线路下存在班次，不能删除");
        }
        lineMapper.deleteById(id);
    }

    // ========== 班次管理 ==========

    public List<DispatchTrip> getTrips(Long lineId) {
        return tripMapper.selectList(
                new LambdaQueryWrapper<DispatchTrip>()
                        .eq(DispatchTrip::getLineId, lineId)
                        .orderByAsc(DispatchTrip::getDepartureTime));
    }

    @Transactional
    public void createTrip(DispatchTrip trip) {
        tripMapper.insert(trip);
    }

    @Transactional
    public void updateTrip(DispatchTrip trip) {
        tripMapper.updateById(trip);
    }

    @Transactional
    public void deleteTrip(Long tripId) {
        tripMapper.deleteById(tripId);
    }
}
