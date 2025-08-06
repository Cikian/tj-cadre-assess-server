package org.jeecg.modules.annual.service;

import org.jeecg.modules.sys.entity.annual.AssessAnnualExcellentNum;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 年度考核分管领导优秀名额
 * @Author: jeecg-boot
 * @Date:   2024-09-18
 * @Version: V1.0
 */
public interface IAssessAnnualExcellentNumService extends IService<AssessAnnualExcellentNum> {
    AssessAnnualExcellentNum getByLeaderId(String year);
    List<AssessAnnualExcellentNum> getByCurrentYear(String year);

    boolean checkNo(String year);

}
