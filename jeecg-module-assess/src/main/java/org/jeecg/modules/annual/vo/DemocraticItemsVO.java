package org.jeecg.modules.annual.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class DemocraticItemsVO {

    private java.lang.String democraticId;

    private java.lang.String itemName;

    private java.math.BigDecimal itemScore;

    private java.math.BigDecimal score;

    private java.lang.String itemOption;

    private java.lang.String optionWeight;
}
