package org.jeecg.modules.annual.vo;


import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;

import java.math.BigDecimal;

/**
 * @className: DepartProgressVO
 * @author: Cikian
 * @date: 2024/12/30 09:35
 * @Version: 1.0
 * @description: 一两，单位测评进度
 */

@Data
public class DepartProgressVO {
    @Dict(dictTable = "sys_depart", dicText = "alias", dicCode = "id")
    private String depart;
    private String currentYear;
    private String endDate;
    private Integer filledNum;
    private Integer num;
}
