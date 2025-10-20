package org.jeecg.modules.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Synchronized;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.system.vo.SysDepartModel;
import org.jeecg.modules.business.entity.BusinessIndexEmployee;
import org.jeecg.modules.business.entity.BusinessIndexLeader;
import org.jeecg.modules.business.service.IAssessBusinessDepartFillService;
import org.jeecg.modules.business.service.IAssessBusinessGradeService;
import org.jeecg.modules.business.service.IYearCodeService;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.regular.entity.AssessRegularReport;
import org.jeecg.modules.regular.entity.AssessRegularReportItem;
import org.jeecg.modules.regular.entity.RegularIndexEmployee;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.dto.AssessBusinessDepartFillDTO;
import org.jeecg.modules.sys.dto.BusinessFieldDTO;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.business.*;
import org.jeecg.modules.sys.mapper.business.*;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.entity.SysUserRole;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.system.service.ISysRoleService;
import org.jeecg.modules.system.service.ISysUserDepartService;
import org.jeecg.modules.system.service.ISysUserRoleService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 业务考核填报列表
 * @Author: jeecg-boot
 * @Date: 2024-08-22
 * @Version: V1.0
 */
@Service
public class AssessBusinessDepartFillServiceImpl extends ServiceImpl<AssessBusinessDepartFillMapper, AssessBusinessDepartFill> implements IAssessBusinessDepartFillService {

    @Autowired
    private AssessBusinessDepartFillMapper departFillMapper;
    @Autowired
    private AssessBusinessFillItemMapper fillItemMapper;
    @Autowired
    private AssessBusinessDepartmentContactMapper contactMapper;
    @Autowired
    private ISysUserDepartService sysUserDepartService;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private ISysUserRoleService sysUserRoleService;
    @Autowired
    private ISysRoleService sysRoleService;
    @Autowired
    private IYearCodeService yearCodeService;
    @Autowired
    private IAssessBusinessGradeService gradeService;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private DepartCommonApi departCommonApi;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private AssessBusinessCommendMapper commendMapper;
    @Autowired
    private AssessBusinessDenounceMapper denounceMapper;
    @Autowired
    private AssessBusinessGradeMapper gradeMapper;
    @Autowired
    private AssessLeaderDepartConfigMapper departConfigMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMain(AssessBusinessDepartFill assessBusinessDepartFill, List<AssessBusinessFillItem> assessBusinessFillItemList) {
        departFillMapper.insert(assessBusinessDepartFill);
        if (assessBusinessFillItemList != null && assessBusinessFillItemList.size() > 0) {
            for (AssessBusinessFillItem entity : assessBusinessFillItemList) {
                // 外键设置
                entity.setFillId(assessBusinessDepartFill.getId());
                fillItemMapper.insert(entity);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateMain(AssessBusinessDepartFill assessBusinessDepartFill, List<AssessBusinessFillItem> assessBusinessFillItemList) {
        // 判断当前考核是否已经审核通过
        if (assessBusinessDepartFill.getStatus() == 3) {
            return Result.error("审核已通过，不可修改!");
        }

        List<AssessBusinessFillItem> oldItem = fillItemMapper.selectByMainId(assessBusinessDepartFill.getId());


        // 将assessBusinessFillItemList和oldItem合并，合并分数和原因。以及判断是否有未填写的分数以及原因
        for (AssessBusinessFillItem entity : assessBusinessFillItemList) {
            AssessBusinessFillItem old = oldItem.stream().filter(item -> item.getId().equals(entity.getId())).findFirst().get();
            old.setScore(entity.getScore());
            old.setReasonOfDeduction(entity.getReasonOfDeduction());

            if (old.isMustFill() && (old.getScore() == null || old.getScore().compareTo(new BigDecimal(0)) <= 0)) {
                return Result.error("您存在必须考核处室（单位）未填写分数，请核对后重新提交！");
            }
            if (old.getScore() != null && old.getScore().compareTo(new BigDecimal(100)) < 0) {
                if (old.getReasonOfDeduction() == null || "".equals(old.getReasonOfDeduction().trim()) || entity.getReasonOfDeduction().isEmpty()) {
                    return Result.error("分数小于100分时，必须填写扣分原因，请核对后重新提交！");
                }
            }
        }

        // 更新主表
        assessBusinessDepartFill.setStatus(2);
        departFillMapper.updateById(assessBusinessDepartFill);
        // 1.先删除子表数据
//        fillItemMapper.deleteByMainId(assessBusinessDepartFill.getId());

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        AssessBusinessFillItemMapper fillItemMapperNew = sqlSession.getMapper(AssessBusinessFillItemMapper.class);

//        List<AssessBusinessFillItem> itemList = new ArrayList<>();
//        // 2.子表数据重新插入
//        if (oldItem != null && oldItem.size() > 0) {
//            for (AssessBusinessFillItem entity : oldItem) {
//                // 获取第一个id相同的数据，使用stream流过滤
//                AssessBusinessFillItem old = oldItem.stream().filter(item -> item.getId().equals(entity.getId())).findFirst().get();
//                old.setScore(entity.getScore());
//                old.setReasonOfDeduction(entity.getReasonOfDeduction());
//
//                itemList.add(old);
//            }
//        }

        for (AssessBusinessFillItem entity : oldItem){
            BigDecimal score = entity.getScore();
            if (score == null || score.compareTo(BigDecimal.ZERO) == 0) {
                LambdaUpdateWrapper<AssessBusinessFillItem> luw = new LambdaUpdateWrapper<>();
                luw.eq(AssessBusinessFillItem::getId, entity.getId());
                luw.set(AssessBusinessFillItem::getScore, null);
                luw.set(AssessBusinessFillItem::getReasonOfDeduction, null);
                fillItemMapper.update(null, luw);
                entity.setScore(null);
                entity.setReasonOfDeduction(null);
            }
        }

        oldItem.forEach(fillItemMapperNew::updateById);

        // 提交事务、清理缓存、关闭sqlSession
        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();

        return Result.OK("提交成功!");
    }

    @Override
    public void saveTemp(AssessBusinessDepartFill assessBusinessDepartFill, List<AssessBusinessFillItem> assessBusinessFillItemList) {
        // 1.先删除子表数据
//        fillItemMapper.deleteByMainId(assessBusinessDepartFill.getId());

        List<AssessBusinessFillItem> oldItem = fillItemMapper.selectByMainId(assessBusinessDepartFill.getId());

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        AssessBusinessFillItemMapper fillItemMapperNew = sqlSession.getMapper(AssessBusinessFillItemMapper.class);


        // 2.子表数据重新插入
        for (AssessBusinessFillItem entity : assessBusinessFillItemList) {
            AssessBusinessFillItem old = oldItem.stream().filter(item -> item.getId().equals(entity.getId())).findFirst().get();
            BigDecimal score = entity.getScore();
            if (score == null || score.compareTo(BigDecimal.ZERO) == 0) {
                LambdaUpdateWrapper<AssessBusinessFillItem> luw = new LambdaUpdateWrapper<>();
                luw.eq(AssessBusinessFillItem::getId, old.getId());
                luw.set(AssessBusinessFillItem::getScore, null);
                luw.set(AssessBusinessFillItem::getReasonOfDeduction, null);
                fillItemMapper.update(null, luw);
                old.setScore(null);
                old.setReasonOfDeduction(null);
            } else {
                old.setScore(entity.getScore());
                old.setReasonOfDeduction(entity.getReasonOfDeduction());
            }
        }

        oldItem.forEach(fillItemMapperNew::updateById);

        departFillMapper.updateById(assessBusinessDepartFill);

        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delMain(String id) {
        fillItemMapper.deleteByMainId(id);
        departFillMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delBatchMain(Collection<? extends Serializable> idList) {
        for (Serializable id : idList) {
            fillItemMapper.deleteByMainId(id.toString());
            departFillMapper.deleteById(id);
        }
    }

    @Override
    public BusinessIndexLeader getAllItems() {

        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("business");
        if (currentAssessInfo != null && currentAssessInfo.isAssessing()) {
            List<BusinessFieldDTO> items = departFillMapper.getAllItems(currentAssessInfo.getCurrentYear());

            // 统计未完成状态数量
            long uncompleted = items.stream()
                    .filter(item -> item.getStatus() != 3)
                    .count();

            // 所有 items 的数量
            int totalCount = items.size();

            // 计算完成的数量
            int completed = totalCount - (int) uncompleted;

            // 计算占比
            double ratio = totalCount > 0 ? completed / (double) totalCount : 0.0;

            // 获取所有部门信息并存入 Map
            List<SysDepartModel> sysDepartModels = departCommonApi.queryAllDepart();
            Map<String, String> departMap = sysDepartModels.stream()
                    .collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getDepartName));

            // 获取所有 status 不等于 3 的记录
            List<BusinessFieldDTO> reports = items.stream()
                    .filter(item -> item.getStatus() != 3)
                    .peek(businessFieldDTO -> {
                        // 通过 departMap 获取 reportDepart 的名称
                        String reportDepartName = departMap.get(businessFieldDTO.getReportDepart());
                        businessFieldDTO.setReportDepart(reportDepartName != null ? reportDepartName : "未知部门");

                        // 获取 assessDepart 字符串并拆分为单个 ID
                        String[] assessDepartIds = businessFieldDTO.getAssessDepart().split(",");
                        // 创建一个 StringBuilder 来拼接部门名称
                        StringBuilder assessDepartNames = new StringBuilder();

                        for (String id : assessDepartIds) {
                            String assessDepartName = departMap.get(id.trim()); // 从 departMap 中获取部门名称
                            if (assessDepartName != null) { // 进行空值检查
                                assessDepartNames.append(assessDepartName).append(","); // 拼接部门名称
                            }
                        }

                        // 去掉最后一个逗号
                        if (assessDepartNames.length() > 0) {
                            assessDepartNames.setLength(assessDepartNames.length() - 1);
                        }

                        businessFieldDTO.setAssessDepart(assessDepartNames.toString());
                    })
                    .collect(Collectors.toList());

            // 输出结果
            return new BusinessIndexLeader(completed, uncompleted, ratio, reports);
        }
        return null;
    }

    @Override
    public BusinessIndexEmployee[] getPartial(String departId, AssessCurrentAssess assessInfo) {
        if (assessInfo == null || StringUtils.isBlank(assessInfo.getCurrentYear()) || !assessInfo.isAssessing()) {
            return new BusinessIndexEmployee[]{new BusinessIndexEmployee("当前无正在进行的考核")};
        }
        // 创建查询条件，匹配 department_code 字段
        QueryWrapper<AssessBusinessDepartFill> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("report_depart", departId).eq("current_year", assessInfo.getCurrentYear());

        List<AssessBusinessDepartFill> reports = departFillMapper.selectList(queryWrapper);

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

        // 创建并返回结果数组
        return new BusinessIndexEmployee[]{new BusinessIndexEmployee(statusMessage)};
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initAssess(AssessBusinessDepartFillDTO map) {
        String currentYear = map.getCurrentYear();
        // 判断当前年度是否已经初始化
        LambdaQueryWrapper<AssessBusinessDepartFill> lqw1 = new LambdaQueryWrapper<>();
        lqw1.eq(AssessBusinessDepartFill::getCurrentYear, currentYear);
        lqw1.select(AssessBusinessDepartFill::getId);
        List<AssessBusinessDepartFill> assessBusinessDepartFills = departFillMapper.selectList(lqw1);
        if (!assessBusinessDepartFills.isEmpty()) {
            throw new JeecgBootException("当前年度已存在业务工作测评，不可重复发起！");
        }

        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("business");
        if (currentAssessInfo != null) {
            int lastAssessYear = Integer.parseInt(currentAssessInfo.getCurrentYear());
            int currentYearInt = Integer.parseInt(currentYear);
            if (currentYearInt <= lastAssessYear) {
                throw new JeecgBootException("考核年度已结束，不可重复发起！");
            }
        }

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        AssessBusinessFillItemMapper fillItemMapperNew = CommonUtils.getBatchMapper(sqlSession, AssessBusinessFillItemMapper.class);
        AssessBusinessDepartFillMapper departFillMapperNew = CommonUtils.getBatchMapper(sqlSession, AssessBusinessDepartFillMapper.class);


        // 获取所有处室以及基层单位
        List<SysDepartModel> departs1 = departCommonApi.queryDepartByType(new String[]{"1"});
        List<SysDepartModel> departs2 = departCommonApi.queryDepartByType(new String[]{"2", "3", "4"});
        StringBuilder assessedDepartIds1 = new StringBuilder();
        StringBuilder assessedDepartIds2 = new StringBuilder();
        departs1.forEach(item -> {
            if (assessedDepartIds1.length() <= 0) {
                assessedDepartIds1.append(item.getId());
            } else {
                assessedDepartIds1.append(",").append(item.getId());
            }
        });
        departs2.forEach(item -> {
            if (assessedDepartIds2.length() <= 0) {
                assessedDepartIds2.append(item.getId());
            } else {
                assessedDepartIds2.append(",").append(item.getId());
            }
        });

        List<AssessBusinessDepartFill> fillList = new ArrayList<>();
        List<AssessBusinessFillItem> itemList = new ArrayList<>();

        // 插入各单位考核填报表
        getInitInsertList(map, departs1, departs2, assessedDepartIds2, fillList, itemList);

        // 插入各基层单位考核填报表
        getInitInsertList(map, departs2, departs1, assessedDepartIds1, fillList, itemList);

        // 批量插入各处室考核填报表和各处室考核填报明细
        fillList.forEach(departFillMapperNew::insert);
        itemList.forEach(fillItemMapperNew::insert);

        // 添加年份字典数据
        departCommonApi.saveAssessYear(map.getCurrentYear());
        assessCommonApi.updateAssessCurrentYear(new AssessCurrentAssess("business", map.getAssessName(), currentYear, new Date(), map.getDeadline(), null, true));

        // 提交事务、清理缓存、关闭sqlSession
        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();
    }

    @Override
    public Result<String> passAudit(String assessId, String name, List<AssessBusinessFillItem> itemList) {

        AssessBusinessDepartFill needAuditAssess = departFillMapper.selectById(assessId);
        if (needAuditAssess.getStatus() == 1) {
            return Result.error("当前考核未提交审核，请稍后重试！");
        }
        if (needAuditAssess.getStatus() == 3) {
            return Result.error("当前考核已通过审核，不可重复提交！");
        }

        // 过滤出分数不为空的数据
        itemList = itemList.stream().filter(item -> item.getScore() != null).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        StringBuilder assessDepartIds = new StringBuilder();
        itemList.forEach(item -> {
            if (assessDepartIds.length() <= 0) {
                assessDepartIds.append(item.getDepartCode());
            } else {
                assessDepartIds.append(",").append(item.getDepartCode());
            }
            item.setCanEdit(false);
        });

        LambdaQueryWrapper<AssessBusinessFillItem> deleteLqw = new LambdaQueryWrapper<>();
        deleteLqw.eq(AssessBusinessFillItem::getFillId, needAuditAssess.getId());
        deleteLqw.notIn(AssessBusinessFillItem::getDepartCode, assessDepartIds.toString().split(","));

        fillItemMapper.delete(deleteLqw);

        LambdaUpdateWrapper<AssessBusinessDepartFill> lqw = new LambdaUpdateWrapper<>();
        lqw.eq(AssessBusinessDepartFill::getId, assessId);
        lqw.set(AssessBusinessDepartFill::getStatus, 3);
        lqw.set(AssessBusinessDepartFill::getAuditBy, name);
        lqw.set(AssessBusinessDepartFill::getRemark, null);
        lqw.set(AssessBusinessDepartFill::getAssessDepart, assessDepartIds.toString());
        boolean update = this.update(lqw);

        // 汇总得分
        boolean b = gradeService.summaryScore(needAuditAssess.getCurrentYear(), itemList);

        // 审核通过表彰和通报
        String reportDepart = needAuditAssess.getReportDepart();
        LambdaUpdateWrapper<AssessBusinessCommend> luw = new LambdaUpdateWrapper<>();
        luw.eq(AssessBusinessCommend::getCurrentYear, needAuditAssess.getCurrentYear());
        luw.eq(AssessBusinessCommend::getDepartmentCode, reportDepart);
        luw.ne(AssessBusinessCommend::getStatus, 3);
        luw.set(AssessBusinessCommend::getStatus, 2);
        commendMapper.update(null, luw);

        LambdaUpdateWrapper<AssessBusinessDenounce> luw2 = new LambdaUpdateWrapper<>();
        luw2.eq(AssessBusinessDenounce::getCurrentYear, needAuditAssess.getCurrentYear());
        luw2.eq(AssessBusinessDenounce::getDepartmentCode, reportDepart);
        luw2.ne(AssessBusinessDenounce::getStatus, 3);
        luw2.set(AssessBusinessDenounce::getStatus, 2);
        denounceMapper.update(null, luw2);

        if (update && b) return Result.ok("审核成功");
        return Result.error("系统异常，请稍后重试！");
    }

    @Override
    public IPage<AssessBusinessDepartFill> listByPageToReporter(Page page, QueryWrapper queryWrapper) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        List<DepartIdModel> departIdModels = sysUserDepartService.queryDepartIdsOfUser(sysUser.getId());
        return null;
    }

    @Override
    public IPage<AssessBusinessDepartFill> listByPageToHeader(Page page, QueryWrapper queryWrapper) {
        return null;
    }

    @Override
    public IPage<AssessBusinessDepartFill> listByPageDistRole(Page page, QueryWrapper<AssessBusinessDepartFill> queryWrapper) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<SysUserRole> roleList = sysUserRoleService.list(new QueryWrapper<SysUserRole>().lambda().eq(SysUserRole::getUserId, sysUser.getId()));

        List<DepartIdModel> departIdModels = sysUserDepartService.queryDepartIdsOfUser(sysUser.getId());
        List<String> departIdslist = new ArrayList<>();
        if (departIdModels != null) {
            departIdslist = CollectionUtils.transform(departIdModels, d -> ((DepartIdModel) d).getValue());
        }

        boolean getDepartData = false;

        for (SysUserRole sysUserRole : roleList) {
            SysRole role = sysRoleService.getById(sysUserRole.getRoleId());
            if ("department_head".equals(role.getRoleCode()) || "depart_report".equals(role.getRoleCode())) {
                getDepartData = true;
            }
            if ("department_cadre_admin".equals(role.getRoleCode())) {
                getDepartData = false;
                break;
            }
        }

        if (getDepartData) {
            queryWrapper.in("report_depart", departIdslist);
        }
        return this.page(page, queryWrapper);
    }

    @Override
    public void stopAssess() {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("business");
        if (currentAssessInfo != null && currentAssessInfo.isAssessing()) {
            currentAssessInfo.setAssessing(false);
            assessCommonApi.updateAssessCurrentYear(currentAssessInfo);

            LambdaUpdateWrapper<AssessBusinessDepartFill> luw = new LambdaUpdateWrapper<>();
            luw.eq(AssessBusinessDepartFill::getCurrentYear, currentAssessInfo.getCurrentYear());
            luw.set(AssessBusinessDepartFill::getStatus, "3");
            departFillMapper.update(null, luw);

            // todo: 未锁定
        }
    }

    @Override
    public void revocationAssess() {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("business");
        if (currentAssessInfo != null && currentAssessInfo.isAssessing()) {
            currentAssessInfo.setAssessing(false);

            LambdaQueryWrapper<AssessBusinessDepartFill> lqw = new LambdaQueryWrapper<>();
            lqw.eq(AssessBusinessDepartFill::getCurrentYear, currentAssessInfo.getCurrentYear());

            List<AssessBusinessDepartFill> fills = departFillMapper.selectList(lqw);
            List<String> fillIds = new ArrayList<>();
            for (AssessBusinessDepartFill fill : fills) {
                fillIds.add(fill.getId());
            }
            departFillMapper.delete(lqw);

            LambdaQueryWrapper<AssessBusinessFillItem> lqw2 = new LambdaQueryWrapper<>();
            lqw2.in(AssessBusinessFillItem::getFillId, fillIds);
            fillItemMapper.delete(lqw2);

            LambdaQueryWrapper<AssessBusinessGrade> lqw3 = new LambdaQueryWrapper<>();
            lqw3.eq(AssessBusinessGrade::getCurrentYear, currentAssessInfo.getCurrentYear());
            gradeMapper.delete(lqw3);

            assessCommonApi.revocationCurrentYear(currentAssessInfo);
        }

    }

    private void getInitInsertList(AssessBusinessDepartFillDTO map, List<SysDepartModel> assessDepart, List<SysDepartModel> assessedDepart, StringBuilder assessedDepartIds, List<AssessBusinessDepartFill> fillList, List<AssessBusinessFillItem> itemList) {
        for (SysDepartModel depart : assessDepart) {
            // 雪花算法生成ID
            String fillId = IdWorker.getIdStr();

            AssessBusinessDepartFill badf = new AssessBusinessDepartFill();
            badf.setId(fillId);
            badf.setAssessName(map.getAssessName());
            badf.setCurrentYear(map.getCurrentYear());
            badf.setDeadline(map.getDeadline());
            badf.setReportDepart(depart.getId());
            badf.setAssessDepart(assessedDepartIds.toString());
            fillList.add(badf);

            // 插入各处室考核填报明细
            for (SysDepartModel s : assessedDepart) {
                AssessBusinessFillItem abfi = new AssessBusinessFillItem();
                abfi.setReportDepart(depart.getId());
                abfi.setDepartCode(s.getId());
                abfi.setFillId(fillId);
                abfi.setCanEdit(true);
                abfi.setMustFill(false);
                itemList.add(abfi);
            }
        }
    }

    @Override
    public boolean assessNone(List<AssessBusinessFillItem> list) {
        for (AssessBusinessFillItem assessBusinessFillItem :
                list) {
            BigDecimal score = assessBusinessFillItem.getScore();
            if (score != null && score.compareTo(BigDecimal.ZERO) != 0) {
                return false;
            }
        }
        return true;
    }
}
