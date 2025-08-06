package org.jeecg.modules.report.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jeecg.modules.sys.dto.ReportFieldDTO;

import java.util.List;

@Data
@AllArgsConstructor
public class ReportIndexLeader {

    long complete;

    int uncompleted;

    double percent;

    List<ReportFieldDTO> reports;
}
