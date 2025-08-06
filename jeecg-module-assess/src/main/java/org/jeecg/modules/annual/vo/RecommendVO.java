package org.jeecg.modules.annual.vo;


import lombok.Data;
import org.jeecg.modules.sys.entity.annual.AssessAnnualSummary;

import java.util.List;

/**
 * @className: RecommendVO
 * @author: Cikian
 * @date: 2024/11/4 17:46
 * @Version: 1.0
 * @description:
 */

@Data
public class RecommendVO {
    String year;
    String leader;
    List<String> groupList;
    List<String> bureauList;
    List<String> basicList;
    List<String> institutionList;
}
