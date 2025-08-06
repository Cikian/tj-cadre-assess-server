package org.jeecg.modules.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jeecg.modules.annual.entity.AnnualIndexLeader;
import org.jeecg.modules.business.entity.BusinessIndexLeader;
import org.jeecg.modules.regular.entity.RegularIndexLeader;
import org.jeecg.modules.report.entity.ReportIndexLeader;

@Data
@AllArgsConstructor
public class IndexLeader {

    RegularIndexLeader regularIndexLeader;

    BusinessIndexLeader businessIndexLeader;

    AnnualIndexLeader annualIndexLeader;

    ReportIndexLeader reportIndexLeader;
}
