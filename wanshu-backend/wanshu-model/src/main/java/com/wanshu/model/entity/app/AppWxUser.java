package com.wanshu.model.entity.app;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanshu.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 微信用户
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_wx_user")
public class AppWxUser extends BaseEntity {

    private String openid;
    private String unionid;
    private String nickname;
    private String avatar;
    private String phone;
    private Integer gender;
    private LocalDate birthday;
}
