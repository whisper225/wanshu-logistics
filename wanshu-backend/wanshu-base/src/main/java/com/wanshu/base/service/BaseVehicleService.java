package com.wanshu.base.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.ResultCode;
import com.wanshu.model.entity.base.BaseVehicle;
import com.wanshu.model.entity.base.BaseVehicleDriver;
import com.wanshu.model.entity.base.BaseVehicleType;
import com.wanshu.base.mapper.BaseVehicleDriverMapper;
import com.wanshu.base.mapper.BaseVehicleMapper;
import com.wanshu.base.mapper.BaseVehicleTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BaseVehicleService {

    private final BaseVehicleTypeMapper vehicleTypeMapper;
    private final BaseVehicleMapper vehicleMapper;
    private final BaseVehicleDriverMapper vehicleDriverMapper;

    // ========== 车型管理 ==========

    public IPage<BaseVehicleType> pageTypes(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<BaseVehicleType> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(BaseVehicleType::getTypeName, keyword)
                    .or().like(BaseVehicleType::getTypeNumber, keyword);
        }
        wrapper.orderByDesc(BaseVehicleType::getCreatedTime);
        return vehicleTypeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public List<BaseVehicleType> listAllTypes() {
        return vehicleTypeMapper.selectList(null);
    }

    public BaseVehicleType getTypeById(Long id) {
        return vehicleTypeMapper.selectById(id);
    }

    @Transactional
    public void createType(BaseVehicleType type) {
        vehicleTypeMapper.insert(type);
    }

    @Transactional
    public void updateType(BaseVehicleType type) {
        vehicleTypeMapper.updateById(type);
    }

    @Transactional
    public void deleteType(Long id) {
        LambdaQueryWrapper<BaseVehicle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseVehicle::getVehicleTypeId, id);
        if (vehicleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该车型下存在车辆，不能删除");
        }
        vehicleTypeMapper.deleteById(id);
    }

    // ========== 车辆管理 ==========

    public IPage<BaseVehicle> pageVehicles(int pageNum, int pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<BaseVehicle> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(BaseVehicle::getLicensePlate, keyword)
                    .or().like(BaseVehicle::getVehicleNumber, keyword);
        }
        if (status != null) {
            wrapper.eq(BaseVehicle::getStatus, status);
        }
        wrapper.orderByDesc(BaseVehicle::getCreatedTime);
        return vehicleMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public BaseVehicle getVehicleById(Long id) {
        BaseVehicle vehicle = vehicleMapper.selectById(id);
        if (vehicle == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return vehicle;
    }

    /** 不存在时返回 null，不抛异常 */
    public BaseVehicle findVehicleById(Long id) {
        return vehicleMapper.selectById(id);
    }

    @Transactional
    public void createVehicle(BaseVehicle vehicle) {
        vehicleMapper.insert(vehicle);
    }

    @Transactional
    public void updateVehicle(BaseVehicle vehicle) {
        vehicleMapper.updateById(vehicle);
    }

    @Transactional
    public void deleteVehicle(Long id) {
        vehicleMapper.deleteById(id);
    }

    /**
     * 仅更新启用状态，避免整对象 PUT 误清空字段。0=停用，1=可用。
     */
    @Transactional
    public void updateVehicleStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态只能为 0（停用）或 1（可用）");
        }
        getVehicleById(id);
        vehicleMapper.update(null, new LambdaUpdateWrapper<BaseVehicle>()
                .eq(BaseVehicle::getId, id)
                .set(BaseVehicle::getStatus, status));
    }

    // ========== 车辆司机绑定 ==========

    public List<Long> getDriverIds(Long vehicleId) {
        return vehicleDriverMapper.selectList(
                new LambdaQueryWrapper<BaseVehicleDriver>().eq(BaseVehicleDriver::getVehicleId, vehicleId)
        ).stream().map(BaseVehicleDriver::getDriverId).toList();
    }

    /** 司机已绑定的车辆 ID 列表 */
    public List<Long> getVehicleIdsByDriverId(Long driverId) {
        return vehicleDriverMapper.selectList(
                new LambdaQueryWrapper<BaseVehicleDriver>().eq(BaseVehicleDriver::getDriverId, driverId)
        ).stream().map(BaseVehicleDriver::getVehicleId).toList();
    }

    @Transactional
    public void bindDriver(Long vehicleId, Long driverId) {
        LambdaQueryWrapper<BaseVehicleDriver> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseVehicleDriver::getVehicleId, vehicleId);
        long count = vehicleDriverMapper.selectCount(wrapper);
        if (count >= 10) {
            throw new BusinessException("一辆车最多绑定10个司机");
        }
        wrapper.eq(BaseVehicleDriver::getDriverId, driverId);
        if (vehicleDriverMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该司机已绑定此车辆");
        }
        BaseVehicleDriver vd = new BaseVehicleDriver();
        vd.setVehicleId(vehicleId);
        vd.setDriverId(driverId);
        vehicleDriverMapper.insert(vd);
    }

    @Transactional
    public void unbindDriver(Long vehicleId, Long driverId) {
        vehicleDriverMapper.delete(new LambdaQueryWrapper<BaseVehicleDriver>()
                .eq(BaseVehicleDriver::getVehicleId, vehicleId)
                .eq(BaseVehicleDriver::getDriverId, driverId));
    }
}
