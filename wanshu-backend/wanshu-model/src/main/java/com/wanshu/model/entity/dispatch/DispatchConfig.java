package com.wanshu.model.entity.dispatch;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 调度配置
 */
@Data
@TableName("dispatch_config")
public class DispatchConfig implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Integer latestDispatchHour;
    private Integer maxAssignTime;
    private Integer priorityFirst;
    private Integer prioritySecond;
    private Long organId;
    private LocalDateTime updatedTime;
}
