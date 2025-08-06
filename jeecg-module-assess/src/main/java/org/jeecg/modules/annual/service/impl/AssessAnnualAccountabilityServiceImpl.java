package org.jeecg.modules.annual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.annual.service.IAssessAnnualAccountabilityService;
import org.jeecg.modules.annual.service.IAssessAnnualFillService;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.AssessAnnualAccountability;
import org.jeecg.modules.sys.entity.annual.AssessAnnualSummary;
import org.jeecg.modules.sys.mapper.annual.AssessAnnualAccountabilityMapper;
import org.jeecg.modules.sys.mapper.annual.AssessAnnualSummaryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Description: 年度考核受问责情况
 * @Author: jeecg-boot
 * @Date: 2024-09-02
 * @Version: V1.0
 */
@Service
public class AssessAnnualAccountabilityServiceImpl extends ServiceImpl<AssessAnnualAccountabilityMapper, AssessAnnualAccountability> implements IAssessAnnualAccountabilityService {
    @Autowired
    private DepartCommonApi commonApi;
    @Autowired
    private AssessAnnualAccountabilityMapper accountabilityMapper;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Lazy
    @Autowired
    private IAssessAnnualFillService annualFillService;
    @Autowired
    private AssessAnnualSummaryMapper annualSummaryMapper;

    @Override
    public Map<String, String> getCurrentDepart() {
        return commonApi.getCurrentUserDepart();
    }

    @Override
    public List<AssessAnnualAccountability> queryByLeaderIds(String ids, String year) {
        List<String> list = Arrays.asList(ids.split(","));

        LambdaQueryWrapper<AssessAnnualAccountability> qw = new LambdaQueryWrapper<>();
        qw.eq(AssessAnnualAccountability::getCurrentYear, year);
        qw.in(AssessAnnualAccountability::getHashId, list);
        return accountabilityMapper.selectList(qw);
    }

    /**
     * 判断当前日期是否在问责期内
     *
     * @param uid
     * @return Y:在问责期内 N:不在问责期内
     */
    @Override
    public String judgeAccountabilityByPersonId(String uid) {
        LambdaQueryWrapper<AssessAnnualAccountability> qw = new LambdaQueryWrapper<>();
        qw.eq(AssessAnnualAccountability::getHashId, uid);

        List<AssessAnnualAccountability> list = accountabilityMapper.selectList(qw);

        long now = System.currentTimeMillis();

        for (AssessAnnualAccountability accountability : list) {
            Date endDate = accountability.getEndDate();
            long endTime = endDate.getTime();
            if (now <= endTime) {
                return "Y";
            }
        }
        return "N";
    }

    /**
     * 问责信息查询
     */
    @Override
    public List<AssessAnnualAccountability> getAccountabilityInformationQuery(String currentYear, String depart) {
        if ("0".equals(currentYear)) {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
            currentYear = currentAssessInfo.getCurrentYear();
        }
        LambdaQueryWrapper<AssessAnnualAccountability> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualAccountability::getCurrentYear, currentYear);
        if (!"0".equals(depart)) lqw.eq(AssessAnnualAccountability::getDepart, depart);
        return accountabilityMapper.selectList(lqw);

//        return accountabilityMapper.getAccountabilityInformationQuery(currentYear, depart);
    }

    @Override
    public void updateAnnualSummarySuggestion(AssessAnnualAccountability entity) {
        String hashId = entity.getHashId();
        String currentYear = entity.getCurrentYear();
        LambdaQueryWrapper<AssessAnnualSummary> qw = new LambdaQueryWrapper<>();
        qw.eq(AssessAnnualSummary::getHashId, hashId);
        qw.eq(AssessAnnualSummary::getCurrentYear, currentYear);
        AssessAnnualSummary summary = annualSummaryMapper.selectOne(qw);
        if (summary != null) {
            annualFillService.getEvaluationSuggestion(summary);
            annualSummaryMapper.updateById(summary);
        }

    }
}
