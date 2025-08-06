package org.jeecg.modules.annual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.annual.service.IAssessAnnualFillService;
import org.jeecg.modules.annual.service.IAssessAnnualNegativeListService;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.AssessAnnualAccountability;
import org.jeecg.modules.sys.entity.annual.AssessAnnualNegativeList;
import org.jeecg.modules.sys.entity.annual.AssessAnnualSummary;
import org.jeecg.modules.sys.mapper.annual.AssessAnnualNegativeListMapper;
import org.jeecg.modules.sys.mapper.annual.AssessAnnualSummaryMapper;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.user.UserCommonApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @Description: 年度考核负面清单
 * @Author: jeecg-boot
 * @Date:   2024-11-03
 * @Version: V1.0
 */
@Service
public class AssessAnnualNegativeListServiceImpl extends ServiceImpl<AssessAnnualNegativeListMapper, AssessAnnualNegativeList> implements IAssessAnnualNegativeListService {

    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private UserCommonApi userCommonApi;
    @Autowired
    private DepartCommonApi departCommonApi;
    @Autowired
    private AssessAnnualNegativeListMapper listMapper;
    @Autowired
    private AssessAnnualSummaryMapper annualSummaryMapper;
    @Lazy
    @Autowired
    private IAssessAnnualFillService annualFillService;

    @Deprecated
    @Override
    public List<AssessAnnualNegativeList> getDict(List<AssessAnnualNegativeList> records) {
        for (AssessAnnualNegativeList record : records) {
            if ("1".equals(record.getType())){
                SysDepart sysDepart = departCommonApi.queryDepartById(record.getDepart());
            }
        }
        return records;
    }

    @Override
    public void passOrReject(String id, String status) {
        AssessAnnualNegativeList list = listMapper.selectById(id);
        if (status.equals(list.getStatus())){
            throw new JeecgBootException("已是当前状态，无需操作！");
        }
        list.setStatus(status);
        listMapper.updateById(list);
    }

    @Override
    public List<AssessAnnualNegativeList> getByCurrentDep() {
        Map<String, String> currentUserDepart = departCommonApi.getCurrentUserDepart();

        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");

        if (currentUserDepart != null && currentAssessInfo != null){
            String departId = currentUserDepart.get("departId");
            LambdaQueryWrapper<AssessAnnualNegativeList> lqw = new LambdaQueryWrapper<>();
            lqw.eq(AssessAnnualNegativeList::getReportDepart, departId);
            lqw.eq(AssessAnnualNegativeList::getCurrentYear, currentAssessInfo.getCurrentYear());
            return listMapper.selectList(lqw);
        }

        return Collections.emptyList();
    }

    @Override
    public boolean passOrDelete(String id, Integer var) {
        AssessAnnualNegativeList list = listMapper.selectById(id);
        if (list == null){
            throw new JeecgBootException("数据不存在！");
        }
        if (0 == var){
            return this.removeById(id);
        }
        if (1 == var){
            list.setStatus("2");
            int i = listMapper.updateById(list);
            return i > 0;
        }
        return false;
    }

    @Override
    public void updateAnnualSummarySuggestion(AssessAnnualNegativeList entity) {
        String currentYear = entity.getCurrentYear();
        LambdaQueryWrapper<AssessAnnualSummary> qw = new LambdaQueryWrapper<>();

        String hashId = entity.getHashId();
        String depart = entity.getDepart();
        if (hashId != null){
            qw.eq(AssessAnnualSummary::getHashId, hashId);
        } else {
            qw.eq(AssessAnnualSummary::getDepart, depart);
            qw.eq(AssessAnnualSummary::getHashId, "group");
        }

        qw.eq(AssessAnnualSummary::getCurrentYear, currentYear);
        AssessAnnualSummary summary = annualSummaryMapper.selectOne(qw);
        if (summary != null) {
            AssessAnnualSummary evaluationSuggestion = annualFillService.getEvaluationSuggestion(summary);
            annualSummaryMapper.updateById(evaluationSuggestion);
        }

    }
}
