package com.wanshu.model.entity.sys;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
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
    /** 允许更新为 null（解除机构绑定） */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long organId;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    /** 微信小程序 openid（用户端登录凭证） */
    private String wxOpenid;
    /** 微信 unionid */
    private String wxUnionid;
}
