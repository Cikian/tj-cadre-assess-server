package org.jeecg.modules.annual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.system.vo.SysDepartModel;
import org.jeecg.modules.annual.service.IAssessAnnualSummaryService;
import org.jeecg.modules.annual.vo.*;
import org.jeecg.modules.common.enums.AnnualLevel;
import org.jeecg.modules.common.vo.AccountabilityTableVo;
import org.jeecg.modules.common.vo.NegativeTableVo;
import org.jeecg.modules.common.vo.WordTemResVo;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.regular.entity.AssessRegularReportItem;
import org.jeecg.modules.regular.mapper.AssessRegularReportItemMapper;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.dto.RegularGradeDTO;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.*;
import org.jeecg.modules.sys.entity.business.AssessBusinessGrade;
import org.jeecg.modules.sys.entity.business.AssessLeaderDepartConfig;
import org.jeecg.modules.sys.mapper.annual.*;
import org.jeecg.modules.sys.mapper.business.AssessBusinessGradeMapper;
import org.jeecg.modules.sys.mapper.business.AssessLeaderDepartConfigMapper;
import org.jeecg.modules.system.mapper.SysCategoryMapper;
import org.jeecg.modules.user.UserCommonApi;
import org.jeecg.modules.utils.CommonUtils;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 年度考核成绩汇总
 * @Author: jeecg-boot
 * @Date: 2024-09-10
 * @Version: V1.0
 */
@Slf4j
@Service
public class AssessAnnualSummaryServiceImpl extends ServiceImpl<AssessAnnualSummaryMapper, AssessAnnualSummary> implements IAssessAnnualSummaryService {
    @Autowired
    private AssessAnnualSummaryMapper summaryMapper;
    @Autowired
    private AssessAnnualVacationMapper vacationMapper;
    @Autowired
    private AssessAnnualAccountabilityMapper accountabilityMapper;
    @Autowired
    private AssessAnnualFillMapper fillMapper;
    @Autowired
    private AssessBusinessGradeMapper businessGradeMapper;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private UserCommonApi userCommonApi;
    @Autowired
    private DepartCommonApi departCommonApi;
    @Autowired
    private AssessLeaderDepartConfigMapper leaderDepartConfigMapper;
    @Autowired
    private AssessAnnualRecommendTempMapper tempMapper;
    @Autowired
    private AssessRegularReportItemMapper regularItemMapper;
    @Autowired
    private SysCategoryMapper sysCategoryMapper;
    @Autowired
    private AssessDemocraticEvaluationSummaryMapper evaluationSummaryMapper;
    @Autowired
    private AssessAnnualRecommendTempMapper recommendTempMapper;
    @Autowired
    private AssessAnnualNegativeListMapper negativeListMapper;
    @Autowired
    private AssessAssistConfigMapper assistConfigMapper;
    @Autowired
    private AssessLeaderConfigMapper leaderConfigMapper;
    @Autowired
    private AssessLeaderRecMapper leaderRecMapper;
    @Autowired
    private AssessLeaderRecItemMapper leaderRecItemMapper;
    @Autowired
    private AssessAnnualExcellentRatioMapper excellentRatioMapper;
    @Autowired
    private AssessAnnualArrangeMapper arrangeMapper;

    private static final Logger logger = LoggerFactory.getLogger(AssessAnnualSummaryServiceImpl.class);

    @Override
    public String getAssessLastYear() {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
        return currentAssessInfo.getCurrentYear();
    }

//    @Override
//    public List<AssessAnnualSummary> getEvaluationSuggestion(List<AssessAnnualSummary> list) {
//        long start = System.currentTimeMillis();
//
//        if (list.isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        List<String> userIds = list.stream().map(AssessAnnualSummary::getPerson).collect(Collectors.toList());
//        List<String> departIds = list.stream().map(AssessAnnualSummary::getDepart).collect(Collectors.toList());
//
//        List<String> canNotExcellence = new ArrayList<>();
//
//        // todo：书记的党建考核结果不是优，不可以评优。
//
//        // 获取业务考核成绩
//        if (!departIds.isEmpty()) {
//            List<AssessBusinessGrade> businessGradeList = businessGradeMapper.queryBusinessGradeByDepartIds(departIds, list.get(0).getCurrentYear());
//            for (AssessBusinessGrade businessGrade : businessGradeList) {
//                if (businessGrade.getLevel().equals("3")) {
//                    for (AssessAnnualSummary summary : list) {
//                        if (summary.getDepart().equals(businessGrade.getDepartId())) {
//                            String personnel = summary.getPerson();
//                            canNotExcellence.add(personnel);
//                            userIds.remove(personnel);
//                        }
//                    }
//                }
//            }
//        }
//
//
//        // 获取问责
//        if (!userIds.isEmpty()) {
//            List<AssessAnnualAccountability> assessAccountability = accountabilityMapper.getAssessAccountability(userIds);
//            // 判断是否可以评优
//            for (AssessAnnualAccountability accountability : assessAccountability) {
//                // TODO: 条件需核对！！！有问责，开始时间为本年度，不可以评优
//                Date startDate = accountability.getStartDate();
//                String startYear = startDate.toString().substring(0, 4);
//                if (startYear.equals(list.get(0).getCurrentYear())) {
//                    // 判断accountability中type是否以B03A01开头
//                    if (accountability.getType().startsWith("B03A01")) {
//                        // 获取type后两位
//                        String type = accountability.getType().substring(accountability.getType().length() - 2);
//                        // 转换为int
//                        int typeInt = 0;
//                        try {
//                            typeInt = Integer.parseInt(type);
//                        } catch (NumberFormatException e) {
//                            e.printStackTrace();
//                        }
//
//                        // 判断是否大于等于06
//                        if (typeInt >= 6) {
//                            // 不可以评优
//                            String personnel = accountability.getPersonnel();
//                            canNotExcellence.add(personnel);
//                            userIds.remove(personnel);
//                        }
//                    }
//                }
//
//            }
//        }
//
//        if (!userIds.isEmpty()) {
//            // 获取休假情况
//            List<AssessAnnualVacation> assessVacation = vacationMapper.getAssessVacation(userIds, list.get(0).getCurrentYear());
//            for (AssessAnnualVacation vacation : assessVacation) {
//                if (vacation.getType() == 3 || vacation.getType() == 4) {
//                    Date endDate = vacation.getEndDate();
//                    Date startDate = vacation.getStartDate();
//                    long diff = endDate.getTime() - startDate.getTime();
//                    long days = diff / (1000 * 60 * 60 * 24);
//                    if (days > 180) {
//                        // 事假或病假超过半年，不可以评优
//                        String personnel = vacation.getName();
//                        canNotExcellence.add(personnel);
//                        userIds.remove(personnel);
//                    }
//                }
//            }
//        }
//
//        // 获取平时成绩
//        if (!userIds.isEmpty()) {
//            List<RegularGradeDTO> regularGradeDTOS = fillMapper.queryRegularGradeByUserIds(userIds, list.get(0).getCurrentYear());
//            for (RegularGradeDTO regularGradeDTO : regularGradeDTOS) {
//                if ("A".equals(regularGradeDTO.getQuarter1())
//                        || "A".equals(regularGradeDTO.getQuarter2())
//                        || "A".equals(regularGradeDTO.getQuarter3())
//                        || "A".equals(regularGradeDTO.getQuarter4())) {
//                    // 有一次为好，有资格评优
//                    for (AssessAnnualSummary summary : list) {
//                        if (summary.getPerson().equals(regularGradeDTO.getId())) {
//                            summary.setEvaluation("2");
//                        }
//                    }
//                }
//            }
//        }
//
//        // 如果id在不可评优列表中，设置为3
//        for (AssessAnnualSummary summary : list) {
//            if (canNotExcellence.contains(summary.getPerson())) {
//                summary.setEvaluation("3");
//            }
//        }
//
//        long end = System.currentTimeMillis();
//
//        return list;
//    }


    @Override
    public void leaderRecommend(String summaryId) {
        LambdaUpdateWrapper<AssessAnnualSummary> luw = new LambdaUpdateWrapper<>();
        luw.eq(AssessAnnualSummary::getId, summaryId);
        luw.set(AssessAnnualSummary::getLeaderRecommend, 1);
        summaryMapper.update(null, luw);
    }

    @Override
    public List<AssessAnnualSummary> recommendList(String year) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        // todo:不同领导的推荐列表不同
        LambdaQueryWrapper<AssessLeaderDepartConfig> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessLeaderDepartConfig::getLeaderId, sysUser.getId());
        AssessLeaderDepartConfig config = leaderDepartConfigMapper.selectOne(lqw);
        String departIdsStr = config.getDepartId();
        List<String> departIds = Arrays.asList(departIdsStr.split(","));

        LambdaQueryWrapper<AssessAnnualSummary> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(AssessAnnualSummary::getCurrentYear, year);
        lqw2.in(AssessAnnualSummary::getDepart, departIds);

        return summaryMapper.selectList(lqw2);
    }

    @Override
    public List<AssessAnnualSummary> queryPersonByFillId(String fillId) {
        LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualSummary::getFillId, fillId);
        lqw.ne(AssessAnnualSummary::getType, "group");
        return summaryMapper.selectList(lqw);
    }

    @Override
    public void recommend(RecommendVO recommendVO) {
        String year = recommendVO.getYear();
        String leader = recommendVO.getLeader();
        List<String> groupList = recommendVO.getGroupList();
        List<String> bureauList = recommendVO.getBureauList();
        List<String> basicList = recommendVO.getBasicList();
        List<String> institutionList = recommendVO.getInstitutionList();

        String groupIdsStr = null;
        String bureauIdsStr = null;
        String basicIdsStr = null;
        String institutionIdsStr = null;

        List<String> totalIds = new ArrayList<>();

        // 拼接字符串
        if (groupList != null && !groupList.isEmpty()) {
            groupIdsStr = String.join(",", groupList);
            totalIds.addAll(groupList);
        } else {
            groupIdsStr = "";
        }
        if (bureauList != null && !bureauList.isEmpty()) {
            bureauIdsStr = String.join(",", bureauList);
            totalIds.addAll(bureauList);
        } else {
            bureauIdsStr = "";
        }
        if (basicList != null && !basicList.isEmpty()) {
            basicIdsStr = String.join(",", basicList);
            totalIds.addAll(basicList);
        } else {
            basicIdsStr = "";
        }
        if (institutionList != null && !institutionList.isEmpty()) {
            institutionIdsStr = String.join(",", institutionList);
            totalIds.addAll(institutionList);
        } else {
            institutionIdsStr = "";
        }

        LambdaQueryWrapper<AssessAnnualRecommendTemp> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualRecommendTemp::getCurrentYear, year);
        lqw.eq(AssessAnnualRecommendTemp::getLeader, leader);
        AssessAnnualRecommendTemp temp = tempMapper.selectOne(lqw);

        LambdaQueryWrapper<AssessLeaderRec> qw = new LambdaQueryWrapper<>();
        qw.eq(AssessLeaderRec::getLeader, leader);
        qw.eq(AssessLeaderRec::getCurrentYear, year);
        AssessLeaderRec leaderRec = leaderRecMapper.selectOne(qw);
        if (leaderRec != null) {
            if ("3".equals(leaderRec.getStatus())) {
                throw new JeecgBootException("已经推优，不可重复推优");
            }
            leaderRec.setStatus("3");
            leaderRec.setBureauRec(bureauIdsStr);
            leaderRec.setGroupRec(groupIdsStr);
            leaderRec.setBasicRec(basicIdsStr);
            leaderRec.setInstitutionRec(institutionIdsStr);
            leaderRecMapper.updateById(leaderRec);

            LambdaUpdateWrapper<AssessLeaderRecItem> luw = new LambdaUpdateWrapper<>();
            luw.eq(AssessLeaderRecItem::getMainId, leaderRec.getId());
            luw.in(AssessLeaderRecItem::getHashId, totalIds);
            // luw.in(AssessLeaderRecItem::getHashId, totalIds).or(n -> n.in(AssessLeaderRecItem::getName, totalIds));

            luw.set(AssessLeaderRecItem::getLeaderRecommend, true);
            leaderRecItemMapper.update(null, luw);

            luw.clear();
            luw.eq(AssessLeaderRecItem::getMainId, leaderRec.getId());
            luw.in(AssessLeaderRecItem::getName, totalIds);
            luw.set(AssessLeaderRecItem::getLeaderRecommend, true);
            leaderRecItemMapper.update(null, luw);


        }

        if (temp != null) {
            temp.setCurrentYear(year);
            temp.setLeader(leader);
            temp.setGroupRec(groupIdsStr);
            temp.setBureauRec(bureauIdsStr);
            temp.setBasicRec(basicIdsStr);
            temp.setInstitutionRec(institutionIdsStr);
            temp.setPass(true);
            tempMapper.updateById(temp);
        } else {
            AssessAnnualRecommendTemp save = new AssessAnnualRecommendTemp();
            save.setCurrentYear(year);
            save.setLeader(leader);
            save.setGroupRec(groupIdsStr);
            save.setBureauRec(bureauIdsStr);
            save.setBasicRec(basicIdsStr);
            save.setInstitutionRec(institutionIdsStr);
            save.setPass(true);
            tempMapper.insert(save);
        }

        LambdaQueryWrapper<AssessAnnualSummary> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(AssessAnnualSummary::getCurrentYear, year);
        // lqw2.in(AssessAnnualSummary::getHashId, totalIds).or().in(AssessAnnualSummary::getDepart, totalIds);
        lqw2.in(AssessAnnualSummary::getHashId, totalIds);

        List<AssessAnnualSummary> list = summaryMapper.selectList(lqw2);
        for (AssessAnnualSummary summary : list) {
            summary.setLevel("1");
            summary.setLeaderRecommend(true);
        }

        lqw2.clear();
        lqw2.eq(AssessAnnualSummary::getCurrentYear, year);
        lqw2.in(AssessAnnualSummary::getDepart, totalIds);
        lqw2.eq(AssessAnnualSummary::getHashId, "group");
        List<AssessAnnualSummary> groupSummary = summaryMapper.selectList(lqw2);
        if (groupSummary != null && !groupSummary.isEmpty()) {
            for (AssessAnnualSummary summary : groupSummary) {
                summary.setLevel("1");
                summary.setLeaderRecommend(true);
            }
        }

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        AssessAnnualSummaryMapper summaryMapperNew = sqlSession.getMapper(AssessAnnualSummaryMapper.class);

        if (!list.isEmpty()) list.forEach(summaryMapperNew::updateById);
        if (groupSummary != null && !groupSummary.isEmpty()) groupSummary.forEach(summaryMapperNew::updateById);

        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();
    }

    @Override
    public void submitMerit(String year, List<String> idList) {
        LambdaUpdateWrapper<AssessAnnualSummary> luw = new LambdaUpdateWrapper<>();
        luw.eq(AssessAnnualSummary::getCurrentYear, year);
        luw.set(AssessAnnualSummary::getThirdClass, null);
        summaryMapper.update(null, luw);
        luw.clear();
        luw.eq(AssessAnnualSummary::getCurrentYear, year);
        luw.in(AssessAnnualSummary::getId, idList);
        luw.set(AssessAnnualSummary::getThirdClass, "Y");
        summaryMapper.update(null, luw);
    }

    @Override
    public void submitPartyAssess(String year, List<String> idList) {
        LambdaUpdateWrapper<AssessAnnualSummary> luw = new LambdaUpdateWrapper<>();
        luw.eq(AssessAnnualSummary::getCurrentYear, year);
        luw.set(AssessAnnualSummary::getPartyAssess, null);
        summaryMapper.update(null, luw);

        luw.clear();

        if (idList.isEmpty()) {
            return;
        }
        luw.eq(AssessAnnualSummary::getCurrentYear, year);
        luw.in(AssessAnnualSummary::getId, idList);
        luw.set(AssessAnnualSummary::getPartyAssess, "Y");

        summaryMapper.update(null, luw);
    }

    @Override
    public void submitInspection(String year, List<String> idList) {
        LambdaUpdateWrapper<AssessAnnualSummary> luw = new LambdaUpdateWrapper<>();
        luw.eq(AssessAnnualSummary::getCurrentYear, year);
        luw.set(AssessAnnualSummary::getDisciplineRecommend, "");
        luw.set(AssessAnnualSummary::getLeaderRecommend, 0);
        luw.set(AssessAnnualSummary::getLevel, "2");
        summaryMapper.update(null, luw);
        luw.clear();

        if (idList.isEmpty()) {
            return;
        }

        luw.eq(AssessAnnualSummary::getCurrentYear, year);
        luw.in(AssessAnnualSummary::getId, idList);
        luw.set(AssessAnnualSummary::getDisciplineRecommend, "Y");
        luw.set(AssessAnnualSummary::getLeaderRecommend, 1);
        luw.set(AssessAnnualSummary::getLevel, "1");
        summaryMapper.update(null, luw);
    }

    @Override
    public AssessAnnualSummary getGroupSummary(String fillId) {
        LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualSummary::getFillId, fillId);
        lqw.eq(AssessAnnualSummary::getRecommendType, "group");
        AssessAnnualSummary summary = summaryMapper.selectOne(lqw);
        return summary;
    }

    @Override
    public List<AssessAnnualSummary> getMerit(String depart) {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
        LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
        lqw.ne(AssessAnnualSummary::getRecommendType, "group");
        lqw.ne(AssessAnnualSummary::getRecommendType, "institution");
        lqw.eq(AssessAnnualSummary::getCurrentYear, currentAssessInfo.getCurrentYear());
        if (!"0".equals(depart)) lqw.eq(AssessAnnualSummary::getDepart, depart);
        return summaryMapper.selectList(lqw);
    }

    @Override
    public List<AssessAnnualSummary> getPartyOrganization() {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
        LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualSummary::getSecretary, true);
        lqw.eq(AssessAnnualSummary::getCurrentYear, currentAssessInfo.getCurrentYear());
        return summaryMapper.selectList(lqw);
    }

    @Override
    public List<AssessLeaderRecItem> getLeaderRecommendList(String year) {

        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        String leaderId = sysUser.getId();

        LambdaQueryWrapper<AssessLeaderRec> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessLeaderRec::getLeader, leaderId);
        lqw.eq(AssessLeaderRec::getCurrentYear, year);
        AssessLeaderRec leaderRecs = leaderRecMapper.selectOne(lqw);

        if (leaderRecs == null) {
            throw new JeecgBootException("当前年度考核未到推优节点，详情请联系管理员！");
        }

        if ("1".equals(leaderRecs.getStatus()) || "5".equals(leaderRecs.getStatus()) || "6".equals(leaderRecs.getStatus())) {
            throw new JeecgBootException("当前年度考核未到推优节点，详情请联系管理员！");
        }

        String mainId = leaderRecs.getId();

        LambdaQueryWrapper<AssessLeaderRecItem> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(AssessLeaderRecItem::getMainId, mainId);
        List<AssessLeaderRecItem> items = leaderRecItemMapper.selectList(lqw2);

        // 对item排序，通过personOrder，升序，如果personOrder为空，排在后面
        // items.sort(Comparator.comparing(AssessLeaderRecItem::getPersonOrder, Comparator.nullsLast(Comparator.naturalOrder())));
        items.sort(Comparator.comparing(AssessLeaderRecItem::getPersonOrder, Comparator.nullsLast(Comparator.comparingInt(Integer::parseInt))));

        // 将正职兼二巡、正职、副职、其他分开
        List<AssessLeaderRecItem> items1 = new ArrayList<>(); // 总师二巡
        List<AssessLeaderRecItem> items2 = new ArrayList<>(); // 正职
        List<AssessLeaderRecItem> items3 = new ArrayList<>(); // 副职
        List<AssessLeaderRecItem> items4 = new ArrayList<>(); // 其他
        for (AssessLeaderRecItem item : items) {
            if ("正职兼总师、二巡".equals(item.getType())) item.setType("正职");

            if ("局管总师二巡".equals(item.getDepartName())) {
                items1.add(item);
                continue;
            }

            switch (item.getType()) {
                case "正职":
                    items2.add(item);
                    break;
                case "副职":
                    items3.add(item);
                    break;
                default:
                    items4.add(item);
                    break;
            }
        }

        items.clear();
        // 将正职兼二巡、正职、副职、其他合并， 顺序为正职兼总师、二巡、正职、副职、其他
        items.addAll(items1);
        items.addAll(items2);
        items.addAll(items3);
        items.addAll(items4);
        return items;
    }

    @Override
    public List<AssessLeaderRecItem> getLeaderRecommendListByMainId(String mainId) {

        LambdaQueryWrapper<AssessLeaderRecItem> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(AssessLeaderRecItem::getMainId, mainId);
        List<AssessLeaderRecItem> items = leaderRecItemMapper.selectList(lqw2);

        // 对item排序，通过personOrder，升序，如果personOrder为空，排在后面
        // items.sort(Comparator.comparing(AssessLeaderRecItem::getPersonOrder, Comparator.nullsLast(Comparator.naturalOrder())));
        items.sort(Comparator.comparing(AssessLeaderRecItem::getPersonOrder, Comparator.nullsLast(Comparator.comparingInt(Integer::parseInt))));

        // 将正职兼二巡、正职、副职、其他分开
        List<AssessLeaderRecItem> items1 = new ArrayList<>(); // 总师二巡
        List<AssessLeaderRecItem> items2 = new ArrayList<>(); // 正职
        List<AssessLeaderRecItem> items3 = new ArrayList<>(); // 副职
        List<AssessLeaderRecItem> items4 = new ArrayList<>(); // 其他
        for (AssessLeaderRecItem item : items) {
            if ("正职兼总师、二巡".equals(item.getType())) item.setType("正职");

            if ("局管总师二巡".equals(item.getDepartName())) {
                items1.add(item);
                continue;
            }

            switch (item.getType()) {
                case "正职":
                    items2.add(item);
                    break;
                case "副职":
                    items3.add(item);
                    break;
                default:
                    items4.add(item);
                    break;
            }
        }

        items.clear();
        // 将正职兼二巡、正职、副职、其他合并， 顺序为正职兼总师、二巡、正职、副职、其他
        items.addAll(items1);
        items.addAll(items2);
        items.addAll(items3);
        items.addAll(items4);
        return items;
    }

    // @Override
    // public List<LeaderRecommendListVO> getLeaderRecommendList(String year, String leader) {
    //
    //     LambdaQueryWrapper<AssessLeaderDepartConfig> lqw = new LambdaQueryWrapper<>();
    //     lqw.eq(AssessLeaderDepartConfig::getLeaderId, leader);
    //     AssessLeaderDepartConfig assessLeaderDepartConfig = leaderDepartConfigMapper.selectOne(lqw);
    //     // todo：如果局领导配置中不存在记录会空指针
    //     String departIds = assessLeaderDepartConfig.getDepartId();
    //     List<String> departIdList = Arrays.asList(departIds.split(","));
    //
    //     List<String> specialPeople = new ArrayList<>();
    //
    //     LambdaQueryWrapper<AssessAssistConfig> configQw1 = new LambdaQueryWrapper<>();
    //     configQw1.like(AssessAssistConfig::getLeader, leader);
    //     List<AssessAssistConfig> assistConfig = assistConfigMapper.selectList(configQw1);
    //     for (AssessAssistConfig config : assistConfig) {
    //         if (!specialPeople.contains(config.getHashId())) {
    //             specialPeople.add(config.getHashId());
    //         }
    //     }
    //
    //     LambdaQueryWrapper<AssessLeaderConfig> configQw2 = new LambdaQueryWrapper<>();
    //     configQw2.eq(AssessLeaderConfig::getLeader, leader);
    //     List<AssessLeaderConfig> leaderConfig = leaderConfigMapper.selectList(null);
    //     for (AssessLeaderConfig config : leaderConfig) {
    //         if (!specialPeople.contains(config.getHashId())) {
    //             specialPeople.add(config.getHashId());
    //         }
    //     }
    //
    //     LambdaQueryWrapper<AssessAnnualSummary> lqw2 = new LambdaQueryWrapper<>();
    //     lqw2.eq(AssessAnnualSummary::getCurrentYear, year);
    //     lqw2.in(AssessAnnualSummary::getDepart, departIdList).and(i -> i.notIn(AssessAnnualSummary::getHashId, specialPeople));
    //     // lqw2.notIn(AssessAnnualSummary::getHashId, specialPeople);
    //     List<AssessAnnualSummary> summaries = summaryMapper.selectList(lqw2);
    //
    //     lqw2.clear();
    //
    //     leaderConfig = leaderConfig.stream().filter(config -> !config.getLeader().equals(leader)).collect(Collectors.toList());
    //     // 从specialPeople中移除过滤后的人
    //     for (AssessLeaderConfig config : leaderConfig) {
    //         specialPeople.remove(config.getHashId());
    //     }
    //
    //     lqw2.eq(AssessAnnualSummary::getCurrentYear, year);
    //     lqw2.in(AssessAnnualSummary::getHashId, specialPeople);
    //     List<AssessAnnualSummary> specialSummaries = summaryMapper.selectList(lqw2);
    //     summaries.addAll(specialSummaries);
    //
    //     LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw3 = new LambdaQueryWrapper<>();
    //     lqw3.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year);
    //     LambdaQueryWrapper<AssessAnnualAccountability> lqw5 = new LambdaQueryWrapper<>();
    //     lqw5.eq(AssessAnnualAccountability::getCurrentYear, year);
    //     lqw5.eq(AssessAnnualAccountability::getStatus, 3);
    //     LambdaQueryWrapper<AssessAnnualNegativeList> lqw4 = new LambdaQueryWrapper<>();
    //     lqw4.eq(AssessAnnualNegativeList::getCurrentYear, year);
    //     lqw4.eq(AssessAnnualNegativeList::getStatus, 3);
    //
    //     List<AssessAnnualNegativeList> allNegativeLists = negativeListMapper.selectList(lqw4);
    //     List<AssessAnnualAccountability> allAccountability2NList = accountabilityMapper.selectList(lqw5);
    //     List<AssessDemocraticEvaluationSummary> evaluationSummaries = evaluationSummaryMapper.selectList(lqw3);
    //     List<SysDepartModel> sysDepartModels = departCommonApi.queryAllDepart();
    //
    //
    //     List<LeaderRecommendListVO> list = new ArrayList<>();
    //     for (AssessAnnualSummary summary : summaries) {
    //         LeaderRecommendListVO vo = new LeaderRecommendListVO();
    //         vo.setHashId(summary.getHashId());
    //         // 设置部门
    //         sysDepartModels.stream()
    //                 .filter(depart -> depart.getId().equals(summary.getDepart()))
    //                 .findFirst()
    //                 .ifPresent(depart -> vo.setDepart(depart.getAlias()));
    //
    //         vo.setName(summary.getPerson());  // 设置姓名
    //         vo.setDuties(summary.getDuties());// 设置职务
    //         vo.setPost(summary.getPosition());// 设置职级
    //         vo.setType(summary.getType().equals("chief") ? "正职" : summary.getType().equals("deputy") ? "副职" : "其他");// 设置类别
    //
    //         // 设置民主测评考核单元内排名
    //         if (!"group".equals(summary.getRecommendType())) {
    //             AssessDemocraticEvaluationSummary s = evaluationSummaries.stream()
    //                     .filter(e -> summary.getHashId().equals(e.getAppraisee()))
    //                     .findFirst()
    //                     .orElse(null);
    //
    //             if (s != null) {
    //                 vo.setDemocraticRanking(s.getScopeRanking());
    //             }
    //         }
    //
    //         switch (summary.getEvaluation()) {
    //             case "1":
    //                 vo.setSuggestion("建议评优");
    //                 vo.setCanExcellent(true);
    //                 break;
    //             case "2":
    //                 vo.setSuggestion("可以评优");
    //                 vo.setCanExcellent(true);
    //                 break;
    //             case "3":
    //                 vo.setSuggestion("不可以评优");
    //                 vo.setCanExcellent(false);
    //                 break;
    //         }
    //
    //         // 设置不能评优情况汇总
    //         vo.setRemark(summary.getRemark());
    //         // 设置参考信息
    //         List<AssessAnnualNegativeList> negativeLists;
    //         List<AssessAnnualAccountability> aList = new ArrayList<>();
    //         if (summary.getRecommendType().equals("group")) {
    //             negativeLists = allNegativeLists.stream().filter(n -> summary.getDepart().equals(n.getDepart())).collect(Collectors.toList());
    //         } else {
    //             negativeLists = allNegativeLists.stream().filter(n -> summary.getHashId().equals(n.getHashId())).collect(Collectors.toList());
    //             aList = allAccountability2NList.stream().filter(n -> summary.getHashId().equals(n.getHashId())).collect(Collectors.toList());
    //         }
    //
    //         String content = "";
    //         if (!negativeLists.isEmpty()) {
    //             content = negativeLists.stream().map(AssessAnnualNegativeList::getContent).collect(Collectors.joining("；"));
    //         }
    //         if (!aList.isEmpty()) {
    //             String content2 = aList.stream().map(AssessAnnualAccountability::getRemark).collect(Collectors.joining("；"));
    //             if (content.isEmpty()) content = content2;
    //             else content = content + "；" + content2;
    //         }
    //         vo.setNegativeList(content);
    //         // 设置是否纪检组推荐
    //         vo.setDisciplineRecommend("Y".equals(summary.getDisciplineRecommend()));
    //         // 设置推优类型
    //         vo.setRecommendType(summary.getRecommendType());
    //         Boolean leaderRecommend = summary.getLeaderRecommend();
    //         vo.setLeaderRecommend(leaderRecommend);
    //         // 设置连续两年评为优秀
    //         vo.setTwoYear("Y".equals(summary.getThirdClass()) ? "是" : "否");
    //         // 设置优秀副职
    //         vo.setExcellentDeputy("Y".equals(summary.getExcellentDeputy()) ? "单位推荐优秀副职" : "");
    //         list.add(vo);
    //     }
    //     return list;
    // }
    @Override
    public List<AssessLeaderRecItem> getLeaderRecommendList(String year, String leader) {

        LambdaQueryWrapper<AssessLeaderRec> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessLeaderRec::getLeader, leader);
        lqw.eq(AssessLeaderRec::getCurrentYear, year);
        AssessLeaderRec assessLeaderRec = leaderRecMapper.selectOne(lqw);

        LambdaQueryWrapper<AssessLeaderRecItem> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(AssessLeaderRecItem::getMainId, assessLeaderRec.getId());
        List<AssessLeaderRecItem> items = leaderRecItemMapper.selectList(lqw2);
        // 通过personOrder字段排序,该字段为字符串类型,升序排序,如果该字段为null,排在后面
        items.sort(Comparator.comparing(AssessLeaderRecItem::getPersonOrder, Comparator.nullsLast(Comparator.comparingInt(Integer::parseInt))));
        return items;
    }

    @Override
    public List<RecommendTempVO> getRecommendTemp(String year) {
        if (year == null || "0".equals(year)) {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
            year = currentAssessInfo.getCurrentYear();
        }

        LambdaQueryWrapper<AssessAnnualRecommendTemp> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualRecommendTemp::getCurrentYear, year);
        lqw.eq(AssessAnnualRecommendTemp::isPass, true);

        List<AssessAnnualRecommendTemp> list = recommendTempMapper.selectList(lqw);

        List<RecommendTempVO> res = new ArrayList<>();

        for (AssessAnnualRecommendTemp item : list) {
            String bureauRec = item.getBureauRec();
            String basicRec = item.getBasicRec();
            String institutionRec = item.getInstitutionRec();
            List<String> bureauList = new ArrayList<>();
            List<String> basicList = new ArrayList<>();
            List<String> institutionList = new ArrayList<>();

            if (bureauRec != null && !bureauRec.isEmpty()) {
                bureauList = Arrays.asList(bureauRec.split(","));
            }
            if (basicRec != null && !basicRec.isEmpty()) {
                basicList = Arrays.asList(basicRec.split(","));
            }
            if (institutionRec != null && !institutionRec.isEmpty()) {
                institutionList = Arrays.asList(institutionRec.split(","));
            }

            StringBuilder bureauSrt = new StringBuilder();
            StringBuilder basicSrt = new StringBuilder();
            StringBuilder institutionSrt = new StringBuilder();

            for (String hashId : bureauList) {
                String name = CommonUtils.getNameByHashId(hashId);
                if (bureauSrt.length() < 1) {
                    bureauSrt.append(name);
                } else {
                    bureauSrt.append(",").append(name);
                }
            }

            for (String hashId : basicList) {
                String name = CommonUtils.getNameByHashId(hashId);
                if (basicSrt.length() < 1) {
                    basicSrt.append(name);
                } else {
                    basicSrt.append(",").append(name);
                }
            }

            for (String hashId : institutionList) {
                String name = CommonUtils.getNameByHashId(hashId);
                if (institutionSrt.length() < 1) {
                    institutionSrt.append(name);
                } else {
                    institutionSrt.append(",").append(name);
                }
            }

            RecommendTempVO vo = new RecommendTempVO();
            vo.setId(item.getId());
            vo.setLeader(item.getLeader());
            vo.setBureau(bureauSrt.toString());
            vo.setBasic(basicSrt.toString());
            vo.setInstitution(institutionSrt.toString());
            vo.setGroup(item.getGroupRec());
            res.add(vo);
        }

        return res;
    }

    @Override
    public void returnLeaderRecommend(String leaderId) {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
        String year = currentAssessInfo.getCurrentYear();

        LambdaQueryWrapper<AssessAnnualRecommendTemp> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualRecommendTemp::getLeader, leaderId);
        lqw.eq(AssessAnnualRecommendTemp::getCurrentYear, year);

        List<AssessAnnualRecommendTemp> temps = tempMapper.selectList(lqw);
        AssessAnnualRecommendTemp temp = temps.get(0);

        String bureauRec = temp.getBureauRec();
        List<String> l1 = Arrays.asList(bureauRec.split(","));
        String basicRec = temp.getBasicRec();
        List<String> l2 = Arrays.asList(basicRec.split(","));
        String institutionRec = temp.getInstitutionRec();
        List<String> l3 = Arrays.asList(institutionRec.split(","));

        List<String> personTotal = new ArrayList<>();
        personTotal.addAll(l1);
        personTotal.addAll(l2);
        personTotal.addAll(l3);

        // 从personTotal中删除以“(纪检组推优)”开头的元素
        personTotal.removeIf(hashId -> hashId.startsWith("(纪检组推优)"));

        lqw.clear();

        lqw.eq(AssessAnnualRecommendTemp::getCurrentYear, year);
        lqw.ne(AssessAnnualRecommendTemp::getLeader, leaderId);

        List<AssessAnnualRecommendTemp> temp2 = tempMapper.selectList(lqw);
        List<String> otherLeaderRec = new ArrayList<>();
        if (!temp2.isEmpty()) {
            for (AssessAnnualRecommendTemp item : temp2) {
                otherLeaderRec.addAll(Arrays.asList(item.getBureauRec().split(",")));
                otherLeaderRec.addAll(Arrays.asList(item.getBasicRec().split(",")));
                otherLeaderRec.addAll(Arrays.asList(item.getInstitutionRec().split(",")));
            }
        }

        // 判断personTotal中的元素是否在otherLeaderRec中出现，如果有，从personTotal中删除
        personTotal.removeAll(otherLeaderRec);

        LambdaUpdateWrapper<AssessAnnualSummary> luw = new LambdaUpdateWrapper<>();
        luw.eq(AssessAnnualSummary::getCurrentYear, year);
        luw.in(AssessAnnualSummary::getHashId, personTotal);
        luw.set(AssessAnnualSummary::getLeaderRecommend, false);
        luw.set(AssessAnnualSummary::getLevel, "2");
        summaryMapper.update(null, luw);

        String groupRec = temp.getGroupRec();
        List<String> l4 = Arrays.asList(groupRec.split(","));
        luw.clear();
        luw.eq(AssessAnnualSummary::getCurrentYear, year);
        luw.in(AssessAnnualSummary::getDepart, l4);
        luw.eq(AssessAnnualSummary::getRecommendType, "group");
        luw.set(AssessAnnualSummary::getLeaderRecommend, false);
        luw.set(AssessAnnualSummary::getLevel, "2");
        summaryMapper.update(null, luw);

        tempMapper.deleteById(temp.getId());

        LambdaQueryWrapper<AssessLeaderRec> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(AssessLeaderRec::getCurrentYear, year);
        lqw2.eq(AssessLeaderRec::getLeader, leaderId);
        AssessLeaderRec leaderRec = leaderRecMapper.selectOne(lqw2);

        LambdaUpdateWrapper<AssessLeaderRec> luw3 = new LambdaUpdateWrapper<>();
        luw3.eq(AssessLeaderRec::getCurrentYear, year);
        luw3.eq(AssessLeaderRec::getLeader, leaderId);
        luw3.set(AssessLeaderRec::getBureauRec, null);
        luw3.set(AssessLeaderRec::getGroupRec, null);
        luw3.set(AssessLeaderRec::getBasicRec, null);
        luw3.set(AssessLeaderRec::getInstitutionRec, null);
        luw3.set(AssessLeaderRec::getStatus, "2");

        leaderRecMapper.update(null, luw3);

        LambdaUpdateWrapper<AssessLeaderRecItem> luw4 = new LambdaUpdateWrapper<>();
        luw4.eq(AssessLeaderRecItem::getMainId, leaderRec.getId());
        luw4.set(AssessLeaderRecItem::getLeaderRecommend, false);
        leaderRecItemMapper.update(null, luw4);
    }

    @Override
    public List<AssessAnnualRecommendTemp> getRecommendTempExl(String year) {
        if (year == null || "0".equals(year)) {
            AssessCurrentAssess annual = assessCommonApi.getCurrentAssessInfo("annual");
            year = annual.getCurrentYear();
        }
        LambdaQueryWrapper<AssessAnnualRecommendTemp> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualRecommendTemp::getCurrentYear, year);

        return tempMapper.selectList(lqw);
    }

    @Override
    public List<InspectionVO> getInspection(String year) {
        LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualSummary::getCurrentYear, year);
        lqw.eq(AssessAnnualSummary::getInspectionWork, true);
        List<AssessAnnualSummary> list = summaryMapper.selectList(lqw);
        if (list != null && !list.isEmpty()) {
            List<String> hashIds = list.stream().map(AssessAnnualSummary::getHashId).collect(Collectors.toList());

            LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw2 = new LambdaQueryWrapper<>();
            lqw2.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year);
            lqw2.in(AssessDemocraticEvaluationSummary::getAppraisee, hashIds);
            List<AssessDemocraticEvaluationSummary> evaluationSummaries = evaluationSummaryMapper.selectList(lqw2);
            // 装换为hashId为key，evaluationSummaries为value的map
            Map<String, AssessDemocraticEvaluationSummary> evaluationMap = evaluationSummaries.stream().collect(Collectors.toMap(AssessDemocraticEvaluationSummary::getAppraisee, e -> e));

            List<InspectionVO> res = new ArrayList<>();
            for (AssessAnnualSummary summary : list) {
                AssessDemocraticEvaluationSummary s = evaluationMap.get(summary.getHashId());


                InspectionVO vo = new InspectionVO();
                vo.setId(summary.getId());
                vo.setDepart(summary.getDepart());
                vo.setPerson(summary.getPerson());
                vo.setScore(s == null || s.getScore() == null ? new BigDecimal(0) : s.getScore());
                vo.setScopeRanking(s == null || s.getScopeRanking() == null ? 0 : s.getScopeRanking());
                vo.setRanking(s == null || s.getRanking() == null ? 0 : s.getRanking());
                vo.setInspection("Y".equals(summary.getDisciplineRecommend()));
                vo.setHashId(summary.getHashId());
                res.add(vo);
            }
            return res;
        }

        return Collections.emptyList();
    }

    @Override
    public List<AnnualGroupSummaryVO> getGroupSummaryInfo(List<AssessAnnualSummary> records, String year) {

        if (records == null || records.isEmpty()) {
            return null;
        }
        List<AnnualGroupSummaryVO> res = new ArrayList<>();

        LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessDemocraticEvaluationSummary::getType, "99");
        lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year);
        List<AssessDemocraticEvaluationSummary> evaluationSummaries = evaluationSummaryMapper.selectList(lqw);
        Map<String, AssessDemocraticEvaluationSummary> evaluationMap = evaluationSummaries.stream().collect(Collectors.toMap(AssessDemocraticEvaluationSummary::getDepart, e -> e));

        LambdaQueryWrapper<AssessAnnualFill> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(AssessAnnualFill::getCurrentYear, year);
        List<AssessAnnualFill> fills = fillMapper.selectList(lqw2);
        Map<String, AssessAnnualFill> fillMap = fills.stream().collect(Collectors.toMap(AssessAnnualFill::getId, e -> e));

        for (AssessAnnualSummary record : records) {
            AnnualGroupSummaryVO vo = new AnnualGroupSummaryVO();
            BeanUtils.copyProperties(record, vo);

            // 获取民主测评信息
            AssessDemocraticEvaluationSummary evaluationSummary = evaluationMap.get(record.getDepart());
            if (evaluationSummary != null) {
                vo.setRanking(evaluationSummary.getRanking());
            }

            // 获取年度工作总结
            AssessAnnualFill fill = fillMap.get(record.getFillId());
            if (fill != null) {
                vo.setDutyReport(fill.getDuty());
            }

            // 获取负面清单
            LambdaQueryWrapper<AssessAnnualNegativeList> lqw3 = new LambdaQueryWrapper<>();
            lqw3.eq(AssessAnnualNegativeList::getCurrentYear, year);
            lqw3.eq(AssessAnnualNegativeList::getType, "1");
            lqw3.eq(AssessAnnualNegativeList::getDepart, record.getDepart());
            List<AssessAnnualNegativeList> negativeLists = negativeListMapper.selectList(lqw3);
            // 将negativeLists转换为字符串，以逗号分隔
            String negativeList = negativeLists.stream().map(AssessAnnualNegativeList::getContent).collect(Collectors.joining("；"));
            vo.setList(negativeList);

            res.add(vo);
        }
        return res;
    }

    @Override
    public List<AssessAnnualSummary> queryByIds(List<String> ids, String year) {
        LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
        lqw.in(AssessAnnualSummary::getId, ids);
        lqw.eq(AssessAnnualSummary::getCurrentYear, year);
        return summaryMapper.selectList(lqw);
    }

    @Override
    public List<AssessAnnualSummary> getNotExcellent(LambdaQueryWrapper<AssessAnnualSummary> lqw) {
        return summaryMapper.selectList(lqw);
    }

    @Override
    public List<AssessAnnualSummary> getAssessResByType(String year, String type) {

        LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualSummary::getCurrentYear, year);
        lqw.eq(AssessAnnualSummary::getRecommendType, type);
        List<AssessAnnualSummary> list = summaryMapper.selectList(lqw);
        if (!type.equals("group")) {
            list.sort(Comparator.comparing(AssessAnnualSummary::getPersonOrder, Comparator.nullsLast(Comparator.comparingInt(Integer::parseInt))));
        }

        List<SysDepartModel> allSysDepart = departCommonApi.getAllSysDepart();
        // id未k，name为v转换为map
        Map<String, String> departMap = allSysDepart.stream().collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getDepartName));

        int i = 1;
        for (AssessAnnualSummary s : list) {
            s.setId(String.valueOf(i));
            s.setDepart(departMap.get(s.getDepart()));
            if (!"group".equals(type)) {
                s.setType("chiefPro".equals(s.getType()) ? "正职兼二巡" : "chief".equals(s.getType()) ? "正职" : "deputy".equals(s.getType()) ? "副职" : "其他");
                s.setSex("1".equals(s.getSex()) ? "男" : "女");
                s.setThirdClass("Y".equals(s.getThirdClass()) ? "是" : "");
                s.setPartyAssess("Y".equals(s.getPartyAssess()) ? "优秀" : "");
                s.setDisciplineRecommend("Y".equals(s.getDisciplineRecommend()) ? "优秀" : "");
                s.setExcellentDeputy("Y".equals(s.getExcellentDeputy()) ? "是" : "");
            }
            s.setLevel(AnnualLevel.getByTypeAndLevel(s.getRecommendType(), s.getLevel()).getDisplayName());
            i++;
        }


        return list;
    }


    /**
     * @param year
     * @param newList
     * @param type
     * @return
     * @Author: Cikian
     * @Date: 2024/12/31 18:09
     * @Description: 获取新修改
     * type：party：党组织书记考核，dis：纪检组推优
     */
    @Override
    public List<String> getDistancePartyAssessIdList(String year, List<String> newList, String type) {
        LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualSummary::getCurrentYear, year);
        if ("party".equals(type)) lqw.eq(AssessAnnualSummary::getPartyAssess, "Y");
        else if ("dis".equals(type)) lqw.eq(AssessAnnualSummary::getDisciplineRecommend, "Y");
        List<AssessAnnualSummary> list = summaryMapper.selectList(lqw);
        List<String> oldList = list.stream().map(AssessAnnualSummary::getId).collect(Collectors.toList());

        List<String> idList = new ArrayList<>();
        List<String> newListDev = new ArrayList<>(newList);
        List<String> oldListDev = new ArrayList<>(oldList);

        for (String id : newListDev) {
            if (!oldList.contains(id)) {
                idList.add(id);
            }
        }

        for (String id : oldListDev) {
            if (!newList.contains(id)) {
                idList.add(id);
            }
        }

        return idList;
    }

    @Override
    public List<String> notExcellent(String year) {
        List<SysDepartModel> basicDeparts = departCommonApi.queryDepartByType(new String[]{"2", "3", "4"});
        List<String> departIds = basicDeparts.stream().map(SysDepartModel::getId).collect(Collectors.toList());
        // id到alias的映射
        Map<String, String> departMap = basicDeparts.stream().collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getAlias));

        LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualSummary::getCurrentYear, year);
        lqw.eq(AssessAnnualSummary::getLevel, "1");
        lqw.in(AssessAnnualSummary::getDepart, departIds);
        List<AssessAnnualSummary> list = summaryMapper.selectList(lqw);
        // 转为List<String>
        List<String> eDepartIds = list.stream().map(AssessAnnualSummary::getDepart).collect(Collectors.toList());
        // 获取不是优秀的部门
        List<String> notExcellentDepartIds = departIds.stream().filter(id -> !eDepartIds.contains(id)).collect(Collectors.toList());
        // 获取不是优秀的部门的别名
        return notExcellentDepartIds.stream().map(departMap::get).collect(Collectors.toList());


    }

    @Override
    public Map<String, Object> assertMap() {
        Map<String, Object> params = new HashMap<>();

        LambdaQueryWrapper<AssessAnnualExcellentRatio> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(AssessAnnualExcellentRatio::getEnable, true);
        List<AssessAnnualExcellentRatio> list2 = excellentRatioMapper.selectList(lqw2);
        if (!list2.isEmpty()) {
            AssessAnnualExcellentRatio ratio = list2.get(0);
            BigDecimal bureauRatio = ratio.getBureauRatio();
            BigDecimal departRatio = ratio.getBasicRatio();
            BigDecimal groupRatio = ratio.getGroupRatio();

            // 分别乘以100，转换为int
            params.put("rate1", bureauRatio.multiply(new BigDecimal(100)).intValue());
            params.put("rate3", departRatio.multiply(new BigDecimal(100)).intValue());
            params.put("rate2", groupRatio.multiply(new BigDecimal(100)).intValue());

        }

        List<SysDepartModel> sysDepartModels = departCommonApi.queryAllDepart();
        // 将id作为k，name作为V，转为map
        Map<String, String> departMap = sysDepartModels.stream().collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getDepartName));
        LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
        AssessCurrentAssess annual = assessCommonApi.getCurrentAssessInfo("annual");
        if (annual == null) {
            throw new JeecgBootException("当前没有年度考核");
        }
        lqw.eq(AssessAnnualSummary::getCurrentYear, annual.getCurrentYear());

        lqw.eq(AssessAnnualSummary::getLeaderRecommend, true);
        List<AssessAnnualSummary> lrList = summaryMapper.selectList(lqw);
        // 排序，通过元素personOrder字段升序排序，注意，personOrder字段为String类型数字，有些元素的personOrder字段还可能为null，为null的排在最后
        lrList.sort(Comparator.comparing(
                AssessAnnualSummary::getPersonOrder,
                Comparator.nullsLast(Comparator.comparing(Integer::parseInt))
        ));

        lqw.clear();
        lqw.eq(AssessAnnualSummary::getCurrentYear, annual.getCurrentYear());
        lqw.eq(AssessAnnualSummary::getDisciplineRecommend, "Y");
        List<AssessAnnualSummary> drList = summaryMapper.selectList(lqw);
        drList.sort(Comparator.comparing(
                AssessAnnualSummary::getPersonOrder,
                Comparator.nullsLast(Comparator.comparing(Integer::parseInt))
        ));
        lqw.clear();
        lqw.eq(AssessAnnualSummary::getCurrentYear, annual.getCurrentYear());
        lqw.eq(AssessAnnualSummary::getLevel, "1");
        List<AssessAnnualSummary> totalList = summaryMapper.selectList(lqw);
        totalList.sort(Comparator.comparing(
                AssessAnnualSummary::getPersonOrder,
                Comparator.nullsLast(Comparator.comparing(Integer::parseInt))
        ));

        lqw.clear();
        lqw.eq(AssessAnnualSummary::getCurrentYear, annual.getCurrentYear());
        lqw.eq(AssessAnnualSummary::getRecommendType, "bureau");
        int totalNum = Math.toIntExact(summaryMapper.selectCount(lqw));
        lqw.eq(AssessAnnualSummary::getLevel, "1");
        int exeNum = Math.toIntExact(summaryMapper.selectCount(lqw));

        params.put("totalNum", totalNum);
        params.put("exeNum", exeNum);

        int num1 = 0;
        int num2 = 0;
        List<AssessAnnualSummary> tempList1 = new ArrayList<>();
        List<AssessAnnualSummary> tempList2 = new ArrayList<>();
        for (AssessAnnualSummary s : totalList) {
            if ("bureau".equals(s.getRecommendType())) {
                if ("chief".equals(s.getType()) || "chiefPro".equals(s.getType())) {
                    num1++;
                    tempList1.add(s);
                } else {
                    num2++;
                    tempList2.add(s);
                }

            }
        }
        StringBuilder sb = new StringBuilder();
        String jgjw = "";
        for (AssessAnnualSummary s : tempList1) {
            if (s.getPosition() != null) {
                sb.append(s.getPosition()).append(s.getPerson()).append("、");
            } else {
                sb.append(departMap.get(s.getDepart())).append(s.getPerson()).append("、");
            }
            if (s.getInspectionWork()) {
                jgjw = "经驻局纪检监察组研判，建议时任机关纪委书记" + s.getPerson() + "同志评为优秀等次。";
            }
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        params.put("num1", num1);
        params.put("name1", sb.toString());

        // 清空sb
        sb.setLength(0);

        for (AssessAnnualSummary s : tempList2) {
            if (s.getPosition() != null) {
                sb.append(s.getPosition()).append(s.getPerson()).append("、");
            } else {
                sb.append(departMap.get(s.getDepart())).append(s.getPerson()).append("、");
            }
            if (s.getInspectionWork()) {
                jgjw = "经驻局纪检监察组研判，建议时任机关纪委书记" + s.getPerson() + "同志评为优秀等次。";
            }
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        params.put("jgjw", jgjw);
        params.put("num2", num2);
        params.put("name2", sb.toString());

        lqw.clear();
        lqw.eq(AssessAnnualSummary::getCurrentYear, annual.getCurrentYear());
        lqw.eq(AssessAnnualSummary::getLevel, "2");
        lqw.in(AssessAnnualSummary::getRecommendType, "bureau");
        int commonNum = Math.toIntExact(summaryMapper.selectCount(lqw));
        params.put("commonNum", commonNum);

        lqw.clear();
        lqw.eq(AssessAnnualSummary::getCurrentYear, annual.getCurrentYear());
        lqw.in(AssessAnnualSummary::getRecommendType, "group");
        int totalGroupNum = Math.toIntExact(summaryMapper.selectCount(lqw));
        params.put("totalGroupNum", totalGroupNum);

        lqw.clear();
        lqw.eq(AssessAnnualSummary::getCurrentYear, annual.getCurrentYear());
        lqw.in(AssessAnnualSummary::getRecommendType, "group");
        lqw.eq(AssessAnnualSummary::getLevel, "1");
        int exeGroupNum = Math.toIntExact(summaryMapper.selectCount(lqw));
        params.put("exeGroupNum", exeGroupNum);

        tempList1.clear();

        for (AssessAnnualSummary s : totalList) {
            if (s.getType().equals("group")) {
                tempList1.add(s);
            }
        }

        sb.setLength(0);
        for (AssessAnnualSummary s : tempList1) {
            sb.append(departMap.get(s.getDepart())).append("、");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        params.put("gNum1", tempList1.size());
        params.put("gName1", sb.toString());

        num1 = 0;
        num2 = 0;
        tempList1.clear();
        tempList2.clear();
        List<AssessAnnualSummary> tempList3 = new ArrayList<>();

        for (AssessAnnualSummary s : totalList) {
            if ("group".equals(s.getRecommendType())) {
                continue;
            }
            if ("basic".equals(s.getRecommendType())) {
                if ("Y".equals(s.getDisciplineRecommend())) {
                    tempList3.add(s);
                    continue;
                }
                if ("chief".equals(s.getType()) || "chiefPro".equals(s.getType())) {
                    num1++;
                    tempList1.add(s);
                } else {
                    num2++;
                    tempList2.add(s);
                }

            }
        }
        sb.setLength(0);
        for (AssessAnnualSummary s : tempList1) {
            sb.append(departMap.get(s.getDepart())).append(s.getPerson()).append("、");
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        params.put("num3", tempList1.size() + tempList2.size() + tempList3.size());

        params.put("num4", tempList1.size());
        params.put("name4", sb.toString());

        sb.setLength(0);
        for (AssessAnnualSummary s : tempList2) {
            sb.append(departMap.get(s.getDepart())).append(s.getPerson()).append("、");
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        params.put("num5", tempList2.size());
        params.put("name5", sb.toString());

        sb.setLength(0);
        for (AssessAnnualSummary s : tempList3) {
            sb.append(departMap.get(s.getDepart())).append(s.getPerson()).append("、");
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        params.put("num6", tempList3.size());
        params.put("name6", sb.toString());

        num1 = 0;
        num2 = 0;
        tempList1.clear();
        tempList2.clear();
        tempList3.clear();

        for (AssessAnnualSummary s : totalList) {
            if ("group".equals(s.getRecommendType())) {
                continue;
            }
            if ("institution".equals(s.getRecommendType())) {
                if ("Y".equals(s.getDisciplineRecommend())) {
                    tempList3.add(s);
                    continue;
                }
                if ("chief".equals(s.getType()) || "chiefPro".equals(s.getType())) {
                    num1++;
                    tempList1.add(s);
                } else {
                    num2++;
                    tempList2.add(s);
                }

            }
        }
        sb.setLength(0);
        for (AssessAnnualSummary s : tempList1) {
            sb.append(departMap.get(s.getDepart())).append(s.getPerson()).append("、");
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        params.put("num66", tempList1.size() + tempList2.size() + tempList3.size());

        sb.setLength(0);
        for (AssessAnnualSummary s : tempList1) {
            sb.append(departMap.get(s.getDepart())).append(s.getPerson()).append("、");
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        params.put("num7", tempList1.size());
        params.put("name7", sb.toString());

        sb.setLength(0);
        for (AssessAnnualSummary s : tempList2) {
            sb.append(departMap.get(s.getDepart())).append(s.getPerson()).append("、");
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        params.put("num8", tempList2.size());
        params.put("name8", sb.toString());

        sb.setLength(0);
        for (AssessAnnualSummary s : tempList3) {
            sb.append(departMap.get(s.getDepart())).append(s.getPerson()).append("、");
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        params.put("num9", tempList3.size());
        params.put("name9", sb.toString());

        sb.setLength(0);
        lqw.clear();
        lqw.eq(AssessAnnualSummary::getCurrentYear, annual.getCurrentYear());
        lqw.ne(AssessAnnualSummary::getRecommendType, "group");
        lqw.eq(AssessAnnualSummary::getLevel, "5");
        List<AssessAnnualSummary> noLevel = summaryMapper.selectList(lqw);

        noLevel.sort(Comparator.comparing(
                AssessAnnualSummary::getPersonOrder,
                Comparator.nullsLast(Comparator.comparing(Integer::parseInt))
        ));

        List<AssessAnnualSummary> noLevel1 = new ArrayList<>();
        List<AssessAnnualSummary> noLevel2 = new ArrayList<>();
        for (AssessAnnualSummary s : noLevel) {
            if (s.getRecommendType().equals("bureau")) {
                noLevel1.add(s);
            } else {
                noLevel2.add(s);
            }
        }

        if (!noLevel1.isEmpty()) {
            for (AssessAnnualSummary s : noLevel1) {
                sb.append(departMap.get(s.getDepart())).append(s.getPerson()).append("、");
            }

            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
            }

            params.put("title3", "（3）不确定等次人员");
            params.put("noLevel1", sb.toString());
            sb.setLength(0);
        }

        if (!noLevel2.isEmpty()) {
            for (AssessAnnualSummary s : noLevel2) {
                sb.append(departMap.get(s.getDepart())).append(s.getPerson()).append("、");
            }

            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
            }

            params.put("title4", "四是不确定等次人员" + noLevel2.size() + "人：");
            params.put("noLevel2", sb.toString());
            sb.setLength(0);
        }


        return params;
    }

    @Override
    public Map<String, Object> assertMap2Res() {
        Map<String, Object> params = new HashMap<>();

        List<SysDepartModel> sysDepartModels = departCommonApi.queryAllDepart();
        // 将id作为k，name作为V，转为map
        Map<String, String> departMap = sysDepartModels.stream().collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getDepartName));
        LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
        AssessCurrentAssess annual = assessCommonApi.getCurrentAssessInfo("annual");
        if (annual == null) {
            throw new JeecgBootException("当前没有年度考核");
        }

        // 根据departOrder对sysDepartModels进行升序排序
        sysDepartModels.sort(Comparator.comparingInt(SysDepartModel::getDepartOrder));


        lqw.eq(AssessAnnualSummary::getCurrentYear, annual.getCurrentYear());
        lqw.eq(AssessAnnualSummary::getRecommendType, "group");
        List<AssessAnnualSummary> groupList = summaryMapper.selectList(lqw);

        List<AssessAnnualSummary> level1 = new ArrayList<>();
        List<AssessAnnualSummary> level2 = new ArrayList<>();
        List<AssessAnnualSummary> level3 = new ArrayList<>();
        List<AssessAnnualSummary> level4 = new ArrayList<>();

        StringBuilder sb = new StringBuilder();

        for (AssessAnnualSummary s : groupList) {
            if ("1".equals(s.getLevel())) {
                level1.add(s);
            } else if ("2".equals(s.getLevel())) {
                level2.add(s);
            }
        }

        for (AssessAnnualSummary s : level1) {
            sb.append(departMap.get(s.getDepart())).append("、");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        String temp = sb.toString();
        sb.setLength(0);

        for (SysDepartModel d : sysDepartModels) {
            if (temp.contains(d.getDepartName())) {
                sb.append(d.getDepartName()).append("、");
            }
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        params.put("name1", sb.toString());

        sb.setLength(0);

        for (AssessAnnualSummary s : level2) {
            sb.append(departMap.get(s.getDepart())).append("、");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        temp = "";
        temp = sb.toString();
        sb.setLength(0);

        for (SysDepartModel d : sysDepartModels) {
            if (temp.contains(d.getDepartName())) {
                sb.append(d.getDepartName()).append("、");
            }
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        params.put("name2", sb.toString());

        sb.setLength(0);
        level1.clear();
        level2.clear();

        lqw.clear();
        lqw.eq(AssessAnnualSummary::getCurrentYear, annual.getCurrentYear());
        lqw.ne(AssessAnnualSummary::getRecommendType, "group");
        List<AssessAnnualSummary> totalPeople = summaryMapper.selectList(lqw);
        totalPeople.sort(Comparator.comparing(
                AssessAnnualSummary::getPersonOrder,
                Comparator.nullsLast(Comparator.comparing(Integer::parseInt))
        ));

        List<AssessAnnualSummary> temp1 = new ArrayList<>();
        List<AssessAnnualSummary> temp2 = new ArrayList<>();

        for (AssessAnnualSummary s : totalPeople) {
            if (s.getType().equals("chief") || s.getType().equals("chiefPro")) {
                temp1.add(s);
            } else {
                temp2.add(s);
            }
        }

        totalPeople.clear();
        totalPeople.addAll(temp1);
        totalPeople.addAll(temp2);

        for (AssessAnnualSummary s : totalPeople) {
            if ("1".equals(s.getLevel())) {
                switch (s.getRecommendType()) {
                    case "bureau":
                        level1.add(s);
                        break;
                    case "basic":
                        level2.add(s);
                        break;
                    case "institution":
                        level3.add(s);
                        break;
                }
            }
        }

        for (AssessAnnualSummary s : level1) {
            sb.append(s.getPerson()).append("、");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        params.put("name3", sb.toString());
        sb.setLength(0);

        for (AssessAnnualSummary s : level2) {
            sb.append(s.getPerson()).append("、");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        params.put("name4", sb.toString());
        sb.setLength(0);

        for (AssessAnnualSummary s : level3) {
            sb.append(s.getPerson()).append("、");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        params.put("name5", sb.toString());
        sb.setLength(0);
        level1.clear();
        level2.clear();
        level3.clear();

        for (AssessAnnualSummary s : totalPeople) {
            if ("2".equals(s.getLevel())) {
                switch (s.getRecommendType()) {
                    case "bureau":
                        level1.add(s);
                        break;
                    case "basic":
                        level2.add(s);
                        break;
                    case "institution":
                        level3.add(s);
                        break;
                }
            }
        }

        for (AssessAnnualSummary s : level1) {
            sb.append(s.getPerson()).append("、");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        params.put("name6", sb.toString());
        sb.setLength(0);

        for (AssessAnnualSummary s : level2) {
            sb.append(s.getPerson()).append("、");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        params.put("name7", sb.toString());
        sb.setLength(0);

        for (AssessAnnualSummary s : level3) {
            sb.append(s.getPerson()).append("、");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        params.put("name8", sb.toString());
        sb.setLength(0);
        level1.clear();
        level2.clear();
        level3.clear();

        for (AssessAnnualSummary s : totalPeople) {
            if ("5".equals(s.getLevel())) {
                level1.add(s);
            }
        }

        for (AssessAnnualSummary s : level1) {
            sb.append(s.getPerson()).append("、");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        params.put("name9", sb.toString());

        return params;
    }

    @Override
    public WordTemResVo assertMap2List() {
        // Map<String, Object> params = new HashMap<>();
        WordTemResVo vo = new WordTemResVo();

        List<SysDepartModel> sysDepartModels = departCommonApi.queryAllDepart();
        // 将id作为k，name作为V，转为map
        Map<String, String> departMap = sysDepartModels.stream().collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getDepartName));
        LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
        AssessCurrentAssess annual = assessCommonApi.getCurrentAssessInfo("annual");
        if (annual == null) {
            throw new JeecgBootException("当前没有年度考核");
        }

        // 根据departOrder对sysDepartModels进行升序排序
        sysDepartModels.sort(Comparator.comparingInt(SysDepartModel::getDepartOrder));

        lqw.eq(AssessAnnualSummary::getCurrentYear, annual.getCurrentYear());
        lqw.eq(AssessAnnualSummary::getRecommendType, "group");
        List<AssessAnnualSummary> groupList = summaryMapper.selectList(lqw);

        List<AssessAnnualSummary> level1 = new ArrayList<>();
        List<AssessAnnualSummary> level2 = new ArrayList<>();
        List<AssessAnnualSummary> level3 = new ArrayList<>();
        List<AssessAnnualSummary> level4 = new ArrayList<>();

        StringBuilder sb = new StringBuilder();

        for (AssessAnnualSummary s : groupList) {
            if (s.getRemark().contains("业务")) {
                level1.add(s);
            }
        }

        for (AssessAnnualSummary s : level1) {
            sb.append(departMap.get(s.getDepart())).append("、");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        String temp = sb.toString();
        sb.setLength(0);

        for (SysDepartModel d : sysDepartModels) {
            if (temp.contains(d.getDepartName())) {
                sb.append(d.getDepartName()).append("、");
            }
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        vo.setGroup(sb.toString());

        sb.setLength(0);

        for (AssessAnnualSummary s : level2) {
            sb.append(departMap.get(s.getDepart())).append("、");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        temp = "";
        temp = sb.toString();
        sb.setLength(0);

        for (SysDepartModel d : sysDepartModels) {
            if (temp.contains(d.getDepartName())) {
                sb.append(d.getDepartName()).append("、");
            }
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        // params.put("name2", sb.toString());

        sb.setLength(0);
        level1.clear();
        level2.clear();

        lqw.clear();
        lqw.eq(AssessAnnualSummary::getCurrentYear, annual.getCurrentYear());
        lqw.ne(AssessAnnualSummary::getRecommendType, "group");
        List<AssessAnnualSummary> totalPeople = summaryMapper.selectList(lqw);
        totalPeople.sort(Comparator.comparing(
                AssessAnnualSummary::getPersonOrder,
                Comparator.nullsLast(Comparator.comparing(Integer::parseInt))
        ));

        List<AssessAnnualSummary> temp1 = new ArrayList<>();
        List<AssessAnnualSummary> temp2 = new ArrayList<>();

        for (AssessAnnualSummary s : totalPeople) {
            if (s.getType().equals("chief") || s.getType().equals("chiefPro")) {
                temp1.add(s);
            } else {
                temp2.add(s);
            }
        }

        totalPeople.clear();
        totalPeople.addAll(temp1);
        totalPeople.addAll(temp2);

        for (AssessAnnualSummary s : totalPeople) {
            String remark = s.getRemark();
            if (remark.contains("平时")) {
                level1.add(s);
            }
            if (remark.contains("问责")) {
                level2.add(s);
            }
            if (remark.contains("业务工作测评")) {
                level3.add(s);
            }
            if (remark.contains("休假超过半年")) {
                level4.add(s);
            }

        }

        for (AssessAnnualSummary s : level1) {
            sb.append(departMap.get(s.getDepart())).append(s.getPerson()).append("、");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        // params.put("name1", sb.toString());
        vo.setName1(sb.toString());
        sb.setLength(0);

        List<AccountabilityTableVo> aVoList = new ArrayList<>();
        for (AssessAnnualSummary s : level2) {
            // sb.append(departMap.get(s.getDepart())).append(s.getPerson());
            String person = departMap.get(s.getDepart()) + s.getPerson();
            AccountabilityTableVo aVo = new AccountabilityTableVo();
            aVo.setPerson(person);
            aVo.setRemark(s.getRemark());
            aVoList.add(aVo);

        }

        // params.put("name2", sb.toString());
        vo.setAccountability(aVoList);
        sb.setLength(0);

        for (AssessAnnualSummary s : level3) {
            sb.append(departMap.get(s.getDepart())).append(s.getPerson()).append("、");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        // params.put("name3", sb.toString());
        vo.setName3(sb.toString());
        sb.setLength(0);

        for (AssessAnnualSummary s : level4) {
            sb.append(departMap.get(s.getDepart())).append(s.getPerson()).append("、");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        // params.put("name4", sb.toString());
        vo.setName4(sb.toString());
        sb.setLength(0);
        level1.clear();
        level2.clear();
        level3.clear();
        level4.clear();

        for (AssessAnnualSummary s : totalPeople) {
            if ("Y".equals(s.getDisciplineRecommend())) {
                level1.add(s);
            }
            if (!"Y".equals(s.getDisciplineRecommend()) && s.getInspectionWork()) {
                level2.add(s);
            }
        }

        for (AssessAnnualSummary s : level1) {
            sb.append(departMap.get(s.getDepart())).append(s.getPerson()).append("、");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        // params.put("name5", sb.toString());
        vo.setName5(sb.toString());
        sb.setLength(0);

        for (AssessAnnualSummary s : level2) {
            sb.append(departMap.get(s.getDepart())).append(s.getPerson()).append("、");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        // params.put("name6", sb.toString());
        vo.setName6(sb.toString());
        sb.setLength(0);
        level1.clear();
        level2.clear();

        for (AssessAnnualSummary s : totalPeople) {
            if ("Y".equals(s.getPartyAssess())) {
                level1.add(s);
            }
            if (!"Y".equals(s.getPartyAssess()) && s.getSecretary()) {
                level2.add(s);
            }
        }

        for (AssessAnnualSummary s : level1) {
            sb.append(departMap.get(s.getDepart())).append(s.getPerson()).append("、");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        // params.put("name7", sb.toString());
        vo.setName7(sb.toString());
        sb.setLength(0);

        for (AssessAnnualSummary s : level2) {
            sb.append(departMap.get(s.getDepart())).append(s.getPerson()).append("、");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        // params.put("name8", sb.toString());
        vo.setName8(sb.toString());
        sb.setLength(0);
        level1.clear();
        level2.clear();

        for (AssessAnnualSummary s : totalPeople) {
            if ("Y".equals(s.getThirdClass())) {
                level1.add(s);
            }
        }

        for (AssessAnnualSummary s : level1) {
            sb.append(departMap.get(s.getDepart())).append(s.getPerson()).append("、");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        // params.put("name9", sb.toString());
        vo.setName9(sb.toString());
        sb.setLength(0);
        level1.clear();

        List<NegativeTableVo> nVoList = new ArrayList<>();
        for (AssessAnnualSummary s : totalPeople) {
            if (s.getNegativeList() != null && !s.getNegativeList().isEmpty()) {
                level1.add(s);
                String person = departMap.get(s.getDepart()) + s.getPerson();
                NegativeTableVo nVo = new NegativeTableVo();
                nVo.setPerson(person);
                nVo.setNegativeList(s.getNegativeList());
                nVoList.add(nVo);
            }
        }

        vo.setNegative(nVoList);

        sb.setLength(0);
        level1.clear();
        lqw.clear();


        return vo;
    }

    @Override
    public List<Map<String, String>> getDetailInfo(String year, String departId) {

        if (year == null || year.isEmpty() || year.equals("0")) {
            AssessCurrentAssess annual = assessCommonApi.getCurrentAssessInfo("annual");
            year = annual.getCurrentYear();
        }

        LambdaQueryWrapper<AssessAnnualFill> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualFill::getCurrentYear, year);
        lqw.eq(AssessAnnualFill::getDepart, departId);
        List<AssessAnnualFill> fill = fillMapper.selectList(lqw);
        if (fill.isEmpty()) {
            return Collections.emptyList();
        }

        List<SysDepartModel> allSysDepart = departCommonApi.getAllSysDepart();
        Map<String, String> departMap = allSysDepart.stream().collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getDepartName));


        Map<String, String> map = new HashMap<>();
        map.put("assessName", fill.get(0).getAssessName());
        map.put("year", fill.get(0).getCurrentYear());
        map.put("depart", departMap.get(fill.get(0).getDepart()));
        map.put("fillBy", fill.get(0).getReportBy());

        StringBuilder sb = new StringBuilder();
        String leader = fill.get(0).getLeader();
        // 以,分割，转为List
        List<String> leaderList = Arrays.asList(leader.split(","));
        for (String s : leaderList) {
            String name = CommonUtils.getNameByHashId(s);
            if (sb.length() == 0) {
                sb.append(name);
            } else {
                sb.append("、").append(name);
            }
        }
        map.put("leader", sb.toString());

        return Collections.singletonList(map);
    }

    @Override
    public List<Map<String, String>> getDetailFill(String year, String departId) {

        if (year == null || year.isEmpty() || year.equals("0")) {
            AssessCurrentAssess annual = assessCommonApi.getCurrentAssessInfo("annual");
            year = annual.getCurrentYear();
        }

        LambdaQueryWrapper<AssessAnnualFill> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualFill::getCurrentYear, year);
        lqw.eq(AssessAnnualFill::getDepart, departId);
        List<AssessAnnualFill> fill = fillMapper.selectList(lqw);
        if (fill.isEmpty()) {
            return Collections.emptyList();
        }

        List<AssessAnnualArrange> arranges = arrangeMapper.selectByMainId(fill.get(0).getId());

        Map<String, String> map = new HashMap<>();
        map.put("chiefNum", String.valueOf(arranges.get(0).getChiefNum()));
        map.put("deputyNum", String.valueOf(arranges.get(0).getDeputyNum()));
        map.put("voteC", String.valueOf(arranges.get(0).getVoteC()));
        map.put("people", arranges.get(0).getContacts());
        map.put("office", arranges.get(0).getOfficeTel());
        map.put("phone", arranges.get(0).getContactsPhone());
        map.put("remark", arranges.get(0).getRemark());

        return Collections.singletonList(map);
    }

    @Override
    public List<Map<String, String>> getDetailPerson(String year, String departId) {

        if (year == null || year.isEmpty() || year.equals("0")) {
            AssessCurrentAssess annual = assessCommonApi.getCurrentAssessInfo("annual");
            year = annual.getCurrentYear();
        }

        LambdaQueryWrapper<AssessAnnualFill> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualFill::getCurrentYear, year);
        lqw.eq(AssessAnnualFill::getDepart, departId);
        List<AssessAnnualFill> fill = fillMapper.selectList(lqw);
        if (fill.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<AssessAnnualSummary> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(AssessAnnualSummary::getFillId, fill.get(0).getId());
        lqw2.ne(AssessAnnualSummary::getRecommendType, "group");
        List<AssessAnnualSummary> summary = summaryMapper.selectList(lqw2);
        if (summary.isEmpty()) {
            return Collections.emptyList();
        }

        String leader = fill.get(0).getLeader();
        // 以,分割，转为List
        List<String> leaderList = Arrays.asList(leader.split(","));
        LambdaQueryWrapper<AssessRegularReportItem> lqw3 = new LambdaQueryWrapper<>();
        lqw3.in(AssessRegularReportItem::getHashId, leaderList);
        lqw3.eq(AssessRegularReportItem::getCurrentYear, year);
        List<AssessRegularReportItem> regularReportItems = regularItemMapper.selectList(lqw3);

        List<Map<String, String>> list = new ArrayList<>();
        for (AssessAnnualSummary s : summary) {
            Map<String, String> map = new HashMap<>();
            map.put("name", s.getPerson());
            map.put("type", "chiefPro".equals(s.getType()) ? "正职兼二巡" : "chief".equals(s.getType()) ? "正职" : "deputy".equals(s.getType()) ? "副职" : "其他");
            // 判断s.getHashId()是否在regularReportItems中
            if (regularReportItems.stream().anyMatch(item -> item.getHashId().equals(s.getHashId()))) {
                AssessRegularReportItem item = regularReportItems.stream().filter(i -> i.getHashId().equals(s.getHashId())).findFirst().orElse(null);
                map.put("r1", item.getQuarter1());
                map.put("r2", item.getQuarter2());
                map.put("r3", item.getQuarter3());
                map.put("r4", item.getQuarter4());
            } else {
                map.put("r1", "");
                map.put("r2", "");
                map.put("r3", "");
                map.put("r4", "");
            }
            map.put("exeDeputy", "Y".equals(s.getExcellentDeputy()) ? "是" : "");
            list.add(map);
        }

        return list;
    }

    @Override
    public List<Map<String, String>> getDetailAccount(String year, String departId) {

        if (year == null || year.isEmpty() || year.equals("0")) {
            AssessCurrentAssess annual = assessCommonApi.getCurrentAssessInfo("annual");
            year = annual.getCurrentYear();
        }

        LambdaQueryWrapper<AssessAnnualAccountability> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualAccountability::getCurrentYear, year);
        lqw.eq(AssessAnnualAccountability::getDepart, departId);
        List<AssessAnnualAccountability> account = accountabilityMapper.selectList(lqw);

        List<Map<String, String>> list = new ArrayList<>();

        for (AssessAnnualAccountability s : account) {
            Map<String, String> map = new HashMap<>();
            map.put("name", s.getPersonnel());
            map.put("type", s.getType());
            map.put("reason", s.getReason());
            map.put("processingDepart", s.getProcessingDepart());
            Date date = s.getEffectiveDate();
            // 将date格式化
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            map.put("effectiveDate", sdf.format(date));
            list.add(map);
        }

        return list;
    }

    @Override
    public List<Map<String, String>> getDetailVacation(String year, String departId) {

        if (year == null || year.isEmpty() || year.equals("0")) {
            AssessCurrentAssess annual = assessCommonApi.getCurrentAssessInfo("annual");
            year = annual.getCurrentYear();
        }

        LambdaQueryWrapper<AssessAnnualVacation> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualVacation::getCurrentYear, year);
        lqw.eq(AssessAnnualVacation::getDepart, departId);
        List<AssessAnnualVacation> account = vacationMapper.selectList(lqw);

        List<Map<String, String>> list = new ArrayList<>();

        for (AssessAnnualVacation s : account) {
            Map<String, String> map = new HashMap<>();
            map.put("name", s.getName());
            map.put("type", String.valueOf(s.getType()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = s.getStartDate();
            Date endDate = s.getEndDate();
            String date = sdf.format(startDate) + "——" + sdf.format(endDate);
            map.put("date", date);
            map.put("days", String.valueOf(s.getDays()));
            // 将date格式化
            map.put("remark", s.getRemark());
            list.add(map);
        }

        return list;
    }

    @Override
    public void editPerson(AssessAnnualSummary summary) {
        AssessAnnualSummary oldSummary = summaryMapper.selectById(summary.getId());
        // summary.getBirth去掉字符串中的-
        summary.setBirth(summary.getBirth().replace("-", ""));
        if (summary.getSex().equals("男") || summary.getSex().equals("女")) {
            summary.setSex(summary.getSex().equals("男") ? "1" : "2");
        }


        if (oldSummary == null) {
            throw new JeecgBootException("该人员不存在");
        }

        AssessAnnualFill fill = fillMapper.selectById(summary.getFillId());

        if (!oldSummary.getPerson().equals(summary.getPerson()) || !oldSummary.getSex().equals(summary.getSex()) || !oldSummary.getBirth().equals(summary.getBirth())) {
            String newHashId = summary.getPerson() + "@" + (summary.getSex().equals("1") ? "男" : "女") + "@" + summary.getBirth();
            summary.setHashId(newHashId);

            LambdaUpdateWrapper<AssessAnnualAccountability> luwA = new LambdaUpdateWrapper<>();
            luwA.eq(AssessAnnualAccountability::getHashId, oldSummary.getHashId());
            luwA.set(AssessAnnualAccountability::getHashId, newHashId);
            luwA.set(AssessAnnualAccountability::getPersonnel, summary.getPerson());
            accountabilityMapper.update(null, luwA);

            LambdaUpdateWrapper<AssessAnnualVacation> luwV = new LambdaUpdateWrapper<>();
            luwV.eq(AssessAnnualVacation::getHashId, oldSummary.getHashId());
            luwV.set(AssessAnnualVacation::getHashId, newHashId);
            luwV.set(AssessAnnualVacation::getName, summary.getPerson());
            vacationMapper.update(null, luwV);

            LambdaUpdateWrapper<AssessAnnualNegativeList> luwN = new LambdaUpdateWrapper<>();
            luwN.eq(AssessAnnualNegativeList::getHashId, oldSummary.getHashId());
            luwN.set(AssessAnnualNegativeList::getHashId, newHashId);
            luwN.set(AssessAnnualNegativeList::getPerson, summary.getPerson());
            negativeListMapper.update(null, luwN);

            LambdaUpdateWrapper<AssessDemocraticEvaluationSummary> luwD = new LambdaUpdateWrapper<>();
            luwD.eq(AssessDemocraticEvaluationSummary::getAssessName, oldSummary.getHashId());
            luwD.set(AssessDemocraticEvaluationSummary::getAssessName, newHashId);
            evaluationSummaryMapper.update(null, luwD);

            LambdaUpdateWrapper<AssessLeaderConfig> luwL = new LambdaUpdateWrapper<>();
            luwL.eq(AssessLeaderConfig::getHashId, oldSummary.getHashId());
            luwL.set(AssessLeaderConfig::getHashId, newHashId);
            luwL.set(AssessLeaderConfig::getPerson, summary.getPerson());
            leaderConfigMapper.update(null, luwL);

            LambdaUpdateWrapper<AssessAssistConfig> luwC = new LambdaUpdateWrapper<>();
            luwC.eq(AssessAssistConfig::getHashId, oldSummary.getHashId());
            luwC.set(AssessAssistConfig::getHashId, newHashId);
            luwC.set(AssessAssistConfig::getPeople, summary.getPerson());
            assistConfigMapper.update(null, luwC);

            if (fill != null) {
                fill.setLeader(fill.getLeader().replaceAll(oldSummary.getHashId(), newHashId));
                fillMapper.updateById(fill);
            }
        }

        if (!summary.getType().equals(oldSummary.getType())) {
            List<AssessAnnualArrange> arrangeList = arrangeMapper.selectByMainId(fill.getId());
            if (!arrangeList.isEmpty()) {
                AssessAnnualArrange arrange = arrangeList.get(0);
                if (summary.getType().equals("chief")) {
                    arrange.setChiefNum(arrange.getChiefNum() + 1);
                }
                if (summary.getType().equals("deputy")) {
                    arrange.setDeputyNum(arrange.getDeputyNum() + 1);
                }
                if (oldSummary.getType().equals("chief")) {
                    arrange.setChiefNum(arrange.getChiefNum() - 1);
                }
                if (oldSummary.getType().equals("deputy")) {
                    arrange.setDeputyNum(arrange.getDeputyNum() - 1);
                }

                arrange.setVoteA(arrange.getChiefNum());
                arrange.setVoteB(arrange.getDeputyNum());

                arrangeMapper.updateById(arrange);
            }
        }

        summary.setDepart(oldSummary.getDepart());
        summaryMapper.updateById(summary);


    }

    @Override
    public void editDepart(String hashId, String targetDepart) {
        AssessCurrentAssess annual = assessCommonApi.getCurrentAssessInfo("annual");
        LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualSummary::getHashId, hashId);
        lqw.eq(AssessAnnualSummary::getCurrentYear, annual.getCurrentYear());
        AssessAnnualSummary summary = summaryMapper.selectOne(lqw);

        if (summary == null) throw new JeecgBootException("考核信息不存在");
        if (summary.getDepart().equals(targetDepart))
            throw new JeecgBootException(CommonUtils.getNameByHashId(hashId) + "已是当前处室（单位），无需调整！");

        summary.setDepart(targetDepart);

        AssessAnnualFill oldFill = fillMapper.selectById(summary.getFillId());

        LambdaQueryWrapper<AssessAnnualFill> lqwFill = new LambdaQueryWrapper<>();
        lqwFill.eq(AssessAnnualFill::getDepart, targetDepart);
        lqwFill.eq(AssessAnnualFill::getCurrentYear, annual.getCurrentYear());
        AssessAnnualFill newFill = fillMapper.selectOne(lqwFill);

        if (newFill == null) {
            throw new JeecgBootException("目标处室（单位）不存在");
        }

        summary.setFillId(newFill.getId());

        String leader = null;
        if (oldFill.getLeader() != null) {
            leader = oldFill.getLeader().replaceAll(hashId, "").replaceAll(",,", ",");
        }

        if (leader != null) {
            if (leader.endsWith(",")) {
                leader = leader.substring(0, leader.length() - 1);
            }
            oldFill.setLeader(leader);
        }
        if (newFill.getLeader() != null) {
            newFill.setLeader(newFill.getLeader() + "," + hashId);
        }

        AssessAnnualArrange oldArrange = arrangeMapper.selectByMainId(oldFill.getId()).get(0);
        AssessAnnualArrange newArrange = arrangeMapper.selectByMainId(newFill.getId()).get(0);

        if ("chief".equals(summary.getType())) {
            if (oldArrange.getChiefNum()!=null){
                oldArrange.setChiefNum(Math.max(oldArrange.getChiefNum() - 1, 0));
            }
            if (oldArrange.getVoteA() != null) {
                oldArrange.setVoteA(Math.max(oldArrange.getVoteA() - 1, 0));
            }
            if (newArrange.getChiefNum() != null) {
                newArrange.setChiefNum(newArrange.getChiefNum() + 1);
            } else {
                newArrange.setChiefNum(1);
            }
            if (newArrange.getVoteA() != null) {
                newArrange.setVoteA(newArrange.getVoteA() + 1);
            }
        }

        if ("deputy".equals(summary.getType())) {
            if (oldArrange.getDeputyNum() != null) {
                oldArrange.setDeputyNum(Math.max(oldArrange.getDeputyNum() - 1, 0));
            }
            if (oldArrange.getVoteB() != null) {
                oldArrange.setVoteB(Math.max(oldArrange.getVoteB() - 1, 0));
            }
            if (newArrange.getDeputyNum() != null) {
                newArrange.setDeputyNum(newArrange.getDeputyNum() + 1);
            } else {
                newArrange.setDeputyNum(1);
            }
            if (newArrange.getVoteB() != null) {
                newArrange.setVoteB(newArrange.getVoteB() + 1);
            }
        }

        this.updateById(summary);
        fillMapper.updateById(oldFill);
        fillMapper.updateById(newFill);
        arrangeMapper.updateById(oldArrange);
        arrangeMapper.updateById(newArrange);

        LambdaUpdateWrapper<AssessAnnualAccountability> luw1 = new LambdaUpdateWrapper<>();
        luw1.eq(AssessAnnualAccountability::getCurrentYear, summary.getCurrentYear());
        luw1.eq(AssessAnnualAccountability::getHashId, hashId);
        luw1.set(AssessAnnualAccountability::getDepart, targetDepart);
        accountabilityMapper.update(null, luw1);

        LambdaUpdateWrapper<AssessAnnualVacation> luw2 = new LambdaUpdateWrapper<>();
        luw2.eq(AssessAnnualVacation::getCurrentYear, summary.getCurrentYear());
        luw2.eq(AssessAnnualVacation::getHashId, hashId);
        luw2.set(AssessAnnualVacation::getDepart, targetDepart);
        vacationMapper.update(null, luw2);

        LambdaUpdateWrapper<AssessAnnualNegativeList> luw3 = new LambdaUpdateWrapper<>();
        luw3.eq(AssessAnnualNegativeList::getCurrentYear, summary.getCurrentYear());
        luw3.eq(AssessAnnualNegativeList::getHashId, hashId);
        luw3.set(AssessAnnualNegativeList::getDepart, targetDepart);
        negativeListMapper.update(null, luw3);

    }

    @Override
    public List<DepartResVO> getDepartRes(String year) {
        AssessCurrentAssess annual = assessCommonApi.getCurrentAssessInfo("annual");
        if (annual == null) {
            throw new JeecgBootException("当前没有考核信息");
        }

        Map<String, String> currentUserDepart = departCommonApi.getCurrentUserDepart();
        String departId = currentUserDepart.get("departId");

        LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualSummary::getDepart, departId);

        if (year == null || annual.getCurrentYear().equals(year)) {
            if (annual.isAssessing()) {
                throw new JeecgBootException("当前年度考核正在进行中，暂无结果。可在页面左上角查看其他年度考核结果！");
            } else {
                lqw.eq(AssessAnnualSummary::getCurrentYear, annual.getCurrentYear());
                year = annual.getCurrentYear();
            }
        } else {
            lqw.eq(AssessAnnualSummary::getCurrentYear, year);
        }

        List<AssessAnnualSummary> list = summaryMapper.selectList(lqw);

        List<DepartResVO> res = new ArrayList<>();
        for (AssessAnnualSummary summary : list) {
            DepartResVO departResVO = new DepartResVO();
            if ("group".equals(summary.getRecommendType()))
                departResVO.setName(currentUserDepart.get("departName") + "领导班子");
            else departResVO.setName(summary.getPerson());
            departResVO.setName(summary.getPerson());
            departResVO.setDepart(currentUserDepart.get("departName"));
            departResVO.setYear(year);
            departResVO.setIdentityType(summary.getIdentityType());
            departResVO.setLevel(summary.getLevel());
            String personOrder = summary.getPersonOrder();
            if (personOrder == null) personOrder = "0";
            int order = Integer.parseInt(personOrder);
            departResVO.setOrder(order);

            res.add(departResVO);
        }

        // 以order升序排序
        res.sort(Comparator.comparingInt(DepartResVO::getOrder));


        return res;
    }


    /**
     * 检查业务考核成绩，判断是否可以评优
     *
     * @param departIds
     * @param list
     * @param canNotExcellence
     * @param userIds
     */
    private void checkBusinessGrades(List<String> departIds, List<AssessAnnualSummary> list, List<String> canNotExcellence, List<String> userIds) {
        if (!departIds.isEmpty()) {
            List<AssessBusinessGrade> businessGradeList = businessGradeMapper.queryBusinessGradeByDepartIds(departIds, list.get(0).getCurrentYear());
            for (AssessBusinessGrade businessGrade : businessGradeList) {
                if ("3".equals(businessGrade.getLevel())) {
                    list.stream()
                            .filter(summary -> summary.getDepart().equals(businessGrade.getDepartId()))
                            .forEach(summary -> {
                                String personnel = summary.getPerson();
                                canNotExcellence.add(personnel);
                                userIds.remove(personnel);
                            });
                }
            }
        }
    }

    /**
     * 检查问责情况，判断是否可以评优
     *
     * @param userIds
     * @param list
     * @param canNotExcellence
     */
    private void checkAccountability(List<String> userIds, List<AssessAnnualSummary> list, List<String> canNotExcellence) {
        if (!userIds.isEmpty()) {
            List<AssessAnnualAccountability> assessAccountability = accountabilityMapper.getAssessAccountability(userIds);
            for (AssessAnnualAccountability accountability : assessAccountability) {
                String startYear = accountability.getEffectiveDate().toString().substring(0, 4);
                if (startYear.equals(list.get(0).getCurrentYear()) && accountability.getType().startsWith("B03A01")) {
                    int typeInt = parseType(accountability.getType());
                    if (typeInt >= 6) {
                        String personnel = accountability.getPersonnel();
                        canNotExcellence.add(personnel);
                        userIds.remove(personnel);
                    }
                }
            }
        }
    }

    private int parseType(String type) {
        try {
            return Integer.parseInt(type.substring(type.length() - 2));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 检查休假情况，判断是否可以评优
     *
     * @param userIds
     * @param list
     * @param canNotExcellence
     */
    private void checkVacations(List<String> userIds, List<AssessAnnualSummary> list, List<String> canNotExcellence) {
        if (!userIds.isEmpty()) {
            String currentYear = list.get(0).getCurrentYear();
            List<AssessAnnualVacation> assessVacation = vacationMapper.getAssessVacation(userIds, list.get(0).getCurrentYear());
            for (AssessAnnualVacation vacation : assessVacation) {
                if (vacation.getType() == 3 || vacation.getType() == 4) {
                    long days = (vacation.getEndDate().getTime() - vacation.getStartDate().getTime()) / (1000 * 60 * 60 * 24);
                    if (days > 180) {
                        String personnel = vacation.getName();
                        canNotExcellence.add(personnel);
                        userIds.remove(personnel);
                    }
                }
            }
        }
    }

    /**
     * 检查平时成绩，判断是否可以评优
     *
     * @param userIds
     * @param list
     */
    private void checkRegularGrades(List<String> userIds, List<AssessAnnualSummary> list) {
        if (!userIds.isEmpty()) {
            List<RegularGradeDTO> regularGradeDTOS = fillMapper.queryRegularGradeByUserIds(userIds, list.get(0).getCurrentYear());
            for (RegularGradeDTO regularGradeDTO : regularGradeDTOS) {
                if (Arrays.asList(regularGradeDTO.getQuarter1(), regularGradeDTO.getQuarter2(), regularGradeDTO.getQuarter3(), regularGradeDTO.getQuarter4()).contains("A")) {
                    list.stream()
                            .filter(summary -> summary.getPerson().equals(regularGradeDTO.getName()))
                            .forEach(summary -> summary.setEvaluation("1"));
                }
            }
        }
    }

    private void updateEvaluationStatus(List<AssessAnnualSummary> list, List<String> canNotExcellence) {
        list.stream()
                .filter(summary -> canNotExcellence.contains(summary.getPerson()))
                .forEach(summary -> summary.setEvaluation("3"));
    }
}
