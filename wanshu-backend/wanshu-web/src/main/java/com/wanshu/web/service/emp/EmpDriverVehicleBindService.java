package com.wanshu.web.service.emp;

import com.wanshu.base.service.BaseVehicleService;
import com.wanshu.base.service.EmpDriverService;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.model.entity.base.BaseVehicle;
import com.wanshu.model.entity.emp.EmpDriver;
import com.wanshu.model.entity.sys.SysUser;
import com.wanshu.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理端司机绑定车辆：员工模块仅允许查看，绑定走独立校验。
 * <ul>
 *   <li>司机资料已完善：姓名、手机、所属机构、擅长车型、驾驶证照片均有值</li>
 *   <li>司机已排班：当前以 emp_driver.work_status=1（上班）为准；若后续有独立排班表可替换校验</li>
 *   <li>车辆未处于「可用/启用」状态：base_vehicle.status≠1（schema：0=停用 1=可用），仅允许绑定停用车辆</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class EmpDriverVehicleBindService {

    private final EmpDriverService empDriverService;
    private final SysUserService userService;
    private final BaseVehicleService vehicleService;

    public List<BaseVehicle> listBoundVehicles(Long driverId) {
        ensureDriverExists(driverId);
        List<Long> ids = vehicleService.getVehicleIdsByDriverId(driverId);
        List<BaseVehicle> list = new ArrayList<>();
        for (Long vid : ids) {
            BaseVehicle v = vehicleService.findVehicleById(vid);
            if (v != null) {
                list.add(v);
            }
        }
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public void bind(Long driverId, Long vehicleId) {
        EmpDriver driver = ensureDriverExists(driverId);
        SysUser user = userService.getById(driverId);
        assertProfileComplete(user, driver);
        assertScheduleOk(driver);

        BaseVehicle vehicle = vehicleService.getVehicleById(vehicleId);
        assertVehicleEligibleForBind(vehicle);

        vehicleService.bindDriver(vehicleId, driverId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void unbind(Long driverId, Long vehicleId) {
        ensureDriverExists(driverId);
        vehicleService.unbindDriver(vehicleId, driverId);
    }

    private EmpDriver ensureDriverExists(Long driverId) {
        EmpDriver d = empDriverService.getById(driverId);
        if (d == null) {
            throw new BusinessException("司机不存在");
        }
        return d;
    }

    private void assertProfileComplete(SysUser user, EmpDriver driver) {
        if (!StringUtils.hasText(user.getRealName()) || !StringUtils.hasText(user.getPhone())) {
            throw new BusinessException("司机信息未完善：请先填写姓名与手机号");
        }
        if (user.getOrganId() == null) {
            throw new BusinessException("司机信息未完善：请先设置所属机构");
        }
        if (!StringUtils.hasText(driver.getVehicleTypes())) {
            throw new BusinessException("司机信息未完善：请先填写擅长车型");
        }
        if (!StringUtils.hasText(driver.getLicenseImage())) {
            throw new BusinessException("司机信息未完善：请先上传驾驶证照片");
        }
    }

    /** 上班状态视为已纳入排班/可出车；与独立排班表对接时可替换实现 */
    private void assertScheduleOk(EmpDriver driver) {
        if (driver.getWorkStatus() == null || driver.getWorkStatus() != 1) {
            throw new BusinessException("司机未处于上班状态，无法分配车辆（请先完成排班/设为上班）");
        }
    }

    /**
     * 仅允许绑定「停用」车辆，避免占用可用状态车辆。
     * base_vehicle.status：0=停用，1=可用
     */
    private void assertVehicleEligibleForBind(BaseVehicle vehicle) {
        if (vehicle.getStatus() != null && vehicle.getStatus() == 1) {
            throw new BusinessException("该车辆为可用/启用状态，不可在此绑定；请选择停用状态的车辆");
        }
    }
}
