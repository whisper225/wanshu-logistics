package com.wanshu.web.dto.emp;

import lombok.Data;

@Data
public class DriverCreateRequest {
    private String username;
    private String password;
    private String realName;
    private String phone;
    private Long organId;
    private String vehicleTypes;
    private String licenseImage;
    private Integer workStatus;
}
