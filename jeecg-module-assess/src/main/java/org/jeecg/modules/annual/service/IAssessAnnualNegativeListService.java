package org.jeecg.modules.annual.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.sys.entity.annual.AssessAnnualAccountability;
import org.jeecg.modules.sys.entity.annual.AssessAnnualNegativeList;

import java.util.List;

/**
 * @Description: 年度考核负面清单
 * @Author: jeecg-boot
 * @Date:   2024-11-03
 * @Version: V1.0
 */
public interface IAssessAnnualNegativeListService extends IService<AssessAnnualNegativeList> {

    List<AssessAnnualNegativeList> getDict(List<AssessAnnualNegativeList> records);

    void passOrReject(String id, String status);

    List<AssessAnnualNegativeList> getByCurrentDep();

    boolean passOrDelete(String id, Integer var);

    void updateAnnualSummarySuggestion(AssessAnnualNegativeList entity);

}
