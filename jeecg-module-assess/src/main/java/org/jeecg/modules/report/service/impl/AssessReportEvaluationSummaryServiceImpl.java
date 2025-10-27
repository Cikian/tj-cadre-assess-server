package org.jeecg.modules.report.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Synchronized;
import net.sf.saxon.expr.Component;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.handler.IFillRuleHandler;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.report.service.*;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.dto.*;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.report.*;
import org.jeecg.modules.report.vo.AssessReportDemocracyFillVO;
import org.jeecg.modules.sys.mapper.annual.AssessDemocraticEvaluationSummaryMapper;
import org.jeecg.modules.sys.mapper.report.*;
import org.jeecg.modules.user.UserCommonApi;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.MarshalledObject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.text.DecimalFormat;

/**
 * @Description: 一报告两评议民主测评汇总
 * @Author: jeecg-boot
 * @Date: 2024-10-08
 * @Version: V1.0
 */
@Service
public class AssessReportEvaluationSummaryServiceImpl extends ServiceImpl<AssessReportEvaluationSummaryMapper, AssessReportEvaluationSummary> implements IAssessReportEvaluationSummaryService {

    @Autowired
    private AssessReportEvaluationSummaryMapper democraticSummaryMapper;
    @Autowired
    private AssessReportEvaluationItemMapper democraticItemMapper;
    @Autowired
    private DepartCommonApi departCommonApi;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private AssessReportFillMapper fillMapper;
    @Autowired
    private AssessReportArrangeMapper arrangeMapper;
    @Autowired
    private AssessReportDemocracyMultiMapper multiMapper;
    @Autowired
    private AssessReportEvaluationNewleaderMapper newLeaderMapper;
    @Autowired
    private AssessReportNewLeaderMapper reportNewLeaderMapper;
    @Autowired
    private UserCommonApi userCommonApi;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private IAssessReportDemocracyConfigService assessReportDemocracyConfig;
    @Autowired
    private IAssessReportDemocracyConfigItemService assessReportDemocracyConfigItemService;
    @Autowired
    private IAssessReportEvaluationItemService assessReportEvaluationItemService;
    @Autowired
    private AssessReportMultiItemMapper assessReportMultiItemMapper;
    @Autowired
    private AssessReportDemocracyMultiMapper assessReportDemocracyMultiMapper;
    @Autowired
    private IAssessReportNewLeaderService assessReportNewLeaderService;
    @Autowired
    private IAssessReportFillService assessReportFillService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMain(AssessReportEvaluationSummary assessReportEvaluationSummary, List<AssessReportEvaluationItem> assessReportEvaluationItemList) {
        democraticSummaryMapper.insert(assessReportEvaluationSummary);
        if (assessReportEvaluationItemList != null && assessReportEvaluationItemList.size() > 0) {
            for (AssessReportEvaluationItem entity : assessReportEvaluationItemList) {
                // 外键设置
                entity.setSummaryId(assessReportEvaluationSummary.getId());
                democraticItemMapper.insert(entity);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMain(AssessReportEvaluationSummary assessReportEvaluationSummary, List<AssessReportEvaluationItem> assessReportEvaluationItemList) {
        democraticSummaryMapper.updateById(assessReportEvaluationSummary);

        // 1.先删除子表数据
        democraticItemMapper.deleteByMainId(assessReportEvaluationSummary.getId());

        // 2.子表数据重新插入
        if (assessReportEvaluationItemList != null && assessReportEvaluationItemList.size() > 0) {
            for (AssessReportEvaluationItem entity : assessReportEvaluationItemList) {
                // 外键设置
                entity.setSummaryId(assessReportEvaluationSummary.getId());
                democraticItemMapper.insert(entity);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delMain(String id) {
        democraticItemMapper.deleteByMainId(id);
        democraticSummaryMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delBatchMain(Collection<? extends Serializable> idList) {
        for (Serializable id : idList) {
            democraticItemMapper.deleteByMainId(id.toString());
            democraticSummaryMapper.deleteById(id);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean initDemocratic(DemocraticInitDTO map) {
        String currentYear = map.getCurrentYear();
        // 判断当前年度是否已经初始化
        LambdaQueryWrapper<AssessReportEvaluationSummary> lqw1 = new LambdaQueryWrapper<>();
        lqw1.eq(AssessReportEvaluationSummary::getCurrentYear, currentYear);
        lqw1.select(AssessReportEvaluationSummary::getId);
        List<AssessReportEvaluationSummary> summaryList = democraticSummaryMapper.selectList(lqw1);
        if (!summaryList.isEmpty()) {
            throw new JeecgBootException("当前年度已存在一报告两评议民主测评，不可重复发起！");
        }

        userCommonApi.deleteAllAnonymousAccount("report");


        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        AssessReportEvaluationSummaryMapper summaryMapperNew = sqlSession.getMapper(AssessReportEvaluationSummaryMapper.class);
        AssessReportEvaluationItemMapper itemMapperNew = sqlSession.getMapper(AssessReportEvaluationItemMapper.class);
        AssessReportDemocracyMultiMapper multiMapperNew = sqlSession.getMapper(AssessReportDemocracyMultiMapper.class);

        // 获取填报列表
        LambdaQueryWrapper<AssessReportFill> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(AssessReportFill::getCurrentYear, currentYear);
        List<AssessReportFill> fills = fillMapper.selectList(lqw2);

        List<AssessReportEvaluationSummary> insertSummaryList = new ArrayList<>();


        // 插入各处室考核填报表
        for (AssessReportFill fill : fills) {
            // 雪花算法生成ID
            String summaryId = IdWorker.getIdStr();

            AssessReportEvaluationSummary summary = new AssessReportEvaluationSummary();
            summary.setId(summaryId);
            summary.setReportFillId(fill.getId());
            summary.setAssessName(map.getAssessName());
            summary.setCurrentYear(map.getCurrentYear());
            summary.setDepart(fill.getDepart());
            Date date = new Date();
            summary.setStartDate(date);
            summary.setEndDate(map.getDeadline());

            Integer evaluationNum = arrangeMapper.selectEvaluationNumByDepartId(fill.getDepart(), fill.getId());
            summary.setNum(evaluationNum);

            if (evaluationNum != null && evaluationNum != 0) {
                insertSummaryList.add(summary);
            }
        }

        // 批量插入
        insertSummaryList.forEach(summaryMapperNew::insert);

        // 添加年份字典数据
        departCommonApi.saveAssessYear(map.getCurrentYear());

        // 提交事务、清理缓存、关闭sqlSession
        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();
        return !insertSummaryList.isEmpty();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    @Synchronized
    public void saveDemocracy(AssessReportDemocracyFillVO map) {
        checkSubmit(map);

        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        map.getItems().forEach(item -> {
            item.setSummaryId(map.getSummaryId());
            item.setVoteType(sysUser.getVoteType());
            if (item.getMultiList() != null && !item.getMultiList().isEmpty() && "2".equals(item.getInputType())) {
                item.getMultiList().forEach(multi -> {
                    multi.setSummaryId(map.getSummaryId());
                    multiMapper.insert(multi);
                });
            }
            democraticItemMapper.insert(item);
        });

        List<String> newLeaders = new ArrayList<>();

        map.getNewLeadersItems().forEach(item -> {
            AssessReportEvaluationNewleader newLeader = new AssessReportEvaluationNewleader();
            BeanUtils.copyProperties(item, newLeader);
            newLeader.setSummaryId(map.getSummaryId());
            newLeader.setVoteType(sysUser.getVoteType());
            newLeaderMapper.insert(newLeader);
            newLeaders.add(newLeader.getNewLeaderItem());
        });

        AssessReportEvaluationSummary summary = democraticSummaryMapper.selectById(map.getSummaryId());
        summary.setFilledNum(summary.getFilledNum() + 1);
        if (map.getReason() != null && !map.getReason().isEmpty()) {
            String reason = summary.getReason();
            if (reason != null && !reason.isEmpty()) reason += "\n" + map.getReason();
            else reason = map.getReason();
            summary.setReason(reason);
        }
        democraticSummaryMapper.updateById(summary);

        this.updateRank(summary.getCurrentYear());
        this.updateRankingChange(summary.getCurrentYear());
        if (!newLeaders.isEmpty()) this.updateNewLeaderRank(newLeaders);

    }

    @Override
    public Map<String, List<?>> getFillData(String summaryId) {
        LambdaQueryWrapper<AssessReportEvaluationItem> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessReportEvaluationItem::getSummaryId, summaryId);
        lqw.ne(AssessReportEvaluationItem::getInputType, "2");
        lqw.isNotNull(AssessReportEvaluationItem::getFill);
        List<AssessReportEvaluationItem> items = democraticItemMapper.selectList(lqw);
        LambdaQueryWrapper<AssessReportDemocracyMulti> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(AssessReportDemocracyMulti::getSummaryId, summaryId);
        lqw2.eq(AssessReportDemocracyMulti::getSelected, true);
        List<AssessReportDemocracyMulti> multis = multiMapper.selectList(lqw2);

        // for (AssessReportEvaluationItem item : items) {
        //     if ("1".equals(item.getInputType())) {
        //
        //     }
        // }

        Map<String, Integer> multiMap = new HashMap<>();
        for (AssessReportDemocracyMulti item : multis) {
            if (multiMap.containsKey(item.getTopic())) {
                multiMap.put(item.getTopic(), multiMap.get(item.getTopic()) + 1);
            } else {
                multiMap.put(item.getTopic(), 1);
            }
        }

        // 遍历map
        List<Map<String, Object>> multiList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : multiMap.entrySet()) {
            Map<String, Object> multi = new HashMap<>();
            String output = entry.getKey().replaceFirst("^（.*?）", "");
            multi.put("topic", output);
            multi.put("num", entry.getValue());
            multiList.add(multi);
        }

        // 对multiList进行排序，按照value降序
        multiList.sort((o1, o2) -> {
            Integer v1 = (Integer) o1.get("num");
            Integer v2 = (Integer) o2.get("num");
            return v2 - v1;
        });


        Map<String, List<?>> result = new HashMap<>();
        result.put("items", items);
        result.put("multiList", multiList);

        return result;
    }

    @Override
    public List<ReportNewLeaderDTO> getNewLeaderSummaryByYear(String year, String column, String order) {
        List<ReportNewLeaderDTO> list = reportNewLeaderMapper.getNewLeaderSummaryByYear(year, column, order);
        if (list != null && !list.isEmpty()) {
            return list;
        }
        return Collections.emptyList();
    }

    void checkSubmit(AssessReportDemocracyFillVO map) {
        AssessReportEvaluationSummary summary = democraticSummaryMapper.selectById(map.getSummaryId());
        if (summary.getFilledNum() >= summary.getNum()) {
            throw new JeecgBootException("已达到填报人数上限！");
        }

        List<AssessReportEvaluationItem> items = map.getItems();
        for (AssessReportEvaluationItem item : items) {
            if ("1".equals(item.getInputType()) && item.getFill() == null) {
                throw new JeecgBootException("《干部选拔任用工作民主评议表》有必填项目未填写！");
            }
        }

        List<AssessReportEvaluationNewleader> newLeadersItems = map.getNewLeadersItems();
        for (AssessReportEvaluationNewleader item : newLeadersItems) {
            if (item.getFill() == null) {
                throw new JeecgBootException("《新提拔任用干部民主评议表》中有未填写的对提拔任用干部的看法！");
            }
        }

    }

    @Synchronized
    void updateRank(String year) {
        LambdaQueryWrapper<AssessReportEvaluationSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessReportEvaluationSummary::getCurrentYear, year);
        List<AssessReportEvaluationSummary> summaries = democraticSummaryMapper.selectList(lqw);
        List<String> summaryIds = new ArrayList<>();
        for (AssessReportEvaluationSummary summary : summaries) {
            summaryIds.add(summary.getId());
        }


        LambdaQueryWrapper<AssessReportEvaluationItem> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(AssessReportEvaluationItem::getInputType, "1");
        lqw2.in(AssessReportEvaluationItem::getSummaryId, summaryIds);
        List<AssessReportEvaluationItem> items = democraticItemMapper.selectList(lqw2);
        Map<String, List<AssessReportEvaluationItem>> itemMap = items.stream().collect(Collectors.groupingBy(AssessReportEvaluationItem::getSummaryId));


        // todo: sql不兼容
        // List<ReportDemocracyGoodNum> rankingByUpdate = democraticItemMapper.getRankingByUpdate(summaryIds);

        for (AssessReportEvaluationSummary summary : summaries) {
            int goodNum = 0;
            int totalItem = 0;
            List<AssessReportEvaluationItem> item = itemMap.get(summary.getId());
            if (item == null || item.isEmpty()) {
                summary.setGoodNum("0");
                summary.setGoodRate(new BigDecimal(0));
                continue;
            }
            totalItem = item.size();
            for (AssessReportEvaluationItem i : item) {
                if ("A".equals(i.getFill())) {
                    goodNum++;
                }
            }

            BigDecimal rate = new BigDecimal(goodNum).divide(new BigDecimal(totalItem), 4, RoundingMode.HALF_UP);

            summary.setGoodNum(String.valueOf(goodNum));
            summary.setGoodRate(rate);


//            for (ReportDemocracyGoodNum rank : rankingByUpdate) {
//                if (summary.getId().equals(rank.getSummaryId())) {
//                    summary.setGoodNum(rank.getGood());
//                    summary.setGoodRate(rank.getRate());
//                    summary.setRanking(rank.getRanking());
//                }
//            }
        }

        // 按照rate从大到小排序summaries
        summaries.sort(Comparator.comparing(AssessReportEvaluationSummary::getGoodRate).reversed());
        // 更新summaries的ranking
        for (int i = 0; i < summaries.size(); i++) {
            summaries.get(i).setRanking(i + 1);
        }

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        AssessReportEvaluationSummaryMapper summaryMapperNew = sqlSession.getMapper(AssessReportEvaluationSummaryMapper.class);

        summaries.forEach(summaryMapperNew::updateById);

        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();
    }

    @Synchronized
    void updateRankingChange(String year) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        AssessReportEvaluationSummaryMapper summaryMapperNew = sqlSession.getMapper(AssessReportEvaluationSummaryMapper.class);


        LambdaQueryWrapper<AssessReportEvaluationSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessReportEvaluationSummary::getCurrentYear, year);
        List<AssessReportEvaluationSummary> currentYear = democraticSummaryMapper.selectList(lqw);
        lqw.clear();
        lqw.eq(AssessReportEvaluationSummary::getCurrentYear, String.valueOf(Integer.parseInt(year) - 1));
        List<AssessReportEvaluationSummary> lastYear = democraticSummaryMapper.selectList(lqw);

        // 使用stream转为map，key为depart,value为ranking
        Map<String, Integer> lastYearMap = new HashMap<>();
        if (lastYear != null && !lastYear.isEmpty()) {
            try {
                lastYearMap = lastYear.stream().collect(Collectors.toMap(AssessReportEvaluationSummary::getDepart, AssessReportEvaluationSummary::getRanking));
            } catch (NullPointerException e) {
                log.error("获取上一年度排名失败");
                return;
            }
        }

        if (!lastYearMap.isEmpty()) {
            for (AssessReportEvaluationSummary current : currentYear) {
                Integer lastRanking = lastYearMap.get(current.getDepart());
                if (lastRanking != null) {
                    int change = lastRanking - current.getRanking();
                    current.setRankingChange(change);
                }
            }
        } else {
            for (AssessReportEvaluationSummary current : currentYear) {
                current.setRankingChange(0);
            }
        }

        currentYear.forEach(summaryMapperNew::updateById);

        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();
    }

    // 更新提拔干部认同率与不认同率
    void updateNewLeaderRank(List<String> newLeaderIds) {
        for (String newLeaderId : newLeaderIds) {

            // 查找唯一的新干部
            LambdaQueryWrapper<AssessReportNewLeader> lqw = new LambdaQueryWrapper<>();
            lqw.eq(AssessReportNewLeader::getId, newLeaderId);
            AssessReportNewLeader newLeader = reportNewLeaderMapper.selectOne(lqw);

            LambdaQueryWrapper<AssessReportEvaluationNewleader> lqw2 = new LambdaQueryWrapper<>();
            lqw2.eq(AssessReportEvaluationNewleader::getNewLeaderItem, newLeaderId);
            List<AssessReportEvaluationNewleader> newLeadersItem = newLeaderMapper.selectList(lqw2);
            int total = newLeadersItem.size();
            int numA = 0;
            int numB = 0;
            int numC = 0;
            int numD = 0;
            for (AssessReportEvaluationNewleader newLeaderItem : newLeadersItem) {
                switch (newLeaderItem.getFill()) {
                    case "A":
                        numA++;
                        break;
                    case "B":
                        numB++;
                        break;
                    case "C":
                        numC++;
                        break;
                    case "D":
                        numD++;
                        break;
                    default:
                        break;
                }
            }

            BigDecimal rateA = BigDecimal.valueOf(numA).divide(BigDecimal.valueOf(total), 6, RoundingMode.HALF_UP);
            BigDecimal rateB = BigDecimal.valueOf(numB).divide(BigDecimal.valueOf(total), 6, RoundingMode.HALF_UP);
            BigDecimal rateC = BigDecimal.valueOf(numC).divide(BigDecimal.valueOf(total), 6, RoundingMode.HALF_UP);
            BigDecimal rateD = BigDecimal.valueOf(numD).divide(BigDecimal.valueOf(total), 6, RoundingMode.HALF_UP);

            // 乘以100，保留两位小数
            rateA = rateA.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
            rateB = rateB.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
            rateC = rateC.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
            rateD = rateD.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);

            // 100 - A - B - C，保留两位小数，保证和为100
            if (rateD.compareTo(BigDecimal.ZERO) > 0) {
                rateD = BigDecimal.valueOf(100).subtract(rateA).subtract(rateB).subtract(rateC).setScale(2, RoundingMode.HALF_UP);
            } else if (rateC.compareTo(BigDecimal.ZERO) > 0) {
                rateC = BigDecimal.valueOf(100).subtract(rateA).subtract(rateB).subtract(rateD).setScale(2, RoundingMode.HALF_UP);
            } else if (rateB.compareTo(BigDecimal.ZERO) > 0) {
                rateB = BigDecimal.valueOf(100).subtract(rateA).subtract(rateC).subtract(rateD).setScale(2, RoundingMode.HALF_UP);
            } else {
                rateA = BigDecimal.valueOf(100).subtract(rateB).subtract(rateC).subtract(rateD).setScale(2, RoundingMode.HALF_UP);
            }

            newLeader.setRecognitionRate(rateA);
            newLeader.setIdentityRate(rateB);
            newLeader.setDisagreementRate(rateC);
            newLeader.setUnknownRate(rateD);

            reportNewLeaderMapper.updateById(newLeader);
        }
    }

    @Override
    public List<ReportEnterpriseAgreeRankDTO> getEnterpriseAgreeRankByYear(String year) {
        List<ReportEnterpriseAgreeRankDTO> list = new LinkedList<>();
        LambdaQueryWrapper<AssessReportEvaluationSummary> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AssessReportEvaluationSummary::getCurrentYear, year);
        List<AssessReportEvaluationSummary> summaryList = this.list(lambdaQueryWrapper);
        for (AssessReportEvaluationSummary summary : summaryList) {
            ReportEnterpriseAgreeRankDTO item = new ReportEnterpriseAgreeRankDTO();
            item.setId(UUIDGenerator.generate());
            item.setYear(year);
            String departName = departCommonApi.queryDepartById(summary.getDepart()).getDepartName();
            item.setDepart(departName);
            item.setCount(String.valueOf(getNewLeaderCount(summary.getReportFillId())));
            item.setAgreeRate(calAgreeRate(summary.getReportFillId()));
            list.add(item);
        }
        List<ReportEnterpriseAgreeRankDTO> result = calRank(list);
        return result;
    }

    /**
     * 干部选拔任用工作民主测评排名（第1题）
     *
     * @param year
     * @return
     */
    @Override
    public List<ReportLeaderWorkRankDTO> getLeaderWorkRankByQuestion1(String year) {
        return rankByQuestion(year, "1");
    }

    /**
     * 干部选拔任用工作民主测评排名（第2题）
     *
     * @param year
     * @return
     */
    @Override
    public List<ReportLeaderWorkRankDTO> getLeaderWorkRankByQuestion2(String year) {
        return rankByQuestion(year, "2");
    }

    /**
     * 干部选拔任用工作民主测评排名（第3题）
     *
     * @param year
     * @return
     */
    @Override
    public List<ReportLeaderWorkRankDTO> getLeaderWorkRankByQuestion3(String year) {
        return rankByQuestion(year, "3");
    }

    public List<ReportLeaderWorkRankDTO> rankByQuestion(String year, String itemOrder) {
        String id = getCurrentConfigId();
        String topic = getTopic(id, "1", itemOrder);
        List<ReportLeaderWorkRankDTO> result = new ArrayList<>();
        LambdaQueryWrapper<AssessReportEvaluationSummary> lqw = new LambdaQueryWrapper<>();
        lqw.select(AssessReportEvaluationSummary::getId, AssessReportEvaluationSummary::getCurrentYear, AssessReportEvaluationSummary::getDepart);
        lqw.eq(AssessReportEvaluationSummary::getCurrentYear, year);
        List<AssessReportEvaluationSummary> list = this.list(lqw);
        for (AssessReportEvaluationSummary assessReportEvaluationSummary :
                list) {
            ReportLeaderWorkRankDTO dto = new ReportLeaderWorkRankDTO();
            dto.setYear(year);
            String departName = departCommonApi.queryDepartById(assessReportEvaluationSummary.getDepart()).getDepartName();
            dto.setDepart(departName);
            String summaryId = assessReportEvaluationSummary.getId();
            LambdaQueryWrapper<AssessReportEvaluationItem> lambdaQueryWrapperSum = new LambdaQueryWrapper<>();
            lambdaQueryWrapperSum.select(AssessReportEvaluationItem::getId);
            lambdaQueryWrapperSum.eq(AssessReportEvaluationItem::getSummaryId, summaryId).eq(AssessReportEvaluationItem::getTopic, topic);
            int total = (int) assessReportEvaluationItemService.count(lambdaQueryWrapperSum);

            LambdaQueryWrapper<AssessReportEvaluationItem> lambdaQueryWrapperGood = new LambdaQueryWrapper<>();
            lambdaQueryWrapperGood.select(AssessReportEvaluationItem::getId);
            lambdaQueryWrapperGood.eq(AssessReportEvaluationItem::getSummaryId, summaryId).eq(AssessReportEvaluationItem::getTopic, topic).eq(AssessReportEvaluationItem::getFill, "A");
            int count = (int) assessReportEvaluationItemService.count(lambdaQueryWrapperGood);
            double v = (double) count / total;
            String formattedNumber = String.format("%.5f", v);
            double dV = Double.valueOf(formattedNumber);
            dto.setRate(dV);
            String percentV = String.format("%.3f%%", dV * 100);
            dto.setRateText(percentV);
            result.add(dto);
        }
        return calWorkRank(result);
    }

    private int getNewLeaderCount(String reportFillId) {
        LambdaQueryWrapper<AssessReportNewLeader> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessReportNewLeader::getFillId, reportFillId);
        return reportNewLeaderMapper.selectCount(lqw).intValue();
    }

    private double calAgreeRate(String reportFillId) {
        LambdaQueryWrapper<AssessReportNewLeader> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AssessReportNewLeader::getFillId, reportFillId);
        lambdaQueryWrapper.select(AssessReportNewLeader::getId, AssessReportNewLeader::getRecognitionRate);
        List<AssessReportNewLeader> list = reportNewLeaderMapper.selectList(lambdaQueryWrapper);
        double ave = average(list);
        DecimalFormat df = new DecimalFormat("#.00000");
        String rate = df.format(ave);
        return Double.valueOf(rate);
    }

    private double average(List<AssessReportNewLeader> list) {
        double ave = 0;
        if (list != null && !list.isEmpty()) {
            double sum = 0;
            for (AssessReportNewLeader leader :
                    list) {
                BigDecimal recognitionRate = leader.getRecognitionRate();
                if (recognitionRate == null) {
                    recognitionRate = new BigDecimal(0);
                }
                sum += recognitionRate.floatValue();
            }
            ave = sum / list.size();
        }
        return ave / 100;
    }

    private List<ReportEnterpriseAgreeRankDTO> calRank(List<ReportEnterpriseAgreeRankDTO> list) {
        List<ReportEnterpriseAgreeRankDTO> list2 = list.stream().sorted(Comparator.comparing(ReportEnterpriseAgreeRankDTO::getAgreeRate).reversed()).collect(Collectors.toList());//降序排序
        int count = 1;
        for (int i = 0; i < list2.size(); i++) {
            ReportEnterpriseAgreeRankDTO rankDTO = list2.get(i);
            if (i == 0) {
                rankDTO.setRank("1");
            } else {
                ReportEnterpriseAgreeRankDTO preRankDTO = list2.get(i - 1);
                if (rankDTO.getAgreeRate() == preRankDTO.getAgreeRate()) {
                    rankDTO.setRank(preRankDTO.getRank());
                    count++;
                } else {
                    int preRank = Integer.valueOf(preRankDTO.getRank());
                    rankDTO.setRank(String.valueOf(preRank + count));
                    count = 1;
                }
            }
        }

        ReportEnterpriseAgreeRankDTO item = new ReportEnterpriseAgreeRankDTO();
        item.setDepart("以上有提拔干部的单位的平均值");
        double v = calAverageRate(list2);
        DecimalFormat df = new DecimalFormat("#.000");
        String rate = df.format(v);
        item.setAgreeRate(Double.valueOf(rate));
        item.setRank("");
        item.setCount("");
        list2.add(item);
        return list2;
    }

    private List<ReportLeaderWorkRankDTO> calWorkRank(List<ReportLeaderWorkRankDTO> list) {
        List<ReportLeaderWorkRankDTO> list2 = list.stream().sorted(Comparator.comparing(ReportLeaderWorkRankDTO::getRate).reversed()).collect(Collectors.toList());//降序排序
        int count = 1;
        for (int i = 0; i < list2.size(); i++) {
            ReportLeaderWorkRankDTO rankDTO = list2.get(i);
            if (i == 0) {
                rankDTO.setRank("1");
            } else {
                ReportLeaderWorkRankDTO preRankDTO = list2.get(i - 1);
                if (rankDTO.getRate() == (preRankDTO.getRate())) {
                    rankDTO.setRank(preRankDTO.getRank());
                    count++;
                } else {
                    int preRank = Integer.valueOf(preRankDTO.getRank());
                    rankDTO.setRank(String.valueOf(preRank + count));
                    count = 1;
                }
            }
        }
        return list2;
    }

    private double calAverageRate(List<ReportEnterpriseAgreeRankDTO> list) {
        double ave = 0;
        int size = 0;
        if (list != null && list.size() > 0) {
            double sum = 0;
            for (ReportEnterpriseAgreeRankDTO agreeRankDTO :
                    list) {
                double dV = agreeRankDTO.getAgreeRate();
                if (dV != 0) {
                    sum += dV;
                    size++;
                }
            }
            ave = sum / size;
        }
        return ave;
    }

    /**
     * 获取当前启用的配置表的ID
     *
     * @return
     */
    private String getCurrentConfigId() {
        LambdaQueryWrapper<AssessReportDemocracyConfig> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AssessReportDemocracyConfig::getEnable, 1);
        AssessReportDemocracyConfig item = assessReportDemocracyConfig.getOne(lambdaQueryWrapper);
        return item.getId();
    }

    /**
     * 获取当前启用的配置表的ID
     *
     * @return
     */
    private String getTopic(String configId, String type, String itemOrder) {
        LambdaQueryWrapper<AssessReportDemocracyConfigItem> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AssessReportDemocracyConfigItem::getConfigId, configId).eq(AssessReportDemocracyConfigItem::getType, type).eq(AssessReportDemocracyConfigItem::getItemOrder, itemOrder);
        AssessReportDemocracyConfigItem item = assessReportDemocracyConfigItemService.getOne(lambdaQueryWrapper);
        return item.getTitle();
    }

    /**
     * 获取当前启用的配置表对象
     *
     * @return
     */
    private AssessReportDemocracyConfigItem getMutiTopic(String configId, String type, String itemOrder) {
        LambdaQueryWrapper<AssessReportDemocracyConfigItem> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AssessReportDemocracyConfigItem::getConfigId, configId).eq(AssessReportDemocracyConfigItem::getType, type).eq(AssessReportDemocracyConfigItem::getItemOrder, itemOrder);
        AssessReportDemocracyConfigItem item = assessReportDemocracyConfigItemService.getOne(lambdaQueryWrapper);
        return item;
    }

    @Override
    public void reRanking() {
        AssessCurrentAssess assess = assessCommonApi.getCurrentAssessInfo("report");
        if (assess == null || !assess.isAssessing()) {
            throw new JeecgBootException("当前不在考核期内!");
        }

        LambdaQueryWrapper<AssessReportNewLeader> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessReportNewLeader::getCurrentYear, assess.getCurrentYear());
        List<AssessReportNewLeader> newLeaders = reportNewLeaderMapper.selectList(lqw);
        if (!newLeaders.isEmpty()) {
            // 将id取出
            List<String> ids = newLeaders.stream().map(AssessReportNewLeader::getId).collect(Collectors.toList());
            this.updateRank(assess.getCurrentYear());
            this.updateRankingChange(assess.getCurrentYear());
            if (!newLeaders.isEmpty()) this.updateNewLeaderRank(ids);
        }
    }

    /**
     * 获取年度干部选拔任用工作民主评议结果汇总表数据
     *
     * @param year
     * @return
     */
    @Override
    public List<ReportLeaderWorkDetailsDTO> getLeaderWorkDetailsData(String year) {
        int code = 0;
        String id = getCurrentConfigId();
        String topic1 = getTopic(id, "1", "1");
        String topic2 = getTopic(id, "1", "2");
        String topic3 = getTopic(id, "1", "3");
        AssessReportDemocracyConfigItem mutiItem = getMutiTopic(id, "2", "4");
        List<AssessReportMultiItem> mutiTopics = getMutiItemConfigTopic(mutiItem.getId());
        String topic5 = getTopic(id, "3", "5");
        String topic6 = getTopic(id, "3", "6");
        List<ReportLeaderWorkDetailsDTO> result = new LinkedList<>();
        LambdaQueryWrapper<AssessReportEvaluationSummary> lqw = new LambdaQueryWrapper<>();
        lqw.select(AssessReportEvaluationSummary::getId, AssessReportEvaluationSummary::getCurrentYear, AssessReportEvaluationSummary::getDepart);
        lqw.eq(AssessReportEvaluationSummary::getCurrentYear, year);
        List<AssessReportEvaluationSummary> list = this.list(lqw);
        for (AssessReportEvaluationSummary assessReportEvaluationSummary :
                list) {
            code++;
            ReportLeaderWorkDetailsDTO dtoA = new ReportLeaderWorkDetailsDTO();
            ReportLeaderWorkDetailsDTO dtoB = new ReportLeaderWorkDetailsDTO();
            ReportLeaderWorkDetailsDTO dtoSum = new ReportLeaderWorkDetailsDTO();
            dtoA.setCode(code);
            dtoB.setCode(code);
            dtoSum.setCode(code);
            dtoA.setYear(year);
            dtoB.setYear(year);
            dtoSum.setYear(year);
            dtoA.setVoteType("A");
            dtoB.setVoteType("B");
            dtoSum.setVoteType("小计");
            String departName = departCommonApi.queryDepartById(assessReportEvaluationSummary.getDepart()).getDepartName();
            dtoA.setDepart(departName);
            dtoB.setDepart(departName);
            dtoSum.setDepart(departName);
            String summaryId = assessReportEvaluationSummary.getId();

            int totalA = getFilledVoteTotal(summaryId, topic1, "A");//A票总数
            int totalB = getFilledVoteTotal(summaryId, topic1, "B");//B票总数
            int totalSum = totalA + totalB;//AB票总数
            dtoA.setTotal(totalA);
            dtoB.setTotal(totalB);
            dtoSum.setTotal(totalSum);

            //单选题测评结果赋值
            setSingleValue1(dtoA, dtoB, dtoSum, summaryId, topic1, totalA, totalB);//第1题
            setSingleValue2(dtoA, dtoB, dtoSum, summaryId, topic2, totalA, totalB);//第2题
            setSingleValue3(dtoA, dtoB, dtoSum, summaryId, topic3, totalA, totalB);//第3题

            //多选题测评结果赋值
            int index = 0;
            Map<String, String> map = getMap(summaryId);
            for (AssessReportMultiItem assessReportMultiItem :
                    mutiTopics) {
                index++;
                String subTopic = assessReportMultiItem.getTopic();
                List<AssessReportDemocracyMulti> mutiVotelist = getMutiVoteData(summaryId, subTopic);
                JSONObject jsonObject = getMutiVoteTypeCount(mutiVotelist, map);
                setMutiValue(dtoA, dtoB, dtoSum, jsonObject, index, totalA, totalB);
            }

            //问答题测评结果赋值
            String suggestA5 = getSuggest(summaryId, topic5, "A");
            String suggestB5 = getSuggest(summaryId, topic5, "B");
            String suggestA6 = getSuggest(summaryId, topic6, "A");
            String suggestB6 = getSuggest(summaryId, topic6, "B");
            dtoA.setSuggest16(suggestA5);
            dtoB.setSuggest16(suggestB5);
            dtoA.setSuggest17(suggestA6);
            dtoB.setSuggest17(suggestB6);
            result.add(dtoA);
            result.add(dtoB);
            result.add(dtoSum);
        }
        return result;
    }

    /**
     * 获取AB票总数
     *
     * @param summaryId
     * @param topic
     * @param voteType
     * @return
     */
    public int getFilledVoteTotal(String summaryId, String topic, String voteType) {
        LambdaQueryWrapper<AssessReportEvaluationItem> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AssessReportEvaluationItem::getSummaryId, summaryId).eq(AssessReportEvaluationItem::getVoteType, voteType).like(AssessReportEvaluationItem::getTopic, topic);
        int total = (int) assessReportEvaluationItemService.count(lambdaQueryWrapper);
        return total;
    }

    /**
     * 单选题赋值(第1题)
     *
     * @param dtoA
     * @param dtoB
     * @param summaryId
     * @param topic
     */
    private void setSingleValue1(ReportLeaderWorkDetailsDTO dtoA, ReportLeaderWorkDetailsDTO dtoB, ReportLeaderWorkDetailsDTO dtoSum, String summaryId, String topic, int totalA, int totalB) {
        int countGoodA1 = getSingleVoteNum(summaryId, topic, "A", "A");
        int countGoodB1 = getSingleVoteNum(summaryId, topic, "B", "A");
        int countBetterA1 = getSingleVoteNum(summaryId, topic, "A", "B");
        int countBetterB1 = getSingleVoteNum(summaryId, topic, "B", "B");
        int countGeneralA1 = getSingleVoteNum(summaryId, topic, "A", "C");
        int countGeneralB1 = getSingleVoteNum(summaryId, topic, "B", "C");
        int countBadA1 = getSingleVoteNum(summaryId, topic, "A", "D");
        int countBadB1 = getSingleVoteNum(summaryId, topic, "B", "D");
        int totalSum = totalA + totalB;
        dtoA.setGoodNum1(countGoodA1);
        dtoA.setGoodRate1(calRate(countGoodA1, totalA));
        dtoB.setGoodNum1(countGoodB1);
        dtoB.setGoodRate1(calRate(countGoodB1, totalB));
        dtoSum.setGoodNum1(countGoodA1 + countGoodB1);
        dtoSum.setGoodRate1(calRate(countGoodA1 + countGoodB1, totalSum));

        dtoA.setBetterNum1(countBetterA1);
        dtoA.setBetterRate1(calRate(countBetterA1, totalA));
        dtoB.setBetterNum1(countBetterB1);
        dtoB.setBetterRate1(calRate(countBetterB1, totalB));
        dtoSum.setBetterNum1(countBetterA1 + countBetterB1);
        dtoSum.setBetterRate1(calRate(countBetterA1 + countBetterB1, totalSum));

        dtoA.setGeneralNum1(countGeneralA1);
        dtoA.setGeneralRate1(calRate(countGeneralA1, totalA));
        dtoB.setGeneralNum1(countGeneralB1);
        dtoB.setGeneralRate1(calRate(countGeneralB1, totalB));
        dtoSum.setGeneralNum1(countGeneralA1 + countGeneralB1);
        dtoSum.setGeneralRate1(calRate(countGeneralA1 + countGeneralB1, totalSum));

        dtoA.setBadNum1(countBadA1);
        dtoA.setBadRate1(calRate(countBadA1, totalA));
        dtoB.setBadNum1(countBadB1);
        dtoB.setBadRate1(calRate(countBadB1, totalB));
        dtoSum.setBadNum1(countBadA1 + countBadB1);
        dtoSum.setBadRate1(calRate(countBadA1 + countBadB1, totalSum));
    }

    /**
     * 单选题赋值(第2题)
     *
     * @param dtoA
     * @param dtoB
     * @param summaryId
     * @param topic
     */
    private void setSingleValue2(ReportLeaderWorkDetailsDTO dtoA, ReportLeaderWorkDetailsDTO dtoB, ReportLeaderWorkDetailsDTO dtoSum, String summaryId, String topic, int totalA, int totalB) {
        int countGoodA2 = getSingleVoteNum(summaryId, topic, "A", "A");
        int countGoodB2 = getSingleVoteNum(summaryId, topic, "B", "A");
        int countBetterA2 = getSingleVoteNum(summaryId, topic, "A", "B");
        int countBetterB2 = getSingleVoteNum(summaryId, topic, "B", "B");
        int countGeneralA2 = getSingleVoteNum(summaryId, topic, "A", "C");
        int countGeneralB2 = getSingleVoteNum(summaryId, topic, "B", "C");
        int countBadA2 = getSingleVoteNum(summaryId, topic, "A", "D");
        int countBadB2 = getSingleVoteNum(summaryId, topic, "B", "D");
        int totalSum = totalA + totalB;
        dtoA.setGoodNum2(countGoodA2);
        dtoA.setGoodRate2(calRate(countGoodA2, totalA));
        dtoB.setGoodNum2(countGoodB2);
        dtoB.setGoodRate2(calRate(countGoodB2, totalB));
        dtoSum.setGoodNum2(countGoodA2 + countGoodB2);
        dtoSum.setGoodRate2(calRate(countGoodA2 + countGoodB2, totalSum));

        dtoA.setBetterNum2(countBetterA2);
        dtoA.setBetterRate2(calRate(countBetterA2, totalA));
        dtoB.setBetterNum2(countBetterB2);
        dtoB.setBetterRate2(calRate(countBetterB2, totalB));
        dtoSum.setBetterNum2(countBetterA2 + countBetterB2);
        dtoSum.setBetterRate2(calRate(countBetterA2 + countBetterB2, totalSum));

        dtoA.setGeneralNum2(countGeneralA2);
        dtoA.setGeneralRate2(calRate(countGeneralA2, totalA));
        dtoB.setGeneralNum2(countGeneralB2);
        dtoB.setGeneralRate2(calRate(countGeneralB2, totalB));
        dtoSum.setGeneralNum2(countGeneralA2 + countGeneralB2);
        dtoSum.setGeneralRate2(calRate(countGeneralA2 + countGeneralB2, totalSum));

        dtoA.setBadNum2(countBadA2);
        dtoA.setBadRate2(calRate(countBadA2, totalA));
        dtoB.setBadNum2(countBadB2);
        dtoB.setBadRate2(calRate(countBadB2, totalB));
        dtoSum.setBadNum2(countBadA2 + countBadB2);
        dtoSum.setBadRate2(calRate(countBadA2 + countBadB2, totalSum));
    }

    /**
     * 单选题赋值(第3题)
     *
     * @param dtoA
     * @param dtoB
     * @param summaryId
     * @param topic
     */
    private void setSingleValue3(ReportLeaderWorkDetailsDTO dtoA, ReportLeaderWorkDetailsDTO dtoB, ReportLeaderWorkDetailsDTO dtoSum, String summaryId, String topic, int totalA, int totalB) {
        int countGoodA3 = getSingleVoteNum(summaryId, topic, "A", "A");
        int countGoodB3 = getSingleVoteNum(summaryId, topic, "B", "A");
        int countBetterA3 = getSingleVoteNum(summaryId, topic, "A", "B");
        int countBetterB3 = getSingleVoteNum(summaryId, topic, "B", "B");
        int countGeneralA3 = getSingleVoteNum(summaryId, topic, "A", "C");
        int countGeneralB3 = getSingleVoteNum(summaryId, topic, "B", "C");
        int countBadA3 = getSingleVoteNum(summaryId, topic, "A", "D");
        int countBadB3 = getSingleVoteNum(summaryId, topic, "B", "D");
        int totalSum = totalA + totalB;
        dtoA.setGoodNum3(countGoodA3);
        dtoA.setGoodRate3(calRate(countGoodA3, totalA));
        dtoB.setGoodNum3(countGoodB3);
        dtoB.setGoodRate3(calRate(countGoodB3, totalB));
        dtoSum.setGoodNum3(countGoodA3 + countGoodB3);
        dtoSum.setGoodRate3(calRate(countGoodA3 + countGoodB3, totalSum));

        dtoA.setBetterNum3(countBetterA3);
        dtoA.setBetterRate3(calRate(countBetterA3, totalA));
        dtoB.setBetterNum3(countBetterB3);
        dtoB.setBetterRate3(calRate(countBetterB3, totalB));
        dtoSum.setBetterNum3(countBetterA3 + countBetterB3);
        dtoSum.setBetterRate3(calRate(countBetterA3 + countBetterB3, totalSum));

        dtoA.setGeneralNum3(countGeneralA3);
        dtoA.setGeneralRate3(calRate(countGeneralA3, totalA));
        dtoB.setGeneralNum3(countGeneralB3);
        dtoB.setGeneralRate3(calRate(countGeneralB3, totalB));
        dtoSum.setGeneralNum3(countGeneralA3 + countGeneralB3);
        dtoSum.setGeneralRate3(calRate(countGeneralA3 + countGeneralB3, totalSum));

        dtoA.setBadNum3(countBadA3);
        dtoA.setBadRate3(calRate(countBadA3, totalA));
        dtoB.setBadNum3(countBadB3);
        dtoB.setBadRate3(calRate(countBadB3, totalB));
        dtoSum.setBadNum3(countBadA3 + countBadB3);
        dtoSum.setBadRate3(calRate(countBadA3 + countBadB3, totalSum));
    }

    /**
     * 获取单选（好，较好，一般，差）的A、B票人数
     *
     * @param summaryId
     * @param topic
     * @param voteType
     * @return
     */
    private int getSingleVoteNum(String summaryId, String topic, String voteType, String fill) {
        LambdaQueryWrapper<AssessReportEvaluationItem> lambdaQueryWrapperSum = new LambdaQueryWrapper<>();
        lambdaQueryWrapperSum.eq(AssessReportEvaluationItem::getSummaryId, summaryId).eq(AssessReportEvaluationItem::getVoteType, voteType).eq(AssessReportEvaluationItem::getFill, fill).like(AssessReportEvaluationItem::getTopic, topic);
        int total = (int) assessReportEvaluationItemService.count(lambdaQueryWrapperSum);
        return total;
    }

    /**
     * 获取多选题子项
     *
     * @param configItemId
     * @return
     */
    private List<AssessReportMultiItem> getMutiItemConfigTopic(String configItemId) {
        LambdaQueryWrapper<AssessReportMultiItem> lambdaQueryWrapperSum = new LambdaQueryWrapper<>();
        lambdaQueryWrapperSum.select(AssessReportMultiItem::getId, AssessReportMultiItem::getItemId, AssessReportMultiItem::getTopic);
        lambdaQueryWrapperSum.eq(AssessReportMultiItem::getItemId, configItemId).orderByAsc(AssessReportMultiItem::getId);
        List<AssessReportMultiItem> result = assessReportMultiItemMapper.selectList(lambdaQueryWrapperSum);
        return result;
    }


    /**
     * 获取多选题单个子项测评结果
     *
     * @param summaryId
     * @param topic
     * @return
     */
    private List<AssessReportDemocracyMulti> getMutiVoteData(String summaryId, String topic) {
        LambdaQueryWrapper<AssessReportDemocracyMulti> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(AssessReportDemocracyMulti::getId, AssessReportDemocracyMulti::getSummaryId, AssessReportDemocracyMulti::getTopic, AssessReportDemocracyMulti::getItemId, AssessReportDemocracyMulti::getSelected);
        lambdaQueryWrapper.eq(AssessReportDemocracyMulti::getSummaryId, summaryId).eq(AssessReportDemocracyMulti::getSelected, true).like(AssessReportDemocracyMulti::getTopic, topic);
        List<AssessReportDemocracyMulti> result = assessReportDemocracyMultiMapper.selectList(lambdaQueryWrapper);
        return result;
    }

    /**
     * 从投票结果中区分AB票数量
     *
     * @param list
     * @return
     */
    private JSONObject getMutiVoteTypeCount(List<AssessReportDemocracyMulti> list, Map<String, String> map) {
        JSONObject jsonObject = new JSONObject();
        int countA = 0;
        int countB = 0;
        for (AssessReportDemocracyMulti assessReportDemocracyMulti :
                list) {
            String itemId = assessReportDemocracyMulti.getItemId();
            if (map.containsKey(itemId)) {
                String voteType = map.get(itemId);
                if ("A".equals(voteType)) {
                    countA++;
                } else if ("B".equals(voteType)) {
                    countB++;
                }
            }
        }
        jsonObject.put("A", countA);
        jsonObject.put("B", countB);
        return jsonObject;
    }

    /**
     * 获取ItemId和VoteType的键值对关系，用于辅助判断A/B票选择数量
     *
     * @param summaryId
     * @return
     */
    private Map<String, String> getMap(String summaryId) {
        Map<String, String> map = new HashMap<>();
        LambdaQueryWrapper<AssessReportEvaluationItem> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(AssessReportEvaluationItem::getId, AssessReportEvaluationItem::getSummaryId, AssessReportEvaluationItem::getVoteType);
        lambdaQueryWrapper.eq(AssessReportEvaluationItem::getSummaryId, summaryId);
        List<AssessReportEvaluationItem> result = assessReportEvaluationItemService.list(lambdaQueryWrapper);
        for (AssessReportEvaluationItem item :
                result) {
            map.put(item.getId(), item.getVoteType());
        }
        return map;
    }

    /**
     * 多选题子项赋值
     *
     * @param dtoA
     * @param dtoB
     * @param jsonObject
     */
    private void setMutiValue(ReportLeaderWorkDetailsDTO dtoA, ReportLeaderWorkDetailsDTO dtoB, ReportLeaderWorkDetailsDTO dtoSum, JSONObject jsonObject, int index, int totalA, int totalB) {
        int countA = (int) jsonObject.get("A");
        int countB = (int) jsonObject.get("B");
        double rateA = calRate(countA, totalA);
        double rateB = calRate(countB, totalB);
        int countAB = countA + countB;
        int totalSum = totalA + totalB;
        if (index == 1) {
            dtoA.setNum4(countA);
            dtoB.setNum4(countB);
            dtoA.setRate4(rateA);
            dtoB.setRate4(rateB);
            dtoSum.setNum4(countAB);
            dtoSum.setRate4(calRate(countAB, totalSum));
        } else if (index == 2) {
            dtoA.setNum5(countA);
            dtoB.setNum5(countB);
            dtoA.setRate5(rateA);
            dtoB.setRate5(rateB);
            dtoSum.setNum5(countAB);
            dtoSum.setRate5(calRate(countAB, totalSum));
        } else if (index == 3) {
            dtoA.setNum6(countA);
            dtoB.setNum6(countB);
            dtoA.setRate6(rateA);
            dtoB.setRate6(rateB);
            dtoSum.setNum6(countAB);
            dtoSum.setRate6(calRate(countAB, totalSum));
        } else if (index == 4) {
            dtoA.setNum7(countA);
            dtoB.setNum7(countB);
            dtoA.setRate7(rateA);
            dtoB.setRate7(rateB);
            dtoSum.setNum7(countAB);
            dtoSum.setRate7(calRate(countAB, totalSum));
        } else if (index == 5) {
            dtoA.setNum8(countA);
            dtoB.setNum8(countB);
            dtoA.setRate8(rateA);
            dtoB.setRate8(rateB);
            dtoSum.setNum8(countAB);
            dtoSum.setRate8(calRate(countAB, totalSum));
        } else if (index == 6) {
            dtoA.setNum9(countA);
            dtoB.setNum9(countB);
            dtoA.setRate9(rateA);
            dtoB.setRate9(rateB);
            dtoSum.setNum9(countAB);
            dtoSum.setRate9(calRate(countAB, totalSum));
        } else if (index == 7) {
            dtoA.setNum10(countA);
            dtoB.setNum10(countB);
            dtoA.setRate10(rateA);
            dtoB.setRate10(rateB);
            dtoSum.setNum10(countAB);
            dtoSum.setRate10(calRate(countAB, totalSum));
        } else if (index == 8) {
            dtoA.setNum11(countA);
            dtoB.setNum11(countB);
            dtoA.setRate11(rateA);
            dtoB.setRate11(rateB);
            dtoSum.setNum11(countAB);
            dtoSum.setRate11(calRate(countAB, totalSum));
        } else if (index == 9) {
            dtoA.setNum12(countA);
            dtoB.setNum12(countB);
            dtoA.setRate12(rateA);
            dtoB.setRate12(rateB);
            dtoSum.setNum12(countAB);
            dtoSum.setRate12(calRate(countAB, totalSum));
        } else if (index == 10) {
            dtoA.setNum13(countA);
            dtoB.setNum13(countB);
            dtoA.setRate13(rateA);
            dtoB.setRate13(rateB);
            dtoSum.setNum13(countAB);
            dtoSum.setRate13(calRate(countAB, totalSum));
        } else if (index == 11) {
            dtoA.setNum14(countA);
            dtoB.setNum14(countB);
            dtoA.setRate14(rateA);
            dtoB.setRate14(rateB);
            dtoSum.setNum14(countAB);
            dtoSum.setRate14(calRate(countAB, totalSum));
        } else if (index == 12) {
            dtoA.setNum15(countA);
            dtoB.setNum15(countB);
            dtoA.setRate15(rateA);
            dtoB.setRate15(rateB);
            dtoSum.setNum15(countAB);
            dtoSum.setRate15(calRate(countAB, totalSum));
        }
    }

    /**
     * 计算比例
     *
     * @param count
     * @param total
     * @return
     */
    private double calRate(int count, int total) {
        double v = (double) count / total;
        String formattedNumber = String.format("%.4f", v);
        double dV = Double.valueOf(formattedNumber);
        return dV;
    }

    /**
     * 获取建议
     *
     * @param summaryId
     * @param topic
     * @param voteType
     * @return
     */
    private String getSuggest(String summaryId, String topic, String voteType) {
        String result = "";
        int index = 0;
        List<String> excludeList = new ArrayList<>(Arrays.asList("无", "无。", "没有", "没有。", "无意见", "无意见。", "无问题", "无问题。", "没问题", "无建议"));
        LambdaQueryWrapper<AssessReportEvaluationItem> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(AssessReportEvaluationItem::getId, AssessReportEvaluationItem::getTopic, AssessReportEvaluationItem::getVoteType, AssessReportEvaluationItem::getSummaryId, AssessReportEvaluationItem::getFill, AssessReportEvaluationItem::getInputType);
        lambdaQueryWrapper.eq(AssessReportEvaluationItem::getSummaryId, summaryId).eq(AssessReportEvaluationItem::getVoteType, voteType).eq(AssessReportEvaluationItem::getInputType, "3").isNotNull(AssessReportEvaluationItem::getFill).like(AssessReportEvaluationItem::getTopic, topic);
        List<AssessReportEvaluationItem> list = assessReportEvaluationItemService.list(lambdaQueryWrapper);
        for (AssessReportEvaluationItem assessReportEvaluationItem :
                list) {
            String str = assessReportEvaluationItem.getFill().trim();
            if (StringUtils.isNotEmpty(str)) {
                if (!excludeList.contains(str)) {
                    index++;
                    result += index + "、" + str + "\n";
                }
            }
        }
        return result;
    }

    @Override
    public List<ReportDepartLeaderDataDTO> getLeaderDataByDepart(String year, String depart) {
        String departName = departCommonApi.queryDepartById(depart).getDepartName();
        List<ReportDepartLeaderDataDTO> result = new LinkedList<>();
        LambdaQueryWrapper<AssessReportFill> lqw = new LambdaQueryWrapper<>();
        lqw.select(AssessReportFill::getId, AssessReportFill::getCurrentYear, AssessReportFill::getDepart);
        lqw.eq(AssessReportFill::getCurrentYear, year).eq(AssessReportFill::getDepart, depart);
        List<AssessReportFill> list = assessReportFillService.list(lqw);
        if (list.size() == 0) {
            return result;
        }
        AssessReportFill assessReportFill = list.get(0);
        String fillId = assessReportFill.getId();

        LambdaQueryWrapper<AssessReportNewLeader> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(AssessReportNewLeader::getId, AssessReportNewLeader::getFillId, AssessReportNewLeader::getName);
        lambdaQueryWrapper.eq(AssessReportNewLeader::getCurrentYear, year).eq(AssessReportNewLeader::getFillId, fillId);
        List<AssessReportNewLeader> leaders = assessReportNewLeaderService.list(lambdaQueryWrapper);
        Map<AssessReportNewLeader, List<ReportDepartLeaderDataDTO>> map = new HashMap<>();
        for (AssessReportNewLeader assessReportNewLeader :
                leaders) {
            String id = assessReportNewLeader.getId();
            List<AssessReportEvaluationNewleader> evaluationNewleaders = getEvaluationNewleaderData(id);
            List<ReportDepartLeaderDataDTO> data = calEvaluation(evaluationNewleaders, departName, year, assessReportNewLeader.getName());
            result.addAll(data);
            map.put(assessReportNewLeader, data);
        }
        ReportDepartLeaderDataDTO sumDTO = getLeaderSum(map);
        result.add(sumDTO);
        return result;
    }

    private List<AssessReportEvaluationNewleader> getEvaluationNewleaderData(String newLeaderId) {
        LambdaQueryWrapper<AssessReportEvaluationNewleader> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(AssessReportEvaluationNewleader::getId, AssessReportEvaluationNewleader::getNewLeaderItem, AssessReportEvaluationNewleader::getFill, AssessReportEvaluationNewleader::getVoteType, AssessReportEvaluationNewleader::getSummaryId);
        lambdaQueryWrapper.eq(AssessReportEvaluationNewleader::getNewLeaderItem, newLeaderId);
        List<AssessReportEvaluationNewleader> leaders = newLeaderMapper.selectList(lambdaQueryWrapper);
        return leaders;
    }

    private List<ReportDepartLeaderDataDTO> calEvaluation(List<AssessReportEvaluationNewleader> leaders, String departName, String year, String person) {
        List<ReportDepartLeaderDataDTO> list = new LinkedList<>();
        int numA = 0;
        int numB = 0;
        int agreeNumA = 0;//A票认同人数
        int basicAgreeNumA = 0;//A票基本认同人数
        int disagreeNumA = 0;//A票不认同人数
        int notUnderstandNumA = 0;//A票不了解人数
        int agreeNumB = 0;//B票认同人数
        int basicAgreeNumB = 0;//B票基本认同人数
        int disagreeNumB = 0;//B票不认同人数
        int notUnderstandNumB = 0;//B票不了解人数
        for (AssessReportEvaluationNewleader assessReportEvaluationNewleader :
                leaders) {
            String fill = assessReportEvaluationNewleader.getFill();
            String voteType = assessReportEvaluationNewleader.getVoteType();
            if ("A".equals(voteType)) {
                numA++;
                if ("A".equals(fill)) {
                    agreeNumA++;
                } else if ("B".equals(fill)) {
                    basicAgreeNumA++;
                } else if ("C".equals(fill)) {
                    disagreeNumA++;
                } else if ("D".equals(fill)) {
                    notUnderstandNumA++;
                }
            } else if ("B".equals(voteType)) {
                numB++;
                if ("A".equals(fill)) {
                    agreeNumB++;
                } else if ("B".equals(fill)) {
                    basicAgreeNumB++;
                } else if ("C".equals(fill)) {
                    disagreeNumB++;
                } else if ("D".equals(fill)) {
                    notUnderstandNumB++;
                }
            }

        }
        int total = numA + numB;
        ReportDepartLeaderDataDTO dtoA = new ReportDepartLeaderDataDTO();
        dtoA.setDepart(departName);
        dtoA.setYear(year);
        dtoA.setPerson(person);
        dtoA.setVoteType("A");
        dtoA.setTotal(numA);
        dtoA.setAgreeNum(agreeNumA);
        dtoA.setAgreeRate(calRate(agreeNumA, numA));
        dtoA.setBasicAgreeNum(basicAgreeNumA);
        dtoA.setBasicAgreeRate(calRate(basicAgreeNumA, numA));
        dtoA.setDisagreeNum(disagreeNumA);
        dtoA.setDisagreeRate(calRate(disagreeNumA, numA));
        dtoA.setNotUnderstandNum(notUnderstandNumA);
        dtoA.setNotUnderstandRate(calRate(notUnderstandNumA, numA));
        list.add(dtoA);

        ReportDepartLeaderDataDTO dtoB = new ReportDepartLeaderDataDTO();
        dtoB.setDepart(departName);
        dtoB.setYear(year);
        dtoB.setPerson(person);
        dtoB.setVoteType("B");
        dtoB.setTotal(numB);
        dtoB.setAgreeNum(agreeNumB);
        dtoB.setAgreeRate(calRate(agreeNumB, numB));
        dtoB.setBasicAgreeNum(basicAgreeNumB);
        dtoB.setBasicAgreeRate(calRate(basicAgreeNumB, numB));
        dtoB.setDisagreeNum(disagreeNumB);
        dtoB.setDisagreeRate(calRate(disagreeNumB, numB));
        dtoB.setNotUnderstandNum(notUnderstandNumB);
        dtoB.setNotUnderstandRate(calRate(notUnderstandNumB, numB));
        list.add(dtoB);

        ReportDepartLeaderDataDTO dtosubTotal = new ReportDepartLeaderDataDTO();
        dtosubTotal.setDepart(departName);
        dtosubTotal.setYear(year);
        dtosubTotal.setPerson(person);
        dtosubTotal.setVoteType("小计");
        dtosubTotal.setTotal(total);
        dtosubTotal.setAgreeNum(agreeNumA + agreeNumB);
        dtosubTotal.setAgreeRate(calRate(agreeNumA + agreeNumB, total));
        dtosubTotal.setBasicAgreeNum(basicAgreeNumA + basicAgreeNumB);
        dtosubTotal.setBasicAgreeRate(calRate(basicAgreeNumA + basicAgreeNumB, total));
        dtosubTotal.setDisagreeNum(disagreeNumA + disagreeNumB);
        dtosubTotal.setDisagreeRate(calRate(disagreeNumA + disagreeNumB, total));
        dtosubTotal.setNotUnderstandNum(notUnderstandNumA + notUnderstandNumB);
        dtosubTotal.setNotUnderstandRate(calRate(notUnderstandNumA + notUnderstandNumB, total));
        list.add(dtosubTotal);
        return list;
    }

    private ReportDepartLeaderDataDTO getLeaderSum(Map<AssessReportNewLeader, List<ReportDepartLeaderDataDTO>> map) {
        int sum = 0;
        int agreeNum = 0;//认同人数
        int basicAgreeNum = 0;//基本认同人数
        int disagreeNum = 0;//不认同人数
        int notUnderstandNum = 0;//不了解人数
        ReportDepartLeaderDataDTO dtoSum = new ReportDepartLeaderDataDTO();
        if (map.keySet().size() == 0) {
            return dtoSum;
        }
        AssessReportNewLeader leader = map.keySet().iterator().next();
        List<ReportDepartLeaderDataDTO> list = map.get(leader);
        if (list.size() == 0) {
            return dtoSum;
        }
        ReportDepartLeaderDataDTO dto = list.get(0);
        dtoSum.setDepart(dto.getDepart());
        dtoSum.setYear(dto.getYear());
        dtoSum.setPerson(String.valueOf(map.keySet().size()));
        dtoSum.setVoteType("合计");
        for (AssessReportNewLeader assessReportNewLeader :
                map.keySet()) {
            List<ReportDepartLeaderDataDTO> items = map.get(assessReportNewLeader);
            for (ReportDepartLeaderDataDTO reportDepartLeaderDataDTO :
                    items) {
                if ("小计".equals(reportDepartLeaderDataDTO.getVoteType())) {
                    sum += reportDepartLeaderDataDTO.getTotal();
                    agreeNum += reportDepartLeaderDataDTO.getAgreeNum();
                    basicAgreeNum += reportDepartLeaderDataDTO.getBasicAgreeNum();
                    disagreeNum += reportDepartLeaderDataDTO.getDisagreeNum();
                    notUnderstandNum += reportDepartLeaderDataDTO.getNotUnderstandNum();
                }
            }
        }
        dtoSum.setTotal(sum);
        dtoSum.setAgreeNum(agreeNum);
        dtoSum.setAgreeRate(calRate(agreeNum, sum));
        dtoSum.setBasicAgreeNum(basicAgreeNum);
        dtoSum.setBasicAgreeRate(calRate(basicAgreeNum, sum));
        dtoSum.setDisagreeNum(disagreeNum);
        dtoSum.setDisagreeRate(calRate(disagreeNum, sum));
        dtoSum.setNotUnderstandNum(notUnderstandNum);
        dtoSum.setNotUnderstandRate(calRate(notUnderstandNum, sum));
        return dtoSum;

    }

}
