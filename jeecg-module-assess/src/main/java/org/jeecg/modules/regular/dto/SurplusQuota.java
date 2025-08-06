package org.jeecg.modules.regular.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SurplusQuota {

    int totalQuota;

    int remainingQuota;
}
