package org.jeecg.modules.business.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jeecg.modules.sys.dto.BusinessFieldDTO;

import java.util.List;

@Data
@AllArgsConstructor
public class BusinessIndexLeader {

    long complete;

    long uncompleted;

    double percent;

    List<BusinessFieldDTO> reports;
}
