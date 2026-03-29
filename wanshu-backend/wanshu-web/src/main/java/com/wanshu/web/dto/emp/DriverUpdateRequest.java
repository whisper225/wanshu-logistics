package com.wanshu.web.dto.emp;

import lombok.Data;

@Data
public class DriverUpdateRequest {
    private String realName;
    private String phone;
    private Long organId;
    private String vehicleTypes;
    private String licenseImage;
    private Integer workStatus;
    private String password;
}
