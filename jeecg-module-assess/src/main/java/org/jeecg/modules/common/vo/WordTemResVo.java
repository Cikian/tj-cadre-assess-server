package org.jeecg.modules.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jeecg.modules.sys.entity.annual.AssessAnnualSummary;

import java.util.List;

/**
 * @Title: WordTemResVo
 * @Author Cikian
 * @Package org.jeecg.modules.common.vo
 * @Date 2025/3/6 16:15
 * @description: jeecg-boot-parent: 参考信息汇总导出VO
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordTemResVo {
    private String name1;
    private List<AccountabilityTableVo> accountability;
    private String name3;
    private String name4;
    private String name5;
    private String name6;
    private String name7;
    private String name8;
    private String name9;
    private String name10;
    private String group;
    private List<NegativeTableVo> negative;
}
