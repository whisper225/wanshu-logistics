package com.wanshu.model.entity.sys;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanshu.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 系统用户
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String username;
    private String password;
    private String realName;
    private String phone;
    private String avatar;
    private Integer gender;
    private LocalDate birthday;
    private Long organId;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
}
