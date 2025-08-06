package org.jeecg.modules.annual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jeecg.modules.annual.service.IAssessAnnualFillService;
import org.jeecg.modules.annual.service.IAssessExcellentNumService;
import org.jeecg.modules.annual.service.IAssessWorkDaysService;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.AssessAnnualSummary;
import org.jeecg.modules.sys.entity.annual.AssessAnnualVacation;
import org.jeecg.modules.sys.entity.annual.AssessExcellentNum;
import org.jeecg.modules.sys.entity.annual.AssessWorkDays;
import org.jeecg.modules.sys.mapper.annual.AssessAnnualSummaryMapper;
import org.jeecg.modules.sys.mapper.annual.AssessAnnualVacationMapper;
import org.jeecg.modules.sys.mapper.annual.AssessExcellentNumMapper;
import org.jeecg.modules.sys.mapper.annual.AssessWorkDaysMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description: 年度考核优秀比例设置
 * @Author: jeecg-boot
 * @Date: 2024-09-16
 * @Version: V1.0
 */
@Service
public class AssessWorkDaysServiceImpl extends ServiceImpl<AssessWorkDaysMapper, AssessWorkDays> implements IAssessWorkDaysService {
    @Autowired
    private AssessWorkDaysMapper daysMapper;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private AssessAnnualVacationMapper vacationMapper;
    @Autowired
    private AssessAnnualSummaryMapper summaryMapper;
    @Autowired
    private IAssessAnnualFillService fillService;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public Integer getCurrentYearWorkDays() {
        AssessCurrentAssess annual = assessCommonApi.getCurrentAssessInfo("annual");
        LambdaQueryWrapper<AssessWorkDays> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessWorkDays::getCurrentYear, annual.getCurrentYear());

        List<AssessWorkDays> assessWorkDays = daysMapper.selectList(lqw);
        if (!assessWorkDays.isEmpty()) {
            return assessWorkDays.get(0).getWorkDays() == null ? 0 : assessWorkDays.get(0).getWorkDays();
        }

        return 0;
    }

    @Override
    public void updateWorkDays(Integer days) {
        AssessCurrentAssess annual = assessCommonApi.getCurrentAssessInfo("annual");
        LambdaQueryWrapper<AssessWorkDays> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessWorkDays::getCurrentYear, annual.getCurrentYear());

        List<AssessWorkDays> assessWorkDays = daysMapper.selectList(lqw);
        if (assessWorkDays.isEmpty()) {
            AssessWorkDays workDays = new AssessWorkDays();
            workDays.setWorkDays(days);
            workDays.setCurrentYear(annual.getCurrentYear());
            this.save(workDays);
        } else {
            AssessWorkDays workDays = assessWorkDays.get(0);
            workDays.setWorkDays(days);
            this.updateById(workDays);
        }

        LambdaQueryWrapper<AssessAnnualVacation> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(AssessAnnualVacation::getCurrentYear, annual.getCurrentYear());
        List<AssessAnnualVacation> allVacations = vacationMapper.selectList(lqw2);

        if (!allVacations.isEmpty()) {
            // 将allVacations中的hashId取出存入List，去重
            List<String> hashIds = allVacations.stream().map(AssessAnnualVacation::getHashId).distinct().collect(Collectors.toList());
            LambdaQueryWrapper<AssessAnnualSummary> lqw3 = new LambdaQueryWrapper<>();
            lqw3.eq(AssessAnnualSummary::getCurrentYear, annual.getCurrentYear());
            lqw3.in(AssessAnnualSummary::getHashId, hashIds);
            List<AssessAnnualSummary> allSummaries = summaryMapper.selectList(lqw3);
            List<AssessAnnualSummary> summaries = fillService.getEvaluationSuggestion(allSummaries);

            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
            AssessAnnualSummaryMapper summaryMapperNew = sqlSession.getMapper(AssessAnnualSummaryMapper.class);
            summaries.forEach(summaryMapperNew::updateById);

            sqlSession.commit();
            sqlSession.clearCache();
            sqlSession.close();
        }


    }
}
