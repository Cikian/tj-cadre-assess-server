package org.jeecg.modules.annual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.system.vo.SysDepartModel;
import org.jeecg.modules.annual.service.IAssessLeaderRecService;
import org.jeecg.modules.annual.vo.LeaderRecommendListVO;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.*;
import org.jeecg.modules.sys.entity.business.AssessLeaderDepartConfig;
import org.jeecg.modules.sys.mapper.annual.*;
import org.jeecg.modules.sys.mapper.business.AssessLeaderDepartConfigMapper;
import org.jeecg.modules.user.UserCommonApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 业务考核等次默认比例
 * @Author: jeecg-boot
 * @Date: 2024-10-29
 * @Version: V1.0
 */
@Service
public class AssessLeaderRecServiceImpl extends ServiceImpl<AssessLeaderRecMapper, AssessLeaderRec> implements IAssessLeaderRecService {
    @Autowired
    private AssessLeaderRecMapper recMapper;
    @Autowired
    private AssessLeaderRecItemMapper recItemMapper;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private DepartCommonApi departCommonApi;
    @Autowired
    private AssessLeaderDepartConfigMapper leaderDepartConfigMapper;
    @Autowired
    private AssessAnnualNegativeListMapper negativeListMapper;
    @Autowired
    private AssessAssistConfigMapper assistConfigMapper;
    @Autowired
    private AssessLeaderConfigMapper leaderConfigMapper;
    @Autowired
    private AssessAnnualSummaryMapper summaryMapper;
    @Autowired
    private AssessAnnualAccountabilityMapper accountabilityMapper;
    @Autowired
    private AssessDemocraticEvaluationSummaryMapper evaluationSummaryMapper;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private UserCommonApi userCommonApi;

    @Override
    public void initRec() {
        AssessCurrentAssess annual = assessCommonApi.getCurrentAssessInfo("annual");
        if (annual == null || !annual.isAssessing()) {
            throw new JeecgBootException("当前无进行中的年度考核，无法初始化推优数据！");
        }

        LambdaQueryWrapper<AssessLeaderRec> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssessLeaderRec::getCurrentYear, annual.getCurrentYear());
        List<AssessLeaderRec> assessLeaderRecs = recMapper.selectList(queryWrapper);

        if (!assessLeaderRecs.isEmpty()) {
            throw new JeecgBootException("当前年度考核推优已经初始化，无法重复初始化！");
        }

        String year = annual.getCurrentYear();

        List<AssessLeaderDepartConfig> ldConfigs = leaderDepartConfigMapper.selectList(null);
        if (ldConfigs.isEmpty()) {
            throw new JeecgBootException("请先配置局领导分管处室配置！");
        }

        List<AssessLeaderRec> recList = new ArrayList<>();
        List<AssessLeaderRecItem> itemList = new ArrayList<>();

        for (AssessLeaderDepartConfig ldConfig : ldConfigs) {
            String leaderId = ldConfig.getLeaderId();
            // 主表
            String mainId = IdWorker.getIdStr();
            AssessLeaderRec rec = new AssessLeaderRec();
            rec.setId(mainId);
            rec.setLeader(leaderId);
            rec.setCurrentYear(year);
            rec.setStatus("1");
            recList.add(rec);

            // 附表
            String departIds = ldConfig.getDepartId();
            List<String> departIdList = Arrays.asList(departIds.split(","));

            List<String> specialPeople = new ArrayList<>();

            LambdaQueryWrapper<AssessAssistConfig> configQw1 = new LambdaQueryWrapper<>();
            configQw1.like(AssessAssistConfig::getLeader, leaderId);
            List<AssessAssistConfig> assistConfig = assistConfigMapper.selectList(configQw1);
            for (AssessAssistConfig config : assistConfig) {
                if (!specialPeople.contains(config.getHashId())) {
                    specialPeople.add(config.getHashId());
                }
            }

            LambdaQueryWrapper<AssessLeaderConfig> configQw2 = new LambdaQueryWrapper<>();
            configQw2.eq(AssessLeaderConfig::getLeader, leaderId);
            List<AssessLeaderConfig> leaderConfig = leaderConfigMapper.selectList(null);
            for (AssessLeaderConfig config : leaderConfig) {
                if (!specialPeople.contains(config.getHashId())) {
                    specialPeople.add(config.getHashId());
                }
            }

            LambdaQueryWrapper<AssessAnnualSummary> lqw2 = new LambdaQueryWrapper<>();
            lqw2.eq(AssessAnnualSummary::getCurrentYear, year);
            lqw2.in(AssessAnnualSummary::getDepart, departIdList)
                    .and(i -> i.notIn(AssessAnnualSummary::getHashId, specialPeople));
            // lqw2.notIn(AssessAnnualSummary::getHashId, specialPeople);
            List<AssessAnnualSummary> summaries = summaryMapper.selectList(lqw2);
            lqw2.clear();

            leaderConfig = leaderConfig.stream().filter(config -> !config.getLeader().equals(leaderId)).collect(Collectors.toList());
            // 从specialPeople中移除过滤后的人
            for (AssessLeaderConfig config : leaderConfig) {
                specialPeople.remove(config.getHashId());
            }

            lqw2.eq(AssessAnnualSummary::getCurrentYear, year);
            lqw2.in(AssessAnnualSummary::getHashId, specialPeople);
            if (!specialPeople.isEmpty()) {
                List<AssessAnnualSummary> specialSummaries = summaryMapper.selectList(lqw2);
                summaries.addAll(specialSummaries);
            }

            LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw3 = new LambdaQueryWrapper<>();
            lqw3.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year);

            List<AssessDemocraticEvaluationSummary> evaluationSummaries = evaluationSummaryMapper.selectList(lqw3);
            List<SysDepartModel> sysDepartModels = departCommonApi.queryAllDepart();

            // 对summaries进行排序，通过personOrder字段，升序，如果personOder为空，则排在最后
            // summaries.sort(Comparator.comparing(AssessAnnualSummary::getPersonOrder, Comparator.nullsLast(Comparator.naturalOrder())));

            for (AssessAnnualSummary summary : summaries) {

                AssessLeaderRecItem vo = new AssessLeaderRecItem();
                vo.setId(IdWorker.getIdStr());
                vo.setMainId(mainId);

                vo.setHashId(summary.getHashId());
                // 设置部门
                sysDepartModels.stream()
                        .filter(depart -> depart.getId().equals(summary.getDepart()))
                        .findFirst()
                        .ifPresent(depart -> vo.setDepart(depart.getAlias()));

                vo.setName(summary.getPerson());  // 设置姓名
                vo.setDuties(summary.getDuties());// 设置职务
                vo.setPost(summary.getPosition());// 设置职级
                vo.setType(summary.getType().equals("chief") ? "正职" : summary.getType().equals("deputy") ? "副职" : summary.getType().equals("chiefPro") ? "正职兼总师、二巡" : "其他");// 设置类别

                // 设置民主测评考核单元内排名
                if (!"group".equals(summary.getRecommendType())) {
                    AssessDemocraticEvaluationSummary s = evaluationSummaries.stream()
                            .filter(e -> summary.getHashId().equals(e.getAppraisee()))
                            .findFirst()
                            .orElse(null);

                    if (s != null) {
                        vo.setDemocraticRanking(s.getScopeRanking());
                    }
                } else {
                    AssessDemocraticEvaluationSummary s = evaluationSummaries.stream()
                            .filter(e -> summary.getDepart().equals(e.getDepart()) && e.getAppraisee().equals("group"))
                            .findFirst()
                            .orElse(null);

                    if (s != null) {
                        vo.setDemocraticRanking(s.getScopeRanking());
                    }
                }

                // case "1":
                //     vo.setSuggestion("建议评优");
                //     vo.setCanExcellent(true);
                //     break;
                // case "2":
                //     vo.setSuggestion("可以评优");
                //     vo.setCanExcellent(true);
                //     break;
                if (summary.getEvaluation().equals("3")) {
                    vo.setSuggestion("不可以评优");
                    vo.setCanExcellent(false);
                } else {
                    vo.setCanExcellent(true);
                }

                // 设置不能评优情况汇总
                vo.setRemark(summary.getRemark());
                // 设置参考信息
                vo.setNegativeList(summary.getNegativeList());
                // 设置是否纪检组推荐
                vo.setDisciplineRecommend("Y".equals(summary.getDisciplineRecommend()));
                // 设置推优类型
                vo.setRecommendType(summary.getRecommendType());
                Boolean leaderRecommend = summary.getLeaderRecommend();
                vo.setLeaderRecommend(leaderRecommend);
                // 设置连续两年评为优秀
                vo.setTwoYear("Y".equals(summary.getThirdClass()) ? "是" : "否");
                // 设置优秀副职
                vo.setExcellentDeputy("Y".equals(summary.getExcellentDeputy()) ? "推荐优秀" : "");

                vo.setPersonOrder(summary.getPersonOrder() == null ? "999" : summary.getPersonOrder());

                if ("局管总师".equals(summary.getPosition()) || "二级巡视员".equals(summary.getPosition()))
                    vo.setDepartName("局管总师二巡");

                itemList.add(vo);
            }
        }

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        AssessLeaderRecMapper recMapperNew = sqlSession.getMapper(AssessLeaderRecMapper.class);
        AssessLeaderRecItemMapper recItemMapperNew = sqlSession.getMapper(AssessLeaderRecItemMapper.class);

        recList.forEach(recMapperNew::insert);
        itemList.forEach(recItemMapperNew::insert);

        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();
    }

    @Override
    public void flush() {
        AssessCurrentAssess annual = assessCommonApi.getCurrentAssessInfo("annual");
        if (annual == null || !annual.isAssessing()) {
            throw new JeecgBootException("当前年度考核已结束，无法刷新推优数据！");
        }

        String year = annual.getCurrentYear();

        LambdaQueryWrapper<AssessLeaderRec> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessLeaderRec::getCurrentYear, year);
        List<AssessLeaderRec> recList = recMapper.selectList(lqw);
        List<String> recIds = new ArrayList<>();
        if (!recList.isEmpty()) {
            for (AssessLeaderRec rec : recList) {
                if (!"1".equals(rec.getStatus())) {
                    throw new JeecgBootException("推优信息已推送至局领导，无法刷新！");
                }
                recIds.add(rec.getId());
            }
        }
        // 删除附表
        LambdaQueryWrapper<AssessLeaderRecItem> lqw2 = new LambdaQueryWrapper<>();
        lqw2.in(AssessLeaderRecItem::getMainId, recIds);
        recItemMapper.delete(lqw2);
        // 删除主表
        recMapper.delete(lqw);
        // 重新初始化
        this.initRec();
    }

    @Override
    public void pushDown(String id) {
        AssessLeaderRec rec = recMapper.selectById(id);
        if (rec == null) {
            throw new RuntimeException("不存在该记录");
        }

        // if (!"1".equals(rec.getStatus())) {
        //     throw new RuntimeException("该记录已推送，不可重复推送！");
        // }

        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        List<String> roles1 = Collections.singletonList("department_cadre_admin");
        boolean b1 = userCommonApi.hasRole(sysUser.getUsername(), roles1);
        if (b1) {
            if (!rec.getStatus().equals("1")){
                throw new JeecgBootException("当前不属于您的操作节点，无需推送！");
            }
            rec.setStatus("5"); // 推送至副处长
        }
        List<String> roles2 = Collections.singletonList("gbc_fcz");
        boolean b2 = userCommonApi.hasRole(sysUser.getUsername(), roles2);
        if (b2) {
            if (!rec.getStatus().equals("5")){
                throw new JeecgBootException("当前不属于您的操作节点，无需推送！");
            }
            rec.setStatus("6"); // 推送至正处长
        }
        List<String> roles3 = Collections.singletonList("gbc_cz");
        boolean b3 = userCommonApi.hasRole(sysUser.getUsername(), roles3);
        if (b3) {
            if (!rec.getStatus().equals("6")){
                throw new JeecgBootException("当前不属于您的操作节点，无需推送！");
            }
            rec.setStatus("2"); // 推送至局领导
        }
        recMapper.updateById(rec);
    }

    @Override
    public void reject(String leader) {
        AssessCurrentAssess assess = assessCommonApi.getCurrentAssessInfo("annual");
        if (assess == null || !assess.isAssessing()) {
            throw new JeecgBootException("当前年度考核已结束，无法重新推优！");
        }

        // 查询

        // 1. AssessAnnualSummary复原

    }

    @Override
    public AssessLeaderRec getByLeaderId() {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
        if (currentAssessInfo == null) {
            throw new JeecgBootException("当前没有考核信息");
        }

        LambdaQueryWrapper<AssessLeaderRec> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessLeaderRec::getLeader, sysUser.getId());
        lqw.eq(AssessLeaderRec::getCurrentYear, currentAssessInfo.getCurrentYear());

        return recMapper.selectList(lqw).get(0);
    }
}
