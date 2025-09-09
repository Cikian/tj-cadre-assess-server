package org.jeecg.modules.regular.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jeecg.modules.depart.dto.DepartLeadersRegularGradeDTO;
import org.jeecg.modules.regular.entity.AssessRegularMergeDepart;
import org.jeecg.modules.regular.entity.AssessRegularReport;
import org.jeecg.modules.regular.entity.AssessRegularReportItem;
import org.jeecg.modules.regular.mapper.AssessRegularReportItemMapper;
import org.jeecg.modules.regular.mapper.AssessRegularReportMapper;
import org.jeecg.modules.regular.service.IAssessRegularMergeDepartService;
import org.jeecg.modules.regular.service.IAssessRegularReportItemService;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.AssessAnnualSummary;
import org.jeecg.modules.sys.entity.business.AssessLeaderDepartConfig;
import org.jeecg.modules.sys.mapper.annual.AssessAnnualSummaryMapper;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: 平时考核填报明细表
 * @Author: admin
 * @Date: 2024-08-30
 * @Version: V1.0
 */
@Service
public class AssessRegularReportItemServiceImpl extends ServiceImpl<AssessRegularReportItemMapper, AssessRegularReportItem> implements IAssessRegularReportItemService {

    private static final String NOT_FOUND_MESSAGE = "未找到对应的评估定期报告项";

    @Autowired
    private AssessRegularReportMapper assessRegularReportMapper;

    @Autowired
    private AssessRegularReportItemMapper assessRegularReportItemMapper;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private AssessAnnualSummaryMapper annualSummaryMapper;

    @Autowired
    private IAssessRegularMergeDepartService assessRegularMergeDepartService;
    @Autowired
    private AssessCommonApi assessCommonApi;

    @Override
    public List<AssessRegularReportItem> selectByMainId(String mainId) {
        List<AssessRegularReportItem> list= assessRegularReportItemMapper.selectByMainId(mainId);
        AssessRegularReport assessRegularReport= assessRegularReportMapper.selectById(mainId);
        String departId=assessRegularReport.getDepartmentCode();
        LambdaQueryWrapper<AssessRegularMergeDepart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(AssessRegularMergeDepart::getDepart,departId).eq(AssessRegularMergeDepart::getReportDepart,"Y");
        if (assessRegularMergeDepartService.count(queryWrapper)>0){
            List<AssessRegularReportItem> OtherMergeDepartData =getOtherMergeDepartData(assessRegularReport);
            list.addAll(OtherMergeDepartData);
        }
        return list;
    }

    @Override
    public void addAssessRegularReportItem(AssessRegularReportItem item) {
        if(!isYearItem(item)){
            // 若ID为默认值或空值
            if (item.getId() == null || item.getId().isEmpty()) {
                item.setId(IdWorker.getIdStr());
            }
            String sex = item.getSex() == 1 ? "男" : "女";
            item.setHashId(CommonUtils.hashId(item.getName(), sex, item.getBirthDate()));
            assessRegularReportItemMapper.insert(item);
        }

    }

    @Override
    public void addAssessRegularReportItem(List<AssessRegularReportItem> item) {

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        AssessRegularReportItemMapper mapper = sqlSession.getMapper(AssessRegularReportItemMapper.class);

        // 遍历集合，为每个对象设置相关属性
        item.forEach(i -> {
            // 若 ID 为默认值或空值
            if (i.getId() == null || i.getId().isEmpty()) {
                i.setId(IdWorker.getIdStr());
            }
            String sex = i.getSex() == 1? "男" : "女";
            i.setHashId(CommonUtils.hashId(i.getName(), sex, i.getBirthDate()));
        });

        item.forEach(mapper::insert);

        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();
    }

    /**
     * 解决子数据重复问题
     */
    @Override
    public void upDataAssessRegularReportItem(List<AssessRegularReportItem> upDataDuplicateItem) {
        for (AssessRegularReportItem item : upDataDuplicateItem) {
            // 判断是否存在之前的记录
            if (!isDuplicateItem(item)) {
                addAssessRegularReportItem(item);
                // 是否更换部门
                isDepartmentUpdated(item);
            }
        }
    }

    /**
     * 检查是否存在重复的记录
     */
    private boolean isDuplicateItem(AssessRegularReportItem item) {
        return this.count(new LambdaQueryWrapper<AssessRegularReportItem>()
                .eq(AssessRegularReportItem::getCurrentYear, item.getCurrentYear())
                .eq(AssessRegularReportItem::getHashId, item.getHashId())
                .eq(AssessRegularReportItem::getMainId, item.getMainId())) > 0;
    }

    /**
     * 检查本年是否存在重复的记录
     */
    private boolean isYearItem(AssessRegularReportItem item) {
        return this.count(new LambdaQueryWrapper<AssessRegularReportItem>()
                .eq(AssessRegularReportItem::getCurrentYear, item.getCurrentYear())
                .eq(AssessRegularReportItem::getHashId, item.getHashId())) > 0;
    }

    /**
     * 检查部门是否更新
     */
    private void isDepartmentUpdated(AssessRegularReportItem item) {
        // 查询与 item 的 getHashId 相同的记录集合
        List<AssessRegularReportItem> existingItems = assessRegularReportItemMapper.selectList(new LambdaQueryWrapper<AssessRegularReportItem>().eq(AssessRegularReportItem::getHashId, item.getHashId()));

        if (!existingItems.isEmpty()) {
            // 遍历集合，将 departmentCode 的值替换为 item 的 departmentCode
            for (AssessRegularReportItem existingItem : existingItems) {
                existingItem.setDepartmentCode(item.getDepartmentCode());

                // 根据 getCurrentYear 和最新的 departmentCode 查询 AssessRegularReport 主表的信息，并将 id 赋值给集合对象的 MainId
                AssessRegularReport assessRegularReport = assessRegularReportMapper.selectOne(new LambdaQueryWrapper<AssessRegularReport>().eq(AssessRegularReport::getCurrentYear, existingItem.getCurrentYear()).eq(AssessRegularReport::getDepartmentCode, existingItem.getDepartmentCode()));
                existingItem.setMainId(assessRegularReport.getId());

                // 更新集合对象的信息
                assessRegularReportItemMapper.updateById(existingItem);


            }
        }

    }

    /**
     * 更新现有项目的季度数据
     */
    private void updateExistingItem(AssessRegularReportItem removalDuplicateItem, String currentQuarter) {
        String id = getAssessRegularReportItemID(removalDuplicateItem);
        AssessRegularReportItem assessRegularReportItem = Optional.ofNullable(assessRegularReportItemMapper.selectById(id))
                .orElseThrow(() -> new RuntimeException(NOT_FOUND_MESSAGE));
        switch (currentQuarter) {
            case "1":
                assessRegularReportItem.setQuarter1(removalDuplicateItem.getQuarter1());
                break;
            case "2":
                assessRegularReportItem.setQuarter2(removalDuplicateItem.getQuarter2());
                break;
            case "3":
                assessRegularReportItem.setQuarter3(removalDuplicateItem.getQuarter3());
                break;
            case "4":
                assessRegularReportItem.setQuarter4(removalDuplicateItem.getQuarter4());
                break;
        }
        assessRegularReportItemMapper.updateById(assessRegularReportItem);
    }

    /**
     * 获取现有记录的 ID
     */
    private String getAssessRegularReportItemID(AssessRegularReportItem removalDuplicateItem) {
        LambdaQueryWrapper<AssessRegularReportItem> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AssessRegularReportItem::getCurrentYear, removalDuplicateItem.getCurrentYear())
                .eq(AssessRegularReportItem::getHashId, removalDuplicateItem.getHashId())
                .eq(AssessRegularReportItem::getMainId, removalDuplicateItem.getMainId());

        AssessRegularReportItem assessRegularReportItem = this.getOne(lambdaQueryWrapper);
        if (assessRegularReportItem == null) {
            throw new RuntimeException(NOT_FOUND_MESSAGE);
        }
        return assessRegularReportItem.getId();
    }

    @Override
    public List<DepartLeadersRegularGradeDTO> queryDepartLeadersRegularGrade(String departId, String year) {
        LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualSummary::getCurrentYear, year);
        lqw.eq(AssessAnnualSummary::getDepart, departId);
        lqw.ne(AssessAnnualSummary::getType, "group");
        List<AssessAnnualSummary> assessAnnualSummaries = annualSummaryMapper.selectList(lqw);

        LambdaQueryWrapper<AssessRegularReportItem> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(AssessRegularReportItem::getCurrentYear, year);
        lqw2.eq(AssessRegularReportItem::getDepartmentCode, departId);
        List<AssessRegularReportItem> assessRegularReportItems = assessRegularReportItemMapper.selectList(lqw2);
        Map<String, AssessRegularReportItem> regularItemMap = assessRegularReportItems.stream().collect(Collectors.toMap(AssessRegularReportItem::getHashId, Function.identity()));

        List<DepartLeadersRegularGradeDTO> regularGradeDTOS = new ArrayList<>();

        for (AssessAnnualSummary assessAnnualSummary : assessAnnualSummaries) {
            AssessRegularReportItem regular = regularItemMap.get(assessAnnualSummary.getHashId());
            if (regular == null) {
                continue;
            }

            DepartLeadersRegularGradeDTO dto = new DepartLeadersRegularGradeDTO();
            dto.setHashId(assessAnnualSummary.getHashId());
            dto.setRealname(assessAnnualSummary.getPerson());
            dto.setRoleCode(regular.getPositionType());
            dto.setRoleName(regular.getPositionType());
            dto.setQuarter1(regular.getQuarter1());
            dto.setQuarter2(regular.getQuarter2());
            dto.setQuarter3(regular.getQuarter3());
            dto.setQuarter4(regular.getQuarter4());

            regularGradeDTOS.add(dto);
        }


        for (DepartLeadersRegularGradeDTO regularGradeDTO : regularGradeDTOS) {
            if ("正职".equals(regularGradeDTO.getRoleCode())) {
                regularGradeDTO.setType("chief");
            } else if ("副职".equals(regularGradeDTO.getRoleCode())) {
                regularGradeDTO.setType("deputy");
            }
        }

        return regularGradeDTOS;
    }

    @Override
    public void resetStatus(String year) {
        LambdaUpdateWrapper<AssessRegularReportItem> luw = new LambdaUpdateWrapper<>();
        luw.eq(AssessRegularReportItem::getCurrentYear, year);
        luw.set(AssessRegularReportItem::getStatus, "1");
        assessRegularReportItemMapper.update(null, luw);
    }

    private List<AssessRegularReportItem> getOtherMergeDepartData(AssessRegularReport assessRegularReport){
        LambdaQueryWrapper<AssessRegularMergeDepart> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AssessRegularMergeDepart::getReportDepart,"N");
        List<AssessRegularMergeDepart> mergeDepartList=assessRegularMergeDepartService.list(lambdaQueryWrapper);

        List<AssessRegularReportItem> result=new LinkedList<>();
        for (AssessRegularMergeDepart assessRegularMergeDepart:
        mergeDepartList) {
            String departCode=assessRegularMergeDepart.getDepart();
            LambdaQueryWrapper<AssessRegularReport> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(AssessRegularReport::getDepartmentCode,departCode).eq(AssessRegularReport::getCurrentYear,assessRegularReport.getCurrentYear()).eq(AssessRegularReport::getCurrentQuarter,assessRegularReport.getCurrentQuarter());
            List<AssessRegularReport> regularReports=assessRegularReportMapper.selectList(queryWrapper);
            if (regularReports.size()>0){
                AssessRegularReport assessRegularReport1=regularReports.get(0);
                List<AssessRegularReportItem> list= assessRegularReportItemMapper.selectByMainId(assessRegularReport1.getId());
                result.addAll(list);
            }
        }
        return result;
    }

    @Override
    public JSONObject getAssessingStatus(SysUser user, List<AssessCurrentAssess> assessingList, JSONObject res) {
        int count = 0;
        boolean assessing = false;

        String userId = user.getId();

        // 获取领导分管处室配置
        AssessLeaderDepartConfig config = assessCommonApi.getAssessUnitByLeader(userId);
        if (config != null) {
            List<String> departIds = Arrays.asList(config.getDepartId().split(","));

            for (AssessCurrentAssess assess : assessingList) {
                if ("regular".equals(assess.getAssess())) {
                    LambdaQueryWrapper<AssessRegularReportItem> lqw = new LambdaQueryWrapper<>();
                    lqw.eq(AssessRegularReportItem::getCurrentYear, assess.getCurrentYear());
                    lqw.eq(AssessRegularReportItem::getStatus, "1");
                    lqw.eq(AssessRegularReportItem::getIsLeader, "1");
                    lqw.in(AssessRegularReportItem::getDepartmentCode, departIds);
                    List<AssessRegularReportItem> items = assessRegularReportItemMapper.selectList(lqw);
                    if (items != null && !items.isEmpty()) {
                        count++;
                        assessing = true;
                    }
                }
            }
        }

        res.put("status", assessing);
        res.put("count", count);

        return res;
    }

    public void deleteRetiree(String currentYear, List<AssessRegularReportItem> reportItems) {
        // 1. 查询当前年份所有记录
        LambdaQueryWrapper<AssessRegularReportItem> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessRegularReportItem::getCurrentYear, currentYear);
        List<AssessRegularReportItem> allItems = assessRegularReportItemMapper.selectList(lqw);

        // 2. 构建需保留的记录的联合键集合（内存去重）
        Set<String> keepKeys = reportItems.stream()
                .map(item -> CommonUtils.hashId(item.getName(), item.getSex() == 1 ? "男" : "女", item.getBirthDate()))
                .collect(Collectors.toSet());

        // 3. 筛选需要删除的ID（利用HashSet O(1)查找）
        List<String> idsToDelete = allItems.stream()
                .filter(item -> !keepKeys.contains(CommonUtils.hashId(item.getName(), item.getSex() == 1 ? "男" : "女", item.getBirthDate())))
                .map(AssessRegularReportItem::getId)
                .collect(Collectors.toList());

        LambdaUpdateWrapper<AssessRegularReportItem> luw = new LambdaUpdateWrapper<>();
        luw.in(AssessRegularReportItem::getId, idsToDelete);
        luw.set(AssessRegularReportItem::getDelFlag, 1);
        assessRegularReportItemMapper.update(null, luw);
    }


}
