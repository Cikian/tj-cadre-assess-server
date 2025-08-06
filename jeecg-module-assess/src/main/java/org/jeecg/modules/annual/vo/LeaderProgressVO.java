package org.jeecg.modules.annual.vo;


import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.springframework.boot.context.event.SpringApplicationEvent;

import java.math.BigDecimal;

/**
 * @className: LeaderProgressVO
 * @author: Cikian
 * @date: 2024/11/28 22:25
 * @Version: 1.0
 * @description: 局领导民主测评进度
 */

@Data
public class LeaderProgressVO {
    private String leader;
    @Dict(dictTable = "sys_depart", dicText = "alias", dicCode = "id")
    private String over;
    @Dict(dictTable = "sys_depart", dicText = "alias", dicCode = "id")
    private String noOver;
    private BigDecimal rate;
}
