package org.jeecg.modules.sys.entity.annual;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;

/**
 * @Description: 考核领导配置
 * @Author: jeecg-boot
 * @Date: 2024-12-09
 * @Version: V1.0
 */
@Data
@TableName("assess_leader_config")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "assess_leader_config对象", description = "考核领导配置")
public class AssessLeaderConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
    /**
     * 人员
     */
    @Excel(name = "人员", width = 15)
    @ApiModelProperty(value = "人员")
    private String person;
    /**
     * 领导
     */
    @Excel(name = "领导", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "id")
    @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "id")
    @ApiModelProperty(value = "领导")
    private String leader;
    /**
     * hashID
     */
    @Excel(name = "hashID", width = 15)
    @ApiModelProperty(value = "hashID")
    private String hashId;
}
