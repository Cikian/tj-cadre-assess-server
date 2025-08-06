package org.jeecg.modules.sys.entity.annual;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Description: 年度考核工作安排
 * @Author: jeecg-boot
 * @Date: 2024-09-04
 * @Version: V1.0
 */
@ApiModel(value = "assess_excellent_num对象", description = "年度考核优秀副职")
@Data
@TableName("assess_excellent_num")
public class AssessExcellentNum implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @Dict(dictTable = "sys_depart", dicText = "alias", dicCode = "id")
    private String depart;

    private Integer excellentNum;
}
