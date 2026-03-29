package com.wanshu.web.dto.emp;

import lombok.Data;

@Data
public class CourierUpdateRequest {
    private String realName;
    private String phone;
    private Long organId;
    private String employeeNo;
    private Integer workStatus;
    /** 非空则修改密码 */
    private String password;
}
