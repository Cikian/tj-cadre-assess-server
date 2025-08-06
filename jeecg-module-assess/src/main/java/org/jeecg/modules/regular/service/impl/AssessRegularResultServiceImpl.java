package org.jeecg.modules.regular.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.regular.entity.AssessRegularReport;
import org.jeecg.modules.regular.entity.AssessRegularReportItem;
import org.jeecg.modules.regular.mapper.AssessRegularReportItemMapper;
import org.jeecg.modules.regular.mapper.AssessRegularReportMapper;
import org.jeecg.modules.regular.mapper.AssessRegularResultMapper;
import org.jeecg.modules.regular.service.IAssessRegularReportService;
import org.jeecg.modules.regular.service.IAssessRegularResultService;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.AssessAssistConfig;
import org.jeecg.modules.sys.entity.annual.AssessLeaderConfig;
import org.jeecg.modules.sys.entity.business.AssessLeaderDepartConfig;
import org.jeecg.modules.sys.mapper.annual.AssessAssistConfigMapper;
import org.jeecg.modules.sys.mapper.annual.AssessLeaderConfigMapper;
import org.jeecg.modules.sys.mapper.business.AssessLeaderDepartConfigMapper;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 平时考核结果
 * @Author: admin
 * @Date: 2024-10-09
 * @Version: V1.0
 */
@Service
public class AssessRegularResultServiceImpl extends ServiceImpl<AssessRegularResultMapper, AssessRegularReportItem> implements IAssessRegularResultService {
    @Autowired
    private AssessRegularResultMapper resultMapper;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private AssessRegularReportMapper reportMapper;
    @Autowired
    @Lazy
    private IAssessRegularReportService reportService;
    @Autowired
    private AssessLeaderDepartConfigMapper leaderDepartConfigMapper;
    @Autowired
    private AssessRegularReportItemMapper itemMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private AssessAssistConfigMapper assistConfigMapper;
    @Autowired
    private AssessLeaderConfigMapper leaderConfigMapper;

    @Override
    public void deleteByCurrentYear(String year) {

        resultMapper.deleteByYear(year);
    }

    @Override
    public Map<String, Object> getCurrentDetail() {
        AssessCurrentAssess assess = assessCommonApi.getCurrentAssessInfo("regular");
        if (assess == null) {
            throw new JeecgBootException("当前没有正在进行的考核");
        }

        Map<String, Object> res = new HashMap<>();
        res.put("assessName", assess.getAssessName());
        res.put("year", assess.getCurrentYear());
        res.put("quarter", assess.getQuarter());

        LambdaQueryWrapper<AssessRegularReport> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessRegularReport::getCurrentYear, assess.getCurrentYear());
        lqw.eq(AssessRegularReport::getCurrentQuarter, assess.getQuarter());

        List<AssessRegularReport> reports = reportMapper.selectList(lqw);

        // todo：已经填报处室，未填报处室
        int over = 0;
        int notOver = 0;
        for (AssessRegularReport report : reports) {
            if ("3".equals(report.getStatus())) over++;
            else notOver++;
        }
        res.put("over", String.valueOf(over));
        res.put("notOver", String.valueOf(notOver));
        // 进度百分比，保留两位小数
        res.put("progress", String.format("%.2f", (double) over / (over + notOver) * 100));


        // 将reports中的id转为List
        List<String> ids = reports.stream().map(AssessRegularReport::getId).collect(Collectors.toList());
        if (ids.isEmpty()) {
            throw new JeecgBootException("当前季度没有处室考核");
        }

        LambdaQueryWrapper<AssessRegularReportItem> lqw2 = new LambdaQueryWrapper<>();
        lqw2.in(AssessRegularReportItem::getMainId, ids);
        List<AssessRegularReportItem> items = itemMapper.selectList(lqw2);

        int leader = 0;
        int dept = 0;
        int totalGood = 0;
        int leaderGood = 0;
        int deptGood = 0;

        String quarter = assess.getQuarter();

        for (AssessRegularReportItem item : items) {

            String grade = "";
            switch (quarter) {
                case "1":
                    grade = item.getQuarter1();
                    break;
                case "2":
                    grade = item.getQuarter2();
                    break;
                case "3":
                    grade = item.getQuarter3();
                    break;
                case "4":
                    grade = item.getQuarter4();
                    break;
            }

            if ("干部处".equals(item.getAdminDepartment())) {
                leader++;
                if ("A".equals(grade)) {
                    leaderGood++;
                    totalGood++;
                }
            } else {
                dept++;
                if ("A".equals(grade)) {
                    deptGood++;
                    totalGood++;
                }
            }
        }

        double v = items.size() * 0.4;
        res.put("totalNum", String.valueOf(items.size()));
        res.put("totalGoodTheory", String.valueOf((int) v));
        res.put("totalGood", String.valueOf(totalGood));

        res.put("leaderNum", String.valueOf(leader));
        res.put("leaderGoodTheory", String.valueOf((int) (leader * 0.4)));
        res.put("leaderGood", String.valueOf(leaderGood));

        res.put("deptNum", String.valueOf(dept));
        res.put("deptGoodTheory", String.valueOf((int) (dept * 0.4)));
        res.put("deptGood", String.valueOf(deptGood));

        List<Map<String, Object>> leader1 = reportService.getDetailItem("leader");
        if (!leader1.isEmpty()) {
            Map<String, Object> stringObjectMap = leader1.get(leader1.size() - 1);
            res.put("leaderTotal", stringObjectMap.get("num"));
        }

        List<Map<String, Object>> dep1 = reportService.getDetailItem("other");
        if (!leader1.isEmpty()) {
            Map<String, Object> stringObjectMap = dep1.get(dep1.size() - 1);
            res.put("depTotal", stringObjectMap.get("num"));
        }


        return res;
    }

    @Override
    public Map<String, Boolean> getLeaderPro(String year, String quarter) {
        // 获取所有领导分工
        List<AssessLeaderDepartConfig> configs = leaderDepartConfigMapper.selectList(null);

        // 将用户ID存入List
        List<String> userIds = configs.stream().map(AssessLeaderDepartConfig::getLeaderId).collect(Collectors.toList());
        // 根据用户ID查询用户信息
        List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
        // 将用户信息存入Map，key为用户ID，value为用户信息
        Map<String, String> userMap = users.stream().collect(Collectors.toMap(SysUser::getId, SysUser::getRealname));

        List<String> specials = new ArrayList<>();
        List<AssessAssistConfig> assistConfigs = assistConfigMapper.selectList(null);
        // 将assistConfigs中的HashId存入specials
        List<String> hashIds = assistConfigs.stream().map(AssessAssistConfig::getHashId).collect(Collectors.toList());
        specials.addAll(hashIds);
        List<AssessLeaderConfig> leaderConfigs = leaderConfigMapper.selectList(null);
        // 将leaderConfigs中的HashId存入specials
        hashIds.clear();
        hashIds = leaderConfigs.stream().map(AssessLeaderConfig::getHashId).collect(Collectors.toList());
        specials.addAll(hashIds);

        LambdaQueryWrapper<AssessRegularReportItem> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessRegularReportItem::getCurrentYear, year);
        lqw.eq(AssessRegularReportItem::getIsLeader, "1");
        lqw.notIn(AssessRegularReportItem::getHashId, specials);

        List<AssessRegularReportItem> items = itemMapper.selectList(lqw);



        Map<String, Boolean> res = new HashMap<>();

        for (AssessLeaderDepartConfig ld : configs) {
            String departIds = ld.getDepartId();
            String[] split = departIds.split(",");
            // 转为List
            List<String> ids = Arrays.asList(split);

            // 从items中过滤出departId在ids中的元素
            List<AssessRegularReportItem> collect = items.stream().filter(item -> ids.contains(item.getDepartmentCode())).collect(Collectors.toList());

            switch (quarter) {
                case "1":
                    res.put(userMap.get(ld.getLeaderId()), collect.stream().map(AssessRegularReportItem::getQuarter1).noneMatch(StringUtils::isEmpty));
                    break;
                case "2":
                    res.put(userMap.get(ld.getLeaderId()), collect.stream().map(AssessRegularReportItem::getQuarter2).noneMatch(StringUtils::isEmpty));
                    break;
                case "3":
                    res.put(userMap.get(ld.getLeaderId()), collect.stream().map(AssessRegularReportItem::getQuarter3).noneMatch(StringUtils::isEmpty));
                    break;
                case "4":
                    res.put(userMap.get(ld.getLeaderId()), collect.stream().map(AssessRegularReportItem::getQuarter4).noneMatch(StringUtils::isEmpty));
                    break;
            }
        }
        return res;
    }
}
