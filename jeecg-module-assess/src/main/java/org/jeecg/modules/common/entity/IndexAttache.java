package org.jeecg.modules.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jeecg.modules.annual.entity.AnnualIndexEmployee;
import org.jeecg.modules.business.entity.BusinessIndexEmployee;
import org.jeecg.modules.regular.entity.RegularIndexEmployee;
import org.jeecg.modules.sys.entity.CommissionItem;

import java.util.List;

@Data
@AllArgsConstructor
public class IndexAttache {

    RegularIndexEmployee[] regularIndexEmployee;

    BusinessIndexEmployee[] businessIndexEmployee;

    AnnualIndexEmployee[] annualIndexEmployee;

    List<CommissionItem> commissionItems;
}
