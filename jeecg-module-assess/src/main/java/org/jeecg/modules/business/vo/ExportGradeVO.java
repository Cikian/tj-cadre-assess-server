package org.jeecg.modules.business.vo;


import lombok.Data;

import java.util.List;

/**
 * @className: ExportGradeVO
 * @author: Cikian
 * @date: 2024/10/11 09:18
 * @Version: 1.0
 * @description: 业务考核导出结果选项
 */

@Data
public class ExportGradeVO {
    private String currentYear;
    private String exportType;
    private List<String> exportScope;
    private String departmentIds;
    private boolean merge;
    private boolean exportDepart;
}
