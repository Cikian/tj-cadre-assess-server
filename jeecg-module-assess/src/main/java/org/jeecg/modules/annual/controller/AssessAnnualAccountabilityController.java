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
import org.jeecg.modules.annual.service.IAssessAnnualAccountabilityService;
import org.jeecg.modules.annual.service.IAssessWorkDaysService;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.dto.AssessAnnualAccountabilityDTO;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.AssessAnnualAccountability;
import org.jeecg.modules.system.entity.SysCategory;
import org.jeecg.modules.system.mapper.SysCategoryMapper;
import org.jeecg.modules.user.UserCommonApi;
import org.jeecg.modules.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Description: 年度考核受问责情况
 * @Author: jeecg-boot
 * @Date: 2024-09-02
 * @Version: V1.0
 */
@Api(tags = "年度考核受问责情况")
@RestController
@RequestMapping("/modules/annual/assessAnnualAccountability")
@Slf4j
public class AssessAnnualAccountabilityController extends JeecgController<AssessAnnualAccountability, IAssessAnnualAccountabilityService> {
    @Autowired
    private IAssessAnnualAccountabilityService accountabilityService;

    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private UserCommonApi userCommonApi;
    @Autowired
    private SysCategoryMapper sysCategoryMapper;

    /**
     * 分页列表查询
     *
     * @param assessAnnualAccountability
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "年度考核受问责情况-分页列表查询")
    @ApiOperation(value = "年度考核受问责情况-分页列表查询", notes = "年度考核受问责情况-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessAnnualAccountability>> queryPageList(AssessAnnualAccountability assessAnnualAccountability,
                                                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                   HttpServletRequest req) {
        QueryWrapper<AssessAnnualAccountability> queryWrapper = QueryGenerator.initQueryWrapper(assessAnnualAccountability, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");

        Map<String, String[]> parameterMap = new HashMap<>(req.getParameterMap());
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
        if (parameterMap.get("currentYear") == null) {
            if (currentAssessInfo != null) {
                queryWrapper.eq("current_year", currentAssessInfo.getCurrentYear());
            }
        }

        queryWrapper.ne("status", 1);

        Page<AssessAnnualAccountability> page = new Page<>(pageNo, pageSize);
        IPage<AssessAnnualAccountability> pageList = accountabilityService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param
     * @return
     */
    @AutoLog(value = "年度考核受问责情况-添加")
    @ApiOperation(value = "年度考核受问责情况-添加", notes = "年度考核受问责情况-添加")
    //@RequiresPermissions("org.jeecg:assess_annual_accountability:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessAnnualAccountability entry) {
        if (entry.getHashId() == null || entry.getHashId().isEmpty()) {
            String name = entry.getPersonnel();
            String sex = entry.getSex();
            Date birth = entry.getBirth();
            String birthStr = CommonUtils.dateToString(birth);
            String hashId = CommonUtils.hashId(name, sex, birthStr);
            entry.setHashId(hashId);
        }
        Date birthByHashId = CommonUtils.getBirthByHashId(entry.getHashId());
        entry.setBirth(birthByHashId);

        // Date endDate = entry.getEndDate();
        // endDate.setTime(endDate.getTime() + 86400000);
        // entry.setEndDate(endDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        // entry.setCurrentYear(sdf.format(entry.getEffectiveDate()));

        this.getRemark(entry);
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        List<String> roles = userCommonApi.getUserRolesByUserName(sysUser.getUsername());
        if (roles.contains("department_cadre_admin")) entry.setStatus(3);
        else entry.setStatus(1);

        String type = entry.getType();
        String[] split = type.split(",");
        String typeText = "";
        for (String s : split){
            SysCategory sysCategory = sysCategoryMapper.selectById(s);
            if (typeText.isEmpty()) typeText = typeText + sysCategory.getName();
             else typeText = typeText + "," + sysCategory.getName();
        }
        entry.setTypeText(typeText);

        accountabilityService.save(entry);
        accountabilityService.updateAnnualSummarySuggestion(entry);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param entry
     * @return
     */
    @AutoLog(value = "年度考核受问责情况-编辑")
    @ApiOperation(value = "年度考核受问责情况-编辑", notes = "年度考核受问责情况-编辑")
    //@RequiresPermissions("org.jeecg:assess_annual_accountability:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessAnnualAccountability entry) {
        // Date endDate = entry.getEndDate();
        // endDate.setTime(endDate.getTime() + 86400000);
        // entry.setEndDate(endDate);
        this.getRemark(entry);

        String type = entry.getType();
        String[] split = type.split(",");
        String typeText = "";
        for (String s : split){
            SysCategory sysCategory = sysCategoryMapper.selectById(s);
            if (typeText.isEmpty()) typeText = typeText + sysCategory.getName();
            else typeText = typeText + "," + sysCategory.getName();
        }
        entry.setTypeText(typeText);

        accountabilityService.updateById(entry);
        accountabilityService.updateAnnualSummarySuggestion(entry);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "年度考核受问责情况-通过id删除")
    @ApiOperation(value = "年度考核受问责情况-通过id删除", notes = "年度考核受问责情况-通过id删除")
    //@RequiresPermissions("org.jeecg:assess_annual_accountability:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        accountabilityService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "年度考核受问责情况-批量删除")
    @ApiOperation(value = "年度考核受问责情况-批量删除", notes = "年度考核受问责情况-批量删除")
    //@RequiresPermissions("org.jeecg:assess_annual_accountability:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.accountabilityService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "年度考核受问责情况-通过id查询")
    @ApiOperation(value = "年度考核受问责情况-通过id查询", notes = "年度考核受问责情况-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessAnnualAccountability> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessAnnualAccountability assessAnnualAccountability = accountabilityService.getById(id);
        if (assessAnnualAccountability == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessAnnualAccountability);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessAnnualAccountability
     */
    //@RequiresPermissions("org.jeecg:assess_annual_accountability:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessAnnualAccountability assessAnnualAccountability) {
        return super.exportXls(request, assessAnnualAccountability, AssessAnnualAccountability.class, "年度考核受问责情况");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresPermissions("assess_annual_accountability:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AssessAnnualAccountability.class);
    }

    @GetMapping(value = "/getCurrentDepart")
    public Result<Map<String, String>> getCurrentDepart() {
        return Result.OK(accountabilityService.getCurrentDepart());
    }

    @GetMapping(value = "/queryByLeaderIds")
    public Result<IPage<AssessAnnualAccountability>> getCurrentDepart(@RequestParam String leader, @RequestParam String year) {
        if (leader == null || year == null) {
            return null;
        }

        List<AssessAnnualAccountability> accountability = accountabilityService.queryByLeaderIds(leader, year);
        IPage<AssessAnnualAccountability> page = new Page<>();
        page.setRecords(accountability);

        return Result.OK(page);
    }

    @GetMapping("/accountabilityInformationQuery")
    public JSONObject getAccountabilityInformationExport(@RequestParam(defaultValue = "0") String currentYear, @RequestParam(defaultValue = "0") String depart) {
        List<AssessAnnualAccountability> accountabilityInformationQuery = accountabilityService.getAccountabilityInformationQuery(currentYear, depart);
        JSONObject json = new JSONObject();
        json.put("data", accountabilityInformationQuery);
        return json;
    }

    /**
     * 问责信息
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
        param.put("excelConfigId", "1013698024754290688");
        param.put("queryParam", queryParam);
        String filename = "问责信息.xlsx";
        assessCommonApi.getExportExcel(url, filename, param, response);

    }

    /*
     * 采用
     */
    @PostMapping("/adopt")
    public Result<String> adopt(@RequestBody AssessAnnualAccountability accountability) {
        accountability.setStatus(3);
        accountabilityService.updateById(accountability);
        accountabilityService.updateAnnualSummarySuggestion(accountability);
        return Result.OK("操作成功！");
    }

    /*
     * 不采用
     */
    @PostMapping("/noAdopt")
    public Result<String> noAdopt(@RequestBody AssessAnnualAccountability accountability) {
        accountability.setStatus(4);
        accountabilityService.updateById(accountability);
        accountabilityService.updateAnnualSummarySuggestion(accountability);
        return Result.OK("操作成功！");
    }

    private void getRemark(AssessAnnualAccountability entry) {
        // 某时因某事被某单位问责
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        String a = sdf.format(entry.getEffectiveDate());
        String type = entry.getTypeText();
        String content = a + "受" + type + "问责";
        entry.setRemark(content);
    }

}
