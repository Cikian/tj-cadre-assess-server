package org.jeecg.modules.annual.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.sys.entity.annual.AssessAnnualAccountability;

import java.util.List;
import java.util.Map;

/**
 * @Description: 年度考核受问责情况
 * @Author: jeecg-boot
 * @Date: 2024-09-02
 * @Version: V1.0
 */
public interface IAssessAnnualAccountabilityService extends IService<AssessAnnualAccountability> {

    Map<String, String> getCurrentDepart();

    List<AssessAnnualAccountability> queryByLeaderIds(String ids, String year);

    // 判断是否被问责
    String judgeAccountabilityByPersonId(String id);

    // 问责信息导出
    List<AssessAnnualAccountability> getAccountabilityInformationQuery(String currentYear, String depart);

    void updateAnnualSummarySuggestion(AssessAnnualAccountability entity);
}
