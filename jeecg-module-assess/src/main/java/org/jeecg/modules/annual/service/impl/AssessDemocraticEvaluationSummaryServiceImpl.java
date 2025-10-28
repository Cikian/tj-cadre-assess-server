package org.jeecg.modules.annual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Synchronized;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.SysDepartModel;
import org.jeecg.modules.annual.dto.Dem4jjGradeDTO;
import org.jeecg.modules.annual.dto.DemGradeDTO;
import org.jeecg.modules.annual.dto.DemGradeItemsDTO;
import org.jeecg.modules.annual.service.IAssessAnnualDemocraticConfigService;
import org.jeecg.modules.annual.service.IAssessDemocraticEvaluationSummaryService;
import org.jeecg.modules.annual.vo.DepartProgressVO;
import org.jeecg.modules.annual.vo.LeaderProgressVO;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.dto.DemocraticInitDTO;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.*;
import org.jeecg.modules.sys.entity.business.AssessLeaderDepartConfig;
import org.jeecg.modules.sys.mapper.annual.*;
import org.jeecg.modules.sys.mapper.business.AssessLeaderDepartConfigMapper;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.user.UserCommonApi;
import org.jeecg.modules.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Description: 民主测评汇总
 * @Author: jeecg-boot
 * @Date: 2024-09-12
 * @Version: V1.0
 */
@Service
public class AssessDemocraticEvaluationSummaryServiceImpl extends ServiceImpl<AssessDemocraticEvaluationSummaryMapper, AssessDemocraticEvaluationSummary> implements IAssessDemocraticEvaluationSummaryService {

    @Autowired
    private AssessDemocraticEvaluationSummaryMapper democraticSummaryMapper;
    @Autowired
    private AssessDemocraticEvaluationMapper democraticEvaluationMapper;
    @Autowired
    private IAssessAnnualDemocraticConfigService democraticConfigService;
    @Autowired
    private DepartCommonApi departCommonApi;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private AssessAnnualSummaryMapper annualSummaryMapper;
    @Autowired
    private AssessAnnualArrangeMapper annualArrangeMapper;
    @Autowired
    private AssessDemocraticEvaluationItemMapper itemMapper;
    @Autowired
    private AssessLeaderDepartConfigMapper leaderDepartConfigMapper;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private UserCommonApi userCommonApi;
    @Autowired
    private AssessAnnualFillMapper annualFillMapper;
    @Autowired
    private AssessLeaderConfigMapper leaderConfigMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMain(AssessDemocraticEvaluationSummary assessDemocraticEvaluationSummary, List<AssessDemocraticEvaluation> assessDemocraticEvaluationList) {
        democraticSummaryMapper.insert(assessDemocraticEvaluationSummary);
        if (assessDemocraticEvaluationList != null && assessDemocraticEvaluationList.size() > 0) {
            for (AssessDemocraticEvaluation entity : assessDemocraticEvaluationList) {
                // 外键设置
                entity.setEvaluationSummaryId(assessDemocraticEvaluationSummary.getId());
                democraticEvaluationMapper.insert(entity);
            }
        }
    }

    @Override
    public List<AssessDemocraticEvaluationSummary> getData(QueryWrapper<AssessDemocraticEvaluationSummary> queryWrapper) {
        return democraticSummaryMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMain(AssessDemocraticEvaluationSummary assessDemocraticEvaluationSummary, List<AssessDemocraticEvaluation> assessDemocraticEvaluationList) {
        democraticSummaryMapper.updateById(assessDemocraticEvaluationSummary);

        // 1.先删除子表数据
        democraticEvaluationMapper.deleteByMainId(assessDemocraticEvaluationSummary.getId());

        // 2.子表数据重新插入
        if (assessDemocraticEvaluationList != null && assessDemocraticEvaluationList.size() > 0) {
            for (AssessDemocraticEvaluation entity : assessDemocraticEvaluationList) {
                // 外键设置
                entity.setEvaluationSummaryId(assessDemocraticEvaluationSummary.getId());
                democraticEvaluationMapper.insert(entity);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delMain(String id) {
        democraticEvaluationMapper.deleteByMainId(id);
        democraticSummaryMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delBatchMain(Collection<? extends Serializable> idList) {
        for (Serializable id : idList) {
            democraticEvaluationMapper.deleteByMainId(id.toString());
            democraticSummaryMapper.deleteById(id);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean initDemocratic(DemocraticInitDTO map) {
        String currentYear = map.getCurrentYear();
        // 判断当前年度是否已经初始化
        LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw1 = new LambdaQueryWrapper<>();
        lqw1.eq(AssessDemocraticEvaluationSummary::getCurrentYear, currentYear);
        lqw1.select(AssessDemocraticEvaluationSummary::getId);
        List<AssessDemocraticEvaluationSummary> summaryList = democraticSummaryMapper.selectList(lqw1);
        if (!summaryList.isEmpty()) {
            throw new JeecgBootException("当前年度已存在年度考核民主测评，不可重复发起！");
        }

        // 删除已经存在的匿名账号
        userCommonApi.deleteAllAnonymousAccount("annual");

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        AssessDemocraticEvaluationSummaryMapper summaryMapperNew = sqlSession.getMapper(AssessDemocraticEvaluationSummaryMapper.class);

        // 获取当前年度考核汇总列表
        LambdaQueryWrapper<AssessAnnualSummary> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(AssessAnnualSummary::getCurrentYear, currentYear);
        List<AssessAnnualSummary> assessAnnualSummaries = annualSummaryMapper.selectList(lqw2);

        List<AssessDemocraticEvaluationSummary> insertSummaryList = new ArrayList<>();


        // 插入各处室考核填报表
        for (AssessAnnualSummary annualSummary : assessAnnualSummaries) {
            // todo: 如果其他类型不参加民主测评，解注释下一行
            // if ("other".equals(annualSummary.getType())) continue;
            // 雪花算法生成ID
            String fillId = IdWorker.getIdStr();

            AssessDemocraticEvaluationSummary summary = new AssessDemocraticEvaluationSummary();
            summary.setId(fillId);
            summary.setAnnualSummaryId(annualSummary.getId());
            summary.setAssessName(map.getAssessName());
            summary.setCurrentYear(map.getCurrentYear());
            summary.setAppraisee(annualSummary.getHashId());
            Date date = new Date();
            summary.setStartDate(date);
            summary.setEndDate(map.getDeadline());
            summary.setDepart(annualSummary.getDepart());
            summary.setDepartType(annualSummary.getDepartType());
            String type = annualSummary.getType();

            String departType = annualSummary.getDepartType();
            String position = annualSummary.getPosition();

            boolean setType = false;
            if (position != null && !position.isEmpty()) {
                if ("局管总师".equals(position) || "二级巡视员".equals(position)) {
                    summary.setType("23");
                    summary.setDepart("JG" + annualSummary.getDepart());
                    setType = true;
                }
            }

            if (!setType) {
                if ("bureau".equals(departType)) {
                    if ("chief".equals(type)) {
                        summary.setType("22");
                    } else if ("deputy".equals(type)) {
                        summary.setType("21");
                    } else if ("chiefPro".equals(type)) {
                        summary.setType("22");
                    } else if ("other".equals(type)) {
                        summary.setType("21");
                    }
                } else {
                    if ("chief".equals(type)) {
                        summary.setType("32");
                    } else if ("deputy".equals(type)) {
                        summary.setType("31");
                    } else if ("group".equals(type)) {
                        summary.setType("99");
                    } else if ("other".equals(type)) {
                        summary.setType("31");
                    }
                }
            }

            // Integer voteNum = annualArrangeMapper.selectEvaluationNumByDepartId(annualSummary.getFillId());
            AssessAnnualArrange arrange = annualArrangeMapper.selectByMainId(annualSummary.getFillId()).get(0);
            Integer voteNum = arrange.getVoteNum();
            if (voteNum != null && voteNum > 0) {
                summary.setNum(arrange.getVoteNum());
                insertSummaryList.add(summary);
            }
        }

        // 批量插入各处室考核填报表和各处室考核填报明细
        insertSummaryList.forEach(summaryMapperNew::insert);

        // 添加年份字典数据
        departCommonApi.saveAssessYear(map.getCurrentYear());

        // 提交事务、清理缓存、关闭sqlSession
        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();

        return !insertSummaryList.isEmpty();
    }

    @Override
    @Synchronized
    public void statisticalScore(List<String> ids) {
        // 获取配置中的ABC权重
        List<AssessAnnualDemocraticConfig> lastVersions = democraticConfigService.getLastVersions();

        Map<String, AssessAnnualDemocraticConfig> typeMap = new HashMap<>();
        for (AssessAnnualDemocraticConfig config : lastVersions) {
            typeMap.put(config.getType(), config);
        }

        for (String id : ids) {
            BigDecimal scoreA = new BigDecimal(0);
            BigDecimal scoreB = new BigDecimal(0);
            BigDecimal scoreC = new BigDecimal(0);

            BigDecimal countA = new BigDecimal(0);
            BigDecimal countB = new BigDecimal(0);
            BigDecimal countC = new BigDecimal(0);

            // 获取summaryId对应的所有评分
            List<AssessDemocraticEvaluation> items = democraticEvaluationMapper.selectByMainId(id);
            for (AssessDemocraticEvaluation item : items) {
                // 分别计算A、B、C级的总分和数量
                switch (item.getVoteType()) {
                    case "A":
                        scoreA = scoreA.add(item.getScore());
                        countA = countA.add(new BigDecimal(1));
                        break;
                    case "B":
                        scoreB = scoreB.add(item.getScore());
                        countB = countB.add(new BigDecimal(1));
                        break;
                    case "C":
                        scoreC = scoreC.add(item.getScore());
                        countC = countC.add(new BigDecimal(1));
                        break;
                    default:
                        break;
                }
            }

            // 获取权重配置
            AssessAnnualDemocraticConfig config = typeMap.get(items.get(0).getType());
            // 计算加权平均分
            BigDecimal avgA = new BigDecimal(0);
            BigDecimal avgB = new BigDecimal(0);
            BigDecimal avgC = new BigDecimal(0);

            if (scoreA.compareTo(new BigDecimal(0)) > 0 && countA.compareTo(new BigDecimal(0)) > 0) {
                avgA = scoreA.divide(countA, 6, RoundingMode.HALF_UP).multiply(config.getWeightA());
            }
            if (scoreB.compareTo(new BigDecimal(0)) > 0 && countB.compareTo(new BigDecimal(0)) > 0) {
                avgB = scoreB.divide(countB, 6, RoundingMode.HALF_UP).multiply(config.getWeightB());
            }
            if (scoreC.compareTo(new BigDecimal(0)) > 0 && countC.compareTo(new BigDecimal(0)) > 0) {
                avgC = scoreC.divide(countC, 6, RoundingMode.HALF_UP).multiply(config.getWeightC());
            }

            BigDecimal averageScore = avgA.add(avgB).add(avgC).setScale(3, RoundingMode.HALF_UP);
            // 更新summaryId对应的平均分
            // 更新已评数量
            AssessDemocraticEvaluationSummary summary = new AssessDemocraticEvaluationSummary();
            summary.setId(id);
            summary.setScore(averageScore);
            // summary.setFilledNum(countA.add(countB).add(countC).intValue());
            democraticSummaryMapper.updateById(summary);
        }
    }

    @Override
    @Synchronized
    public void updateRanking(String year) {
        // 定义需要处理的类型分组
        List<TypeGroup> typeGroups = Arrays.asList(
                new TypeGroup("局机关正职", Arrays.asList("22", "23")),
                new TypeGroup("局机关副职", Arrays.asList("21")),
                new TypeGroup("基层正职", Arrays.asList("32")),
                new TypeGroup("基层副职", Arrays.asList("31")),
                new TypeGroup("班子", Arrays.asList("99"))
        );

        List<AssessDemocraticEvaluationSummary> newList = new ArrayList<>();

        // 处理每种类型分组
        for (TypeGroup group : typeGroups) {
            List<AssessDemocraticEvaluationSummary> summaries = getSummariesByType(year, group.types);
            if (!summaries.isEmpty()) {
                updateRankingsForGroup(summaries);
                newList.addAll(summaries);
            }
        }

        // 批量更新数据库
        batchUpdateSummaries(newList);
    }

    // 辅助类：封装类型分组信息
    private static class TypeGroup {
        final String name;
        final List<String> types;

        TypeGroup(String name, List<String> types) {
            this.name = name;
            this.types = types;
        }
    }

    // 辅助方法1：根据类型查询数据
    private List<AssessDemocraticEvaluationSummary> getSummariesByType(String year, List<String> types) {
        LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year)
                .in(AssessDemocraticEvaluationSummary::getType, types)
                .isNotNull(AssessDemocraticEvaluationSummary::getScore);

        return democraticSummaryMapper.selectList(lqw);
    }

    // 辅助方法2：更新分组内的排名
    private void updateRankingsForGroup(List<AssessDemocraticEvaluationSummary> summaries) {
        // 按分数降序排序
        summaries.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getScore).reversed());

        // 初始化排名和分数跟踪
        int rank = 1;
        int trueRank = 0;
        BigDecimal previousScore = null;

        // 更新排名
        for (AssessDemocraticEvaluationSummary summary : summaries) {
            BigDecimal currentScore = summary.getScore();

            // 处理第一个元素
            if (previousScore == null) {
                previousScore = currentScore;
                trueRank = 1;
            }
            // 分数相同则保持相同排名
            else if (currentScore.compareTo(previousScore) == 0) {
                trueRank++;
            }
            // 分数不同则更新排名
            else {
                trueRank++;
                rank = trueRank;
                previousScore = currentScore;
            }

            // 设置排名
            summary.setRanking(rank);
        }
    }

    // 辅助方法3：批量更新数据库
    private void batchUpdateSummaries(List<AssessDemocraticEvaluationSummary> summaries) {
        if (summaries.isEmpty()) return;

        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
            AssessDemocraticEvaluationSummaryMapper summaryMapper = sqlSession.getMapper(AssessDemocraticEvaluationSummaryMapper.class);

            for (AssessDemocraticEvaluationSummary summary : summaries) {
                summaryMapper.updateById(summary);
            }

            sqlSession.commit();
        }
    }

//    public void updateRanking(String year) {
//        // 需更新集合
//        List<AssessDemocraticEvaluationSummary> newList = new ArrayList<>();
//
//        LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw = new LambdaQueryWrapper<>();
//        lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year);
//        lqw.in(AssessDemocraticEvaluationSummary::getType, Arrays.asList("22", "23"));
//        lqw.isNotNull(AssessDemocraticEvaluationSummary::getScore);
//        // 局机关正职，包括总师二巡
//        List<AssessDemocraticEvaluationSummary> summaries1 = democraticSummaryMapper.selectList(lqw);
//
//        // if (!summaries1.isEmpty()) {
//        //     // 根据AssessDemocraticEvaluationSummary的score字段降序排序
//        //     summaries1.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getScore).reversed());
//        //     // 更新排名
//        //     for (int i = 0; i < summaries1.size(); i++) {
//        //         AssessDemocraticEvaluationSummary summary = summaries1.get(i);
//        //         summary.setRanking(i + 1);
//        //         newList.add(summary);
//        //     }
//        // }
//
//        if (!summaries1.isEmpty()) {
//            // 根据AssessDemocraticEvaluationSummary的score字段降序排序
//            summaries1.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getScore).reversed());
//
//            // 初始化排名和上一个分数
//            int rank = 1;
//            int trueRank = 0;
//            BigDecimal previousScore = new BigDecimal(0);
//
//            // 更新排名
//            for (int i = 0; i < summaries1.size(); i++) {
//                AssessDemocraticEvaluationSummary summary = summaries1.get(i);
//
//                // 如果当前分数与上一个分数相同，增加相同分数的计数
//                if (summary.getScore().compareTo(previousScore) == 0) {
//                    trueRank++;
//                } else {
//                    // 如果当前分数与上一个分数不同，更新排名
//                    trueRank++;
//                    rank = trueRank;
//                    previousScore = summary.getScore(); // 更新上一个分数
//                }
//
//                // 设置排名
//                summary.setRanking(rank);
//                newList.add(summary);
//            }
//        }
//
//        lqw.clear();
//        // 局机关副职
//        lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year);
//        lqw.eq(AssessDemocraticEvaluationSummary::getType, "21");
//        lqw.isNotNull(AssessDemocraticEvaluationSummary::getScore);
//        List<AssessDemocraticEvaluationSummary> summaries2 = democraticSummaryMapper.selectList(lqw);
//
//        // if (!summaries2.isEmpty()) {
//        //     summaries2.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getScore).reversed());
//        //     // 更新排名
//        //     for (int i = 0; i < summaries2.size(); i++) {
//        //         AssessDemocraticEvaluationSummary summary = summaries2.get(i);
//        //         summary.setRanking(i + 1);
//        //         newList.add(summary);
//        //     }
//        // }
//
//        if (!summaries2.isEmpty()) {
//            // 根据AssessDemocraticEvaluationSummary的score字段降序排序
//            summaries2.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getScore).reversed());
//
//            // 初始化排名和上一个分数
//            int rank = 1;
//            int trueRank = 0;
//            BigDecimal previousScore = new BigDecimal(0);
//
//            // 更新排名
//            for (int i = 0; i < summaries2.size(); i++) {
//                AssessDemocraticEvaluationSummary summary = summaries2.get(i);
//
//                // 如果当前分数与上一个分数相同，增加相同分数的计数
//                if (summary.getScore().compareTo(previousScore) == 0) {
//                    trueRank++;
//                } else {
//                    // 如果当前分数与上一个分数不同，更新排名
//                    trueRank++;
//                    rank = trueRank;
//                    previousScore = summary.getScore(); // 更新上一个分数
//                }
//
//                // 设置排名
//                summary.setRanking(rank);
//                newList.add(summary);
//            }
//        }
//
//        lqw.clear();
//        // 基层正职
//        lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year);
//        lqw.eq(AssessDemocraticEvaluationSummary::getType, "32");
//        lqw.isNotNull(AssessDemocraticEvaluationSummary::getScore);
//        List<AssessDemocraticEvaluationSummary> summaries3 = democraticSummaryMapper.selectList(lqw);
//
//        // if (!summaries3.isEmpty()) {
//        //     summaries3.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getScore).reversed());
//        //     // 更新排名
//        //     for (int i = 0; i < summaries3.size(); i++) {
//        //         AssessDemocraticEvaluationSummary summary = summaries3.get(i);
//        //         summary.setRanking(i + 1);
//        //         newList.add(summary);
//        //     }
//        // }
//
//        if (!summaries3.isEmpty()) {
//            // 根据AssessDemocraticEvaluationSummary的score字段降序排序
//            summaries3.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getScore).reversed());
//
//            // 初始化排名和上一个分数
//            int rank = 1;
//            int trueRank = 0;
//            BigDecimal previousScore = new BigDecimal(0);
//
//            // 更新排名
//            for (int i = 0; i < summaries3.size(); i++) {
//                AssessDemocraticEvaluationSummary summary = summaries3.get(i);
//
//                // 如果当前分数与上一个分数相同，增加相同分数的计数
//                if (summary.getScore().compareTo(previousScore) == 0) {
//                    trueRank++;
//                } else {
//                    // 如果当前分数与上一个分数不同，更新排名
//                    trueRank++;
//                    rank = trueRank;
//                    previousScore = summary.getScore(); // 更新上一个分数
//                }
//
//                // 设置排名
//                summary.setRanking(rank);
//                newList.add(summary);
//            }
//        }
//
//        lqw.clear();
//        // 基层副职
//        lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year);
//        lqw.eq(AssessDemocraticEvaluationSummary::getType, "31");
//        lqw.isNotNull(AssessDemocraticEvaluationSummary::getScore);
//        List<AssessDemocraticEvaluationSummary> summaries4 = democraticSummaryMapper.selectList(lqw);
//        summaries4.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getScore).reversed());
//        // if (!summaries4.isEmpty()) {
//        //     // 更新排名
//        //     for (int i = 0; i < summaries4.size(); i++) {
//        //         AssessDemocraticEvaluationSummary summary = summaries4.get(i);
//        //         summary.setRanking(i + 1);
//        //         newList.add(summary);
//        //     }
//        // }
//
//        if (!summaries4.isEmpty()) {
//            // 根据AssessDemocraticEvaluationSummary的score字段降序排序
//            summaries4.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getScore).reversed());
//
//            // 初始化排名和上一个分数
//            int rank = 1;
//            int trueRank = 0;
//            BigDecimal previousScore = new BigDecimal(0);
//
//            // 更新排名
//            for (int i = 0; i < summaries4.size(); i++) {
//                AssessDemocraticEvaluationSummary summary = summaries4.get(i);
//
//                // 如果当前分数与上一个分数相同，增加相同分数的计数
//                if (summary.getScore().compareTo(previousScore) == 0) {
//                    trueRank++;
//                } else {
//                    // 如果当前分数与上一个分数不同，更新排名
//                    trueRank++;
//                    rank = trueRank;
//                    previousScore = summary.getScore(); // 更新上一个分数
//                }
//
//                // 设置排名
//                summary.setRanking(rank);
//                newList.add(summary);
//            }
//        }
//
//        lqw.clear();
//        // 班子
//        lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year);
//        lqw.eq(AssessDemocraticEvaluationSummary::getType, "99");
//        lqw.isNotNull(AssessDemocraticEvaluationSummary::getScore);
//        List<AssessDemocraticEvaluationSummary> groupSummaries = democraticSummaryMapper.selectList(lqw);
//        // if (!groupSummaries.isEmpty()) {
//        //     groupSummaries.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getScore).reversed());
//        //
//        //     for (int i = 0; i < groupSummaries.size(); i++) {
//        //         AssessDemocraticEvaluationSummary summary = groupSummaries.get(i);
//        //         summary.setRanking(i + 1);
//        //         newList.add(summary);
//        //     }
//        // }
//
//        if (!groupSummaries.isEmpty()) {
//            // 根据AssessDemocraticEvaluationSummary的score字段降序排序
//            groupSummaries.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getScore).reversed());
//
//            // 初始化排名和上一个分数
//            int rank = 1;
//            int trueRank = 0;
//            BigDecimal previousScore = new BigDecimal(0);
//
//            // 更新排名
//            for (int i = 0; i < groupSummaries.size(); i++) {
//                AssessDemocraticEvaluationSummary summary = groupSummaries.get(i);
//
//                // 如果当前分数与上一个分数相同，增加相同分数的计数
//                if (summary.getScore().compareTo(previousScore) == 0) {
//                    trueRank++;
//                } else {
//                    // 如果当前分数与上一个分数不同，更新排名
//                    trueRank++;
//                    rank = trueRank;
//                    previousScore = summary.getScore(); // 更新上一个分数
//                }
//
//                // 设置排名
//                summary.setRanking(rank);
//                newList.add(summary);
//            }
//        }
//
//        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
//        AssessDemocraticEvaluationSummaryMapper summaryMapperNew = sqlSession.getMapper(AssessDemocraticEvaluationSummaryMapper.class);
//
//        newList.forEach(summaryMapperNew::updateById);
//
//        sqlSession.commit();
//        sqlSession.clearCache();
//        sqlSession.close();
//    }

    @Override
    @Synchronized
    public void updateScopeRanking(String year) {
        List<AssessDemocraticEvaluationSummary> updateList = new ArrayList<>();

        List<AssessLeaderConfig> leaderConfigs = leaderConfigMapper.selectList(null);
        // leader作为k，将hashId作为v，转换为map
        Map<String, String> leaderConfigMap = leaderConfigs.stream().collect(Collectors.toMap(AssessLeaderConfig::getLeader, AssessLeaderConfig::getHashId));
        // 将hashId取出转为list
        List<String> leaderConfigHashIds = leaderConfigs.stream().map(AssessLeaderConfig::getHashId).collect(Collectors.toList());

        // 查询所有领导分管配置
        List<AssessLeaderDepartConfig> configList = leaderDepartConfigMapper.selectList(null);

        // 查询所有总师二巡
        LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqwZ = new LambdaQueryWrapper<>();
        lqwZ.eq(AssessDemocraticEvaluationSummary::getType, "23");
        lqwZ.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year);
        lqwZ.isNotNull(AssessDemocraticEvaluationSummary::getScore);
        List<AssessDemocraticEvaluationSummary> summariesZ = democraticSummaryMapper.selectList(lqwZ);

        LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw = new LambdaQueryWrapper<>();
        // 每个配置（考核单元）内更新排名
        for (AssessLeaderDepartConfig config : configList) {
            List<String> leaderConfigList = new ArrayList<>();

            for (AssessLeaderConfig cl : leaderConfigs) {
                if (cl.getLeader().equals(config.getLeaderId())) {
                    leaderConfigList.add(cl.getHashId());
                }
            }

            List<AssessDemocraticEvaluationSummary> leaderConfigSummaries = new ArrayList<>();
            if (!leaderConfigList.isEmpty()) {
                lqw.clear();
                lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year);
                lqw.in(AssessDemocraticEvaluationSummary::getAppraisee, leaderConfigList);
                lqw.isNotNull(AssessDemocraticEvaluationSummary::getScore);
                leaderConfigSummaries = democraticSummaryMapper.selectList(lqw);
            }

            // 获取考核单元内所有的处室
            String departId = config.getDepartId();
            List<String> configDepartIds = new ArrayList<>(Arrays.asList(departId.split(",")));
            lqw.clear();

            // 1.分管内机关正职，包括总师二巡
            lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year);
            lqw.in(AssessDemocraticEvaluationSummary::getDepart, configDepartIds);
            lqw.notIn(AssessDemocraticEvaluationSummary::getAppraisee, leaderConfigHashIds);
            lqw.eq(AssessDemocraticEvaluationSummary::getType, "22");
            lqw.isNotNull(AssessDemocraticEvaluationSummary::getScore);
            List<AssessDemocraticEvaluationSummary> summaries1 = democraticSummaryMapper.selectList(lqw);
            for (AssessDemocraticEvaluationSummary s : leaderConfigSummaries) {
                if (s.getType().equals("22")) {
                    summaries1.add(s);
                }
            }

            for (AssessDemocraticEvaluationSummary s : summariesZ) {
                String dep = s.getDepart();
                // 删除开头的JG
                if (dep.startsWith("JG")) {
                    dep = dep.substring(2);
                }
                if (configDepartIds.contains(dep) && s.getScore() != null) {
                    summaries1.add(s);
                }
            }

            // 如果summaries为空，跳过此次循环
            // if (!summaries1.isEmpty()) {
            //     // 以score降序排序
            //     summaries1.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getScore).reversed());
            //
            //     for (int i = 0; i < summaries1.size(); i++) {
            //         AssessDemocraticEvaluationSummary summary = summaries1.get(i);
            //         summary.setScopeRanking(i + 1);
            //         updateList.add(summary);
            //     }
            // }

            if (!summaries1.isEmpty()) {
                // 根据AssessDemocraticEvaluationSummary的score字段降序排序
                summaries1.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getScore).reversed());

                // 初始化排名和上一个分数
                int rank = 1;
                int trueRank = 0;
                BigDecimal previousScore = new BigDecimal(0);

                // 更新排名
                for (int i = 0; i < summaries1.size(); i++) {
                    AssessDemocraticEvaluationSummary summary = summaries1.get(i);

                    // 如果当前分数与上一个分数相同，增加相同分数的计数
                    if (summary.getScore().compareTo(previousScore) == 0) {
                        trueRank++;
                    } else {
                        // 如果当前分数与上一个分数不同，更新排名
                        trueRank++;
                        rank = trueRank;
                        previousScore = summary.getScore(); // 更新上一个分数
                    }

                    // 设置排名
                    summary.setScopeRanking(rank);
                    updateList.add(summary);
                }
            }


            lqw.clear();

            // 2.分管内机关副职
            lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year);
            lqw.in(AssessDemocraticEvaluationSummary::getDepart, configDepartIds);
            lqw.eq(AssessDemocraticEvaluationSummary::getType, "21");
            lqw.isNotNull(AssessDemocraticEvaluationSummary::getScore);
            List<AssessDemocraticEvaluationSummary> summaries2 = democraticSummaryMapper.selectList(lqw);

            for (AssessDemocraticEvaluationSummary s : leaderConfigSummaries) {
                if (s.getType().equals("21")) {
                    summaries2.add(s);
                }
            }

            // 如果summaries为空，跳过此次循环
            // if (!summaries2.isEmpty()) {
            //     // 以score降序排序
            //     summaries2.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getScore).reversed());
            //
            //     for (int i = 0; i < summaries2.size(); i++) {
            //         AssessDemocraticEvaluationSummary summary = summaries2.get(i);
            //         summary.setScopeRanking(i + 1);
            //         updateList.add(summary);
            //     }
            // }

            if (!summaries2.isEmpty()) {
                // 根据AssessDemocraticEvaluationSummary的score字段降序排序
                summaries2.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getScore).reversed());

                // 初始化排名和上一个分数
                int rank = 1;
                int trueRank = 0;
                BigDecimal previousScore = new BigDecimal(0);

                // 更新排名
                for (int i = 0; i < summaries2.size(); i++) {
                    AssessDemocraticEvaluationSummary summary = summaries2.get(i);

                    // 如果当前分数与上一个分数相同，增加相同分数的计数
                    if (summary.getScore().compareTo(previousScore) == 0) {
                        trueRank++;
                    } else {
                        // 如果当前分数与上一个分数不同，更新排名
                        trueRank++;
                        rank = trueRank;
                        previousScore = summary.getScore(); // 更新上一个分数
                    }

                    // 设置排名
                    summary.setScopeRanking(rank);
                    updateList.add(summary);
                }
            }

            lqw.clear();

            // 3.分管基层正职
            lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year);
            lqw.in(AssessDemocraticEvaluationSummary::getDepart, configDepartIds);
            lqw.eq(AssessDemocraticEvaluationSummary::getType, "32");
            lqw.isNotNull(AssessDemocraticEvaluationSummary::getScore);
            List<AssessDemocraticEvaluationSummary> summaries3 = democraticSummaryMapper.selectList(lqw);

            for (AssessDemocraticEvaluationSummary s : leaderConfigSummaries) {
                if (s.getType().equals("32")) {
                    summaries3.add(s);
                }
            }

            // 如果summaries为空，跳过此次循环
            // if (!summaries3.isEmpty()) {
            //
            //     // 以score降序排序
            //     summaries3.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getScore).reversed());
            //
            //     for (int i = 0; i < summaries3.size(); i++) {
            //         AssessDemocraticEvaluationSummary summary = summaries3.get(i);
            //         summary.setScopeRanking(i + 1);
            //         updateList.add(summary);
            //     }
            // }

            if (!summaries3.isEmpty()) {
                // 根据AssessDemocraticEvaluationSummary的score字段降序排序
                summaries3.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getScore).reversed());

                // 初始化排名和上一个分数
                int rank = 1;
                int trueRank = 0;
                BigDecimal previousScore = new BigDecimal(0);

                // 更新排名
                for (int i = 0; i < summaries3.size(); i++) {
                    AssessDemocraticEvaluationSummary summary = summaries3.get(i);

                    // 如果当前分数与上一个分数相同，增加相同分数的计数
                    if (summary.getScore().compareTo(previousScore) == 0) {
                        trueRank++;
                    } else {
                        // 如果当前分数与上一个分数不同，更新排名
                        trueRank++;
                        rank = trueRank;
                        previousScore = summary.getScore(); // 更新上一个分数
                    }

                    // 设置排名
                    summary.setScopeRanking(rank);
                    updateList.add(summary);
                }
            }

            lqw.clear();

            // 4.分管基层副职
            lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year);
            lqw.in(AssessDemocraticEvaluationSummary::getDepart, configDepartIds);
            lqw.eq(AssessDemocraticEvaluationSummary::getType, "31");
            lqw.isNotNull(AssessDemocraticEvaluationSummary::getScore);
            List<AssessDemocraticEvaluationSummary> summaries4 = democraticSummaryMapper.selectList(lqw);

            for (AssessDemocraticEvaluationSummary s : leaderConfigSummaries) {
                if (s.getType().equals("31")) {
                    summaries4.add(s);
                }
            }

            // 如果summaries为空，跳过此次循环
            // if (!summaries4.isEmpty()) {
            //
            //     // 以score降序排序
            //     summaries4.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getScore).reversed());
            //
            //     for (int i = 0; i < summaries4.size(); i++) {
            //         AssessDemocraticEvaluationSummary summary = summaries4.get(i);
            //         summary.setScopeRanking(i + 1);
            //         updateList.add(summary);
            //     }
            // }

            if (!summaries4.isEmpty()) {
                // 根据AssessDemocraticEvaluationSummary的score字段降序排序
                summaries4.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getScore).reversed());

                // 初始化排名和上一个分数
                int rank = 1;
                int trueRank = 0;
                BigDecimal previousScore = new BigDecimal(0);

                // 更新排名
                for (int i = 0; i < summaries4.size(); i++) {
                    AssessDemocraticEvaluationSummary summary = summaries4.get(i);

                    // 如果当前分数与上一个分数相同，增加相同分数的计数
                    if (summary.getScore().compareTo(previousScore) == 0) {
                        trueRank++;
                    } else {
                        // 如果当前分数与上一个分数不同，更新排名
                        trueRank++;
                        rank = trueRank;
                        previousScore = summary.getScore(); // 更新上一个分数
                    }

                    // 设置排名
                    summary.setScopeRanking(rank);
                    updateList.add(summary);
                }
            }

            lqw.clear();

            // 5.分管班子
            lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year);
            lqw.in(AssessDemocraticEvaluationSummary::getDepart, configDepartIds);
            lqw.eq(AssessDemocraticEvaluationSummary::getType, "99");
            lqw.isNotNull(AssessDemocraticEvaluationSummary::getScore);
            List<AssessDemocraticEvaluationSummary> summaries5 = democraticSummaryMapper.selectList(lqw);

            // 如果summaries为空，跳过此次循环
            // if (!summaries5.isEmpty()) {
            //
            //     // 以score降序排序
            //     summaries5.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getScore).reversed());
            //
            //     for (int i = 0; i < summaries5.size(); i++) {
            //         AssessDemocraticEvaluationSummary summary = summaries5.get(i);
            //         summary.setScopeRanking(i + 1);
            //         updateList.add(summary);
            //     }
            // }

            if (!summaries5.isEmpty()) {
                // 根据AssessDemocraticEvaluationSummary的score字段降序排序
                summaries5.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getScore).reversed());

                // 初始化排名和上一个分数
                int rank = 1;
                int trueRank = 0;
                BigDecimal previousScore = new BigDecimal(0);

                // 更新排名
                for (int i = 0; i < summaries5.size(); i++) {
                    AssessDemocraticEvaluationSummary summary = summaries5.get(i);

                    // 如果当前分数与上一个分数相同，增加相同分数的计数
                    if (summary.getScore().compareTo(previousScore) == 0) {
                        trueRank++;
                    } else {
                        // 如果当前分数与上一个分数不同，更新排名
                        trueRank++;
                        rank = trueRank;
                        previousScore = summary.getScore(); // 更新上一个分数
                    }

                    // 设置排名
                    summary.setScopeRanking(rank);
                    updateList.add(summary);
                }
            }

            lqw.clear();
        }

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        AssessDemocraticEvaluationSummaryMapper summaryMapperNew = sqlSession.getMapper(AssessDemocraticEvaluationSummaryMapper.class);

        updateList.forEach(summaryMapperNew::updateById);

        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();
    }

    @Override
    public List<AssessDemocraticEvaluationSummary> getByAnnualId(String annualId) {
        LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessDemocraticEvaluationSummary::getAnnualSummaryId, annualId);
        List<AssessDemocraticEvaluationSummary> summaries = democraticSummaryMapper.selectList(lqw);
        return this.getDemocraticLevel(summaries);
    }

    @Override
    public List<LeaderProgressVO> getLeaderProgress() {
        List<LeaderProgressVO> result = new ArrayList<>();

        AssessCurrentAssess assess = assessCommonApi.getCurrentAssessInfo("annual");
        LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, assess.getCurrentYear());

        List<AssessDemocraticEvaluationSummary> summaryLists = this.list(lqw);
        if (summaryLists == null || summaryLists.isEmpty()) {
            throw new JeecgBootException("当前无正在进行中的民主测评。");
        }

        List<AssessLeaderDepartConfig> configs = leaderDepartConfigMapper.selectList(null);
        for (AssessLeaderDepartConfig config : configs) {
            LeaderProgressVO vo = new LeaderProgressVO();
            SysUser leader = sysUserMapper.selectById(config.getLeaderId());
            String noOver = leader.getDepartIds();
            String totals = config.getDepartId();

            // if (noOver == null || noOver.isEmpty()) {
            //     continue;
            // }
            List<String> noOverList = new ArrayList<>(Arrays.asList(noOver.split(",")));
            List<String> totalsList = new ArrayList<>(Arrays.asList(totals.split(",")));

            List<String> overList = new ArrayList<>(totalsList);
            overList.removeAll(noOverList);

            vo.setLeader(leader.getRealname());
            vo.setNoOver(leader.getDepartIds());

            vo.setOver(String.join(",", overList));
            int overNum = overList.size();
            int totalNum = totalsList.size();
            BigDecimal rate = new BigDecimal(overNum).divide(new BigDecimal(totalNum), 4, RoundingMode.HALF_UP);
            // ×100，保留两位
            vo.setRate(rate.multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<DepartProgressVO> getDepartProgressByYear(String year) {
        // 获取当前年度所有考核安排
        LambdaQueryWrapper<AssessAnnualFill> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualFill::getCurrentYear, year);
        List<AssessAnnualFill> fills = annualFillMapper.selectList(lqw);
        // 将fills中的Id提取出来，组成一个List
        List<String> fillIds = fills.stream().map(AssessAnnualFill::getId).collect(Collectors.toList());

        // 获取所有考核安排
        LambdaQueryWrapper<AssessAnnualArrange> lqw2 = new LambdaQueryWrapper<>();
        lqw2.in(AssessAnnualArrange::getAnnualFillId, fillIds);
        lqw2.isNotNull(AssessAnnualArrange::getVoteNum);
        List<AssessAnnualArrange> arranges = annualArrangeMapper.selectList(lqw2);
        // 转为Map，k为fillID，V为AssessAnnualArrange
        Map<String, AssessAnnualArrange> arrangeMap = arranges.stream().collect(Collectors.toMap(AssessAnnualArrange::getAnnualFillId, Function.identity()));

        // 获取所有年度匿名账号
        List<SysUser> annualUser = userCommonApi.getAnonymousAccountByAssess("annual");
        Map<String, Integer> departUserNum = new HashMap<>();

        if (annualUser != null && !annualUser.isEmpty()) {
            for (SysUser user : annualUser) {
                if (user.getDelFlag() == 1) continue;
                String departId = user.getDepart();
                if (departId != null && !departId.isEmpty()) {
                    departUserNum.merge(departId, 1, Integer::sum);
                }
            }
        }
        LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw5 = new LambdaQueryWrapper<>();
        lqw5.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year);
        // 获取第一条记录
        List<AssessDemocraticEvaluationSummary> summaries = democraticSummaryMapper.selectList(lqw5);
        Date endDate = null;
        if (summaries != null && !summaries.isEmpty()) {
            endDate = summaries.get(0).getEndDate();
        }
        String ed = "";
        if (endDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            ed = sdf.format(endDate);
        }
        List<DepartProgressVO> res = new ArrayList<>();

        for (AssessAnnualFill fill : fills) {
            AssessAnnualArrange arrange = arrangeMap.get(fill.getId());
            if (arrange == null) continue;
            DepartProgressVO vo = new DepartProgressVO();
            vo.setNum(arrange.getVoteNum());
            vo.setDepart(fill.getDepart());
            if (annualUser != null && !annualUser.isEmpty()) {
                vo.setFilledNum(arrange.getVoteNum() - (departUserNum.get(fill.getDepart()) == null ? 0 : departUserNum.get(fill.getDepart())));
            } else {
                vo.setFilledNum(arrange.getRecommend() == null ? arrange.getVoteNum() : Integer.valueOf(arrange.getRecommend()));
            }
            vo.setCurrentYear(year);
            vo.setEndDate(ed);
            res.add(vo);
        }


        return res;
    }

    @Override
    public List<DemGradeDTO> getSummary4Excel(String year, String departScope, String personType, String leader) {

        LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year);
        lqw.orderByDesc(AssessDemocraticEvaluationSummary::getScore);
        if (departScope != null && !departScope.isEmpty() && !"0".equals(departScope)) {
            if ("2".equals(departScope)) {
                lqw.eq(AssessDemocraticEvaluationSummary::getDepartType, "bureau");
            } else {
                lqw.ne(AssessDemocraticEvaluationSummary::getDepartType, "bureau");
            }
        }
        String[] type = {"21", "22", "23", "31", "32"};

        if (personType != null && !personType.isEmpty() && !"0".equals(personType)) {
            List<String> typeList = new ArrayList<>(Arrays.asList(type));
            switch (personType) {
                case "2":
                    typeList.remove("21");
                    typeList.remove("31");
                    break;
                case "3":
                    typeList.remove("22");
                    typeList.remove("23");
                    typeList.remove("32");
                    break;
                case "4":
                    typeList.clear();
                    typeList.add("99");
                    lqw.ne(AssessDemocraticEvaluationSummary::getDepartType, "bureau");
                    break;
            }
            lqw.in(AssessDemocraticEvaluationSummary::getType, typeList);
        }

        List<AssessAnnualDemocraticConfig> lastVersionConfigs = democraticConfigService.getLastVersions();
        // type为key，AssessAnnualDemocraticConfig为value转为map
        // 保留第一个遇到的元素
        Map<String, AssessAnnualDemocraticConfig> configMap = lastVersionConfigs.stream()
                .collect(Collectors.toMap(
                        AssessAnnualDemocraticConfig::getType,
                        Function.identity(),
                        (existing, replacement) -> existing  // 如果 key 冲突，保留已存在的值
                ));
        List<AssessDemocraticEvaluationSummary> list = democraticSummaryMapper.selectList(lqw);

        if (leader != null && !leader.isEmpty() && !"0".equals(leader)) {
            List<String> departIdList = userCommonApi.getAssessUnitDepart(year, leader);
            // 转换为set
            Set<String> departIdSet = new HashSet<>(departIdList);
            this.processSummaries(list, departIdSet);

            // 去除特殊人员
            this.processSpecialPerson(list, leader);
        }

        List<String> summaryIds = list.stream().map(AssessDemocraticEvaluationSummary::getId).collect(Collectors.toList());

        List<AssessDemocraticEvaluation> decBySummaryIds = this.getDecBySummaryIds(summaryIds);

        List<String> decIds = decBySummaryIds.stream().map(AssessDemocraticEvaluation::getId).collect(Collectors.toList());
        List<AssessDemocraticEvaluationItem> items = new ArrayList<>();

        if (decIds != null && !decIds.isEmpty()) {
            items = this.getDecItemsByDecIds(decIds);
        }
        List<SysDepartModel> departs = departCommonApi.queryAllDepart();
        // 转为map，id为k，departName为V
        Map<String, String> departMap = departs.stream().collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getDepartName));

        List<DemGradeDTO> res = new ArrayList<>();

        int no = 1;

        for (AssessDemocraticEvaluationSummary summary : list) {
            List<AssessDemocraticEvaluation> decList = decBySummaryIds.stream().filter(dec -> dec.getEvaluationSummaryId().equals(summary.getId())).collect(Collectors.toList());
            List<AssessDemocraticEvaluation> aList = decList.stream().filter(dec -> "A".equals(dec.getVoteType())).collect(Collectors.toList());
            List<AssessDemocraticEvaluation> bList = decList.stream().filter(dec -> "B".equals(dec.getVoteType())).collect(Collectors.toList());
            List<AssessDemocraticEvaluation> cList = decList.stream().filter(dec -> "C".equals(dec.getVoteType())).collect(Collectors.toList());

            int numA = aList.size();
            int numB = bList.size();
            int numC = cList.size();

            BigDecimal scoreA = BigDecimal.ZERO;
            BigDecimal scoreB = BigDecimal.ZERO;
            BigDecimal scoreC = BigDecimal.ZERO;

            BigDecimal avgA = BigDecimal.ZERO;
            BigDecimal avgB = BigDecimal.ZERO;
            BigDecimal avgC = BigDecimal.ZERO;
            // 将aList中score相加得到scoreA
            if (!aList.isEmpty()) {
                for (AssessDemocraticEvaluation item : aList) {
                    scoreA = scoreA.add(item.getScore());
                }
                avgA = scoreA.divide(new BigDecimal(numA), 6, RoundingMode.HALF_UP);
            }
            // 将bList中score相加得到scoreB
            if (!bList.isEmpty()) {
                for (AssessDemocraticEvaluation item : bList) {
                    scoreB = scoreB.add(item.getScore());
                }
                avgB = scoreB.divide(new BigDecimal(numB), 6, RoundingMode.HALF_UP);
            }
            // 将cList中score相加得到scoreC
            if (!cList.isEmpty()) {
                for (AssessDemocraticEvaluation item : cList) {
                    scoreC = scoreC.add(item.getScore());
                }
                avgC = scoreC.divide(new BigDecimal(numC), 6, RoundingMode.HALF_UP);
            }


            // 加权，保留两位小数
            AssessAnnualDemocraticConfig config = configMap.get(summary.getType());

            BigDecimal weightA = config.getWeightA();
            BigDecimal weightB = config.getWeightB();
            BigDecimal weightC = config.getWeightC();

            // 计算每一项的加权分数，保留两位小数
            BigDecimal weightedScoreA = avgA.multiply(weightA).setScale(6, RoundingMode.HALF_UP);
            BigDecimal weightedScoreB = avgB.multiply(weightB).setScale(6, RoundingMode.HALF_UP);
            BigDecimal weightedScoreC = avgC.multiply(weightC).setScale(6, RoundingMode.HALF_UP);

            DemGradeDTO dto = new DemGradeDTO();
            dto.setNo(no);
            dto.setName(CommonUtils.getNameByHashId(summary.getAppraisee()));
            if (summary.getDepart().startsWith("JG")) dto.setDepart("局管总师、二巡");
            else dto.setDepart(departMap.get(summary.getDepart()));
            // 三个分数相加，保留3位小数
            BigDecimal grade = weightedScoreA.add(weightedScoreB).add(weightedScoreC).setScale(3, RoundingMode.HALF_UP);
            dto.setGrade(grade);
            dto.setGraderA(weightedScoreA);
            dto.setGraderB(weightedScoreB);
            dto.setGraderC(weightedScoreC);
            dto.setRanking(summary.getRanking());
            dto.setScopeRanking(summary.getScopeRanking());

            res.add(dto);
            no++;
        }

        res.sort(Comparator.comparing(DemGradeDTO::getRanking));
        return res;
    }

    @Override
    public List<DemGradeItemsDTO> getSummaryItems4Excel(String year, String departScope, String personType, String leader) {
        LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year);
        lqw.orderByDesc(AssessDemocraticEvaluationSummary::getScore);
        if (departScope != null && !departScope.isEmpty() && !"0".equals(departScope)) {
            if ("2".equals(departScope)) {
                lqw.eq(AssessDemocraticEvaluationSummary::getDepartType, "bureau");
            } else {
                lqw.ne(AssessDemocraticEvaluationSummary::getDepartType, "bureau");
            }
        }
        String[] type = {"21", "22", "23", "31", "32"};

        if (personType != null && !personType.isEmpty() && !"0".equals(personType)) {
            List<String> typeList = new ArrayList<>(Arrays.asList(type));
            switch (personType) {
                case "2":
                    typeList.remove("21");
                    typeList.remove("31");
                    break;
                case "3":
                    typeList.remove("22");
                    typeList.remove("23");
                    typeList.remove("32");
                    break;
                case "4":
                    typeList.clear();
                    typeList.add("99");
                    lqw.ne(AssessDemocraticEvaluationSummary::getDepartType, "bureau");
                    break;
            }
            lqw.in(AssessDemocraticEvaluationSummary::getType, typeList);
        }

        // 组合条件后查询符合条件的summary
        List<AssessDemocraticEvaluationSummary> list = democraticSummaryMapper.selectList(lqw);

        if (leader != null && !leader.isEmpty() && !"0".equals(leader)) {
            LambdaQueryWrapper<AssessLeaderDepartConfig> lqw1 = new LambdaQueryWrapper<>();
            lqw1.eq(AssessLeaderDepartConfig::getLeaderId, leader);
            AssessLeaderDepartConfig config = leaderDepartConfigMapper.selectList(lqw1).get(0);
            if (config != null) {
                String departIds = config.getDepartId();
                if (departIds != null && !departIds.isEmpty()) {
                    List<String> departIdList = Arrays.asList(departIds.split(","));
                    // 转换为set
                    Set<String> departIdSet = new HashSet<>(departIdList);
                    this.processSummaries(list, departIdSet);
                }
            }

            // 去除特殊人员
            this.processSpecialPerson(list, leader);
        }

        // 将summary的ID取出
        List<String> summaryIds = list.stream().map(AssessDemocraticEvaluationSummary::getId).collect(Collectors.toList());
        // 根据summary的ID查询对应的dec，为了获取到items
        List<AssessDemocraticEvaluation> decBySummaryIds = this.getDecBySummaryIds(summaryIds);
        List<String> decIds = decBySummaryIds.stream().map(AssessDemocraticEvaluation::getId).collect(Collectors.toList());
        // 根据dec的ID查询对应的items
        List<AssessDemocraticEvaluationItem> items = this.getDecItemsByDecIds(decIds);
        List<SysDepartModel> departs = departCommonApi.queryAllDepart();
        // 转为map，id为k，departName为V
        Map<String, String> departMap = departs.stream().collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getDepartName));

        // 获取加权的配置
        List<AssessAnnualDemocraticConfig> lastVersionConfigs = democraticConfigService.getLastVersions();
        // type为key，AssessAnnualDemocraticConfig为value转为map
        Map<String, AssessAnnualDemocraticConfig> configMap = lastVersionConfigs.stream()
                .collect(Collectors.toMap(
                        AssessAnnualDemocraticConfig::getType,
                        Function.identity(),
                        (existing, replacement) -> existing  // 如果 key 冲突，保留已存在的值
                ));
        List<DemGradeItemsDTO> res = new ArrayList<>();

        int no = 1;

        for (AssessDemocraticEvaluationSummary summary : list) {
            AssessAnnualDemocraticConfig conf = configMap.get(summary.getType());


            List<AssessDemocraticEvaluation> decList = decBySummaryIds.stream().filter(dec -> dec.getEvaluationSummaryId().equals(summary.getId())).collect(Collectors.toList());

            List<String> dec = decList.stream().map(AssessDemocraticEvaluation::getId).collect(Collectors.toList());

            // 从item中过滤出decid在dec中的item
            List<AssessDemocraticEvaluationItem> item = items.stream().filter(i -> dec.contains(i.getDemocraticId())).collect(Collectors.toList());

            String itemName1;
            String itemName2;
            String itemName3;
            String itemName4;
            String itemName5;

            List<AssessDemocraticEvaluationItem> item1;
            List<AssessDemocraticEvaluationItem> item2;
            List<AssessDemocraticEvaluationItem> item3;
            List<AssessDemocraticEvaluationItem> item4;
            List<AssessDemocraticEvaluationItem> item5;

            // 正职
            if ("2".equals(personType)) {
                itemName1 = "政治素质和道德品行（10分）\n" +
                        "执行民主集中制（5分）\n" +
                        "坚持原则和斗争精神（5分）";
                itemName2 = "制度执行力和治理能力（含用网治网能力）（10分）\n" +
                        "组织领导，抓班子带队伍（10分）\n" +
                        "工作思路和改革创新（10分）";
                itemName3 = "担当精神和工作作风（10分）";
                itemName4 = "抓党建工作成效（10分）\n" +
                        "树立和践行正确政绩观和高质量发展成效（10分）\n" +
                        "解决复杂问题、完成年度任务和法治建设成效（10分）";
                itemName5 = "廉政自律（5分）\n" +
                        "履行廉政职责（5分）";
            } else if ("3".equals(personType)) { // 副职
                itemName1 = "政治素质和道德品行（10分）\n" +
                        "执行民主集中制（5分）\n" +
                        "坚持原则和斗争精神（5分）";
                itemName2 = "制度执行力、治理能力（含用网治网能力）（10分）\n" +
                        "组织协调和推动落实（10分）\n" +
                        "专业素养和业务能力（10分）";
                itemName3 = "担当精神和工作作风（10分）";
                itemName4 = "解决复杂问题（10分）\n" +
                        "高质量发展政绩和完成年度任务（15分）\n" +
                        "法治建设成效（5分）";
                itemName5 = "廉洁自律（5分）\n" +
                        "履行一岗双责（5分）";
            } else if ("4".equals(personType)) { // 班子
                itemName1 = "坚定捍卫“两个确立”坚决做到”两个维护“（10分）\n" + " 深学细悟笃行习近平新时代中国特色社会主义思想（10分）";
                itemName2 = "深入贯彻习近平总书记重要指示批示精神和党中央决策部署，落实市委市政府要求（10分）\n" +
                        "贯彻新发展理念，服务高质量发展（10分）";
                itemName3 = "践行群众路线，加强作风建设，服务基层和保障民生（10分）\n" +
                        "担当作为、敢于斗争，深化改革创新，破解重点难点问题（5分）\n" +
                        "坚持依法行政，提升行政效能和法治建设成效，提高制度执行力、治理能力和用网治网能力（5分）";
                itemName4 = "全面履行部门职责，完成重点工作任务（10分）\n" +
                        "严格执行民主集中制，形成团结协作合力（5分）\n" +
                        "加强领导班子和干部队伍建设，树立良好选人用人导向（10分）";
                itemName5 = "加强基层党组织建设，提升组织力，强化政治功能（5分）\n" +
                        "履行全面从严治党主体责任，持之以恒正风肃纪反腐（10分）";
            } else {
                itemName1 = "";
                itemName2 = "";
                itemName3 = "";
                itemName4 = "";
                itemName5 = "";
            }

            item1 = item.stream().filter(i -> i.getItemName().equals(itemName1)).collect(Collectors.toList());
            item2 = item.stream().filter(i -> i.getItemName().equals(itemName2)).collect(Collectors.toList());
            item3 = item.stream().filter(i -> i.getItemName().equals(itemName3)).collect(Collectors.toList());
            item4 = item.stream().filter(i -> i.getItemName().equals(itemName4)).collect(Collectors.toList());
            item5 = item.stream().filter(i -> i.getItemName().equals(itemName5)).collect(Collectors.toList());


            int numA = item1.size();
            int numB = item2.size();
            int numC = item3.size();
            int numD = item4.size();
            int numE = item5.size();

            BigDecimal scoreA = BigDecimal.ZERO;
            BigDecimal scoreB = BigDecimal.ZERO;
            BigDecimal scoreC = BigDecimal.ZERO;
            BigDecimal scoreD = BigDecimal.ZERO;
            BigDecimal scoreE = BigDecimal.ZERO;

            BigDecimal avgA = BigDecimal.ZERO;
            BigDecimal avgB = BigDecimal.ZERO;
            BigDecimal avgC = BigDecimal.ZERO;
            BigDecimal avgD = BigDecimal.ZERO;
            BigDecimal avgE = BigDecimal.ZERO;
            // 将aList中score相加得到scoreA
            if (!item1.isEmpty()) {
                // 三种类型票
                BigDecimal ia = BigDecimal.ZERO;
                BigDecimal ib = BigDecimal.ZERO;
                BigDecimal ic = BigDecimal.ZERO;
                int a = 0;
                int b = 0;
                int c = 0;

                for (AssessDemocraticEvaluationItem i : item1) {

                    switch (i.getVoteType()) {
                        case "A":
                            ia = ia.add(i.getScore());
                            a++;
                            break;
                        case "B":
                            ib = ib.add(i.getScore());
                            b++;
                            break;
                        case "C":
                            ic = ic.add(i.getScore());
                            c++;
                            break;
                    }
                }


                BigDecimal aa = BigDecimal.ZERO;
                BigDecimal ab = BigDecimal.ZERO;
                BigDecimal ac = BigDecimal.ZERO;

                if (a != 0) {
                    aa = ia.divide(new BigDecimal(a), 6, RoundingMode.HALF_UP);
                    // 加权，保留6未小数
                    aa = aa.multiply(conf.getWeightA());
                }

                if (b != 0) {
                    ab = ib.divide(new BigDecimal(b), 6, RoundingMode.HALF_UP);
                    // 加权，保留6未小数
                    ab = ab.multiply(conf.getWeightB());
                }

                if (c != 0) {
                    ac = ic.divide(new BigDecimal(c), 6, RoundingMode.HALF_UP);
                    // 加权，保留6未小数
                    ac = ac.multiply(conf.getWeightC());
                }

                // avgA = scoreA.divide(new BigDecimal(numA), 6, RoundingMode.HALF_UP);

                // aa,ab,ac相加，保留3位小数
                avgA = aa.add(ab).add(ac).setScale(6, RoundingMode.HALF_UP);
            }

            if (!item2.isEmpty()) {
                // 三种类型票
                BigDecimal ia = BigDecimal.ZERO;
                BigDecimal ib = BigDecimal.ZERO;
                BigDecimal ic = BigDecimal.ZERO;
                int a = 0;
                int b = 0;
                int c = 0;

                for (AssessDemocraticEvaluationItem i : item2) {

                    switch (i.getVoteType()) {
                        case "A":
                            ia = ia.add(i.getScore());
                            a++;
                            break;
                        case "B":
                            ib = ib.add(i.getScore());
                            b++;
                            break;
                        case "C":
                            ic = ic.add(i.getScore());
                            c++;
                            break;
                    }
                }


                BigDecimal aa = BigDecimal.ZERO;
                BigDecimal ab = BigDecimal.ZERO;
                BigDecimal ac = BigDecimal.ZERO;

                if (a != 0) {
                    aa = ia.divide(new BigDecimal(a), 6, RoundingMode.HALF_UP);
                    // 加权，保留6未小数
                    aa = aa.multiply(conf.getWeightA());
                }

                if (b != 0) {
                    ab = ib.divide(new BigDecimal(b), 6, RoundingMode.HALF_UP);
                    // 加权，保留6未小数
                    ab = ab.multiply(conf.getWeightB());
                }

                if (c != 0) {
                    ac = ic.divide(new BigDecimal(c), 6, RoundingMode.HALF_UP);
                    // 加权，保留6未小数
                    ac = ac.multiply(conf.getWeightC());
                }

                // avgA = scoreA.divide(new BigDecimal(numA), 6, RoundingMode.HALF_UP);

                // aa,ab,ac相加，保留3位小数
                avgB = aa.add(ab).add(ac).setScale(6, RoundingMode.HALF_UP);
            }

            if (!item3.isEmpty()) {
                if (!item2.isEmpty()) {
                    // 三种类型票
                    BigDecimal ia = BigDecimal.ZERO;
                    BigDecimal ib = BigDecimal.ZERO;
                    BigDecimal ic = BigDecimal.ZERO;
                    int a = 0;
                    int b = 0;
                    int c = 0;

                    for (AssessDemocraticEvaluationItem i : item3) {

                        switch (i.getVoteType()) {
                            case "A":
                                ia = ia.add(i.getScore());
                                a++;
                                break;
                            case "B":
                                ib = ib.add(i.getScore());
                                b++;
                                break;
                            case "C":
                                ic = ic.add(i.getScore());
                                c++;
                                break;
                        }
                    }


                    BigDecimal aa = BigDecimal.ZERO;
                    BigDecimal ab = BigDecimal.ZERO;
                    BigDecimal ac = BigDecimal.ZERO;

                    if (a != 0) {
                        aa = ia.divide(new BigDecimal(a), 6, RoundingMode.HALF_UP);
                        // 加权，保留6未小数
                        aa = aa.multiply(conf.getWeightA());
                    }

                    if (b != 0) {
                        ab = ib.divide(new BigDecimal(b), 6, RoundingMode.HALF_UP);
                        // 加权，保留6未小数
                        ab = ab.multiply(conf.getWeightB());
                    }

                    if (c != 0) {
                        ac = ic.divide(new BigDecimal(c), 6, RoundingMode.HALF_UP);
                        // 加权，保留6未小数
                        ac = ac.multiply(conf.getWeightC());
                    }

                    // avgA = scoreA.divide(new BigDecimal(numA), 6, RoundingMode.HALF_UP);

                    // aa,ab,ac相加，保留3位小数
                    avgC = aa.add(ab).add(ac).setScale(6, RoundingMode.HALF_UP);
                }
            }
            if (!item4.isEmpty()) {
                if (!item2.isEmpty()) {
                    // 三种类型票
                    BigDecimal ia = BigDecimal.ZERO;
                    BigDecimal ib = BigDecimal.ZERO;
                    BigDecimal ic = BigDecimal.ZERO;
                    int a = 0;
                    int b = 0;
                    int c = 0;

                    for (AssessDemocraticEvaluationItem i : item4) {

                        switch (i.getVoteType()) {
                            case "A":
                                ia = ia.add(i.getScore());
                                a++;
                                break;
                            case "B":
                                ib = ib.add(i.getScore());
                                b++;
                                break;
                            case "C":
                                ic = ic.add(i.getScore());
                                c++;
                                break;
                        }
                    }


                    BigDecimal aa = BigDecimal.ZERO;
                    BigDecimal ab = BigDecimal.ZERO;
                    BigDecimal ac = BigDecimal.ZERO;

                    if (a != 0) {
                        aa = ia.divide(new BigDecimal(a), 6, RoundingMode.HALF_UP);
                        // 加权，保留6未小数
                        aa = aa.multiply(conf.getWeightA());
                    }

                    if (b != 0) {
                        ab = ib.divide(new BigDecimal(b), 6, RoundingMode.HALF_UP);
                        // 加权，保留6未小数
                        ab = ab.multiply(conf.getWeightB());
                    }

                    if (c != 0) {
                        ac = ic.divide(new BigDecimal(c), 6, RoundingMode.HALF_UP);
                        // 加权，保留6未小数
                        ac = ac.multiply(conf.getWeightC());
                    }

                    // avgA = scoreA.divide(new BigDecimal(numA), 6, RoundingMode.HALF_UP);

                    // aa,ab,ac相加，保留3位小数
                    avgD = aa.add(ab).add(ac).setScale(6, RoundingMode.HALF_UP);
                }
            }
            if (!item5.isEmpty()) {
                if (!item2.isEmpty()) {
                    // 三种类型票
                    BigDecimal ia = BigDecimal.ZERO;
                    BigDecimal ib = BigDecimal.ZERO;
                    BigDecimal ic = BigDecimal.ZERO;
                    int a = 0;
                    int b = 0;
                    int c = 0;

                    for (AssessDemocraticEvaluationItem i : item5) {

                        switch (i.getVoteType()) {
                            case "A":
                                ia = ia.add(i.getScore());
                                a++;
                                break;
                            case "B":
                                ib = ib.add(i.getScore());
                                b++;
                                break;
                            case "C":
                                ic = ic.add(i.getScore());
                                c++;
                                break;
                        }
                    }


                    BigDecimal aa = BigDecimal.ZERO;
                    BigDecimal ab = BigDecimal.ZERO;
                    BigDecimal ac = BigDecimal.ZERO;

                    if (a != 0) {
                        aa = ia.divide(new BigDecimal(a), 6, RoundingMode.HALF_UP);
                        // 加权，保留6未小数
                        aa = aa.multiply(conf.getWeightA());
                    }

                    if (b != 0) {
                        ab = ib.divide(new BigDecimal(b), 6, RoundingMode.HALF_UP);
                        // 加权，保留6未小数
                        ab = ab.multiply(conf.getWeightB());
                    }

                    if (c != 0) {
                        ac = ic.divide(new BigDecimal(c), 6, RoundingMode.HALF_UP);
                        // 加权，保留6未小数
                        ac = ac.multiply(conf.getWeightC());
                    }

                    // avgA = scoreA.divide(new BigDecimal(numA), 6, RoundingMode.HALF_UP);

                    // aa,ab,ac相加，保留3位小数
                    avgE = aa.add(ab).add(ac).setScale(6, RoundingMode.HALF_UP);
                }
            }

            DemGradeItemsDTO dto = new DemGradeItemsDTO();
            dto.setNo(no);
            dto.setName(CommonUtils.getNameByHashId(summary.getAppraisee()));
            if (summary.getDepart().startsWith("JG")) dto.setDepart("局管总师、二巡");
            else dto.setDepart(departMap.get(summary.getDepart()));
            dto.setGrade(summary.getScore());
            dto.setGraderA(avgA);
            dto.setGraderB(avgB);
            dto.setGraderC(avgC);
            dto.setGraderD(avgD);
            dto.setGraderE(avgE);

            res.add(dto);
            no++;
        }


        return res;
    }

    @Override
    public List<Dem4jjGradeDTO> getSummaryItems4jjExcel(String year) {
        // 1. 查询年度总结数据
        List<String> hashIds = getAnnualSummaryHashIds(year);

        // 2. 查询民主评价数据
        List<AssessDemocraticEvaluationSummary> demList = getDemocraticSummaries(year, hashIds);
        List<AssessDemocraticEvaluationSummary> allDem = getAllDemocraticSummaries(year);

        // 3. 处理部门名称格式
        processDepartmentNames(demList);
        processDepartmentNames(allDem);

        // 4. 获取部门映射
        Map<String, String> departMap = getDepartmentMap();

        // 5. 构建结果DTO列表
        return buildResultDTOs(demList, allDem, departMap);
    }

    // 辅助方法1：获取年度总结的hashIds
    private List<String> getAnnualSummaryHashIds(String year) {
        LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualSummary::getInspectionWork, true)
                .eq(AssessAnnualSummary::getCurrentYear, year);

        return annualSummaryMapper.selectList(lqw).stream()
                .map(AssessAnnualSummary::getHashId)
                .collect(Collectors.toList());
    }

    // 辅助方法2：获取民主评价列表
    private List<AssessDemocraticEvaluationSummary> getDemocraticSummaries(String year, List<String> hashIds) {
        if (hashIds.isEmpty()) return Collections.emptyList();

        LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year)
                .in(AssessDemocraticEvaluationSummary::getAppraisee, hashIds)
                .orderByDesc(AssessDemocraticEvaluationSummary::getScore);

        return democraticSummaryMapper.selectList(lqw);
    }

    // 辅助方法3：获取所有民主评价列表
    private List<AssessDemocraticEvaluationSummary> getAllDemocraticSummaries(String year) {
        LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year)
                .ne(AssessDemocraticEvaluationSummary::getType, "99")
                .orderByDesc(AssessDemocraticEvaluationSummary::getScore);

        return democraticSummaryMapper.selectList(lqw);
    }

    // 辅助方法4：处理部门名称格式
    private void processDepartmentNames(List<AssessDemocraticEvaluationSummary> summaries) {
        summaries.forEach(dem -> {
            if (dem.getDepart().startsWith("JG")) {
                dem.setDepart(dem.getDepart().substring(2));
            }
        });
    }

    // 辅助方法5：获取部门映射
    private Map<String, String> getDepartmentMap() {
        return departCommonApi.queryAllDepart().stream()
                .sorted(Comparator.comparing(SysDepartModel::getDepartOrder))
                .collect(Collectors.toMap(
                        SysDepartModel::getId,
                        SysDepartModel::getDepartName,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    // 辅助方法6：构建结果DTO
    private List<Dem4jjGradeDTO> buildResultDTOs(
            List<AssessDemocraticEvaluationSummary> demList,
            List<AssessDemocraticEvaluationSummary> allDem,
            Map<String, String> departMap) {

        if (demList.isEmpty() || allDem.isEmpty() || departMap.isEmpty()) {
            return Collections.emptyList();
        }

        // 预计算分组数据
        Map<String, List<AssessDemocraticEvaluationSummary>> allDemByDepart = allDem.stream()
                .collect(Collectors.groupingBy(AssessDemocraticEvaluationSummary::getDepart));

        Map<String, List<AssessDemocraticEvaluationSummary>> allDemByType = allDem.stream()
                .collect(Collectors.groupingBy(AssessDemocraticEvaluationSummary::getType));

        // 预加载领导配置
        Set<String> leaderDepartIds = demList.stream()
                .filter(dem -> dem.getType().startsWith("2"))
                .map(AssessDemocraticEvaluationSummary::getDepart)
                .collect(Collectors.toSet());

        Map<String, List<String>> leaderConfigMap = getLeaderConfigMap(leaderDepartIds);

        List<Dem4jjGradeDTO> result = new ArrayList<>();
        // 使用原子整数解决lambda中的final限制
        AtomicInteger sequence = new AtomicInteger(1);

        for (Map.Entry<String, String> entry : departMap.entrySet()) {
            // 创建final变量供lambda使用
            final String currentDepartId = entry.getKey();
            final String currentDepartName = entry.getValue();

            demList.stream()
                    .filter(dem -> currentDepartId.equals(dem.getDepart()))
                    .findFirst()
                    .ifPresent(dem -> {
                        // 创建DTO对象
                        Dem4jjGradeDTO dto = new Dem4jjGradeDTO();
                        dto.setNo(sequence.getAndIncrement());
                        dto.setDepart(currentDepartName);
                        dto.setName(CommonUtils.getNameByHashId(dem.getAppraisee()));
                        dto.setGrade(dem.getScore());

                        // 处理排名逻辑
                        processRanking(dem, allDemByDepart, allDemByType, leaderConfigMap, dto);

                        // 设置最终排名
                        dto.setRanking(dem.getType().endsWith("1") ? dem.getScopeRanking() : null);

                        result.add(dto);
                    });
        }

        return result;
    }

    // 辅助方法7：获取领导配置映射
    private Map<String, List<String>> getLeaderConfigMap(Set<String> departIds) {
        if (departIds.isEmpty()) return Collections.emptyMap();

        LambdaQueryWrapper<AssessLeaderDepartConfig> lqw = new LambdaQueryWrapper<>();
        lqw.in(AssessLeaderDepartConfig::getDepartId, departIds);

        return leaderDepartConfigMapper.selectList(lqw).stream()
                .collect(Collectors.toMap(
                        AssessLeaderDepartConfig::getDepartId,
                        config -> Arrays.asList(config.getDepartId().split(",")),
                        (a, b) -> a,
                        HashMap::new
                ));
    }

    // 辅助方法8：处理排名逻辑
    private void processRanking(AssessDemocraticEvaluationSummary dem,
                                Map<String, List<AssessDemocraticEvaluationSummary>> allDemByDepart,
                                Map<String, List<AssessDemocraticEvaluationSummary>> allDemByType,
                                Map<String, List<String>> leaderConfigMap,
                                Dem4jjGradeDTO dto) {

        String type = dem.getType();

        if (type.startsWith("3")) {
            processType3Ranking(dem, allDemByDepart, dto);
        } else if (type.startsWith("2")) {
            processType2Ranking(dem, allDemByType, leaderConfigMap, dto);
        }
    }

    // 辅助方法9：处理类型3的排名
    private void processType3Ranking(AssessDemocraticEvaluationSummary dem,
                                     Map<String, List<AssessDemocraticEvaluationSummary>> allDemByDepart,
                                     Dem4jjGradeDTO dto) {

        List<AssessDemocraticEvaluationSummary> sameDepart = allDemByDepart.getOrDefault(
                dem.getDepart(), Collections.emptyList());

        if (!sameDepart.isEmpty()) {
            sameDepart.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getScore).reversed());
            int rank = sameDepart.indexOf(dem) + 1;

            // 满分处理
            if (dem.getScore().compareTo(BigDecimal.valueOf(100)) == 0) {
                rank = 1;
            }

            dto.setScopeRanking1(rank);
            dto.setScopeRanking2(sameDepart.size());
            dto.setScopeRanking(rank + " / " + sameDepart.size());
        }
    }

    // 辅助方法10：处理类型2的排名
    private void processType2Ranking(AssessDemocraticEvaluationSummary dem,
                                     Map<String, List<AssessDemocraticEvaluationSummary>> allDemByType,
                                     Map<String, List<String>> leaderConfigMap,
                                     Dem4jjGradeDTO dto) {

        if ("21".equals(dem.getType()) || "22".equals(dem.getType()) || "23".equals(dem.getType())) {
            List<AssessDemocraticEvaluationSummary> sameType = getSameTypeList(dem, allDemByType);
            List<String> configDepartIds = leaderConfigMap.get(dem.getDepart());

            if (configDepartIds != null && !configDepartIds.isEmpty()) {
                sameType = sameType.stream()
                        .filter(item -> configDepartIds.contains(item.getDepart()))
                        .collect(Collectors.toList());
            }

            if (!sameType.isEmpty()) {
                sameType.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getScore).reversed());

                dto.setScopeRanking1(dem.getScopeRanking());
                dto.setScopeRanking2(sameType.size());

                String rankingText = dem.getScopeRanking() + " / " + sameType.size();
                if ("21".equals(dem.getType())) {
                    dto.setScopeRanking(rankingText + "（局机关考核单元内副处职排名）");
                } else {
                    dto.setScopeRanking(rankingText + "（局机关考核单元内正处职排名）");
                }
            }
        }
    }

    // 辅助方法11：获取相同类型列表
    private List<AssessDemocraticEvaluationSummary> getSameTypeList(
            AssessDemocraticEvaluationSummary dem,
            Map<String, List<AssessDemocraticEvaluationSummary>> allDemByType) {

        return "21".equals(dem.getType()) ?
                allDemByType.getOrDefault("21", Collections.emptyList()) :
                Stream.concat(
                        allDemByType.getOrDefault("22", Collections.emptyList()).stream(),
                        allDemByType.getOrDefault("23", Collections.emptyList()).stream()
                ).collect(Collectors.toList());
    }

    private List<AssessDemocraticEvaluation> getDecBySummaryIds(List<String> summaryId) {
        if (summaryId == null || summaryId.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<AssessDemocraticEvaluation> lqw = new LambdaQueryWrapper<>();
        lqw.in(AssessDemocraticEvaluation::getEvaluationSummaryId, summaryId);
        return democraticEvaluationMapper.selectList(lqw);
    }

    private List<AssessDemocraticEvaluationItem> getDecItemsByDecIds(List<String> decIds) {
        if (decIds == null || decIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<AssessDemocraticEvaluationItem> lqw = new LambdaQueryWrapper<>();
        lqw.in(AssessDemocraticEvaluationItem::getDemocraticId, decIds);
        return itemMapper.selectList(lqw);
    }

    List<AssessDemocraticEvaluationSummary> getDemocraticLevel
            (List<AssessDemocraticEvaluationSummary> summaryLists) {
        List<AssessAnnualDemocraticConfig> lastVersions = democraticConfigService.getLastVersions();

        for (AssessDemocraticEvaluationSummary summary : summaryLists) {
            String departType = summary.getType();
            // 获取lastVersions中类型与departType相同的配置
            AssessAnnualDemocraticConfig config = lastVersions.stream()
                    .filter(item -> item.getType()
                            .equals(departType))
                    .findFirst()
                    .orElse(null);

            if (config != null) {
                // 获取配置中的权重
                BigDecimal weightA = config.getWeightA();
                BigDecimal weightB = config.getWeightB();
                BigDecimal weightC = config.getWeightC();
                BigDecimal weightD = config.getWeightD();

                BigDecimal score = summary.getScore();

                // (weightA + weightB) / 2
                BigDecimal aLine = weightA.add(weightB).divide(new BigDecimal(2), 2, RoundingMode.HALF_UP);
                // (weightB + weightC) / 2
                BigDecimal bLine = weightB.add(weightC).divide(new BigDecimal(2), 2, RoundingMode.HALF_UP);
                // (weightC + weightD) / 2
                BigDecimal cLine = weightC.add(weightD).divide(new BigDecimal(2), 2, RoundingMode.HALF_UP);

                // 评分大于等于aLine * 100为A级，大于等于bLine * 100、小于aLine为B级，大于等于cLine * 100、小于bLine为C级，小于cLine * 100为D级
                if (score != null) {
                    if (score.compareTo(aLine.multiply(new BigDecimal(100))) >= 0) {
                        summary.setDemocraticLevel("A");
                    } else if (score.compareTo(bLine.multiply(new BigDecimal(100))) >= 0) {
                        summary.setDemocraticLevel("B");
                    } else if (score.compareTo(cLine.multiply(new BigDecimal(100))) >= 0) {
                        summary.setDemocraticLevel("C");
                    } else {
                        summary.setDemocraticLevel("D");
                    }
                }

            }
        }
        return summaryLists;
    }

    private void processSummaries(List<AssessDemocraticEvaluationSummary> list, Set<String> departIdList) {
        Map<String, List<?>> specialPeople = assessCommonApi.getSpecialPeople();
        List<String> sp = (List<String>) specialPeople.get("allHashIds");

        Iterator<AssessDemocraticEvaluationSummary> iterator = list.iterator();
        while (iterator.hasNext()) {
            AssessDemocraticEvaluationSummary summary = iterator.next();
            // 如果summary的appraisee字段以在sp中，跳过本次循环
            if (sp.contains(summary.getAppraisee())) {
                continue;
            }

            String departId = summary.getDepart();
            if (departId.startsWith("JG")) {
                // 删除开头的JG
                departId = departId.substring(2);
            }
            if (!departIdList.contains(departId)) {
                iterator.remove();
            }
        }
    }

    private void processSpecialPerson(List<AssessDemocraticEvaluationSummary> list, String leader) {
        Map<String, List<?>> specialPeople = assessCommonApi.getSpecialPeople();
        List<AssessLeaderConfig> p1 = (List<AssessLeaderConfig>) specialPeople.get("leader");
        List<AssessAssistConfig> p2 = (List<AssessAssistConfig>) specialPeople.get("assist");

        // 将p1转为Map，key为hashId，value为leader
        Map<String, String> p1Map = p1.stream().collect(Collectors.toMap(AssessLeaderConfig::getHashId, AssessLeaderConfig::getLeader));
        // 将p2转为Map，key为hashId，value为leader
        Map<String, String> p2Map = p2.stream().collect(Collectors.toMap(AssessAssistConfig::getHashId, AssessAssistConfig::getLeader));

        Iterator<AssessDemocraticEvaluationSummary> iterator = list.iterator();
        while (iterator.hasNext()) {
            AssessDemocraticEvaluationSummary summary = iterator.next();
            // 如果summary的hashId在p1中，但是p1中的value不是leader的，删除元素
            if (p1Map.containsKey(summary.getAppraisee()) && !p1Map.get(summary.getAppraisee()).equals(leader)) {
                iterator.remove();
            }

            // 如果summary的hashId在p2中，但是p2中的value（String类型，多个leader，以“,”分割）不包含leader的，删除元素
            if (p2Map.containsKey(summary.getAppraisee()) && !p2Map.get(summary.getAppraisee()).contains(leader)) {
                iterator.remove();
            }
        }
    }

    @Override
    public void reRanking() {
        AssessCurrentAssess currentAssess = assessCommonApi.getCurrentAssessInfo("annual");
        if (currentAssess != null && currentAssess.isAssessing()) {
            LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw = new LambdaQueryWrapper<>();
            lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, currentAssess.getCurrentYear());

            List<AssessDemocraticEvaluationSummary> summaryLists = this.list(lqw);
            if (summaryLists != null && !summaryLists.isEmpty()) {
                // 将IdList转为List
                List<String> ids = summaryLists.stream().map(AssessDemocraticEvaluationSummary::getId).collect(Collectors.toList());
                this.statisticalScore(ids);
                this.updateRanking(currentAssess.getCurrentYear());
                this.updateScopeRanking(currentAssess.getCurrentYear());
            } else {
                throw new JeecgBootException("当前无正在进行中的考核");
            }
        } else {
            throw new JeecgBootException("当前无正在进行中的考核");
        }
    }

}
