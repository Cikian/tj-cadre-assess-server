package org.jeecg.modules.annual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.SysDepartModel;
import org.jeecg.modules.annual.entity.AnnualIndexEmployee;
import org.jeecg.modules.annual.entity.AnnualIndexLeader;
import org.jeecg.modules.annual.service.IAssessAnnualAccountabilityService;
import org.jeecg.modules.annual.service.IAssessAnnualFillService;
import org.jeecg.modules.annual.service.IAssessAnnualVacationService;
import org.jeecg.modules.annual.vo.AssessAnnualListVo;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.regular.entity.AssessRegularReportItem;
import org.jeecg.modules.regular.mapper.AssessRegularReportItemMapper;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.dto.AnnualAssessDepartFillDTO;
import org.jeecg.modules.sys.dto.AnnualFieldDTO;
import org.jeecg.modules.sys.dto.RegularGradeDTO;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.*;
import org.jeecg.modules.sys.entity.business.AssessBusinessDepartFill;
import org.jeecg.modules.sys.entity.business.AssessBusinessFillItem;
import org.jeecg.modules.sys.entity.business.AssessBusinessGrade;
import org.jeecg.modules.sys.entity.business.AssessLeaderDepartConfig;
import org.jeecg.modules.sys.mapper.annual.*;
import org.jeecg.modules.sys.mapper.business.AssessBusinessGradeMapper;
import org.jeecg.modules.sys.mapper.business.AssessLeaderDepartConfigMapper;
import org.jeecg.modules.system.entity.SysCategory;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysDictItem;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysCategoryMapper;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.user.UserCommonApi;
import org.jeecg.modules.utils.CommonUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 年度考核填报列表
 * @Author: jeecg-boot
 * @Date: 2024-09-04
 * @Version: V1.0
 */
@Service
public class AssessAnnualFillServiceImpl extends ServiceImpl<AssessAnnualFillMapper, AssessAnnualFill> implements IAssessAnnualFillService {

    @Autowired
    private AssessAnnualFillMapper annualFillMapper;
    @Autowired
    private AssessAnnualDutyReportMapper assessAnnualDutyReportMapper;
    @Autowired
    private AssessAnnualArrangeMapper assessAnnualArrangeMapper;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private DepartCommonApi departCommonApi;
    @Autowired
    private AssessAnnualAccountabilityMapper accountabilityMapper;
    @Autowired
    private AssessAnnualVacationMapper vacationMapper;
    @Autowired
    private AssessAnnualArrangeMapper arrangeMapper;
    @Autowired
    private IAssessAnnualAccountabilityService accountabilityService;
    @Autowired
    private IAssessAnnualVacationService vacationService;
    @Autowired
    private AssessAnnualSummaryMapper summaryMapper;
    @Autowired
    private AssessAnnualExcellentRatioMapper excellentRatioMapper;
    @Autowired
    private AssessAnnualExcellentNumMapper excellentNumMapper;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private AssessLeaderDepartConfigMapper leaderDepartConfigMapper;
    @Autowired
    private SysCategoryMapper sysCategoryMapper;
    @Autowired
    private AssessRegularReportItemMapper regularItemMapper;
    @Autowired
    private AssessBusinessGradeMapper businessGradeMapper;
    @Autowired
    private UserCommonApi userCommonApi;
    @Autowired
    private AssessAnnualNegativeListMapper negativeListMapper;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private AssessExcellentNumMapper excellentDeputyNumMapper;
    @Autowired
    private AssessWorkDaysMapper workDaysMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMain(AssessAnnualFill assessAnnualFill, List<AssessAnnualArrange> assessAnnualArrangeList) {
        annualFillMapper.insert(assessAnnualFill);
        if (assessAnnualArrangeList != null && assessAnnualArrangeList.size() > 0) {
            for (AssessAnnualArrange entity : assessAnnualArrangeList) {
                // 外键设置
                entity.setAnnualFillId(assessAnnualFill.getId());
                assessAnnualArrangeMapper.insert(entity);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMain(AssessAnnualFill assessAnnualFill, List<AssessAnnualArrange> assessAnnualArrangeList, List<AssessAnnualSummary> summaryList, boolean isSave) {
        String departType = assessAnnualFill.getDepartType();
        AssessAnnualArrange arrange = assessAnnualArrangeList.get(0);
        Integer voteNum = arrange.getVoteNum();
        if (!isSave) {
            if (voteNum == null || voteNum < summaryList.size()) {
                throw new JeecgBootException("参加民主测评人数不正确！");
            }
        }

        if (!isSave) {
            assessAnnualFill.setStatus(2);
        }

        if (assessAnnualFill.getDepartType() == null || assessAnnualFill.getDepartType().isEmpty()) {
            SysDepart sysDepart = departCommonApi.queryDepartById(assessAnnualFill.getDepart());
            assessAnnualFill.setDepartType(sysDepart.getDepartType());
        }


        annualFillMapper.updateById(assessAnnualFill);

        // 1.先删除子表数据
        assessAnnualArrangeMapper.deleteByMainId(assessAnnualFill.getId());

        LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualSummary::getFillId, assessAnnualFill.getId());
        lqw.ne(AssessAnnualSummary::getRecommendType, "group");
        List<AssessAnnualSummary> annualSummaries = summaryMapper.selectList(lqw);

        if (summaryList.size() == annualSummaries.size()) {
            log.warn("列表数量相同");
        } else {
            log.warn("列表数量不同");
        }

        // 2.子表数据重新插入
        if (!assessAnnualArrangeList.isEmpty()) {
            for (AssessAnnualArrange entity : assessAnnualArrangeList) {
                // 外键设置
                entity.setAnnualFillId(assessAnnualFill.getId());
                // todo：计算ABC票数
                if (arrange.getChiefNum() != null && arrange.getDeputyNum() != null) {
                    if ("bureau".equals(departType)) {
                        entity.setVoteA(0);
                        entity.setVoteB(arrange.getChiefNum() + arrange.getDeputyNum());
                    } else {
                        entity.setVoteA(arrange.getChiefNum());
                        entity.setVoteB(arrange.getDeputyNum());
                    }

                } else {
                    entity.setVoteB(0);
                }
                // if (!isSave) entity.setVoteC(arrange.getVoteC() == null ? 0 : arrange.getVoteC());
                entity.setVoteC(arrange.getVoteC() == null ? 0 : arrange.getVoteC());

                assessAnnualArrangeMapper.insert(entity);
            }
        }

        List<AssessRegularReportItem> regularReportItems = new ArrayList<>();

        List<String> hashIdList = summaryList.stream().map(AssessAnnualSummary::getHashId).collect(Collectors.toList());
        LambdaQueryWrapper<AssessRegularReportItem> lqw2 = new LambdaQueryWrapper<>();
        lqw2.in(AssessRegularReportItem::getHashId, hashIdList);
        lqw2.eq(AssessRegularReportItem::getCurrentYear, assessAnnualFill.getCurrentYear());
        List<AssessRegularReportItem> regulars = regularItemMapper.selectList(lqw2);
        List<String> regularHashIds = regulars.stream().map(AssessRegularReportItem::getHashId).collect(Collectors.toList());

        for (AssessAnnualSummary summary : summaryList) {

            if (regularHashIds.contains(summary.getHashId())) {

                AssessRegularReportItem item = regulars.stream().filter(i -> i.getHashId().equals(summary.getHashId())).findFirst().get();
                item.setQuarter1(summary.getQuarter1());
                item.setQuarter2(summary.getQuarter2());
                item.setQuarter3(summary.getQuarter3());
                item.setQuarter4(summary.getQuarter4());
                regularItemMapper.updateById(item);
                continue;
            }

            if ((summary.getQuarter1() != null && !summary.getQuarter1().isEmpty()) || (summary.getQuarter2() != null && !summary.getQuarter2().isEmpty()) || (summary.getQuarter3() != null && !summary.getQuarter3().isEmpty()) || (summary.getQuarter4() != null && !summary.getQuarter4().isEmpty())) {

                AssessRegularReportItem item = new AssessRegularReportItem();
                item.setHashId(summary.getHashId());
                item.setCurrentYear(summary.getCurrentYear());
                item.setDepartmentCode(summary.getDepart());
                item.setName(CommonUtils.getNameByHashId(summary.getHashId()));
                item.setSex(CommonUtils.getSexByHashId(summary.getHashId()).equals("男") ? 1 : 2);
                Date birthByHashId = CommonUtils.getBirthByHashId(summary.getHashId());
                // 转换为yyyyMMdd格式
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String birthStr = sdf.format(birthByHashId);

                item.setBirthDate(birthStr);
                item.setQuarter1(summary.getQuarter1());
                item.setQuarter2(summary.getQuarter2());
                item.setQuarter3(summary.getQuarter3());
                item.setQuarter4(summary.getQuarter4());
                item.setIsLeader("0");
                String type = "";
                switch (summary.getType()) {
                    case "chiefPro":
                        type = "正职兼总师、二巡";
                        break;
                    case "chief":
                        type = "正职";
                        break;
                    case "deputy":
                        type = "副职";
                        break;
                    default:
                        type = "其他";
                        break;

                }
                item.setPositionType(type);
                item.setAdminDepartment("基层单位管理");

                regularReportItems.add(item);
            }
        }

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        AssessAnnualSummaryMapper summaryMapperNew = sqlSession.getMapper(AssessAnnualSummaryMapper.class);
        AssessRegularReportItemMapper regularReportItemMapper = sqlSession.getMapper(AssessRegularReportItemMapper.class);

        summaryList.forEach(summaryMapperNew::updateById);
        regularReportItems.forEach(regularReportItemMapper::insert);

        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();

    }

    private String getDefaultLevelId(List<SysDictItem> levelDictItems) {
        int itemsSize = levelDictItems.size();
        int i;
        if (itemsSize % 2 == 0) i = itemsSize / 2 - 1;
        else i = (itemsSize - 1) / 2 - 1;
        return levelDictItems.get(i).getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delMain(String id) {
        assessAnnualDutyReportMapper.deleteByMainId(id);
        assessAnnualArrangeMapper.deleteByMainId(id);
        annualFillMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delBatchMain(Collection<? extends Serializable> idList) {
        for (Serializable id : idList) {
            assessAnnualDutyReportMapper.deleteByMainId(id.toString());
            assessAnnualArrangeMapper.deleteByMainId(id.toString());
            annualFillMapper.deleteById(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initAssess(AnnualAssessDepartFillDTO map) {
        String currentYear = map.getCurrentYear();
        String assessName = map.getAssessName();
        Date deadline = map.getDeadline();
        List<AssessAnnualSummary> summaryList = map.getSummaryList();

        // 判断当前年度是否已经初始化
        LambdaQueryWrapper<AssessAnnualFill> lqw1 = new LambdaQueryWrapper<>();
        lqw1.eq(AssessAnnualFill::getCurrentYear, currentYear);
        lqw1.select(AssessAnnualFill::getId);
        List<AssessAnnualFill> departFills = annualFillMapper.selectList(lqw1);
        if (!departFills.isEmpty()) {
            throw new JeecgBootException("当前年度已存在年度考核，不可重复发起！");
        }

        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
        if (currentAssessInfo != null) {
            int lastAssessYear = Integer.parseInt(currentAssessInfo.getCurrentYear());
            int currentYearInt = Integer.parseInt(currentYear);
            if (currentYearInt <= lastAssessYear) {
                throw new JeecgBootException("考核年度已结束，不可重复发起！");
            }
        }

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        AssessAnnualFillMapper fillMapperNew = sqlSession.getMapper(AssessAnnualFillMapper.class);
        AssessAnnualArrangeMapper arrangeMapperNew = sqlSession.getMapper(AssessAnnualArrangeMapper.class);
        AssessAnnualExcellentNumMapper excellentNumMapper = sqlSession.getMapper(AssessAnnualExcellentNumMapper.class);
        AssessAnnualSummaryMapper summaryMapperNew = sqlSession.getMapper(AssessAnnualSummaryMapper.class);

        List<SysDepartModel> bureauDepart = departCommonApi.queryDepartByType(new String[]{"1"});
        List<SysDepartModel> basicDepart = departCommonApi.queryDepartByType(new String[]{"2", "4"});
        List<SysDepartModel> institutionDepart = departCommonApi.queryDepartByType(new String[]{"3"});

        String bureauDepartIds = bureauDepart.stream().map(SysDepartModel::getId).collect(Collectors.joining(","));
        String basicDepartIds = basicDepart.stream().map(SysDepartModel::getId).collect(Collectors.joining(","));
        String institutionDepartIds = institutionDepart.stream().map(SysDepartModel::getId).collect(Collectors.joining(","));

        // 获取当前考核的所有单位
        List<AssessAnnualFill> fillList = new ArrayList<>();
        List<AssessAnnualArrange> arrangeList = new ArrayList<>();
        List<AssessAnnualSummary> groupSummaryList = new ArrayList<>();

        Map<String, String> insertMap = new HashMap<>();

        List<AssessExcellentNum> assessExcellentNums = excellentDeputyNumMapper.selectList(null);
        Map<String, Integer> excellentNumMap = assessExcellentNums.stream().collect(Collectors.toMap(AssessExcellentNum::getDepart, AssessExcellentNum::getExcellentNum));

        // 插入各处室考核填报表
        for (AssessAnnualSummary summary : summaryList) {
            String fillId = null;
            // 判断当前departId是否在insertMap中
            if (insertMap.containsKey(summary.getDepart())) {
                fillId = insertMap.get(summary.getDepart());
            } else {
                fillId = IdWorker.getIdStr();
                insertMap.put(summary.getDepart(), fillId);

                // 插入填报表
                AssessAnnualFill fill = new AssessAnnualFill();
                fill.setId(fillId);
                fill.setAssessName(assessName);
                fill.setCurrentYear(currentYear);
                fill.setDeadline(deadline);
                fill.setDepart(summary.getDepart());
                fill.setStatus(1);
                Integer i = excellentNumMap.get(summary.getDepart());
                if (i != null && i > 0) {
                    fill.setExcellentDeputyNum(i);
                } else {
                    fill.setExcellentDeputyNum(1);
                }

                if (bureauDepartIds.contains(fill.getDepart())) {
                    fill.setDepartType("bureau");
                } else if (basicDepartIds.contains(fill.getDepart())) {
                    fill.setDepartType("basic");
                } else if (institutionDepartIds.contains(fill.getDepart())) {
                    fill.setDepartType("institution");
                }

                fillList.add(fill);

                // 插入安排
                AssessAnnualArrange arrange = new AssessAnnualArrange();
                arrange.setAnnualFillId(fillId);
                arrangeList.add(arrange);

                // 插入领导班子汇总，处室无班子
                if (!fill.getDepartType().equals("bureau")) {
                    AssessAnnualSummary groupSummary = new AssessAnnualSummary();
                    groupSummary.setCurrentYear(currentYear);
                    groupSummary.setFillId(fillId);
                    groupSummary.setDepart(summary.getDepart());
                    groupSummary.setPerson(summary.getDepart());
                    groupSummary.setRecommendType("group");
                    groupSummary.setType("group");
                    groupSummary.setDepartType(fill.getDepartType());
                    groupSummary.setHashId("group");

                    groupSummaryList.add(groupSummary);
                }
            }

            String finalFillId = fillId;
            switch (summary.getType()) {
                case "chiefPro":
                    // 在arrangeList中找到第一个fillId为finalFillId的元素，chiefNum加1
                    arrangeList.stream().filter(arrange -> arrange.getAnnualFillId().equals(finalFillId)).findFirst().ifPresent(arrange -> arrange.setChiefNum(arrange.getChiefNum() == null ? 1 : arrange.getChiefNum() + 1));
                    break;
                case "chief":
                    // 在arrangeList中找到第一个fillId为finalFillId的元素，chiefNum加1
                    arrangeList.stream().filter(arrange -> arrange.getAnnualFillId().equals(finalFillId)).findFirst().ifPresent(arrange -> arrange.setChiefNum(arrange.getChiefNum() == null ? 1 : arrange.getChiefNum() + 1));
                    break;
                case "deputy":
                    // 在arrangeList中找到第一个fillId为finalFillId的元素，并将其deputyNum加1
                    arrangeList.stream().filter(arrange -> arrange.getAnnualFillId().equals(finalFillId)).findFirst().ifPresent(arrange -> arrange.setDeputyNum(arrange.getDeputyNum() == null ? 1 : arrange.getDeputyNum() + 1));
                    break;
                default:
                    break;
            }

            summary.setFillId(fillId);
            summary.setStatus(1);
            // todo: 初始都为称职
            summary.setLevel("2");
            summary.setCurrentYear(currentYear);

            if (bureauDepartIds.contains(summary.getDepart())) {
                summary.setDepartType("bureau");
            } else if (basicDepartIds.contains(summary.getDepart())) {
                summary.setDepartType("basic");
            } else if (institutionDepartIds.contains(summary.getDepart())) {
                summary.setDepartType("institution");
            }

            if ("公务员".equals(summary.getIdentityType()) || "参公".equals(summary.getIdentityType())) {
                if (bureauDepartIds.contains(summary.getDepart())) {
                    summary.setRecommendType("bureau");
                } else if (basicDepartIds.contains(summary.getDepart())) {
                    summary.setRecommendType("basic");
                }
            } else {
                summary.setRecommendType("institution");
            }

            String sex = summary.getSex().equals("1") ? "男" : "女";
            String hashId = CommonUtils.hashId(summary.getPerson(), sex, summary.getBirth());
            summary.setHashId(hashId);
        }

        List<SysUser> allLeader = userCommonApi.getCurrentLeader();

        LambdaQueryWrapper<AssessAnnualExcellentRatio> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualExcellentRatio::getEnable, 1);
        AssessAnnualExcellentRatio ratio = excellentRatioMapper.selectOne(lqw);

        List<AssessAnnualExcellentNum> excellentNums = new ArrayList<>();


        if (ratio != null) {
            for (SysUser leader : allLeader) {
                AssessAnnualExcellentNum num = new AssessAnnualExcellentNum();
                num.setLeader(leader.getId());
                num.setGroupRatio(ratio.getGroupRatio());
                num.setBureauRatio(ratio.getBureauRatio());
                num.setBasicRatio(ratio.getBasicRatio());
                num.setInstitutionRatio(ratio.getInstitutionRatio());
                excellentNums.add(num);
            }
        }

        // QueryWrapper<AssessAnnualExcellentNum> queryWrapper = Wrappers.query();
        // List<AssessAnnualExcellentNum> excellentNums = excellentNumMapper.getInitLeaderData();
        // QueryWrapper<AssessAnnualExcellentNum> assessAnnualExcellentNumQueryWrapper = queryWrapper.select("sys_user.id AS leader",
        //                 "sys_user.realname AS name",
        //                 "ratio.group_ratio",
        //                 "ratio.bureau_ratio",
        //                 "ratio.basic_ratio",
        //                 "ratio.institution_ratio")
        //         .eq("ratio.enable", 1) // 假设enable为整数类型，1表示启用
        //         .inSql("sys_user.id", "SELECT leader_id FROM assess_leader_depart_config")
        //         .inSql("sys_user.id", "SELECT leader_id FROM assess_annual_excellent_ratio");
        //
        // List<AssessAnnualExcellentNum> excellentNums = excellentNumMapper.selectList(assessAnnualExcellentNumQueryWrapper);


        if (!excellentNums.isEmpty()) {
            // todo: 遍历,查询领导分管处室,基层单位等,设置数量,年度

            BigDecimal groupRatio = excellentNums.get(0).getGroupRatio();
            BigDecimal bureauRatio = excellentNums.get(0).getBureauRatio();
            BigDecimal basicRatio = excellentNums.get(0).getBasicRatio();
            BigDecimal institutionRatio = excellentNums.get(0).getInstitutionRatio();

            List<AssessLeaderDepartConfig> ldConfigs = leaderDepartConfigMapper.selectList(null);

            for (AssessAnnualExcellentNum num : excellentNums) {
                num.setCurrentYear(currentYear);
                String leader = num.getLeader();
                // 在ldConfigs中查找leader对应的config
                AssessLeaderDepartConfig config = ldConfigs.stream().filter(ldConfig -> ldConfig.getLeaderId().equals(leader)).findFirst().orElse(null);

                // 设置当前领导领导班子优秀比例和数量
                if (config != null) {
                    int groupNum = 0;
                    int bureauNums = 0;
                    int basicNums = 0;
                    int institutionNums = 0;

                    int inspectionNum = 0;

                    String departIds = config.getDepartId();

                    for (AssessAnnualSummary summary : groupSummaryList) {
                        if (!summary.getDepartType().equals("bureau") && departIds.contains(summary.getDepart())) {
                            groupNum++;
                        }
                    }

                    for (AssessAnnualSummary summary : summaryList) {
                        if (departIds.contains(summary.getDepart()) && summary.getInspectionWork()) {
                            inspectionNum++;
                            continue;
                        }
                        if ("bureau".equals(summary.getRecommendType()) && departIds.contains(summary.getDepart())) {
                            bureauNums++;
                        }
                        if ("basic".equals(summary.getRecommendType()) && departIds.contains(summary.getDepart())) {
                            basicNums++;
                        }
                        if ("institution".equals(summary.getRecommendType()) && departIds.contains(summary.getDepart())) {
                            institutionNums++;
                        }
                    }

                    num.setDepart(departIds);

                    num.setGroupNum(new BigDecimal(groupNum));
                    num.setBureauNum(new BigDecimal(bureauNums));
                    num.setBasicNum(new BigDecimal(basicNums));
                    num.setInstitutionNum(new BigDecimal(institutionNums));

                    num.setGroupRatio(groupRatio);
                    num.setBureauRatio(bureauRatio);
                    num.setBasicRatio(basicRatio);
                    num.setInstitutionRatio(institutionRatio);

                    num.setInspectionNum(inspectionNum);

                    // 保留两位小数
                    num.setGroupTheoryNum(new BigDecimal(groupNum).multiply(groupRatio).setScale(2, RoundingMode.HALF_UP));
                    num.setBureauTheoryNum(new BigDecimal(bureauNums).multiply(bureauRatio).setScale(2, RoundingMode.HALF_UP));
                    num.setBasicTheoryNum(new BigDecimal(basicNums).multiply(basicRatio).setScale(2, RoundingMode.HALF_UP));
                    num.setInstitutionTheoryNum(new BigDecimal(institutionNums).multiply(institutionRatio).setScale(2, RoundingMode.HALF_UP));
                }
            }
        }

        // 批量插入各处室考核填报表和各处室考核填报明细
        fillList.forEach(fillMapperNew::insert);
        summaryList.forEach(summaryMapperNew::insert);
        groupSummaryList.forEach(summaryMapperNew::insert);
        arrangeList.forEach(arrangeMapperNew::insert);
        if (excellentNums != null) {
            excellentNums.forEach(excellentNumMapper::insert);
        }

        // 添加年份字典数据
        departCommonApi.saveAssessYear(currentYear);
        assessCommonApi.updateAssessCurrentYear(new AssessCurrentAssess("annual", assessName, currentYear, new Date(), deadline, null, true));

        // 提交事务、清理缓存、关闭sqlSession
        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();
    }


    // TODO 此方法应该在平时考核模块中
    @Override
    public List<RegularGradeDTO> queryRegularGrade(List<String> userIds, String year) {
        return annualFillMapper.queryRegularGradeByUserIds(userIds, year);
    }

    @Override
    @Deprecated
    public List<AssessAnnualListVo> getSummaryList(IPage<AssessAnnualFill> pageList) {
        List<AssessAnnualFill> records = pageList.getRecords();
        if (records != null && records.size() > 0) {
            List<AssessAnnualListVo> list = new ArrayList<>();
            for (AssessAnnualFill record : records) {
                AssessAnnualListVo vo = new AssessAnnualListVo();
                BeanUtils.copyProperties(record, vo);

                // 判断是否存在问责
                LambdaQueryWrapper<AssessAnnualAccountability> lqw = new LambdaQueryWrapper<>();
                lqw.eq(AssessAnnualAccountability::getDepart, record.getDepart());
                lqw.eq(AssessAnnualAccountability::getCurrentYear, record.getCurrentYear());
                List<AssessAnnualAccountability> accountabilities = accountabilityMapper.selectList(lqw);
                if (accountabilities != null && !accountabilities.isEmpty()) {
                    vo.setAccountability(true);
                }

                // 判断是否存在请假
                LambdaQueryWrapper<AssessAnnualVacation> lqw1 = new LambdaQueryWrapper<>();
                lqw1.eq(AssessAnnualVacation::getDepart, record.getDepart());
                lqw1.eq(AssessAnnualVacation::getCurrentYear, record.getCurrentYear());
                List<AssessAnnualVacation> vacations = vacationMapper.selectList(lqw1);
                if (vacations != null && !vacations.isEmpty()) {
                    vo.setVacation(true);
                }

                // 获取推荐优秀副职
                LambdaQueryWrapper<AssessAnnualArrange> lqw2 = new LambdaQueryWrapper<>();
                lqw2.eq(AssessAnnualArrange::getAnnualFillId, record.getId());
                AssessAnnualArrange annualArrange = arrangeMapper.selectOne(lqw2);
                if (annualArrange != null) {
                    vo.setEvaluation(annualArrange.getRecommend());
                }

                // 判断是否可以评优
                vo.setRecommend(false);

                list.add(vo);
            }
            return list;
        }

        return Collections.emptyList();
    }

    @Override
    public AnnualIndexLeader getAllItems() {

        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
        if (currentAssessInfo != null && currentAssessInfo.isAssessing()) {
            List<AnnualFieldDTO> items = annualFillMapper.getAllItems(currentAssessInfo.getCurrentYear());

            // 统计未完成状态数量
            long uncompleted = items.stream().filter(item -> item.getStatus() != 3).count();

            // 所有 items 的数量
            int totalCount = items.size();

            // 计算完成的数量
            int complete = totalCount - (int) uncompleted;

            // 计算占比
            double ratio = totalCount > 0 ? complete / (double) totalCount : 0.0;

            // 获取所有部门信息并存入 Map
            List<SysDepartModel> sysDepartModels = departCommonApi.queryAllDepart();
            Map<String, String> departMap = sysDepartModels.stream().collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getDepartName));

            // 获取所有 status 不等于 3 的记录
            List<AnnualFieldDTO> reports = items.stream().filter(item -> item.getStatus() != 3).peek(annualFieldDTO -> {
                String departName = departMap.get(annualFieldDTO.getDepart());
                annualFieldDTO.setDepart(departName != null ? departName : "未知部门");
            }).collect(Collectors.toList());

            // 输出结果
            return new AnnualIndexLeader(complete, uncompleted, ratio, reports);
        }
        return null;

    }

    @Override
    public AnnualIndexEmployee[] getPartial(String departId, AssessCurrentAssess assessInfo) {
        if (assessInfo == null || StringUtils.isBlank(assessInfo.getCurrentYear()) || !assessInfo.isAssessing()) {
            return new AnnualIndexEmployee[]{new AnnualIndexEmployee("当前无正在进行的考核")};
        }
        // 创建查询条件，匹配 department_code 字段
        QueryWrapper<AssessAnnualFill> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("depart", departId).eq("current_year", assessInfo.getCurrentYear());

        List<AssessAnnualFill> reports = annualFillMapper.selectList(queryWrapper);

        if (reports.isEmpty()) {
            return null;
        }

        int status = reports.get(0).getStatus();
        String statusMessage;
        // 根据 status 设置状态信息
        switch (status) {
            case 1:
                statusMessage = "填报中";
                break;
            case 2:
                statusMessage = "待审核";
                break;
            case 3:
                statusMessage = "填报完成";
                break;
            case 4:
                statusMessage = "已驳回";
                break;
            default:
                statusMessage = "当前无正在进行的考核"; // 处理未知状态
                break;
        }

        return new AnnualIndexEmployee[]{new AnnualIndexEmployee(statusMessage)};

    }

    @Override
    public List<AssessAnnualSummary> getEvaluationSuggestion(List<AssessAnnualSummary> allList) {
        // 过滤type不为group的summary
        List<AssessAnnualSummary> groupList = allList.stream().filter(summary -> summary.getRecommendType().equals("group")).collect(Collectors.toList());
        List<AssessAnnualSummary> summaryList = allList.stream().filter(summary -> !summary.getRecommendType().equals("group")).collect(Collectors.toList());

        // 清空allList
        allList.clear();

        if (!summaryList.isEmpty()) {
            String currentYear = summaryList.get(0).getCurrentYear();
            LambdaQueryWrapper<AssessWorkDays> wdLqw = new LambdaQueryWrapper<>();
            wdLqw.eq(AssessWorkDays::getCurrentYear, currentYear);
            List<AssessWorkDays> assessWorkDays = workDaysMapper.selectList(wdLqw);
            Double workDays = 0.0;
            if (!assessWorkDays.isEmpty()) {
                Integer workDays1 = assessWorkDays.get(0).getWorkDays();
                workDays = Double.valueOf(workDays1);
                // 除以2
                workDays = workDays / 2;
            }

            for (AssessAnnualSummary summary : summaryList) {
                // 默认为中间等次
                summary.setEvaluation("2");
                if ("Y".equals(summary.getDisciplineRecommend())) summary.setLevel("1");
                else summary.setLevel("2");
                summary.setRemark("");

                LambdaQueryWrapper<AssessRegularReportItem> lqw = new LambdaQueryWrapper<>();
                lqw.eq(AssessRegularReportItem::getCurrentYear, summary.getCurrentYear());
                lqw.eq(AssessRegularReportItem::getHashId, summary.getHashId());
                AssessRegularReportItem regularReportItem = regularItemMapper.selectOne(lqw);
                // 平时考核中四个季度只要有一次好，年度考核时才有评优资格。
                if (regularReportItem != null) {
                    if (!"A".equals(regularReportItem.getQuarter1()) && !"A".equals(regularReportItem.getQuarter2()) && !"A".equals(regularReportItem.getQuarter3()) && !"A".equals(regularReportItem.getQuarter4())) {
                        this.setEvaluation(summary, "平时考核无“好”等次");
                    }
                }

                // 干部休假情况表中，病、事假半年（自然日）以上，不可以评优
                LambdaQueryWrapper<AssessAnnualVacation> lqw2 = new LambdaQueryWrapper<>();
                lqw2.eq(AssessAnnualVacation::getCurrentYear, summary.getCurrentYear());
                lqw2.eq(AssessAnnualVacation::getHashId, summary.getHashId());
                lqw2.eq(AssessAnnualVacation::getStatus, "3");
                List<AssessAnnualVacation> vacationList = vacationMapper.selectList(lqw2);
                if (vacationList != null && !vacationList.isEmpty()) {
                    double allDays = 0.0;

                    long totalDays = 0;
                    for (AssessAnnualVacation vacation : vacationList) {
                        Date startDate = vacation.getStartDate();
                        Date endDate = vacation.getEndDate();
                        long diff = endDate.getTime() - startDate.getTime();
                        long days = diff / (1000 * 60 * 60 * 24);
                        totalDays += days;

                        if (vacation.getDays() != null) {
                            allDays += vacation.getDays();
                        }

                    }
                    // if (totalDays >= 180) {
                    if (allDays > workDays) {
                        this.setEvaluation(summary, "休假超过半年");
                    }
                }

                // 问责情况统计表中，影响期在本考核年度（本考核年度：从上一年度确定考核结果的时间开始到本年度确定考核结果结束），问责类型四种形态中达到诫勉及以上的，不可以评优
                LambdaQueryWrapper<AssessAnnualAccountability> lqw3 = new LambdaQueryWrapper<>();
                // lqw3.likeRight(AssessAnnualAccountability::getEffectiveDate, summary.getCurrentYear());
                // todo：多年影响

                // 查询本考核年度、本人、状态为3的问责记录
                lqw3.eq(AssessAnnualAccountability::getCurrentYear, summary.getCurrentYear());
                lqw3.eq(AssessAnnualAccountability::getHashId, summary.getHashId());
                lqw3.eq(AssessAnnualAccountability::getStatus, "3");
                List<AssessAnnualAccountability> accountabilityList = accountabilityMapper.selectList(lqw3);
                List<AssessAnnualAccountability> accountability2Negative = new ArrayList<>();
                if (accountabilityList != null && !accountabilityList.isEmpty()) {
                    // 是否影响评优
                    boolean isAccountability = false;
                    List<String> typeList = new ArrayList<>();

                    for (AssessAnnualAccountability accountability : accountabilityList) {
                        // 获取当前问责的类型并转换为中文数组
                        String types = accountability.getType();
                        String[] list = types.split(",");

                        for (String type : list) {
                            SysCategory sysCategory = sysCategoryMapper.selectById(type);
                            if (sysCategory != null) {
                                // 如果问责类型的pid不是1836236514117480449或者问责类型是B03A01A06，则不可以评优
                                if (!"1836236514117480449".equals(sysCategory.getPid()) || sysCategory.getCode().equals("B03A01A06")) {
                                    isAccountability = true;
                                    if (!typeList.contains(sysCategory.getName())) {
                                        typeList.add(sysCategory.getName());
                                    }
                                } else {
                                    accountability2Negative.add(accountability);
                                }
                                // 是否存在未确定的问责
                                if (sysCategory.getCode().equals("B03A05")) {
                                    summary.setLevel("99");
                                    this.setEvaluation(summary, "存在不确定类型的问责");
                                }
                            }
                        }

                    }
                    if (isAccountability) {
                        // 将问责类型拼接
                        StringBuilder sb = new StringBuilder();
                        for (String type : typeList) {
                            sb.append(type).append("、");
                        }
                        String types = sb.toString();
                        if (types.endsWith("、")) {
                            types = types.substring(0, types.length() - 1);
                        }

                        this.setEvaluation(summary, "受到" + types + "问责");
                    }
                }


                // 书记的党建考核不是优，不可以评优
                if (summary.getSecretary()) {
                    if (!"Y".equals(summary.getPartyAssess())) {
                        this.setEvaluation(summary, "党组织书记考核结果不是“优”");
                    }
                }

                // 业务考核为一般等次的党政主要负责人不能评优
                if (summary.getPartyHeader()) {
                    LambdaQueryWrapper<AssessBusinessGrade> lqw4 = new LambdaQueryWrapper<>();
                    lqw4.eq(AssessBusinessGrade::getCurrentYear, summary.getCurrentYear());
                    lqw4.eq(AssessBusinessGrade::getDepartId, summary.getDepart());
                    AssessBusinessGrade bg = businessGradeMapper.selectOne(lqw4);
                    if (bg != null && "3".equals(bg.getLevel())) {
                        this.setEvaluation(summary, "业务工作测评为一般等次");
                    }
                }

                LambdaQueryWrapper<AssessAnnualNegativeList> lqw5 = new LambdaQueryWrapper<>();
                lqw5.eq(AssessAnnualNegativeList::getCurrentYear, summary.getCurrentYear());
                lqw5.eq(AssessAnnualNegativeList::getHashId, summary.getHashId());
                lqw5.eq(AssessAnnualNegativeList::getStatus, 3);

                List<AssessAnnualNegativeList> negativeLists = negativeListMapper.selectList(lqw5);
                StringBuilder sb = new StringBuilder();
                for (AssessAnnualNegativeList negativeList : negativeLists) {
                    String content = negativeList.getContent();
                    sb.append(content).append("；");
                }
                if (!accountability2Negative.isEmpty()) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("受到");
                    for (AssessAnnualAccountability accountability : accountability2Negative) {
                        String types = accountability.getType();
                        String[] list = types.split(",");
                        for (String type : list) {
                            SysCategory sysCategory = sysCategoryMapper.selectById(type);
                            if (sysCategory != null && sb2.indexOf(sysCategory.getName()) == -1) {
                                sb2.append(sysCategory.getName()).append("、");
                            }
                        }
                    }
                    // 删除最后一个顿号
                    sb2.deleteCharAt(sb2.length() - 1);
                    sb2.append("问责；");
                    sb.append(sb2);
                }
                summary.setNegativeList("");
                summary.setNegativeList(sb.toString());
                allList.add(summary);
            }
        }
        if (!groupList.isEmpty()) {
            for (AssessAnnualSummary summary : groupList) {
                // 默认为中间等次
                summary.setEvaluation("2");
                if ("Y".equals(summary.getDisciplineRecommend())) summary.setLevel("1");
                else summary.setLevel("2");
                summary.setRemark("");

                // 业务考核为一般等次的党政主要负责人不能评优
                LambdaQueryWrapper<AssessBusinessGrade> lqw4 = new LambdaQueryWrapper<>();
                lqw4.eq(AssessBusinessGrade::getCurrentYear, summary.getCurrentYear());
                lqw4.eq(AssessBusinessGrade::getDepartId, summary.getDepart());
                AssessBusinessGrade bg = businessGradeMapper.selectOne(lqw4);
                if (bg != null && "3".equals(bg.getLevel())) {
                    this.setEvaluation(summary, "业务工作测评为一般等次");
                }

                LambdaQueryWrapper<AssessAnnualNegativeList> lqw5 = new LambdaQueryWrapper<>();
                lqw5.eq(AssessAnnualNegativeList::getCurrentYear, summary.getCurrentYear());
                lqw5.isNull(AssessAnnualNegativeList::getHashId);
                lqw5.eq(AssessAnnualNegativeList::getDepart, summary.getDepart());
                lqw5.eq(AssessAnnualNegativeList::getStatus, 3);

                List<AssessAnnualNegativeList> negativeLists = negativeListMapper.selectList(lqw5);
                StringBuilder sb = new StringBuilder();
                for (AssessAnnualNegativeList negativeList : negativeLists) {
                    String content = negativeList.getContent();
                    sb.append(content).append("；");
                }

                summary.setNegativeList(sb.toString());

                allList.add(summary);
            }
        }

        return allList;
    }

    @Override
    public AssessAnnualSummary getEvaluationSuggestion(AssessAnnualSummary summary) {
        if (summary == null) {
            return null;
        }

        String currentYear = summary.getCurrentYear();
        LambdaQueryWrapper<AssessWorkDays> wdLqw = new LambdaQueryWrapper<>();
        wdLqw.eq(AssessWorkDays::getCurrentYear, currentYear);
        List<AssessWorkDays> assessWorkDays = workDaysMapper.selectList(wdLqw);
        Double workDays = 0.0;
        if (!assessWorkDays.isEmpty()) {
            Integer workDays1 = assessWorkDays.get(0).getWorkDays();
            workDays = Double.valueOf(workDays1);
            // 除以2
            workDays = workDays / 2;
        }

        // 默认为中间等次
        summary.setEvaluation("2");
        if ("Y".equals(summary.getDisciplineRecommend())) summary.setLevel("1");
        else summary.setLevel("2");
        summary.setRemark("");

        if (!"group".equals(summary.getRecommendType())) {
            LambdaQueryWrapper<AssessRegularReportItem> lqw = new LambdaQueryWrapper<>();
            lqw.eq(AssessRegularReportItem::getCurrentYear, summary.getCurrentYear());
            lqw.eq(AssessRegularReportItem::getHashId, summary.getHashId());
            AssessRegularReportItem regularReportItem = regularItemMapper.selectOne(lqw);
            // 平时考核中四个季度只要有一次好，年度考核时才有评优资格。
            if (regularReportItem != null) {
                if (!"A".equals(regularReportItem.getQuarter1()) && !"A".equals(regularReportItem.getQuarter2()) && !"A".equals(regularReportItem.getQuarter3()) && !"A".equals(regularReportItem.getQuarter4())) {
                    this.setEvaluation(summary, "平时考核无“好”等次");
                }
            }

            // 干部休假情况表中，病、事假半年（自然日）以上，不可以评优
            LambdaQueryWrapper<AssessAnnualVacation> lqw2 = new LambdaQueryWrapper<>();
            lqw2.eq(AssessAnnualVacation::getCurrentYear, summary.getCurrentYear());
            lqw2.eq(AssessAnnualVacation::getHashId, summary.getHashId());
            lqw2.eq(AssessAnnualVacation::getStatus, "3");
            List<AssessAnnualVacation> vacationList = vacationMapper.selectList(lqw2);
            if (vacationList != null && !vacationList.isEmpty()) {
                double allDays = 0.0;
                long totalDays = 0;
                for (AssessAnnualVacation vacation : vacationList) {
                    Date startDate = vacation.getStartDate();
                    Date endDate = vacation.getEndDate();
                    long diff = endDate.getTime() - startDate.getTime();
                    long days = diff / (1000 * 60 * 60 * 24);
                    totalDays += days;
                    if (vacation.getDays() != null) {
                        allDays += vacation.getDays();
                    }
                }
                // if (totalDays > 180) {
                if (allDays > workDays) {
                    this.setEvaluation(summary, "休假超过半年");
                }
            }

            // 问责情况统计表中，影响期在本考核年度（本考核年度：从上一年度确定考核结果的时间开始到本年度确定考核结果结束），问责类型四种形态中达到诫勉及以上的，不可以评优
            LambdaQueryWrapper<AssessAnnualAccountability> lqw3 = new LambdaQueryWrapper<>();
            // lqw3.likeRight(AssessAnnualAccountability::getEffectiveDate, summary.getCurrentYear());
            lqw3.likeRight(AssessAnnualAccountability::getCurrentYear, summary.getCurrentYear());
            lqw3.eq(AssessAnnualAccountability::getHashId, summary.getHashId());
            lqw3.eq(AssessAnnualAccountability::getStatus, "3");
            List<AssessAnnualAccountability> accountabilityList = accountabilityMapper.selectList(lqw3);
            List<AssessAnnualAccountability> accountability2Negative = new ArrayList<>();
            if (accountabilityList != null && !accountabilityList.isEmpty()) {
                boolean isAccountability = false;
                List<String> typeList = new ArrayList<>();
                for (AssessAnnualAccountability accountability : accountabilityList) {
                    String types = accountability.getType();
                    String[] list = types.split(",");
                    for (String type : list) {
                        SysCategory sysCategory = sysCategoryMapper.selectById(type);
                        if (sysCategory != null) {
                            if (!"1836236514117480449".equals(sysCategory.getPid()) || sysCategory.getCode().equals("B03A01A06")) {
                                isAccountability = true;
                                if (!typeList.contains(sysCategory.getName())) {
                                    typeList.add(sysCategory.getName());
                                }
                            } else {
                                accountability2Negative.add(accountability);
                            }
                            // 是否存在未确定的问责
                            if (sysCategory.getCode().equals("B03A05")) {
                                summary.setLevel("99");
                                this.setEvaluation(summary, "存在不确定类型的问责");
                            }
                        }
                    }

                }
                if (isAccountability) {
                    // 将问责类型拼接
                    StringBuilder sb = new StringBuilder();
                    for (String type : typeList) {
                        sb.append(type).append("、");
                    }
                    String types = sb.toString();
                    if (types.endsWith("、")) {
                        types = types.substring(0, types.length() - 1);
                    }

                    this.setEvaluation(summary, "受到" + types + "问责");
                }
            }


            // 书记的党建考核不是优，不可以评优
            if (summary.getSecretary()) {
                if (!"Y".equals(summary.getPartyAssess())) {
                    this.setEvaluation(summary, "党组织书记考核结果不是“优”");
                }
            }

            // 业务考核为一般等次的党政主要负责人不能评优
            if (summary.getPartyHeader()) {
                LambdaQueryWrapper<AssessBusinessGrade> lqw4 = new LambdaQueryWrapper<>();
                lqw4.eq(AssessBusinessGrade::getCurrentYear, summary.getCurrentYear());
                lqw4.eq(AssessBusinessGrade::getDepartId, summary.getDepart());
                AssessBusinessGrade bg = businessGradeMapper.selectOne(lqw4);
                if (bg != null && "3".equals(bg.getLevel())) {
                    this.setEvaluation(summary, "业务工作测评为一般等次");
                }
            }

            LambdaQueryWrapper<AssessAnnualNegativeList> lqw5 = new LambdaQueryWrapper<>();
            lqw5.eq(AssessAnnualNegativeList::getCurrentYear, summary.getCurrentYear());
            lqw5.eq(AssessAnnualNegativeList::getHashId, summary.getHashId());
            lqw5.eq(AssessAnnualNegativeList::getStatus, 3);
            List<AssessAnnualNegativeList> negativeLists = negativeListMapper.selectList(lqw5);

            StringBuilder sb = new StringBuilder();
            for (AssessAnnualNegativeList negativeList : negativeLists) {
                String content = negativeList.getContent();
                sb.append(content).append("；");
            }
            if (!accountability2Negative.isEmpty()) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("受到");
                for (AssessAnnualAccountability accountability : accountability2Negative) {
                    String types = accountability.getType();
                    String[] list = types.split(",");
                    for (String type : list) {
                        SysCategory sysCategory = sysCategoryMapper.selectById(type);
                        if (sysCategory != null && sb2.indexOf(sysCategory.getName()) == -1) {
                            sb2.append(sysCategory.getName()).append("、");
                        }
                    }
                }
                // 删除最后一个顿号
                sb2.deleteCharAt(sb2.length() - 1);
                sb2.append("问责；");
                sb.append(sb2);
            }
            summary.setNegativeList("");
            summary.setNegativeList(sb.toString());
        } else {
            // 默认为中间等次
            summary.setEvaluation("2");
            if ("Y".equals(summary.getDisciplineRecommend())) summary.setLevel("1");
            else summary.setLevel("2");
            summary.setRemark("");

            // 业务考核为一般等次的党政主要负责人不能评优
            LambdaQueryWrapper<AssessBusinessGrade> lqw4 = new LambdaQueryWrapper<>();
            lqw4.eq(AssessBusinessGrade::getCurrentYear, summary.getCurrentYear());
            lqw4.eq(AssessBusinessGrade::getDepartId, summary.getDepart());
            AssessBusinessGrade bg = businessGradeMapper.selectOne(lqw4);
            if (bg != null && "3".equals(bg.getLevel())) {
                this.setEvaluation(summary, "业务工作测评为一般等次");
            }

            LambdaQueryWrapper<AssessAnnualNegativeList> lqw5 = new LambdaQueryWrapper<>();
            lqw5.eq(AssessAnnualNegativeList::getCurrentYear, summary.getCurrentYear());
            lqw5.eq(AssessAnnualNegativeList::getType, "1");
            lqw5.eq(AssessAnnualNegativeList::getDepart, summary.getDepart());
            lqw5.eq(AssessAnnualNegativeList::getStatus, "3");

            List<AssessAnnualNegativeList> negativeLists = negativeListMapper.selectList(lqw5);
            StringBuilder sb = new StringBuilder();
            for (AssessAnnualNegativeList negativeList : negativeLists) {
                String content = negativeList.getContent();
                sb.append(content).append("；");
            }
            summary.setNegativeList(sb.toString());
        }
        return summary;
    }

    @Override
    public Result<String> passAudit(String assessId, String name) {
        LambdaUpdateWrapper<AssessAnnualFill> lqw = new LambdaUpdateWrapper<>();
        lqw.eq(AssessAnnualFill::getId, assessId);
        lqw.set(AssessAnnualFill::getStatus, 3);
        lqw.set(AssessAnnualFill::getAuditBy, name);
        annualFillMapper.update(null, lqw);

        LambdaQueryWrapper<AssessAnnualSummary> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(AssessAnnualSummary::getFillId, assessId);
        List<AssessAnnualSummary> summaryList = summaryMapper.selectList(lqw2);

        // 审核通过问责和休假
        LambdaUpdateWrapper<AssessAnnualAccountability> luw1 = new LambdaUpdateWrapper<>();
        luw1.eq(AssessAnnualAccountability::getDepart, summaryList.get(0).getDepart());
        luw1.eq(AssessAnnualAccountability::getCurrentYear, summaryList.get(0).getCurrentYear());
        luw1.eq(AssessAnnualAccountability::getStatus, 1);
        luw1.set(AssessAnnualAccountability::getStatus, 2);
        accountabilityMapper.update(null, luw1);
        LambdaUpdateWrapper<AssessAnnualVacation> luw2 = new LambdaUpdateWrapper<>();
        luw2.eq(AssessAnnualVacation::getDepart, summaryList.get(0).getDepart());
        luw2.eq(AssessAnnualVacation::getCurrentYear, summaryList.get(0).getCurrentYear());
        luw2.eq(AssessAnnualVacation::getStatus, 1);
        luw2.set(AssessAnnualVacation::getStatus, 2);
        vacationMapper.update(null, luw2);

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        AssessAnnualSummaryMapper summaryMapperNew = sqlSession.getMapper(AssessAnnualSummaryMapper.class);
        this.getEvaluationSuggestion(summaryList);

        summaryList.forEach(summaryMapperNew::updateById);

        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();


        return Result.ok("审核通过");
    }

    @Override
    public void stopAssess() {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
        if (currentAssessInfo != null && currentAssessInfo.isAssessing()) {
            currentAssessInfo.setAssessing(false);
            assessCommonApi.updateAssessCurrentYear(currentAssessInfo);

            LambdaUpdateWrapper<AssessAnnualFill> luw = new LambdaUpdateWrapper<>();
            luw.eq(AssessAnnualFill::getCurrentYear, currentAssessInfo.getCurrentYear());
            luw.set(AssessAnnualFill::getStatus, "3");
            annualFillMapper.update(null, luw);

            // 年度考核进度归档
            LambdaQueryWrapper<SysUser> lqw = new LambdaQueryWrapper<>();
            lqw.isNotNull(SysUser::getPwdPlainText);
            lqw.eq(SysUser::getAssess, "annual");
            List<SysUser> users = sysUserService.list(lqw);

            if (users != null && !users.isEmpty()) {

                List<String> departIds = users.stream().map(SysUser::getDepart).collect(Collectors.toList());

                LambdaQueryWrapper<AssessAnnualFill> lqw2 = new LambdaQueryWrapper<>();
                lqw2.in(AssessAnnualFill::getDepart, departIds);
                lqw2.eq(AssessAnnualFill::getCurrentYear, currentAssessInfo.getCurrentYear());

                List<AssessAnnualFill> fills = annualFillMapper.selectList(lqw2);
                // 去除所有fillId
                List<String> fillIds = fills.stream().map(AssessAnnualFill::getId).collect(Collectors.toList());

                LambdaQueryWrapper<AssessAnnualArrange> lqw3 = new LambdaQueryWrapper<>();
                lqw3.in(AssessAnnualArrange::getAnnualFillId, fillIds);
                List<AssessAnnualArrange> arrangeList = arrangeMapper.selectList(lqw3);

                if (arrangeList != null && !arrangeList.isEmpty()) {
                    for (AssessAnnualArrange arrange : arrangeList) {
                        // 找到对应的fill
                        AssessAnnualFill fill = fills.stream().filter(f -> f.getId().equals(arrange.getAnnualFillId())).findFirst().orElse(null);
                        if (fill != null) {
                            // 通过找到的fill的depart找到所有的user
                            List<SysUser> user = users.stream().filter(u -> u.getDepart().equals(fill.getDepart())).collect(Collectors.toList());
                            if (!user.isEmpty()) {
                                arrange.setRecommend(String.valueOf(arrange.getVoteNum() - user.size()));
                                arrangeMapper.updateById(arrange);
                            }
                        }
                    }
                }
            }

            // 删除剩余的匿名账号
            userCommonApi.deleteAllAnonymousAccount("annual");
        }
    }

    @Override
    public void revocationAssess() {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
        if (currentAssessInfo != null && currentAssessInfo.isAssessing()) {
            currentAssessInfo.setAssessing(false);
            String year = currentAssessInfo.getCurrentYear();

            LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
            lqw.eq(AssessAnnualSummary::getCurrentYear, currentAssessInfo.getCurrentYear());
            summaryMapper.delete(lqw);

            LambdaQueryWrapper<AssessAnnualFill> lqw2 = new LambdaQueryWrapper<>();
            lqw2.eq(AssessAnnualFill::getCurrentYear, currentAssessInfo.getCurrentYear());

            List<AssessAnnualFill> fills = annualFillMapper.selectList(lqw2);
            List<String> fillIds = new ArrayList<>();
            for (AssessAnnualFill fill : fills) {
                fillIds.add(fill.getId());
            }
            annualFillMapper.delete(lqw2);

            LambdaQueryWrapper<AssessAnnualArrange> lqw3 = new LambdaQueryWrapper<>();
            lqw3.in(AssessAnnualArrange::getAnnualFillId, fillIds);
            arrangeMapper.delete(lqw3);

            LambdaQueryWrapper<AssessAnnualExcellentNum> lqw4 = new LambdaQueryWrapper<>();
            lqw4.eq(AssessAnnualExcellentNum::getCurrentYear, year);
            excellentNumMapper.delete(lqw4);

            assessCommonApi.revocationCurrentYear(currentAssessInfo);
        }
    }

    @Override
    public void stopDem() {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
        if (currentAssessInfo != null) {

            // 年度考核进度归档
            LambdaQueryWrapper<SysUser> lqw = new LambdaQueryWrapper<>();
            lqw.isNotNull(SysUser::getPwdPlainText);
            lqw.eq(SysUser::getAssess, "annual");
            List<SysUser> users = sysUserService.list(lqw);

            if (users != null && !users.isEmpty()) {

                List<String> departIds = users.stream().map(SysUser::getDepart).collect(Collectors.toList());

                LambdaQueryWrapper<AssessAnnualFill> lqw2 = new LambdaQueryWrapper<>();
                lqw2.in(AssessAnnualFill::getDepart, departIds);
                lqw2.eq(AssessAnnualFill::getCurrentYear, currentAssessInfo.getCurrentYear());

                List<AssessAnnualFill> fills = annualFillMapper.selectList(lqw2);
                // 去除所有fillId
                List<String> fillIds = fills.stream().map(AssessAnnualFill::getId).collect(Collectors.toList());

                LambdaQueryWrapper<AssessAnnualArrange> lqw3 = new LambdaQueryWrapper<>();
                lqw3.in(AssessAnnualArrange::getAnnualFillId, fillIds);
                List<AssessAnnualArrange> arrangeList = arrangeMapper.selectList(lqw3);

                if (arrangeList != null && !arrangeList.isEmpty()) {
                    for (AssessAnnualArrange arrange : arrangeList) {
                        // 找到对应的fill
                        AssessAnnualFill fill = fills.stream().filter(f -> f.getId().equals(arrange.getAnnualFillId())).findFirst().orElse(null);
                        if (fill != null) {
                            // 通过找到的fill的depart找到所有的user
                            List<SysUser> user = users.stream().filter(u -> u.getDepart().equals(fill.getDepart())).collect(Collectors.toList());
                            if (!user.isEmpty()) {
                                arrange.setRecommend(String.valueOf(arrange.getVoteNum() - user.size()));
                                arrangeMapper.updateById(arrange);
                            }
                        }
                    }
                }
            }

            // 删除剩余的匿名账号
            userCommonApi.deleteAllAnonymousAccount("annual");
        }
    }

    @Override
    public Boolean checkDepartFillOver(String year) {
        LambdaQueryWrapper<AssessAnnualFill> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualFill::getCurrentYear, year);
        lqw.ne(AssessAnnualFill::getStatus, "3");
        return annualFillMapper.selectCount(lqw) == 0;
    }

    @Override
    public void getEvaluationSuggestionByBusiness(String departId, String year) {
        LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualSummary::getDepart, departId);
        lqw.eq(AssessAnnualSummary::getCurrentYear, year);
        List<AssessAnnualSummary> summaryList = summaryMapper.selectList(lqw);

        if (summaryList != null && !summaryList.isEmpty()) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
            AssessAnnualSummaryMapper summaryMapperNew = sqlSession.getMapper(AssessAnnualSummaryMapper.class);

            this.getEvaluationSuggestion(summaryList);

            summaryList.forEach(summaryMapperNew::updateById);
            sqlSession.commit();
            sqlSession.clearCache();
            sqlSession.close();
        }
    }

    private void setEvaluation(AssessAnnualSummary summary, String msg) {
        summary.setEvaluation("3");
        String remark = summary.getRemark();
        if (remark == null || remark.isEmpty()) {
            summary.setRemark("不能评优。因" + msg + "；");
        } else {
            summary.setRemark(remark + msg + "；");
        }
    }


}
