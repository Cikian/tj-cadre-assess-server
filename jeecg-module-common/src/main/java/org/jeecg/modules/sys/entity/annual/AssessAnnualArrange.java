package org.jeecg.modules.sys.entity.annual;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Description: 年度考核工作安排
 * @Author: jeecg-boot
 * @Date: 2024-09-04
 * @Version: V1.0
 */
@ApiModel(value = "assess_annual_arrange对象", description = "年度考核工作安排")
@Data
@TableName("assess_annual_arrange")
public class AssessAnnualArrange implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private java.lang.String id;

    /**
     * 创建人
     */
    private java.lang.String createBy;

    /**
     * 创建日期
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date createTime;

    /**
     * 更新人
     */
    private java.lang.String updateBy;

    /**
     * 更新日期
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date updateTime;

    /**
     * 参加民主测评人数
     */
    @NotNull(message = "参加民主测评人数不能为空！")
    private java.lang.Integer voteNum;

    /**
     * 需要说明的情况
     */
    private java.lang.String remark;

    /**
     * 联系人
     */
    private java.lang.String contacts;

    /**
     * 手机
     */
    private java.lang.String contactsPhone;

    /**
     * 办公电话
     */
    private java.lang.String officeTel;

    /**
     * 外键
     */
    private java.lang.String annualFillId;

    /**
     * 参加考核（测评）局管处级干部
     */
    private java.lang.String recommend;

    /**
     * A票
     */
    private java.lang.Integer voteA;

    /**
     * B票
     */
    private java.lang.Integer voteB;

    /**
     * C票
     */
    private java.lang.Integer voteC;
    // 正职人数
    private java.lang.Integer chiefNum;
    // 副职人数
    private java.lang.Integer deputyNum;
}
