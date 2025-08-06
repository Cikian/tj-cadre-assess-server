package org.jeecg.modules.annual.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.modules.annual.service.IAssessAnnualVacationService;
import org.jeecg.modules.annual.service.IAssessWorkDaysService;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.dto.AssessAnnualVacationDTO;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.AssessAnnualVacation;
import org.jeecg.modules.user.UserCommonApi;
import org.jeecg.modules.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @Description: 年度考核休假情况
 * @Author: jeecg-boot
 * @Date: 2024-09-03
 * @Version: V1.0
 */
@Api(tags = "年度考核休假情况")
@RestController
@RequestMapping("/modules/assessAnnualVacation")
@Slf4j
public class AssessAnnualVacationController extends JeecgController<AssessAnnualVacation, IAssessAnnualVacationService> {
    @Autowired
    private IAssessAnnualVacationService vacationService;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private UserCommonApi userCommonApi;
    @Autowired
    private IAssessWorkDaysService workDaysService;

    /**
     * 分页列表查询
     *
     * @param assessAnnualVacation
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "年度考核休假情况-分页列表查询")
    @ApiOperation(value = "年度考核休假情况-分页列表查询", notes = "年度考核休假情况-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessAnnualVacation>> queryPageList(AssessAnnualVacation assessAnnualVacation,
                                                             @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                             HttpServletRequest req) {
        QueryWrapper<AssessAnnualVacation> queryWrapper = QueryGenerator.initQueryWrapper(assessAnnualVacation, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");

        Map<String, String[]> parameterMap = new HashMap<>(req.getParameterMap());
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
        if (parameterMap.get("currentYear") == null) {
            if (currentAssessInfo != null && currentAssessInfo.isAssessing()) {
                queryWrapper.eq("current_year", currentAssessInfo.getCurrentYear());
            }
        }

        queryWrapper.ne("status", 1);

        Page<AssessAnnualVacation> page = new Page<>(pageNo, pageSize);
        IPage<AssessAnnualVacation> pageList = vacationService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param entry
     * @return
     */
    @AutoLog(value = "年度考核休假情况-添加")
    @ApiOperation(value = "年度考核休假情况-添加", notes = "年度考核休假情况-添加")
    //@RequiresPermissions("org.jeecg:assess_annual_vacation:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessAnnualVacation entry) {
        if (entry.getHashId() == null || entry.getHashId().isEmpty()) {
            String name = entry.getName();
            String sex = entry.getSex();
            Date birth = entry.getBirth();
            String birthStr = CommonUtils.dateToString(birth);
            String hashId = CommonUtils.hashId(name, sex, birthStr);
            entry.setHashId(hashId);
        }
        Date birthByHashId = CommonUtils.getBirthByHashId(entry.getHashId());
        entry.setBirth(birthByHashId);

        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        List<String> roles = userCommonApi.getUserRolesByUserName(sysUser.getUsername());
        if (roles.contains("department_cadre_admin")) entry.setStatus(3);
        else entry.setStatus(1);

        vacationService.save(entry);
        vacationService.updateAnnualSummarySuggestion(entry);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param assessAnnualVacation
     * @return
     */
    @AutoLog(value = "年度考核休假情况-编辑")
    @ApiOperation(value = "年度考核休假情况-编辑", notes = "年度考核休假情况-编辑")
    //@RequiresPermissions("org.jeecg:assess_annual_vacation:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessAnnualVacation assessAnnualVacation) {
        vacationService.updateById(assessAnnualVacation);
        vacationService.updateAnnualSummarySuggestion(assessAnnualVacation);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "年度考核休假情况-通过id删除")
    @ApiOperation(value = "年度考核休假情况-通过id删除", notes = "年度考核休假情况-通过id删除")
    //@RequiresPermissions("org.jeecg:assess_annual_vacation:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        vacationService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "年度考核休假情况-批量删除")
    @ApiOperation(value = "年度考核休假情况-批量删除", notes = "年度考核休假情况-批量删除")
    //@RequiresPermissions("org.jeecg:assess_annual_vacation:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.vacationService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "年度考核休假情况-通过id查询")
    @ApiOperation(value = "年度考核休假情况-通过id查询", notes = "年度考核休假情况-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessAnnualVacation> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessAnnualVacation assessAnnualVacation = vacationService.getById(id);
        if (assessAnnualVacation == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessAnnualVacation);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessAnnualVacation
     */
    //@RequiresPermissions("org.jeecg:assess_annual_vacation:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessAnnualVacation assessAnnualVacation) {
        return super.exportXls(request, assessAnnualVacation, AssessAnnualVacation.class, "年度考核休假情况");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresPermissions("assess_annual_vacation:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AssessAnnualVacation.class);
    }

    @GetMapping(value = "/queryByLeaderIds")
    public Result<IPage<AssessAnnualVacation>> getCurrentDepart(@RequestParam String leader, @RequestParam String year) {
        if (leader == null || year == null) {
            return null;
        }

        List<AssessAnnualVacation> vacations = vacationService.queryByLeaderIds(leader, year);
        IPage<AssessAnnualVacation> page = new Page<>();
        page.setRecords(vacations);

        return Result.OK(page);
    }

    @GetMapping("/assessAnnualVacationQuery")
    public JSONObject getAssessAnnualVacationExport(@RequestParam(defaultValue = "0") String currentYear, @RequestParam(defaultValue = "0") String depart) {
        List<AssessAnnualVacation> assessAnnualVacation = vacationService.getAssessAnnualVacation(currentYear, depart);

//        for (AssessAnnualVacation vacation : assessAnnualVacation) {
//            int type = vacation.getType();
//            switch (type) {
//                case 1:
//                    vacation.setType("年假");
//            }
//        }

        JSONObject json = new JSONObject();
        json.put("data", assessAnnualVacation);
        return json;
    }

    /**
     * 休假统计PDF
     *
     * @param port 请求参数
     * @return
     */
    @PostMapping("/exportExcel")
    public void exportExcel(@RequestParam(name = "year", required = false) String year, @RequestParam(name = "depart", required = false, defaultValue = "0") String depart, @Value("${server.port}") String port, HttpServletRequest request, HttpServletResponse response) {
        String url = "http://localhost:" + port + "/jmreport/exportAllExcelStream" + "?token=" + TokenUtils.getTokenByRequest(request);
        JSONObject queryParam = new JSONObject();

        if (year == null || year.isEmpty()) {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
            if (currentAssessInfo != null) {
                year = currentAssessInfo.getCurrentYear();
            }
        }
        if (depart == null || depart.isEmpty()) {
            depart = "0";
        }
        queryParam.put("currentYear", year);
        queryParam.put("depart", depart);

        Map<String, Object> param = new JSONObject();
        param.put("excelConfigId", "1013719159856340992");
        param.put("queryParam", queryParam);
        String filename = "休假统计.xlsx";
        assessCommonApi.getExportExcel(url, filename, param, response);
    }

    /*
     * 采用
     */
    @PostMapping("/adopt")
    public Result<String> adopt(@RequestBody AssessAnnualVacation annualVacation) {
        annualVacation.setStatus(3);
        vacationService.updateById(annualVacation);
        vacationService.updateAnnualSummarySuggestion(annualVacation);
        return Result.OK("操作成功！");
    }

    /*
     * 不采用
     */
    @PostMapping("/noAdopt")
    public Result<String> noAdopt(@RequestBody AssessAnnualVacation annualVacation) {
        annualVacation.setStatus(4);
        vacationService.updateById(annualVacation);
        vacationService.updateAnnualSummarySuggestion(annualVacation);
        return Result.OK("操作成功！");
    }

    @GetMapping("/getWorkDays")
    public Result<Integer> getWorkDays() {
        Integer days = workDaysService.getCurrentYearWorkDays();
        return Result.OK(days);
    }

    @PostMapping("/updateWorkDays")
    public Result<Integer> updateWorkDays(@RequestParam (name = "days", required = true) Integer days) {
        workDaysService.updateWorkDays(days);
        return Result.OK("修改成功");
    }
}
