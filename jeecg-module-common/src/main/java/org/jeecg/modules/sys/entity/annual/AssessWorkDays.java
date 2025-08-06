package org.jeecg.modules.sys.entity.annual;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;

import java.io.Serializable;

/**
 * @Description: 年度考核工作安排
 * @Author: jeecg-boot
 * @Date: 2024-09-04
 * @Version: V1.0
 */
@ApiModel(value = "assess_work_days对象", description = "年度考核优秀副职")
@Data
@TableName("assess_work_days")
public class AssessWorkDays implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String currentYear;

    private Integer workDays;
}
