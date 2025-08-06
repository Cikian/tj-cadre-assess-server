package org.jeecg.modules.annual.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.sys.entity.annual.AssessExcellentNum;
import org.jeecg.modules.sys.entity.annual.AssessWorkDays;

/**
 * @Description: 年度考核填报列表
 * @Author: jeecg-boot
 * @Date: 2024-09-04
 * @Version: V1.0
 */
public interface IAssessWorkDaysService extends IService<AssessWorkDays> {

    Integer getCurrentYearWorkDays();

    void updateWorkDays(Integer days);
}
