package org.jeecg.modules.annual.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendTempDTO {

    private int serialNumber; // 序号
    private String name; // 名称
    private String grassrootsLeadership; // 基层领导班子
    private String bureauCadres; // 局机关处级干部
    private String publicBureau; // 分局参公
    private String institution; // 事业单位
    private String remarks; // 备注
}
