package org.jeecg.modules.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jeecg.modules.annual.entity.AnnualIndexEmployee;
import org.jeecg.modules.business.entity.BusinessIndexEmployee;
import org.jeecg.modules.report.entity.ReportIndexEmployee;
import org.jeecg.modules.sys.entity.CommissionItem;

import java.util.List;

@Data
@AllArgsConstructor
public class IndexEmployee {

    BusinessIndexEmployee[] businessIndexEmployee;

    AnnualIndexEmployee[] annualIndexEmployee;

    ReportIndexEmployee[] reportIndexEmployee;

    List<CommissionItem> commissionItems;
}
