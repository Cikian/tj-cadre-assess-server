package org.jeecg.modules.annual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.annual.service.IAssessAnnualFillService;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.AssessAnnualAccountability;
import org.jeecg.modules.sys.entity.annual.AssessAnnualSummary;
import org.jeecg.modules.sys.entity.annual.AssessAnnualVacation;
import org.jeecg.modules.sys.mapper.annual.AssessAnnualSummaryMapper;
import org.jeecg.modules.sys.mapper.annual.AssessAnnualVacationMapper;
import org.jeecg.modules.annual.service.IAssessAnnualVacationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Description: 年度考核休假情况
 * @Author: jeecg-boot
 * @Date: 2024-09-03
 * @Version: V1.0
 */
@Service
public class AssessAnnualVacationServiceImpl extends ServiceImpl<AssessAnnualVacationMapper, AssessAnnualVacation> implements IAssessAnnualVacationService {
    @Autowired
    private AssessAnnualVacationMapper vacationMapper;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private AssessAnnualSummaryMapper annualSummaryMapper;
    @Autowired
    @Lazy
    private IAssessAnnualFillService annualFillService;

    @Override
    public boolean save(AssessAnnualVacation entity) {
        Date startDate = entity.getStartDate();
        Date endDate = entity.getEndDate();
        if (startDate == null || endDate == null) {
            throw new JeecgBootException("请选择起止时间！");
        }
        return super.save(entity);
    }

    @Override
    public List<AssessAnnualVacation> queryByLeaderIds(String ids, String year) {
        List<String> list = Arrays.asList(ids.split(","));

        LambdaQueryWrapper<AssessAnnualVacation> qw = new LambdaQueryWrapper<>();
        qw.eq(AssessAnnualVacation::getCurrentYear, year);
        qw.in(AssessAnnualVacation::getHashId, list);
        return vacationMapper.selectList(qw);
    }

    @Override
    public String judgeVacationByPersonId(String uid, String year) {
        LambdaQueryWrapper<AssessAnnualVacation> qw = new LambdaQueryWrapper<>();
        qw.eq(AssessAnnualVacation::getHashId, uid);
        qw.eq(AssessAnnualVacation::getCurrentYear, year);
        List<AssessAnnualVacation> assessAnnualVacations = vacationMapper.selectList(qw);
        if (assessAnnualVacations != null && !assessAnnualVacations.isEmpty()) {
            return "Y";
        }

        return "N";
    }

    @Override
    public List<AssessAnnualVacation> getAssessAnnualVacation(String currentYear, String depart) {
        if ("0".equals(currentYear)) {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
            currentYear = currentAssessInfo.getCurrentYear();
        }
        LambdaQueryWrapper<AssessAnnualVacation> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualVacation::getCurrentYear, currentYear);
        if (!"0".equals(depart)) lqw.eq(AssessAnnualVacation::getDepart, depart);

        return vacationMapper.selectList(lqw);
//        return vacationMapper.getAssessAnnualVacation(currentYear, depart);
    }

    @Override
    public void updateAnnualSummarySuggestion(AssessAnnualVacation entity) {
        String hashId = entity.getHashId();
        String currentYear = entity.getCurrentYear();
        LambdaQueryWrapper<AssessAnnualSummary> qw = new LambdaQueryWrapper<>();
        qw.eq(AssessAnnualSummary::getHashId, hashId);
        qw.eq(AssessAnnualSummary::getCurrentYear, currentYear);
        AssessAnnualSummary summary = annualSummaryMapper.selectOne(qw);
        if (summary != null) {
            List<AssessAnnualSummary> list = new ArrayList<>();
            list.add(summary);
            annualFillService.getEvaluationSuggestion(list);

            annualSummaryMapper.updateById(list.get(0));
        }

    }
}
