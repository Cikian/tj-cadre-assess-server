package org.jeecg.modules.annual.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jeecg.modules.sys.dto.AnnualFieldDTO;

import java.util.List;

@Data
@AllArgsConstructor
public class AnnualIndexLeader {

    int complete;

    long uncompleted;

    double percent;

    List<AnnualFieldDTO> reports;
}
