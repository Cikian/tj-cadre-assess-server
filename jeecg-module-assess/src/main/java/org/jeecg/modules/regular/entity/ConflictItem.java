package org.jeecg.modules.regular.entity;

import lombok.Data;

import java.util.List;

@Data
public class ConflictItem {

    // 前端重复信息
    private AssessRegularReportItem incoming;

    // 匹配到的冲突集合
    private List<AssessRegularReportItem> conflicts;
}
