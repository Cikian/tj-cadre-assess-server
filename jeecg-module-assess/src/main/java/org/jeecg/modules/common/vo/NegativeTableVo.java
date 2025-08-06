package org.jeecg.modules.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Title: NegativeTableVo
 * @Author Cikian
 * @Package org.jeecg.modules.common.vo
 * @Date 2025/3/6 17:20
 * @description: jeecg-boot-parent:
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NegativeTableVo {
    private String person;
    private String negativeList;
}
