package org.jeecg.modules.annual.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 领导干部统计
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeadershipStatisticsDTO {

    private int bureauChiefLevelCount; // 局正级领导干部人数
    private String bureauChiefLevelNames; // 人员名称（局正级领导干部）
    private String bureauChiefLevelPosition; // 正级职务

    private int deputyDirectorLevelCount; // 副处级人数
    private String deputyDirectorLevelNames; // 副处级名称

    private int leadershipTeamCount; // 领导班子人数
    private String leadershipTeamNames; // 领导班子名称

    private int administrativePublicCount; // 行政参公人数
    private int administrativeChiefLevelCount; // 行政参公正级人数
    private String administrativeChiefLevelNames; // 行政参公正级名称

    private int administrativeDeputyLevelCount; // 行政参公副级人数
    private String administrativeDeputyLevelNames; // 行政参公副级名称

    private int disciplineInspectionCadreCount; // 纪检干部人数
    private String disciplineInspectionCadreNames; // 纪检干部名称

    private int publicInstitutionCount; // 事业单位人数
    private String publicInstitutionNames; // 事业单位名称
}
