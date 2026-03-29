package com.wanshu.web.dto.emp;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DriverVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private Long organId;
    private String organName;
    private String vehicleTypes;
    private String licenseImage;
    private Integer workStatus;
    private LocalDateTime createdTime;
}
