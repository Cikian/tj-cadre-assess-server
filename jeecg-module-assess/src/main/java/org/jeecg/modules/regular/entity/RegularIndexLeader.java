package org.jeecg.modules.regular.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jeecg.modules.regular.dto.RegularFieldDTO;

import java.util.List;

@Data
@AllArgsConstructor
public class RegularIndexLeader {

    long complete;

    int uncompleted;

    double percent;

    List<RegularFieldDTO> reports;

}
