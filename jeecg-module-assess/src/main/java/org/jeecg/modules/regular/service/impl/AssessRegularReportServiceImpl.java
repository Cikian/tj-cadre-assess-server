package org.jeecg.modules.regular.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.SysDepartModel;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.regular.dto.RegularFieldDTO;
import org.jeecg.modules.regular.entity.*;
import org.jeecg.modules.regular.mapper.AssessRegularMergeDepartMapper;
import org.jeecg.modules.regular.mapper.AssessRegularReportItemMapper;
import org.jeecg.modules.regular.mapper.AssessRegularReportMapper;
import org.jeecg.modules.regular.service.IAssessRegularMergeDepartService;
import org.jeecg.modules.regular.service.IAssessRegularReportItemService;
import org.jeecg.modules.regular.service.IAssessRegularReportService;
import org.jeecg.modules.regular.service.IAssessRegularResultService;
import org.jeecg.modules.regular.vo.ConflictInfoVO;
import org.jeecg.modules.regular.vo.RegularStartVO;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.AssessAssistConfig;
import org.jeecg.modules.sys.entity.annual.AssessLeaderConfig;
import org.jeecg.modules.sys.entity.business.AssessLeaderDepartConfig;
import org.jeecg.modules.sys.mapper.annual.AssessAssistConfigMapper;
import org.jeecg.modules.sys.mapper.annual.AssessLeaderConfigMapper;
import org.jeecg.modules.sys.mapper.business.AssessLeaderDepartConfigMapper;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.user.UserCommonApi;
import org.jeecg.modules.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 平时考核填报
 * @Author: admin
 * @Date: 2024-08-30
 * @Version: V1.0
 */
@Service
public class AssessRegularReportServiceImpl extends ServiceImpl<AssessRegularReportMapper, AssessRegularReport> implements IAssessRegularReportService {

    @Autowired
    private AssessRegularReportMapper reportMapper;

    @Autowired
    private AssessRegularReportItemMapper itemMapper;

    @Autowired
    private IAssessRegularReportItemService assessRegularReportItemService;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private DepartCommonApi departCommonApi;

    @Autowired
    private UserCommonApi userCommonApi;

    @Autowired
    private AssessCommonApi assessCommonApi;

    @Autowired
    private IAssessRegularMergeDepartService assessRegularMergeDepartService;
    @Autowired
    private IAssessRegularResultService resultServiceImpl;
    @Autowired
    private AssessLeaderDepartConfigMapper departConfigMapper;
    @Autowired
    private AssessAssistConfigMapper assistConfigMapper;
    @Autowired
    private AssessLeaderConfigMapper leaderConfigMapper;
    @Autowired
    private AssessRegularMergeDepartMapper mergeDepartMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMain(AssessRegularReport assessRegularReport, List<AssessRegularReportItem> assessRegularReportItemList) {
        reportMapper.insert(assessRegularReport);
        if (assessRegularReportItemList != null && !assessRegularReportItemList.isEmpty()) {
            for (AssessRegularReportItem entity : assessRegularReportItemList) {
                //外键设置
                entity.setNote(assessRegularReport.getNote());
                itemMapper.insert(entity);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMain(AssessRegularReport assessRegularReport, List<AssessRegularReportItem> assessRegularReportItemList) {
        reportMapper.updateById(assessRegularReport);

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        AssessRegularReportItemMapper mapper = sqlSession.getMapper(AssessRegularReportItemMapper.class);

        List<AssessRegularReportItem> results = new LinkedList<>();
        //2.子表数据重新插入
        if (assessRegularReportItemList != null && !assessRegularReportItemList.isEmpty()) {
            for (AssessRegularReportItem entity : assessRegularReportItemList) {

                AssessRegularReportItem item = assessRegularReportItemService.getById(entity.getId());
                item.setQuarter1(entity.getQuarter1());
                item.setQuarter2(entity.getQuarter2());
                item.setQuarter3(entity.getQuarter3());
                item.setQuarter4(entity.getQuarter4());

                results.add(item);
            }
            results.forEach(mapper::updateById);
        }
        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();
    }

    // 计算提供的列表中"A"评级的数量的辅助方法
    private int calculateCountOfA(List<AssessRegularReportItem> items, String currentQuarter) {
        int count = 0;
        for (AssessRegularReportItem item : items) {
            if (isAInCurrentQuarter(item, currentQuarter)) {
                count++;
            }
        }
        return count;
    }

    // 判断项目在当前季度是否为"A"的辅助方法
    private boolean isAInCurrentQuarter(AssessRegularReportItem item, String currentQuarter) {
        switch (currentQuarter) {
            case "1":
                return "A".equals(item.getQuarter1());
            case "2":
                return "A".equals(item.getQuarter2());
            case "3":
                return "A".equals(item.getQuarter3());
            case "4":
                return "A".equals(item.getQuarter4());
            default:
                return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delMain(String id) {
        itemMapper.deleteByMainId(id);
        reportMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delBatchMain(Collection<? extends Serializable> idList) {
        for (Serializable id : idList) {
            itemMapper.deleteByMainId(id.toString());
            reportMapper.deleteById(id);
        }
    }

    @Override
    public RegularIndexLeader getAllItems() {

        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("regular");

        if (currentAssessInfo != null && currentAssessInfo.isAssessing()) {
            List<RegularFieldDTO> items = reportMapper.getAllItems(currentAssessInfo.getCurrentYear());

            long countStatus3 = items.stream()
                    .filter(item -> "3".equals(item.getStatus()))
                    .count();

            int totalCount = items.size();
            int uncompleted = totalCount - (int) countStatus3;
            double ratio = totalCount > 0 ? countStatus3 / (double) totalCount : 0.0;

            List<SysDepartModel> sysDepartModels = departCommonApi.queryAllDepart();
            Map<String, String> departMap = sysDepartModels.stream()
                    .collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getAlias));

            List<RegularFieldDTO> allItem = items.stream()
                    .filter(item -> !"3".equals(item.getStatus()))
                    .collect(Collectors.toList());

            allItem.forEach(item -> {
                if (item.getDepartmentCode() != null) {
                    String alias = departMap.get(item.getDepartmentCode());
                    item.setDepartmentCode(alias);
                }
            });

            return new RegularIndexLeader(countStatus3, uncompleted, ratio, allItem);
        }

        return null;
    }

    /**
     * 流程开始
     *
     * @param uniqueDepartmentCodes 部门信息
     */
    @Override
    public void startProcess(RegularStartVO regularStartVO, Set<String> uniqueDepartmentCodes) {
        if (!"1".equals(regularStartVO.getCurrentQuarter())){
            // 如果不是第一季度，判断是否有人离退休，如果有，将其delFlag设置为1
            assessRegularReportItemService.deleteRetiree(regularStartVO.getCurrentYear(), regularStartVO.getReportItems());
        }


        // 处理每个不同的处室信息
        for (String departmentCode : uniqueDepartmentCodes) {
            // 是否为本年季度更新
            if (processStarted(regularStartVO.getCurrentYear(), regularStartVO.getCurrentQuarter(), departmentCode)) {
                // 有重复记录，handleDuplicateRecords？
                upDataProcess(regularStartVO, regularStartVO.getCurrentYear(), regularStartVO.getCurrentQuarter(), departmentCode);
            } else {
                // 没有记录，按流程创建记录
                createAssessRegularReport(regularStartVO, departmentCode);
            }
        }
    }



    /**
     * 判断流程是否已经启动
     *
     * @param currentYear    年份
     * @param currentQuarter 月份
     * @param departmentCode 所在处室
     */
    public boolean processStarted(String currentYear, String currentQuarter, String departmentCode) {

        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("regular");
        if (currentAssessInfo != null && Integer.parseInt(currentYear) < Integer.parseInt(currentAssessInfo.getCurrentYear())) {
            throw new JeecgBootException("当前考核已结束！");
        }

        LambdaQueryWrapper<AssessRegularReport> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AssessRegularReport::getCurrentYear, currentYear).eq(AssessRegularReport::getDepartmentCode, departmentCode);

        // 查找符合条件的记录
        AssessRegularReport report = this.getOne(lambdaQueryWrapper);

        if (report != null) {
            // 比较季度
            String reportQuarter = report.getCurrentQuarter();
            if (reportQuarter != null && reportQuarter.compareTo(currentQuarter) >= 0) {
                // 提示当前年度季度已录入
                throw new JeecgBootException("当前年度、季度已录入");
            }
        }
        return report != null;
    }

    @Override
    public void validateReportItems(List<AssessRegularReportItem> reportItems) {
        if (reportItems.isEmpty()) {
            throw new JeecgBootException("Excel为空！");
        }
    }

    @Override
    public Set<String> getUniqueDepartmentCodes(RegularStartVO regularStartVO) {
        return regularStartVO.getReportItems().stream()
                .map(AssessRegularReportItem::getDepartmentCode)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getDepartmentNames(Set<String> uniqueDepartmentCodes) {
        Set<String> departmentNames = new HashSet<>();
        for (String departmentCode : uniqueDepartmentCodes) {
            SysDepart sysDepart = departCommonApi.queryDepartById(departmentCode);
            if (sysDepart != null && sysDepart.getDepartName() != null) {
                departmentNames.add(sysDepart.getDepartName());
            } else {
                throw new JeecgBootException("部门信息不存在：" + departmentCode);
            }
        }
        return departmentNames;
    }

    @Override
    public ConflictInfoVO checkForConflicts(RegularStartVO regularStartVO) {

        String yearToCheck = regularStartVO.getCurrentQuarter().equals("1")
                ? String.valueOf(Integer.parseInt(regularStartVO.getCurrentYear()) - 1)
                : regularStartVO.getCurrentYear();

        return isConflicted(regularStartVO, yearToCheck);
    }

    /**
     * 检查冲突
     *
     * @param currentYear 年度
     */
    public ConflictInfoVO isConflicted(RegularStartVO regularStartVO, String currentYear) {
        // 最终响应对象
        ConflictInfoVO responseVO = new ConflictInfoVO();

        // 冲突项集合
        List<ConflictItem> conflictItems = new ArrayList<>();

        // 当前年度所有子表对象
        List<AssessRegularReportItem> assessRegularReportItems = itemMapper.itemList(currentYear);

        // 冲突判断
        for (AssessRegularReportItem reportItem : regularStartVO.getReportItems()) {
            findConflicts(reportItem, assessRegularReportItems, conflictItems);
        }

        // 如果有冲突项，设置返回值
        if (!conflictItems.isEmpty()) {
            // 获取报表项
            List<AssessRegularReportItem> reportItems = regularStartVO.getReportItems();
            // 遍历每个报表项并设置性别、部门别名
            for (AssessRegularReportItem item : reportItems) {
                SysDepart sysDepart = departCommonApi.queryDepartById(item.getDepartmentCode());
                item.setSexAlias(item.getSex() == 1 ? "男" : "女");
                item.setDepartmentCodeAlias(sysDepart.getAlias());
            }
            responseVO.setIncomingItems(regularStartVO.getReportItems());
            responseVO.setConflictItems(conflictItems);
            return responseVO;
        }
        return null; // 没有冲突时返回 null
    }

    // 子表冲突项
    private void findConflicts(AssessRegularReportItem reportItem, List<AssessRegularReportItem> assessRegularReportItems, List<ConflictItem> conflictItems) {
        assessRegularReportItems.stream()
                .filter(assessItem -> assessItem.getName().equals(reportItem.getName())
                        && assessItem.getSex().equals(reportItem.getSex())
                        && !assessItem.getBirthDate().equals(reportItem.getBirthDate()))
                .forEach(assessItem -> {
                    assessItem.setSexAlias(assessItem.getSex() == 1 ? "男" : "女");
                    ConflictItem conflictItem = new ConflictItem();
                    conflictItem.setIncoming(reportItem);
                    conflictItem.setConflicts(Collections.singletonList(assessItem));
                    conflictItems.add(conflictItem);
                });
    }

    /**
     * 合并冲突
     *
     * @param conflictItems 冲突项
     */
    @Override
    public void mergeConflicts(List<String> conflictItems) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        AssessRegularReportItemMapper mapper = sqlSession.getMapper(AssessRegularReportItemMapper.class);
        // 所有更新项
        List<AssessRegularReportItem> assessRegularReportItems = new ArrayList<>();
        for (String conflictItem : conflictItems) {
            String[] items = conflictItem.split("@");
            String[] uniqueValue = items[0].split(" ");
            String name = uniqueValue[0];
            String gender = uniqueValue[1];
            String genderNum = "男".equals(gender) ? "1" : "2";
            String birthDate = uniqueValue[2];
            String departmentCode = items[1];
            List<AssessRegularReportItem> reportItems = itemMapper.conflictList(name, genderNum);
            for (AssessRegularReportItem item : reportItems) {
                item.setBirthDate(birthDate);
                item.setDepartmentCode(departmentCode);
                String hashId = CommonUtils.hashId(name, gender, birthDate);
                item.setHashId(hashId);
                assessRegularReportItems.add(item);
            }
        }
        assessRegularReportItems.forEach(mapper::updateById);

        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();
    }

    /**
     * 根据存在的主表更新子表内容
     *
     * @param regularStartVO VO实体
     * @return
     */
    private void upDataProcess(RegularStartVO regularStartVO, String currentYear, String currentQuarter, String departmentCode) {
        // 获取与当前处室值重复记录的id
        LambdaQueryWrapper<AssessRegularReport> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AssessRegularReport::getCurrentYear, currentYear).eq(AssessRegularReport::getDepartmentCode, departmentCode);
        AssessRegularReport assessRegularReport = this.getOne(lambdaQueryWrapper);
        assessRegularReport.setAssessName(regularStartVO.getAssessName());
        assessRegularReport.setCurrentQuarter(currentQuarter);
        assessRegularReport.setStatus("1");
        assessRegularReport.setWarnStatus("1");
        assessRegularReport.setDeadline(regularStartVO.getDeadline());
        reportMapper.updateById(assessRegularReport);
        // 存储符合条件的 AssessRegularReportItem 对象
        List<AssessRegularReportItem> matchingItems = new ArrayList<>();

        String sex = null;
        // 新增子表数据集合
        for (AssessRegularReportItem item : regularStartVO.getReportItems()) {
            // 更新原记录
            if (item.getDepartmentCode().equals(departmentCode)) {
                item.setCurrentYear(currentYear);
                item.setMainId(assessRegularReport.getId());
                sex = item.getSex() == 1 ? "男" : "女";
                item.setHashId(CommonUtils.hashId(item.getName(), sex, item.getBirthDate()));
                // 将符合条件的对象添加到集合中
                matchingItems.add(item);
            }
        }
        assessRegularReportItemService.upDataAssessRegularReportItem(matchingItems);
        updateRegularRecord(regularStartVO);
    }

    private void createAssessRegularReport(RegularStartVO regularStartVO, String departmentCode) {

        AssessRegularReport assessRegularReport = new AssessRegularReport();
        // 设置主表值
        assessRegularReport.setAssessName(regularStartVO.getAssessName());
        assessRegularReport.setCurrentYear(regularStartVO.getCurrentYear());
        assessRegularReport.setCurrentQuarter(regularStartVO.getCurrentQuarter());
        assessRegularReport.setDeadline(regularStartVO.getDeadline());
        assessRegularReport.setId(String.valueOf(IdWorker.getId()));
        assessRegularReport.setDepartmentCode(departmentCode);
        assessRegularReport.setWarnStatus("1");
        assessRegularReport.setStatus("1");
        assessRegularReport.setDeadline(regularStartVO.getDeadline());

        // 新增主表记录
        this.save(assessRegularReport);

        List<AssessRegularReportItem> matchingItems = new ArrayList<>();
        // 提取 reportItems 以避免重复调用
        List<AssessRegularReportItem> reportItems = regularStartVO.getReportItems();
        for (AssessRegularReportItem item : reportItems) {
            if (item.getDepartmentCode().equals(departmentCode)) {
                item.setCurrentYear(assessRegularReport.getCurrentYear());
                item.setMainId(assessRegularReport.getId());
                matchingItems.add(item);
            }
        }
        assessRegularReportItemService.addAssessRegularReportItem(matchingItems);
        updateRegularRecord(regularStartVO);
        departCommonApi.saveAssessYear(regularStartVO.getCurrentYear());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCurrentProcess(RegularStartVO regularStartVO) {
        LambdaQueryWrapper<AssessRegularReport> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AssessRegularReport::getCurrentYear, regularStartVO.getCurrentYear()).eq(AssessRegularReport::getCurrentQuarter, regularStartVO.getCurrentQuarter());
        List<AssessRegularReport> list = this.list(lambdaQueryWrapper);
        for (AssessRegularReport item :
                list) {
            String id = item.getId();
            itemMapper.deleteByMainId(id);
            reportMapper.deleteById(id);
        }
    }

    @Override
    public RegularIndexEmployee[] getPartial(String departName, AssessCurrentAssess assessInfo) {

        if (assessInfo == null || StringUtils.isBlank(assessInfo.getCurrentYear()) || !assessInfo.isAssessing()) {
            return new RegularIndexEmployee[]{new RegularIndexEmployee("当前无正在进行的考核")};
        }

        // 创建查询条件，匹配 department_code 字段
        QueryWrapper<AssessRegularReport> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("department_code", departName).eq("current_year", assessInfo.getCurrentYear());

        // 根据条件查询报告
        List<AssessRegularReport> reports = reportMapper.selectList(queryWrapper);

        if (reports.isEmpty()) {
            return null;
        }
        String status = reports.get(0).getStatus();
        String statusMessage;

        // 根据 status 设置状态信息
        switch (status) {
            case "1":
                statusMessage = "填报中";
                break;
            case "2":
                statusMessage = "待审核";
                break;
            case "3":
                statusMessage = "填报完成";
                break;
            case "4":
                statusMessage = "已退回";
                break;
            default:
                statusMessage = "当前无正在进行的考核"; // 未发起
                break;
        }
        // 创建并返回结果数组
        return new RegularIndexEmployee[]{new RegularIndexEmployee(statusMessage)};
    }

    @Override
    public void stopAssess() {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("regular");
        if (currentAssessInfo != null && currentAssessInfo.isAssessing()) {
            currentAssessInfo.setAssessing(false);
            assessCommonApi.updateAssessCurrentYear(currentAssessInfo);

            LambdaUpdateWrapper<AssessRegularReport> luw = new LambdaUpdateWrapper<>();
            luw.eq(AssessRegularReport::getCurrentYear, currentAssessInfo.getCurrentYear());
            luw.set(AssessRegularReport::getStatus, "3");
            reportMapper.update(null, luw);
        }
    }

    public void revocationAssess() {
        // 获取当前评估信息，参数"regular"表示定期评估
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("regular");
        // 检查当前评估信息是否存在且正在评估
        if (currentAssessInfo != null && currentAssessInfo.isAssessing()) {
            // 将当前评估状态设置为不在评估中
            currentAssessInfo.setAssessing(false);
            String quarter = currentAssessInfo.getQuarter();
            // 转为int
            int currentQuarter = Integer.parseInt(quarter);

            if ("1".equals(currentAssessInfo.getQuarter())) {
                LambdaQueryWrapper<AssessRegularReport> lqw = new LambdaQueryWrapper<>();
                lqw.eq(AssessRegularReport::getCurrentYear, currentAssessInfo.getCurrentYear());
                LambdaQueryWrapper<AssessRegularReportItem> lqw2 = new LambdaQueryWrapper<>();
                lqw2.eq(AssessRegularReportItem::getCurrentYear, currentAssessInfo.getCurrentYear());

                reportMapper.delete(lqw);
                itemMapper.delete(lqw2);
                resultServiceImpl.deleteByCurrentYear(currentAssessInfo.getCurrentYear());
            } else {
                LambdaUpdateWrapper<AssessRegularReport> luw = new LambdaUpdateWrapper<>();
                luw.eq(AssessRegularReport::getCurrentYear, currentAssessInfo.getCurrentYear());
                luw.set(AssessRegularReport::getCurrentQuarter, String.valueOf(currentQuarter - 1));
                reportMapper.update(null, luw);

                LambdaUpdateWrapper<AssessRegularReportItem> luw2 = new LambdaUpdateWrapper<>();
                luw2.eq(AssessRegularReportItem::getCurrentYear, currentAssessInfo.getCurrentYear());
                luw2.set(AssessRegularReportItem::getQuarter2, null);
                luw2.set(AssessRegularReportItem::getQuarter3, null);
                luw2.set(AssessRegularReportItem::getQuarter4, null);
                itemMapper.update(null, luw2);
            }
            // 调用API撤销当前年份
            assessCommonApi.revocationCurrentYear(currentAssessInfo);
        }
    }

    /**
     * 初始化处室被考核人信息
     *
     * @param assessRegularReport
     */
    private void initAssessPerson(AssessRegularReport assessRegularReport) {
        List<AssessRegularReportItem> list = new ArrayList<>();
        List<SysUser> users = userCommonApi.getUserByOrgId(assessRegularReport.getDepartmentCode());
        for (SysUser sysUser :
                users) {
            AssessRegularReportItem assessRegularReportItem = initModel(assessRegularReport, sysUser);
            list.add(assessRegularReportItem);
        }
        assessRegularReportItemService.saveOrUpdateBatch(list);
    }

    private AssessRegularReportItem initModel(AssessRegularReport assessRegularReport, SysUser sysUser) {
        AssessRegularReportItem assessRegularReportItem = new AssessRegularReportItem();
        assessRegularReportItem.setName(sysUser.getId());
        assessRegularReportItem.setSex(sysUser.getSex());
        assessRegularReportItem.setPhone(sysUser.getPhone());
        assessRegularReportItem.setPost(sysUser.getPost());
        assessRegularReportItem.setIsLeader("0");
        //过滤掉正职、副职、局管总师、二巡
        List<String> roles = Arrays.asList("commissioner_chief", "commissioner_deputy", "master_engineer", "second_inspector");
        if (userCommonApi.hasRole(sysUser.getUsername(), roles)) {
            assessRegularReportItem.setIsLeader("1");
        }
        assessRegularReportItem.setDepartmentCode(assessRegularReport.getDepartmentCode());
        assessRegularReportItem.setCurrentYear(assessRegularReport.getCurrentYear());
        assessRegularReportItem.setMainId(assessRegularReport.getId());
        return assessRegularReportItem;
    }

    public void updateRegularRecord(RegularStartVO regularStartVO) {

        LocalDate today = LocalDate.now();

        // 更新平时考核装填
        AssessCurrentAssess assess = new AssessCurrentAssess();
        assess.setAssessName(regularStartVO.getAssessName());
        assess.setAssess("regular");
        assess.setCurrentYear(regularStartVO.getCurrentYear());
        assess.setQuarter(regularStartVO.getCurrentQuarter());
        assess.setStartDate(Date.valueOf(today));
        assess.setDeadline(regularStartVO.getDeadline());
        assess.setAssessing(true);
        assessCommonApi.updateAssessCurrentYear(assess);
    }

    @Override
    public List<Map<String, Object>> getDetailItem(String type) {

        List<Map<String, Object>> list = new ArrayList<>();

        AssessCurrentAssess regular = assessCommonApi.getCurrentAssessInfo("regular");
        if (regular == null) {
            throw new JeecgBootException("当前没有考核");
        }

        LambdaQueryWrapper<AssessRegularReport> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessRegularReport::getCurrentYear, regular.getCurrentYear());
        lqw.eq(AssessRegularReport::getCurrentQuarter, regular.getQuarter());
        List<AssessRegularReport> reports = reportMapper.selectList(lqw);
        // 将Id转为List
        List<String> ids = reports.stream().map(AssessRegularReport::getId).collect(Collectors.toList());


        LambdaQueryWrapper<AssessRegularReportItem> lqw2 = new LambdaQueryWrapper<>();
        lqw2.in(AssessRegularReportItem::getMainId, ids);
        List<AssessRegularReportItem> items = itemMapper.selectList(lqw2);

        List<SysUser> allLeader = userCommonApi.getAllLeader();
        Map<String, String> leaderMap = allLeader.stream().collect(Collectors.toMap(SysUser::getId, SysUser::getRealname));

        List<AssessAssistConfig> assessAssistConfigs = assistConfigMapper.selectList(null);
        List<AssessLeaderConfig> assessLeaderConfigs = leaderConfigMapper.selectList(null);

        // 将hashId取出
        List<String> hashIds = assessAssistConfigs.stream().map(AssessAssistConfig::getHashId).collect(Collectors.toList());
        hashIds.addAll(assessLeaderConfigs.stream().map(AssessLeaderConfig::getHashId).collect(Collectors.toList()));

        int total = 0;


        if ("leader".equals(type)) {
            items = items.stream().filter(item -> "1".equals(item.getIsLeader())).collect(Collectors.toList());
            // 将hashId转为List
            List<String> allHashIds = items.stream().map(AssessRegularReportItem::getHashId).collect(Collectors.toList());
            // 将items中hashId在hashids中的删除
            items = items.stream().filter(item -> !hashIds.contains(item.getHashId())).collect(Collectors.toList());

            List<AssessLeaderDepartConfig> configs = departConfigMapper.selectList(null);
            for (AssessLeaderDepartConfig config : configs) {
                Map<String, Object> map = new HashMap<>();
                String departId = config.getDepartId();
                List<AssessRegularReportItem> collect = items.stream().filter(item -> "1".equals(item.getIsLeader()) && departId.contains(item.getDepartmentCode())).collect(Collectors.toList());

                int i = 0;
                for (AssessLeaderConfig c : assessLeaderConfigs) {
                    if (c.getLeader().equals(config.getLeaderId()) && allHashIds.contains(c.getHashId())) i++;
                }

                for (AssessAssistConfig c : assessAssistConfigs) {
                    if (c.getLeader().contains(config.getLeaderId()) && allHashIds.contains(c.getHashId())) i++;
                }

                map.put("name", leaderMap.get(config.getLeaderId()));
                int num = (int) ((collect.size() + i) * 0.4);
                map.put("num", num);
                total += num;
                list.add(map);
            }
        } else {
            List<AssessRegularMergeDepart> merge = mergeDepartMapper.selectList(null);
            // 将reportDepart为Y的departId取出转为List
            List<String> mergeReport = merge.stream().filter(item -> "Y".equals(item.getReportDepart())).map(AssessRegularMergeDepart::getDepart).collect(Collectors.toList());
            List<String> mergeNonReport = merge.stream().filter(item -> "N".equals(item.getReportDepart())).map(AssessRegularMergeDepart::getDepart).collect(Collectors.toList());
            // 将departId转为List

            List<SysDepartModel> allSysDepart = departCommonApi.getAllSysDepart();
            // 过滤出type为1的
            allSysDepart = allSysDepart.stream().filter(item -> "1".equals(item.getDepartType())).collect(Collectors.toList());
            int mergeNum = 0;
            String mergeName = "";

            for (SysDepartModel sysDepartModel : allSysDepart) {
                Map<String, Object> map = new HashMap<>();
                List<AssessRegularReportItem> collect = items.stream().filter(item -> "0".equals(item.getIsLeader()) && sysDepartModel.getId().equals(item.getDepartmentCode())).collect(Collectors.toList());

                if (mergeReport.contains(sysDepartModel.getId())) {
                    mergeNum += collect.size();
                    mergeName = sysDepartModel.getDepartName();
                    continue;
                }

                if (mergeNonReport.contains(sysDepartModel.getId())) {
                    map.put("name", sysDepartModel.getDepartName());
                    map.put("num", 0);
                    mergeNum += collect.size();
                } else {
                    map.put("name", sysDepartModel.getDepartName());
                    int num = Math.max(((int) (collect.size() * 0.4)), 1);
                    map.put("num", num);
                    total += num;
                }

                list.add(map);
            }
            if (!mergeReport.isEmpty()) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", mergeName);
                int num = Math.max(((int) (mergeNum * 0.4)), 1);
                map.put("num", num);
                total += num;
                list.add(map);
            }

        }

        Map<String, Object> map = new HashMap<>();
        map.put("name", "合计");
        map.put("num", total);
        list.add(map);


        return list;
    }
}
