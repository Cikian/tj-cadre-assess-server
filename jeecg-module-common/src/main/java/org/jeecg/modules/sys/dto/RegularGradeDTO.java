package org.jeecg.modules.sys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 此类应该在平时考核模块中
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegularGradeDTO {
    private String id;
    private String name;
    private String quarter1;
    private String quarter2;
    private String quarter3;
    private String quarter4;
}
