package org.jeecg.modules.annual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.Synchronized;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.*;
import org.jeecg.modules.sys.entity.business.AssessLeaderDepartConfig;
import org.jeecg.modules.sys.mapper.annual.*;
import org.jeecg.modules.annual.service.IAssessDemocraticEvaluationService;
import org.jeecg.modules.annual.service.IAssessDemocraticEvaluationSummaryService;
import org.jeecg.modules.annual.vo.DemocraticFillListVO;
import org.jeecg.modules.annual.vo.DemocraticSubmitPage;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.user.UserCommonApi;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description: 民主测评填报
 * @Author: jeecg-boot
 * @Date: 2024-09-12
 * @Version: V1.0
 */
@Service
public class AssessDemocraticEvaluationServiceImpl extends ServiceImpl<AssessDemocraticEvaluationMapper, AssessDemocraticEvaluation> implements IAssessDemocraticEvaluationService {

    @Autowired
    private AssessDemocraticEvaluationMapper evaluationMapper;
    @Autowired
    private AssessDemocraticEvaluationSummaryMapper summaryMapper;
    @Autowired
    private AssessDemocraticEvaluationItemMapper itemMapper;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private IAssessDemocraticEvaluationSummaryService summaryService;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private UserCommonApi userCommonApi;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private AssessLeaderConfigMapper leaderConfigMapper;
    @Autowired
    private AssessAssistConfigMapper assistConfigMapper;
    @Autowired
    private AssessAnnualSummaryMapper annualSummaryMapper;
    @Autowired
    private AssessDemocraticEvaluationMapper democraticEvaluationMapper;

    @Override
    public List<AssessDemocraticEvaluation> selectByMainId(String mainId) {
        return evaluationMapper.selectByMainId(mainId);
    }

    @Override
    public List<DemocraticFillListVO> getAnonymousList() {
        // 获取当前登录用户的部门id
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<String> roles = userCommonApi.getUserRolesByUserName(sysUser.getUsername());

        String departIds = sysUser.getDepartIds();
        if (departIds == null) {
            return Collections.emptyList();
        }
        List<String> departIdList = Arrays.asList(departIds.split(","));

        AssessCurrentAssess annual = assessCommonApi.getCurrentAssessInfo("annual");
        if (annual == null || !annual.isAssessing()) {
            return Collections.emptyList();
        }

        // 查询当前用户所在部门的所有民主测评填报
        LambdaQueryWrapper<AssessDemocraticEvaluationSummary> qw = new LambdaQueryWrapper<>();

        if (roles.contains("director_leader")) {
            List<String> specialPeople = new ArrayList<>();

            // LambdaQueryWrapper<AssessAssistConfig> configQw1 = new LambdaQueryWrapper<>();
            // configQw1.like(AssessAssistConfig::getLeader, sysUser.getId());
            List<AssessAssistConfig> assistConfig = assistConfigMapper.selectList(null);
            for (AssessAssistConfig config : assistConfig) {
                if (!specialPeople.contains(config.getHashId())) {
                    specialPeople.add(config.getHashId());
                }
            }

            // LambdaQueryWrapper<AssessLeaderConfig> configQw2 = new LambdaQueryWrapper<>();
            // configQw2.eq(AssessLeaderConfig::getLeader, sysUser.getId());
            List<AssessLeaderConfig> leaderConfig = leaderConfigMapper.selectList(null);
            for (AssessLeaderConfig config : leaderConfig) {
                if (!specialPeople.contains(config.getHashId())) {
                    specialPeople.add(config.getHashId());
                }
            }


            if (!specialPeople.isEmpty()) {
                qw.notIn(AssessDemocraticEvaluationSummary::getAppraisee, specialPeople);
            }
        }

        qw.in(AssessDemocraticEvaluationSummary::getDepart, departIdList);
        qw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, annual.getCurrentYear());
        qw.orderByDesc(AssessDemocraticEvaluationSummary::getType);
        List<AssessDemocraticEvaluationSummary> summaries = summaryMapper.selectList(qw);

        qw.clear();

        if (departIdList.contains("JG")) {
            // 获取当前账号所在考核单元
            AssessLeaderDepartConfig unit = null;
            if (roles.contains("director_leader")) {
                unit = assessCommonApi.getAssessUnitByLeader(sysUser.getId());
            } else {
                unit = assessCommonApi.getAssessUnitByDepart(sysUser.getDepart());
            }
            String unitDeparts = unit.getDepartId();
            List<String> unitDepartList = Arrays.asList(unitDeparts.split(","));

            // departIdList中所有元素前面加上JG
            List<String> departIdListWithJG = unitDepartList.stream().map(departId -> "JG" + departId).collect(Collectors.toList());
            qw.in(AssessDemocraticEvaluationSummary::getDepart, departIdListWithJG);
            qw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, annual.getCurrentYear());
            qw.orderByDesc(AssessDemocraticEvaluationSummary::getType);
            List<AssessDemocraticEvaluationSummary> summariesWithJG = summaryMapper.selectList(qw);
            summaries.addAll(summariesWithJG);
            qw.clear();
        }

        if (!roles.contains("department_cadre_admin")) {
            // 局领导不测评基层副职
            if (roles.contains("director_leader")) {
                // 过滤：去除departType不为bureau的并且type为22的记录
                summaries = summaries.stream()
                        .filter(summary -> !summary.getType().equals("31"))
                        .collect(Collectors.toList());


                LambdaQueryWrapper<AssessLeaderConfig> lqw = new LambdaQueryWrapper<>();
                lqw.eq(AssessLeaderConfig::getLeader, sysUser.getId());
                List<AssessLeaderConfig> leaderConfigs = leaderConfigMapper.selectList(lqw);

                if (leaderConfigs != null && !leaderConfigs.isEmpty()) {
                    List<String> personHashId = leaderConfigs.stream().map(AssessLeaderConfig::getHashId).collect(Collectors.toList());

                    qw.in(AssessDemocraticEvaluationSummary::getAppraisee, personHashId);
                    qw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, annual.getCurrentYear());
                    qw.orderByDesc(AssessDemocraticEvaluationSummary::getType);
                    List<AssessDemocraticEvaluationSummary> s = summaryMapper.selectList(qw);
                    qw.clear();

                    if (s != null && !s.isEmpty()) {
                        summaries.addAll(s);
                    }
                }

                LambdaQueryWrapper<AssessAssistConfig> lqw2 = new LambdaQueryWrapper<>();
                lqw2.like(AssessAssistConfig::getLeader, sysUser.getId());
                List<AssessAssistConfig> assistConfigs = assistConfigMapper.selectList(lqw2);

                if (assistConfigs != null && !assistConfigs.isEmpty()) {
                    List<String> personHashId = assistConfigs.stream().map(AssessAssistConfig::getHashId).collect(Collectors.toList());
                    qw.in(AssessDemocraticEvaluationSummary::getAppraisee, personHashId);
                    qw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, annual.getCurrentYear());
                    qw.orderByDesc(AssessDemocraticEvaluationSummary::getType);
                    List<AssessDemocraticEvaluationSummary> s = summaryMapper.selectList(qw);
                    qw.clear();
                    if (s != null && !s.isEmpty()) {
                        summaries.addAll(s);
                    }
                }

                if (!summaries.isEmpty()) {
                    List<String> dSummaryIds = summaries.stream().map(AssessDemocraticEvaluationSummary::getId).collect(Collectors.toList());

                    LambdaQueryWrapper<AssessDemocraticEvaluation> lqw3 = new LambdaQueryWrapper<>();
                    lqw3.in(AssessDemocraticEvaluation::getEvaluationSummaryId, dSummaryIds);
                    lqw3.eq(AssessDemocraticEvaluation::getCreateBy, sysUser.getUsername());
                    List<AssessDemocraticEvaluation> assessDemocraticEvaluations = democraticEvaluationMapper.selectList(lqw3);

                    if (assessDemocraticEvaluations != null && !assessDemocraticEvaluations.isEmpty()) {
                        List<String> filledIds = assessDemocraticEvaluations.stream().map(AssessDemocraticEvaluation::getEvaluationSummaryId).collect(Collectors.toList());


                        if (!filledIds.isEmpty())
                            summaries = summaries.stream().filter(summary -> !filledIds.contains(summary.getId())).collect(Collectors.toList());
                    }
                }
            }


            // 基层A票只查询副职
//            if (!roles.contains("director_leader") && "basic_AB".equals(sysUser.getVoteType())) {
//                summaries = summaries.stream()
//                        .filter(summary -> summary.getType().equals("31"))
//                        .collect(Collectors.toList());
//            }

            // 机关C票不查询总师、二巡
            if (!roles.contains("director_leader") && "bureau_C".equals(sysUser.getVoteType())) {
                summaries = summaries.stream()
                        .filter(summary -> !summary.getType().equals("23"))
                        .collect(Collectors.toList());
            }
        }

        List<String> allPeople = new ArrayList<>();

        // 封装返回数据
        if (summaries != null && !summaries.isEmpty()) {
            List<DemocraticFillListVO> list = new ArrayList<>();
            Set<String> allIds = new HashSet<>();
            for (AssessDemocraticEvaluationSummary summary : summaries) {
                if (allIds.contains(summary.getId())) {
                    continue;
                }
                DemocraticFillListVO vo = new DemocraticFillListVO();
                vo.setAppraisee(summary.getAppraisee());
                vo.setDepartId(summary.getDepart());
                vo.setAssessName(summary.getAssessName());
                vo.setType(summary.getType());
                vo.setSummaryId(summary.getId());
                vo.setEndDate(summary.getEndDate());
                vo.setCurrentYear(summary.getCurrentYear());
                list.add(vo);
                allPeople.add(summary.getAppraisee());
                allIds.add(summary.getId());
            }

            LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
            lqw.eq(AssessAnnualSummary::getCurrentYear, annual.getCurrentYear());
            lqw.in(AssessAnnualSummary::getHashId, allPeople);
            lqw.ne(AssessAnnualSummary::getHashId, "group");
            lqw.orderByAsc(AssessAnnualSummary::getPersonOrder);
            List<AssessAnnualSummary> annualSummaries = annualSummaryMapper.selectList(lqw);

            if (annualSummaries != null && !annualSummaries.isEmpty()) {
                List<DemocraticFillListVO> groupList = list.stream().filter(vo -> "group".equals(vo.getAppraisee())).collect(Collectors.toList());
                List<DemocraticFillListVO> peopleList = list.stream().filter(vo -> !"group".equals(vo.getAppraisee())).collect(Collectors.toList());


                // 将list根据annualSummaries的顺序排序，annualSummaries中的hashId属性对应list中的appraisee属性
                peopleList.sort(Comparator.comparing(o -> annualSummaries.stream().filter(a -> a.getHashId().equals(o.getAppraisee())).findFirst().get().getPersonOrder()));

                list = new ArrayList<>();
                list.addAll(groupList);
                list.addAll(peopleList);
            }

            return list;
        }

        return Collections.emptyList();
    }

    @Override
    public List<DemocraticFillListVO> getAnonymousList(SysUser sysUser) {
        // 获取当前登录用户的部门id
        List<String> roles = userCommonApi.getUserRolesByUserName(sysUser.getUsername());

        String departIds = sysUser.getDepartIds();
        if (departIds == null) {
            return Collections.emptyList();
        }
        List<String> departIdList = Arrays.asList(departIds.split(","));

        AssessCurrentAssess annual = assessCommonApi.getCurrentAssessInfo("annual");
        if (annual == null || !annual.isAssessing()) {
            return Collections.emptyList();
        }

        // 查询当前用户所在部门的所有民主测评填报
        LambdaQueryWrapper<AssessDemocraticEvaluationSummary> qw = new LambdaQueryWrapper<>();

        if (roles.contains("director_leader")) {
            List<String> specialPeople = new ArrayList<>();

            // LambdaQueryWrapper<AssessAssistConfig> configQw1 = new LambdaQueryWrapper<>();
            // configQw1.like(AssessAssistConfig::getLeader, sysUser.getId());
            List<AssessAssistConfig> assistConfig = assistConfigMapper.selectList(null);
            for (AssessAssistConfig config : assistConfig) {
                if (!specialPeople.contains(config.getHashId())) {
                    specialPeople.add(config.getHashId());
                }
            }

            // LambdaQueryWrapper<AssessLeaderConfig> configQw2 = new LambdaQueryWrapper<>();
            // configQw2.eq(AssessLeaderConfig::getLeader, sysUser.getId());
            List<AssessLeaderConfig> leaderConfig = leaderConfigMapper.selectList(null);
            for (AssessLeaderConfig config : leaderConfig) {
                if (!specialPeople.contains(config.getHashId())) {
                    specialPeople.add(config.getHashId());
                }
            }


            if (!specialPeople.isEmpty()) {
                qw.notIn(AssessDemocraticEvaluationSummary::getAppraisee, specialPeople);
            }
        }

        qw.in(AssessDemocraticEvaluationSummary::getDepart, departIdList);
        qw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, annual.getCurrentYear());
        qw.orderByDesc(AssessDemocraticEvaluationSummary::getType);
        List<AssessDemocraticEvaluationSummary> summaries = summaryMapper.selectList(qw);

        qw.clear();

        if (departIdList.contains("JG")) {
            // 获取当前账号所在考核单元
            AssessLeaderDepartConfig unit = null;
            if (roles.contains("director_leader")) {
                unit = assessCommonApi.getAssessUnitByLeader(sysUser.getId());
            } else {
                unit = assessCommonApi.getAssessUnitByDepart(sysUser.getDepart());
            }
            String unitDeparts = unit.getDepartId();
            List<String> unitDepartList = Arrays.asList(unitDeparts.split(","));

            // departIdList中所有元素前面加上JG
            List<String> departIdListWithJG = unitDepartList.stream().map(departId -> "JG" + departId).collect(Collectors.toList());
            qw.in(AssessDemocraticEvaluationSummary::getDepart, departIdListWithJG);
            qw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, annual.getCurrentYear());
            qw.orderByDesc(AssessDemocraticEvaluationSummary::getType);
            List<AssessDemocraticEvaluationSummary> summariesWithJG = summaryMapper.selectList(qw);
            summaries.addAll(summariesWithJG);
            qw.clear();
        }

        if (!roles.contains("department_cadre_admin")) {
            // 局领导不测评基层副职
            if (roles.contains("director_leader")) {
                // 过滤：去除departType不为bureau的并且type为22的记录
                summaries = summaries.stream()
                        .filter(summary -> !summary.getType().equals("31"))
                        .collect(Collectors.toList());


                LambdaQueryWrapper<AssessLeaderConfig> lqw = new LambdaQueryWrapper<>();
                lqw.eq(AssessLeaderConfig::getLeader, sysUser.getId());
                List<AssessLeaderConfig> leaderConfigs = leaderConfigMapper.selectList(lqw);

                if (leaderConfigs != null && !leaderConfigs.isEmpty()) {
                    List<String> personHashId = leaderConfigs.stream().map(AssessLeaderConfig::getHashId).collect(Collectors.toList());

                    qw.in(AssessDemocraticEvaluationSummary::getAppraisee, personHashId);
                    qw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, annual.getCurrentYear());
                    qw.orderByDesc(AssessDemocraticEvaluationSummary::getType);
                    List<AssessDemocraticEvaluationSummary> s = summaryMapper.selectList(qw);
                    qw.clear();

                    if (s != null && !s.isEmpty()) {
                        summaries.addAll(s);
                    }
                }

                LambdaQueryWrapper<AssessAssistConfig> lqw2 = new LambdaQueryWrapper<>();
                lqw2.like(AssessAssistConfig::getLeader, sysUser.getId());
                List<AssessAssistConfig> assistConfigs = assistConfigMapper.selectList(lqw2);

                if (assistConfigs != null && !assistConfigs.isEmpty()) {
                    List<String> personHashId = assistConfigs.stream().map(AssessAssistConfig::getHashId).collect(Collectors.toList());
                    qw.in(AssessDemocraticEvaluationSummary::getAppraisee, personHashId);
                    qw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, annual.getCurrentYear());
                    qw.orderByDesc(AssessDemocraticEvaluationSummary::getType);
                    List<AssessDemocraticEvaluationSummary> s = summaryMapper.selectList(qw);
                    qw.clear();

                    if (s != null && !s.isEmpty()) {
                        summaries.addAll(s);
                    }
                }

                if (!summaries.isEmpty()) {
                    List<String> dSummaryIds = summaries.stream().map(AssessDemocraticEvaluationSummary::getId).collect(Collectors.toList());

                    LambdaQueryWrapper<AssessDemocraticEvaluation> lqw3 = new LambdaQueryWrapper<>();
                    lqw3.in(AssessDemocraticEvaluation::getEvaluationSummaryId, dSummaryIds);
                    lqw3.eq(AssessDemocraticEvaluation::getCreateBy, sysUser.getUsername());
                    List<AssessDemocraticEvaluation> assessDemocraticEvaluations = democraticEvaluationMapper.selectList(lqw3);

                    if (assessDemocraticEvaluations != null && !assessDemocraticEvaluations.isEmpty()) {
                        List<String> filledIds = assessDemocraticEvaluations.stream().map(AssessDemocraticEvaluation::getEvaluationSummaryId).collect(Collectors.toList());


                        if (!filledIds.isEmpty())
                            summaries = summaries.stream().filter(summary -> !filledIds.contains(summary.getId())).collect(Collectors.toList());
                    }
                }
            }


            // 基层A票只查询副职
//            if (!roles.contains("director_leader") && "basic_AB".equals(sysUser.getVoteType())) {
//                summaries = summaries.stream()
//                        .filter(summary -> summary.getType().equals("31"))
//                        .collect(Collectors.toList());
//            }

            // 机关C票不查询总师、二巡
            if (!roles.contains("director_leader") && "bureau_C".equals(sysUser.getVoteType())) {
                summaries = summaries.stream()
                        .filter(summary -> !summary.getType().equals("23"))
                        .collect(Collectors.toList());
            }
        }

        List<String> allPeople = new ArrayList<>();

        // 封装返回数据
        if (summaries != null && !summaries.isEmpty()) {
            List<DemocraticFillListVO> list = new ArrayList<>();
            for (AssessDemocraticEvaluationSummary summary : summaries) {
                DemocraticFillListVO vo = new DemocraticFillListVO();
                vo.setAppraisee(summary.getAppraisee());
                vo.setDepartId(summary.getDepart());
                vo.setAssessName(summary.getAssessName());
                vo.setType(summary.getType());
                vo.setSummaryId(summary.getId());
                vo.setEndDate(summary.getEndDate());
                vo.setCurrentYear(summary.getCurrentYear());
                list.add(vo);
                allPeople.add(summary.getAppraisee());
            }

            LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
            lqw.eq(AssessAnnualSummary::getCurrentYear, annual.getCurrentYear());
            lqw.in(AssessAnnualSummary::getHashId, allPeople);
            lqw.ne(AssessAnnualSummary::getHashId, "group");
            lqw.orderByAsc(AssessAnnualSummary::getPersonOrder);
            List<AssessAnnualSummary> annualSummaries = annualSummaryMapper.selectList(lqw);

            if (annualSummaries != null && !annualSummaries.isEmpty()) {
                List<DemocraticFillListVO> groupList = list.stream().filter(vo -> "group".equals(vo.getAppraisee())).collect(Collectors.toList());
                List<DemocraticFillListVO> peopleList = list.stream().filter(vo -> !"group".equals(vo.getAppraisee())).collect(Collectors.toList());


                // 将list根据annualSummaries的顺序排序，annualSummaries中的hashId属性对应list中的appraisee属性
                peopleList.sort(Comparator.comparing(o -> annualSummaries.stream().filter(a -> a.getHashId().equals(o.getAppraisee())).findFirst().get().getPersonOrder()));

                list = new ArrayList<>();
                list.addAll(groupList);
                list.addAll(peopleList);
            }

            return list;
        }

        return Collections.emptyList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Synchronized
    public void submit(List<DemocraticSubmitPage> democraticSubmitPage) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        AssessDemocraticEvaluationMapper evaluationMapperNew = sqlSession.getMapper(AssessDemocraticEvaluationMapper.class);
        AssessDemocraticEvaluationItemMapper itemMapperNew = sqlSession.getMapper(AssessDemocraticEvaluationItemMapper.class);

        // 测评集合
        List<AssessDemocraticEvaluation> evaluationList = new ArrayList<>();
        // 测评项集合
        List<AssessDemocraticEvaluationItem> itemList = new ArrayList<>();

        // 获取当前登录用户
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String voteType = sysUser.getVoteType();
        // 将voteType以_分割，取后面的内容
        String[] types = voteType.split("_");
        String voteTrueType = types[types.length - 1]; // 真正的ABC票，其中包括"AB"票

        List<String> summaryIds = new ArrayList<>();

        for (DemocraticSubmitPage submitPage : democraticSubmitPage) {
            // 雪花算法生成ID
            String democraticId = IdWorker.getIdStr();

            AssessDemocraticEvaluation evaluation = new AssessDemocraticEvaluation();
            evaluation.setId(democraticId);
            evaluation.setType(submitPage.getType());
            evaluation.setOverallEvaluation(submitPage.getOverallEvaluation());
            evaluation.setEvaluationSummaryId(submitPage.getEvaluationSummaryId());

            summaryIds.add(submitPage.getEvaluationSummaryId());

            BigDecimal score = new BigDecimal(0);

            for (AssessDemocraticEvaluationItem item : submitPage.getItems()) {
                item.setDemocraticId(democraticId);

                if (item.getItemScore() != null && item.getOptionWeight() != null) {
                    // 除以100，保留两位小数
                    BigDecimal itemWeight = item.getOptionWeight().divide(new BigDecimal(100), 4, RoundingMode.HALF_UP);
                    // 项目分数 = 项目分数 * 选项权重，保留两位小数
                    BigDecimal itemScore = item.getItemScore().multiply(itemWeight).setScale(4, RoundingMode.HALF_UP);
                    item.setScore(itemScore);
                    score = score.add(itemScore);
                }

                if ("AB".equals(voteTrueType)) {
                    if ("31".equals(submitPage.getType())) {
                        item.setVoteType("A");
                    } else {
                        item.setVoteType("B");
                    }
                } else {
                    item.setVoteType(voteTrueType);
                }

                itemList.add(item);
            }

            if ("AB".equals(voteTrueType)) {
                if ("31".equals(submitPage.getType())) {
                    evaluation.setVoteType("A");
                } else {
                    evaluation.setVoteType("B");
                }
            } else {
                evaluation.setVoteType(voteTrueType);
            }

            evaluation.setScore(score);
            evaluationList.add(evaluation);
        }

        evaluationList.forEach(evaluationMapperNew::insert);
        itemList.forEach(itemMapperNew::insert);

        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();

        summaryService.statisticalScore(summaryIds);
        summaryService.updateRanking(democraticSubmitPage.get(0).getCurrentYear());

        // todo：考核单元内排名
        summaryService.updateScopeRanking(democraticSubmitPage.get(0).getCurrentYear());

        // 匿名账号删除当前单位
        String depart = democraticSubmitPage.get(0).getDepart();
        String departIds = sysUser.getDepartIds();
        List<String> list = new ArrayList<>(Arrays.asList(departIds.split(",")));
        list.remove(depart);

        // List<String> roles = userCommonApi.getUserRolesByUserName(sysUser.getUsername());
        //
        // if (roles != null && !roles.contains("director_leader") && list.isEmpty()) {
        //     sysUserMapper.deleteById(sysUser.getId());
        // }

        String newDepartIds = String.join(",", list);
        LambdaUpdateWrapper<SysUser> luw = new LambdaUpdateWrapper<>();
        luw.eq(SysUser::getId, sysUser.getId());
        luw.set(SysUser::getDepartIds, newDepartIds);
        sysUserMapper.update(null, luw);

        // 清除sysUser缓存：sys:cache:encrypt:user::liujie
        redisTemplate.delete("sys:cache:encrypt:user::" + sysUser.getUsername());
    }
}
