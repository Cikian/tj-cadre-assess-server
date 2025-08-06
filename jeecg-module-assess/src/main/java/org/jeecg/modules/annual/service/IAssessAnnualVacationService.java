package org.jeecg.modules.annual.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.sys.entity.annual.AssessAnnualVacation;

import java.util.List;

/**
 * @Description: 年度考核休假情况
 * @Author: jeecg-boot
 * @Date: 2024-09-03
 * @Version: V1.0
 */
public interface IAssessAnnualVacationService extends IService<AssessAnnualVacation> {

    List<AssessAnnualVacation> queryByLeaderIds(String ids, String year);

    String judgeVacationByPersonId(String uid, String year);

    List<AssessAnnualVacation> getAssessAnnualVacation(String currentYear, String depart);

    void updateAnnualSummarySuggestion(AssessAnnualVacation entity);

}
