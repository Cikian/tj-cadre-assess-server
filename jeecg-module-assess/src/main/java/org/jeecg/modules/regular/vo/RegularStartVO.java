package org.jeecg.modules.regular.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecg.modules.regular.entity.AssessRegularReportItem;
import org.jeecg.modules.regular.entity.ConflictItem;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
public class RegularStartVO {

    private String assessName;

    private String currentQuarter;

    private String currentYear;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date deadline;

    List<AssessRegularReportItem> reportItems;

    List<ConflictItem> conflictItems;

    List<String> conflictHashId;
}
