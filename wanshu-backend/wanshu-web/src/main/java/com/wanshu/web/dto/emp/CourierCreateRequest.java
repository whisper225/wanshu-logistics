package com.wanshu.web.dto.emp;

import lombok.Data;

@Data
public class CourierCreateRequest {
    private String username;
    private String password;
    private String realName;
    private String phone;
    private Long organId;
    private String employeeNo;
    /** 默认 1=上班 */
    private Integer workStatus;
}
