package org.jeecg.modules.sys.entity.annual;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 人员辅助局领导设置
 * @Author: jeecg-boot
 * @Date: 2024-11-17
 * @Version: V1.0
 */
@Data
@TableName("assess_assist_config")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "assess_assist_config对象", description = "人员辅助局领导设置")
public class AssessAssistConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private String createBy;
    /**
     * 创建日期
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建日期")
    private Date createTime;
    /**
     * 更新人
     */
    @ApiModelProperty(value = "更新人")
    private String updateBy;
    /**
     * 更新日期
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新日期")
    private Date updateTime;
    /**
     * 局领导
     */
    @Excel(name = "局领导", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "id")
    @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "id")
    @ApiModelProperty(value = "局领导")
    private String leader;
    /**
     * 辅助人员
     */
    @Excel(name = "辅助人员", width = 15)
    @ApiModelProperty(value = "辅助人员")
    private String people;
    /**
     * 性别
     */
    @Excel(name = "性别", width = 15)
    @ApiModelProperty(value = "性别")
    private String sex;
    /**
     * 出生日期
     */
    @Excel(name = "出生日期", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "出生日期")
    private Date birth;
    /**
     * hashID
     */
    @Excel(name = "hashID", width = 15)
    @ApiModelProperty(value = "hashID")
    private String hashId;
}
