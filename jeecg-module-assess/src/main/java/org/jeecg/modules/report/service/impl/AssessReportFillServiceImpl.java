package org.jeecg.modules.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.system.vo.SysDepartModel;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.report.entity.ReportIndexEmployee;
import org.jeecg.modules.report.entity.ReportIndexLeader;
import org.jeecg.modules.report.service.IAssessNotReportDepartService;
import org.jeecg.modules.report.service.IAssessReportFillService;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.dto.ReportAssessInitDTO;
import org.jeecg.modules.sys.dto.ReportFieldDTO;
import org.jeecg.modules.sys.dto.ReportFillDTO;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.report.*;
import org.jeecg.modules.sys.mapper.annual.AssessAnnualExcellentNumMapper;
import org.jeecg.modules.sys.mapper.report.*;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.user.UserCommonApi;
import org.jeecg.modules.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 一报告两评议填报
 * @Author: jeecg-boot
 * @Date: 2024-09-24
 * @Version: V1.0
 */
@Service
public class AssessReportFillServiceImpl extends ServiceImpl<AssessReportFillMapper, AssessReportFill> implements IAssessReportFillService {
    @Autowired
    private AssessReportFillMapper reportFillMapper;
    @Autowired
    private AssessReportArrangeMapper arrangeMapper;
    @Autowired
    private AssessReportNewLeaderMapper newLeaderMapper;
    @Autowired
    private AssessReportObjectInfoMapper objectInfoMapper;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private DepartCommonApi departCommonApi;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private IAssessNotReportDepartService assessNotReportDepartService;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private UserCommonApi userCommonApi;
    @Autowired
    private AssessReportEvaluationSummaryMapper evaluationSummaryMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean initAssess(ReportAssessInitDTO map) {
        String currentYear = map.getCurrentYear();
        // 判断当前年度是否已经初始化
        LambdaQueryWrapper<AssessReportFill> lqw1 = new LambdaQueryWrapper<>();
        lqw1.eq(AssessReportFill::getCurrentYear, currentYear);
        lqw1.select(AssessReportFill::getId);
        List<AssessReportFill> departFills = reportFillMapper.selectList(lqw1);
        if (!departFills.isEmpty()) {
            throw new JeecgBootException("当前年度已存在一报告两评议，不可重复发起！");
        }

        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("report");
        if (currentAssessInfo != null) {
            int lastAssessYear = Integer.parseInt(currentAssessInfo.getCurrentYear());
            int currentYearInt = Integer.parseInt(currentYear);
            if (currentYearInt <= lastAssessYear) {
                throw new JeecgBootException("考核年度已结束，不可重复发起！");
            }
        }


        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        AssessReportFillMapper fillMapperNew = sqlSession.getMapper(AssessReportFillMapper.class);
        AssessReportArrangeMapper arrangeMapperNew = sqlSession.getMapper(AssessReportArrangeMapper.class);
        AssessAnnualExcellentNumMapper excellentNumMapper = sqlSession.getMapper(AssessAnnualExcellentNumMapper.class);


        // 获取当前考核的所有单位
        List<SysDepartModel> departs = departCommonApi.queryDepartByType(new ArrayList<String>() {{
            add("2");
            add("3");
            add("4");
        }});
        LambdaQueryWrapper<AssessNotReportDepart> queryWrapper = new LambdaQueryWrapper<>();
        List<AssessNotReportDepart> configList = assessNotReportDepartService.list(queryWrapper);
        for (AssessNotReportDepart assessNotReportDepart :
                configList) {
            List<String> list = Arrays.asList(assessNotReportDepart.getNotReportDepart().split(","));
            for (String departCode :
                    list) {
                departs.removeIf(item -> item.getId().equals(departCode));
            }
        }

        List<AssessReportFill> fillList = new ArrayList<>();
        List<AssessReportArrange> arrangeList = new ArrayList<>();

        // 插入各处室考核填报表
        for (SysDepartModel depart : departs) {
            // 雪花算法生成ID
            String fillId = IdWorker.getIdStr();

            AssessReportFill fill = new AssessReportFill();
            fill.setId(fillId);
            fill.setAssessName(map.getAssessName());
            fill.setCurrentYear(map.getCurrentYear());
            fill.setDeadline(map.getDeadline());
            fill.setDepart(depart.getId());
            fill.setStatus("1");
            fillList.add(fill);

            AssessReportArrange arrange = new AssessReportArrange();
            arrange.setDepart(depart.getId());
            arrange.setScope(map.getScope());
            arrange.setFillId(fillId);
            arrangeList.add(arrange);
        }

        // 批量插入各处室考核填报表和各处室考核填报明细
        fillList.forEach(fillMapperNew::insert);
        arrangeList.forEach(arrangeMapperNew::insert);

        // 添加年份字典数据
        departCommonApi.saveAssessYear(map.getCurrentYear());
        assessCommonApi.updateAssessCurrentYear(new AssessCurrentAssess("report", map.getAssessName(), currentYear, new Date(), map.getDeadline(), null, true));
        this.stopDem();
        // 提交事务、清理缓存、关闭sqlSession
        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();

        return !fillList.isEmpty() && !arrangeList.isEmpty();
    }

    @Override
    public void submitFill(ReportFillDTO reportFillDTO, boolean check, boolean pass) {
        if (check) {
            checkFillSubmit(reportFillDTO);
        }

        AssessReportFill reportFill = reportFillDTO.getReportFill();

        if ("1".equals(reportFill.getStatus()) || "4".equals(reportFill.getStatus())) {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            reportFill.setReportBy(sysUser.getRealname());
        }

        AssessReportArrange reportArrange = reportFillDTO.getReportArrange();
        List<AssessReportNewLeader> reportNewLeader = reportFillDTO.getReportNewLeader();
        List<AssessReportObjectInfo> reportObjectInfo = reportFillDTO.getReportObjectInfo();

        if (check) {
            if (pass) {
                reportFill.setStatus("3");
                reportFill.setRemark(null);
            } else reportFill.setStatus("2");
        }

        for (AssessReportObjectInfo objectInfo : reportObjectInfo) {
            objectInfo.setId(null);
            // 加一天
            Date birthday = objectInfo.getBirthday();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(birthday);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            objectInfo.setBirthday(calendar.getTime());
        }

        for (AssessReportNewLeader newLeader : reportNewLeader) {
            newLeader.setId(null);
            Date birthday = newLeader.getBirthday();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String formattedDate = sdf.format(birthday);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(birthday);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            newLeader.setBirthday(calendar.getTime());

            String hashId = CommonUtils.hashId(newLeader.getName(), newLeader.getSex(), formattedDate);
            newLeader.setHashId(hashId);

            newLeader.setCurrentYear(reportFill.getCurrentYear());//给新提拔干部信息赋年份
        }

        newLeaderMapper.delete(new LambdaQueryWrapper<AssessReportNewLeader>().eq(AssessReportNewLeader::getFillId, reportFill.getId()));
        objectInfoMapper.delete(new LambdaQueryWrapper<AssessReportObjectInfo>().eq(AssessReportObjectInfo::getFillId, reportFill.getId()));

        reportNewLeader.forEach(newLeaderMapper::insert);
        reportObjectInfo.forEach(objectInfoMapper::insert);
        arrangeMapper.updateById(reportArrange);
        reportFillMapper.updateById(reportFill);
    }

    @Override
    public ReportIndexLeader getAllItems() {

        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("report");
        if (currentAssessInfo != null && currentAssessInfo.isAssessing()) {
            List<ReportFieldDTO> items = reportFillMapper.getAllItems(currentAssessInfo.getCurrentYear());

            // 统计完成状态数量
            long countStatus3 = items.stream()
                    .filter(item -> "3".equals(item.getStatus()))
                    .count();

            // 所有 items 的数量
            int totalCount = items.size();

            // 计算未完成的数量
            int uncompleted = totalCount - (int) countStatus3;

            // 计算占比
            double ratio = totalCount > 0 ? countStatus3 / (double) totalCount : 0.0;

            // 获取所有部门信息并存入 Map
            List<SysDepartModel> sysDepartModels = departCommonApi.queryAllDepart();
            Map<String, String> departMap = sysDepartModels.stream()
                    .collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getDepartName));

            // 获取所有 status 不等于 3 的记录
            List<ReportFieldDTO> reports = items.stream()
                    .filter(item -> !"3".equals(item.getStatus()))
                    .filter(reportFieldDTO -> reportFieldDTO.getDepart() != null) // 添加 null 判断
                    .peek(reportFieldDTO -> {
                        String departName = departMap.get(reportFieldDTO.getDepart());
                        reportFieldDTO.setDepart(departName != null ? departName : "未知部门");
                    })
                    .collect(Collectors.toList());

            // 输出结果
            return new ReportIndexLeader(uncompleted, (int) countStatus3, ratio, reports);
        }
        return null;
    }

    @Override
    public ReportIndexEmployee[] getPartial(String departId, AssessCurrentAssess assessInfo) {
        if (assessInfo == null || StringUtils.isBlank(assessInfo.getCurrentYear()) || !assessInfo.isAssessing()) {
            return new ReportIndexEmployee[]{new ReportIndexEmployee("当前无正在进行的考核")};
        }

        // 创建查询条件，匹配 department_code 字段
        QueryWrapper<AssessReportFill> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("depart", departId).eq("current_year", assessInfo.getCurrentYear());

        List<AssessReportFill> reports = reportFillMapper.selectList(queryWrapper);

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
                statusMessage = "已驳回";
                break;
            default:
                statusMessage = "当前无正在进行的考核"; // 处理未知状态
                break;
        }

        return new ReportIndexEmployee[]{new ReportIndexEmployee(statusMessage)};
    }

    @Override
    public void stopAssess() {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("report");
        if (currentAssessInfo != null && currentAssessInfo.isAssessing()) {
            currentAssessInfo.setAssessing(false);
            assessCommonApi.updateAssessCurrentYear(currentAssessInfo);

            LambdaUpdateWrapper<AssessReportFill> luw = new LambdaUpdateWrapper<>();
            luw.eq(AssessReportFill::getCurrentYear, currentAssessInfo.getCurrentYear());
            luw.set(AssessReportFill::getStatus, "3");
            reportFillMapper.update(null, luw);

            // todo: 未锁定
            this.stopDem();
        }
    }

    @Override
    public void revocationAssess() {

        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("report");
        if (currentAssessInfo != null && currentAssessInfo.isAssessing()) {

            LambdaQueryWrapper<AssessReportEvaluationSummary> lqwE = new LambdaQueryWrapper<>();
            lqwE.eq(AssessReportEvaluationSummary::getCurrentYear, currentAssessInfo.getCurrentYear());
            List<AssessReportEvaluationSummary> assessReportEvaluationSummaries = evaluationSummaryMapper.selectList(lqwE);
            if (!assessReportEvaluationSummaries.isEmpty()) {
                throw new JeecgBootException("民主测评已经发起，无法撤销！\n如需帮助，请联系：星际空间（天津）科技发展有限公司！");
            }

            currentAssessInfo.setAssessing(false);

            LambdaQueryWrapper<AssessReportFill> lqw = new LambdaQueryWrapper<>();
            lqw.eq(AssessReportFill::getCurrentYear, currentAssessInfo.getCurrentYear());

            List<AssessReportFill> fills = reportFillMapper.selectList(lqw);
            List<String> fillIds = new ArrayList<>();
            for (AssessReportFill fill : fills) {
                fillIds.add(fill.getId());
            }
            reportFillMapper.delete(lqw);

            LambdaQueryWrapper<AssessReportArrange> lqw2 = new LambdaQueryWrapper<>();
            lqw2.in(AssessReportArrange::getFillId, fillIds);
            arrangeMapper.delete(lqw2);

            LambdaQueryWrapper<AssessReportObjectInfo> lqw3 = new LambdaQueryWrapper<>();
            lqw3.in(AssessReportObjectInfo::getFillId, fillIds);
            objectInfoMapper.delete(lqw3);

            LambdaQueryWrapper<AssessReportNewLeader> lqw4 = new LambdaQueryWrapper<>();
            lqw4.eq(AssessReportNewLeader::getCurrentYear, currentAssessInfo.getCurrentYear());
            newLeaderMapper.delete(lqw4);

            assessCommonApi.revocationCurrentYear(currentAssessInfo);
        } else {
            throw new JeecgBootException("当前无正在进行的考核");
        }

    }

    @Override
    public void stopDem() {
        userCommonApi.deleteAllAnonymousAccount("report");
    }

    private void checkFillSubmit(ReportFillDTO reportFillDTO) {
        AssessReportFill reportFill = reportFillDTO.getReportFill();
        if (reportFill.getReportFile() == null || reportFill.getReportFile().trim().isEmpty()) {
            throw new JeecgBootException("请上传干部选拔任用工作专题报告！");
        }
        AssessReportArrange reportArrange = reportFillDTO.getReportArrange();
        if (reportArrange.getPartinNum() == null || reportArrange.getPartinNum() == 0) {
            throw new JeecgBootException("请填写民主测评应到人数！");
        }
        if (reportArrange.getContacts() == null || reportArrange.getContacts().trim().isEmpty()) {
            throw new JeecgBootException("请填写联系人！");
        }
        if (reportArrange.getOfficeTel() == null || reportArrange.getOfficeTel().trim().isEmpty()) {
            throw new JeecgBootException("请填写办公电话！");
        }

        List<AssessReportNewLeader> reportNewLeaders = reportFillDTO.getReportNewLeader();
        for (AssessReportNewLeader reportNewLeader : reportNewLeaders) {
            if (reportNewLeader.getName() == null || reportNewLeader.getName().trim().isEmpty()) {
                throw new JeecgBootException("新提拔干部名册中，有未填写的姓名！");
            }
            if (reportNewLeader.getBirthday() == null) {
                throw new JeecgBootException("新提拔干部名册中，未填写“" + reportNewLeader.getName() + "”的出生年月！");
            }
            if (reportNewLeader.getOriginalPosition() == null || reportNewLeader.getOriginalPosition().trim().isEmpty()) {
                throw new JeecgBootException("新提拔干部名册中，未填写“" + reportNewLeader.getName() + "”的原任职务及任职时间！");
            }
            if (reportNewLeader.getCurrentPosition() == null || reportNewLeader.getCurrentPosition().trim().isEmpty()) {
                throw new JeecgBootException("新提拔干部名册中，未填写“" + reportNewLeader.getName() + "”的现任职务及任职时间！");
            }
        }

        List<AssessReportObjectInfo> reportObjectInfos = reportFillDTO.getReportObjectInfo();
        for (AssessReportObjectInfo reportObjectInfo : reportObjectInfos) {
            if (reportObjectInfo.getName() == null || reportObjectInfo.getName().trim().isEmpty()) {
                throw new JeecgBootException("评议对象有关信息汇总表中，有未填写的姓名！");
            }
            if (reportObjectInfo.getBirthday() == null) {
                throw new JeecgBootException("评议对象有关信息汇总表中，未填写“" + reportObjectInfo.getName() + "”的出生年月！");
            }
            if (reportObjectInfo.getPresentPosition() == null || reportObjectInfo.getPresentPosition().trim().isEmpty()) {
                throw new JeecgBootException("评议对象有关信息汇总表中，未填写“" + reportObjectInfo.getName() + "”的现任职务！");
            }
            if (reportObjectInfo.getJoinPartyDate() == null) {
                throw new JeecgBootException("评议对象有关信息汇总表中，未填写“" + reportObjectInfo.getName() + "”的入党时间！");
            }
            if (reportObjectInfo.getJoinWorkDate() == null) {
                throw new JeecgBootException("评议对象有关信息汇总表中，未填写“" + reportObjectInfo.getName() + "”的参加工作时间！");
            }

        }
    }
}
