package org.jeecg.modules.annual.vo;

import lombok.Data;

import java.util.List;

/**
 * @Title: ExportDemVO
 * @Author Cikian
 * @Package org.jeecg.modules.annual.vo
 * @Date 2025/1/20 10:53
 * @description: jeecg-boot-parent: 导出民主测评成绩选项
 */

@Data
public class ExportDemVO {
    private String year;
    private String scope;
    private String type;
    private String leader;
}
