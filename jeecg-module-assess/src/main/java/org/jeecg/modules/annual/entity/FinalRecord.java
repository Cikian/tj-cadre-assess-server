package org.jeecg.modules.annual.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinalRecord {
    private String depart; // 单位
    private String name; // 姓名
    private BigDecimal score; // 测评分数
    private String teamRanking; // 班子内排名
    private String totalRanking; // 总排名
    private String recommendation; // 考核推荐等次
    private String remark; // 备注
}
