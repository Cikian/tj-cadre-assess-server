package org.jeecg.modules.regular.vo;

import lombok.Data;
import org.jeecg.modules.regular.entity.AssessRegularReportItem;
import org.jeecg.modules.regular.entity.ConflictItem;

import java.util.List;

@Data
public class ConflictInfoVO {

    // 原信息
    List<AssessRegularReportItem> incomingItems;

    // 冲突信息
    List<ConflictItem> conflictItems;
}
