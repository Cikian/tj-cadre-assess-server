package org.jeecg.modules.system.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user_oa")
public class SysUserOa {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String username;
    private String oaUserId;
}
