package org.jeecg.modules.report.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.sys.dto.*;
import org.jeecg.modules.sys.entity.report.AssessReportEvaluationItem;
import org.jeecg.modules.sys.entity.report.AssessReportEvaluationSummary;
import org.jeecg.modules.report.service.IAssessReportEvaluationItemService;
import org.jeecg.modules.report.service.IAssessReportEvaluationSummaryService;
import org.jeecg.modules.report.vo.AssessReportDemocracyFillVO;
import org.jeecg.modules.report.vo.AssessReportEvaluationSummaryPage;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.user.UserCommonApi;
import org.jeecg.modules.utils.CommonUtils;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * @Description: 一报告两评议民主测评汇总
 * @Author: jeecg-boot
 * @Date: 2024-10-08
 * @Version: V1.0
 */
@Api(tags = "一报告两评议民主测评汇总")
@RestController
@RequestMapping("/modules/report/reportDemocratic")
@Slf4j
public class AssessReportEvaluationSummaryController {
    @Autowired
    private IAssessReportEvaluationSummaryService summaryService;
    @Autowired
    private IAssessReportEvaluationItemService itemService;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private UserCommonApi userCommonApi;
    @Autowired
    private DepartCommonApi departCommonApi;

    /**
     * 分页列表查询
     *
     * @param assessReportEvaluationSummary
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "一报告两评议民主测评汇总-分页列表查询")
    @ApiOperation(value = "一报告两评议民主测评汇总-分页列表查询", notes = "一报告两评议民主测评汇总-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessReportEvaluationSummary>> queryPageList(AssessReportEvaluationSummary assessReportEvaluationSummary,
                                                                      @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                      @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                      HttpServletRequest req) {
        QueryWrapper<AssessReportEvaluationSummary> queryWrapper = QueryGenerator.initQueryWrapper(assessReportEvaluationSummary, req.getParameterMap());


        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<String> roles = userCommonApi.getUserRolesByUserName(sysUser.getUsername());
        if (!roles.contains("department_cadre_admin") && !roles.contains("gbc_cz") && !roles.contains("gbc_fcz")) {
            Map<String, String> currentUserDepart = departCommonApi.getCurrentUserDepart();
            queryWrapper.eq("depart", currentUserDepart.get("departId"));
        }

        Map<String, String[]> parameterMap = req.getParameterMap();
        if (parameterMap.get("currentYear") == null) {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("report");
            if (currentAssessInfo != null && currentAssessInfo.isAssessing()) {
                queryWrapper.eq("current_year", currentAssessInfo.getCurrentYear());
            }
        }

        List<String> userRolesByUserName = userCommonApi.getUserRolesByUserName(sysUser.getUsername());
        if (userRolesByUserName.size() <= 1 && userRolesByUserName.contains("temporary_user")) {
            queryWrapper.eq("depart", sysUser.getDepartIds());
        }
        queryWrapper.orderByAsc("ranking");
        Page<AssessReportEvaluationSummary> page = new Page<AssessReportEvaluationSummary>(pageNo, pageSize);
        IPage<AssessReportEvaluationSummary> pageList = summaryService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param assessReportEvaluationSummaryPage
     * @return
     */
    @AutoLog(value = "一报告两评议民主测评汇总-添加")
    @ApiOperation(value = "一报告两评议民主测评汇总-添加", notes = "一报告两评议民主测评汇总-添加")
    //@RequiresPermissions("org.jeecg:assess_report_evaluation_summary:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessReportEvaluationSummaryPage assessReportEvaluationSummaryPage) {
        AssessReportEvaluationSummary assessReportEvaluationSummary = new AssessReportEvaluationSummary();
        BeanUtils.copyProperties(assessReportEvaluationSummaryPage, assessReportEvaluationSummary);
        summaryService.saveMain(assessReportEvaluationSummary, assessReportEvaluationSummaryPage.getAssessReportEvaluationItemList());
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param assessReportEvaluationSummaryPage
     * @return
     */
    @AutoLog(value = "一报告两评议民主测评汇总-编辑")
    @ApiOperation(value = "一报告两评议民主测评汇总-编辑", notes = "一报告两评议民主测评汇总-编辑")
    //@RequiresPermissions("org.jeecg:assess_report_evaluation_summary:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessReportEvaluationSummaryPage assessReportEvaluationSummaryPage) {
        AssessReportEvaluationSummary assessReportEvaluationSummary = new AssessReportEvaluationSummary();
        BeanUtils.copyProperties(assessReportEvaluationSummaryPage, assessReportEvaluationSummary);
        AssessReportEvaluationSummary assessReportEvaluationSummaryEntity = summaryService.getById(assessReportEvaluationSummary.getId());
        if (assessReportEvaluationSummaryEntity == null) {
            return Result.error("未找到对应数据");
        }
        summaryService.updateMain(assessReportEvaluationSummary, assessReportEvaluationSummaryPage.getAssessReportEvaluationItemList());
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "一报告两评议民主测评汇总-通过id删除")
    @ApiOperation(value = "一报告两评议民主测评汇总-通过id删除", notes = "一报告两评议民主测评汇总-通过id删除")
    //@RequiresPermissions("org.jeecg:assess_report_evaluation_summary:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        summaryService.delMain(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "一报告两评议民主测评汇总-批量删除")
    @ApiOperation(value = "一报告两评议民主测评汇总-批量删除", notes = "一报告两评议民主测评汇总-批量删除")
    //@RequiresPermissions("org.jeecg:assess_report_evaluation_summary:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.summaryService.delBatchMain(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "一报告两评议民主测评汇总-通过id查询")
    @ApiOperation(value = "一报告两评议民主测评汇总-通过id查询", notes = "一报告两评议民主测评汇总-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessReportEvaluationSummary> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessReportEvaluationSummary assessReportEvaluationSummary = summaryService.getById(id);
        if (assessReportEvaluationSummary == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessReportEvaluationSummary);

    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "一报告两评议民主测评项通过主表ID查询")
    @ApiOperation(value = "一报告两评议民主测评项主表ID查询", notes = "一报告两评议民主测评项-通主表ID查询")
    @GetMapping(value = "/queryAssessReportEvaluationItemByMainId")
    public Result<List<AssessReportEvaluationItem>> queryAssessReportEvaluationItemListByMainId(@RequestParam(name = "id", required = true) String id) {
        List<AssessReportEvaluationItem> assessReportEvaluationItemList = itemService.selectByMainId(id);
        return Result.OK(assessReportEvaluationItemList);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessReportEvaluationSummary
     */
    //@RequiresPermissions("org.jeecg:assess_report_evaluation_summary:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessReportEvaluationSummary assessReportEvaluationSummary) {
        // Step.1 组装查询条件查询数据
        QueryWrapper<AssessReportEvaluationSummary> queryWrapper = QueryGenerator.initQueryWrapper(assessReportEvaluationSummary, request.getParameterMap());
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        // 配置选中数据查询条件
        String selections = request.getParameter("selections");
        if (oConvertUtils.isNotEmpty(selections)) {
            List<String> selectionList = Arrays.asList(selections.split(","));
            queryWrapper.in("id", selectionList);
        }
        // Step.2 获取导出数据
        List<AssessReportEvaluationSummary> assessReportEvaluationSummaryList = summaryService.list(queryWrapper);

        // Step.3 组装pageList
        List<AssessReportEvaluationSummaryPage> pageList = new ArrayList<AssessReportEvaluationSummaryPage>();
        for (AssessReportEvaluationSummary main : assessReportEvaluationSummaryList) {
            AssessReportEvaluationSummaryPage vo = new AssessReportEvaluationSummaryPage();
            BeanUtils.copyProperties(main, vo);
            List<AssessReportEvaluationItem> assessReportEvaluationItemList = itemService.selectByMainId(main.getId());
            vo.setAssessReportEvaluationItemList(assessReportEvaluationItemList);
            pageList.add(vo);
        }

        // Step.4 AutoPoi 导出Excel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        mv.addObject(NormalExcelConstants.FILE_NAME, "一报告两评议民主测评汇总列表");
        mv.addObject(NormalExcelConstants.CLASS, AssessReportEvaluationSummaryPage.class);
        mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("一报告两评议民主测评汇总数据", "导出人:" + sysUser.getRealname(), "一报告两评议民主测评汇总"));
        mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
        return mv;
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresPermissions("org.jeecg:assess_report_evaluation_summary:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<AssessReportEvaluationSummaryPage> list = ExcelImportUtil.importExcel(file.getInputStream(), AssessReportEvaluationSummaryPage.class, params);
                for (AssessReportEvaluationSummaryPage page : list) {
                    AssessReportEvaluationSummary po = new AssessReportEvaluationSummary();
                    BeanUtils.copyProperties(page, po);
                    summaryService.saveMain(po, page.getAssessReportEvaluationItemList());
                }
                return Result.OK("文件导入成功！数据行数:" + list.size());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Result.error("文件导入失败:" + e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Result.OK("文件导入失败！");
    }

    @AutoLog(value = "一报告两评议民主测评-发起")
    @ApiOperation(value = "一报告两评议民主测评-发起", notes = "一报告两评议民主测评-发起")
    @PostMapping(value = "/init")
    public Result<?> initAssess(@RequestBody DemocraticInitDTO democraticDTO) {
        boolean b = summaryService.initDemocratic(democraticDTO);
        if (b) {
            List<SysUser> sysUsers = userCommonApi.generateAnonymousAccount("report", democraticDTO.getCurrentYear());
            IPage<SysUser> page = new Page<>();
            page.setRecords(sysUsers);
            return Result.OK("发起成功！", page);
        }
        return Result.OK("发起成功！");
    }

    @AutoLog(value = "一报告两评议民主测评-提交")
    @ApiOperation(value = "一报告两评议民主测评-提交", notes = "一报告两评议民主测评-提交")
    @PostMapping(value = "/submit")
    public Result<String> submit(@RequestBody AssessReportDemocracyFillVO fillVO) {
        summaryService.saveDemocracy(fillVO);
        return Result.OK("提交成功！");
    }

    @GetMapping("/getFillData")
    public Result<?> getFillData(@RequestParam(name = "summaryId", required = true) String summaryId) {
        Map<String, List<?>> fillData = summaryService.getFillData(summaryId);
        return Result.OK(fillData);
    }

    @GetMapping("/getNewLeaderSummary")
    public Result<IPage<ReportNewLeaderDTO>> getNewLeaderSummaryList(@RequestParam(name = "year", required = true, defaultValue = "0") String year,
                                                                     @RequestParam(name = "var1", required = true, defaultValue = "re") String clo,
                                                                     @RequestParam(name = "var2", required = true, defaultValue = "desc") String order) {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("report");
        if (currentAssessInfo == null || !currentAssessInfo.isAssessing()) return Result.error("当前不在考核期间");
        if ("0".equals(year)) {
            year = currentAssessInfo.getCurrentYear();
        }
        List<ReportNewLeaderDTO> list = summaryService.getNewLeaderSummaryByYear(year, clo, order);
        IPage<ReportNewLeaderDTO> page = new Page<>();
        page.setRecords(list);

        return Result.OK(page);
    }

    @GetMapping("/getNewLeaderSummaryData")
    public JSONObject getNewLeaderSummaryData4Excel(@RequestParam(name = "year", required = true, defaultValue = "0") String year,
                                                    @RequestParam(name = "var1", required = true, defaultValue = "re") String clo,
                                                    @RequestParam(name = "var2", required = true, defaultValue = "desc") String order) {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("report");
        if (currentAssessInfo == null || !currentAssessInfo.isAssessing()) return null;
        if ("0".equals(year)) {
            year = currentAssessInfo.getCurrentYear();
        }
        List<ReportNewLeaderDTO> list = summaryService.getNewLeaderSummaryByYear(year, clo, order);
        int sort = 1;
        for (ReportNewLeaderDTO dto : list) {
            dto.setSort(sort++);
        }
        JSONObject json = new JSONObject();
        json.put("data", list);

        return json;
    }


    @PostMapping("/exportExcel")
    public void exportExcel(@Value("${server.port}") String port,
                            @RequestParam(name = "year", required = true, defaultValue = "0") String year,
                            @RequestParam(name = "var1", required = true, defaultValue = "re") String clo,
                            @RequestParam(name = "var2", required = true, defaultValue = "desc") String order,
                            HttpServletRequest request, HttpServletResponse response) {
        String url = "http://localhost:" + port + "/jmreport/exportAllExcelStream" + "?token=" + TokenUtils.getTokenByRequest(request);

        JSONObject queryParam = new JSONObject();
        if ("0".equals(year)) {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("report");
            if (currentAssessInfo != null) year = currentAssessInfo.getCurrentYear();
        }
        queryParam.put("year", year);
        queryParam.put("var1", clo);
        queryParam.put("var2", order);

        Map<String, Object> param = new JSONObject();
        param.put("excelConfigId", "1021553534239821824");
        param.put("queryParam", queryParam);

        String a = "";
        String b = "";
        if ("re".equals(clo)) {
            a = "认同率";
        } else {
            a = "不认同率";
        }
        if ("desc".equals(order)) {
            b = "降序";
        } else {
            b = "升序";
        }
        String filename = year + "年度新提拔干部评议信息（" + a + b + "）.xlsx";
        assessCommonApi.getExportExcel(url, filename, param, response);
    }

    @GetMapping("/exportEnterpriseAgreeRank")
    public void exportEnterpriseAgreeRank(@RequestParam(name = "currentYear", required = false) String currentYear,@Value("${server.port}") String port, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isEmpty(currentYear)){
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("report");
            if (currentAssessInfo != null&& StringUtils.isNotEmpty(currentAssessInfo.getCurrentYear())) {
                currentYear = currentAssessInfo.getCurrentYear();
            }else {
                return;
            }
        }
        String url = "http://localhost:" + port + "/jmreport/exportAllExcelStream" + "?token=" + TokenUtils.getTokenByRequest(request);
        JSONObject queryParam = new JSONObject();
        queryParam.put("currentYear", currentYear);
        Map<String, Object> param = new JSONObject();
        param.put("excelConfigId", "1051297958776782848");
        param.put("queryParam", queryParam);

        String fileName = "本年度新提拔干部民主评议排名.xlsx";
        assessCommonApi.getExportExcel(url, fileName, param, response);
    }

    @GetMapping("/getEnterpriseAgreeRankByYear")
    public JSONObject getEnterpriseAgreeRankByYear(@RequestParam(name = "currentYear", required = true) String currentYear) {
        List<ReportEnterpriseAgreeRankDTO> list = summaryService.getEnterpriseAgreeRankByYear(currentYear);
        JSONObject json = new JSONObject();
        json.put("data", list);
        return json;
    }

    @GetMapping("/exportLeaderWorkRank")
    public void exportLeaderWorkRank(@RequestParam(name = "currentYear", required = false) String currentYear,@Value("${server.port}") String port, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isEmpty(currentYear)){
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("report");
            if (currentAssessInfo != null&& StringUtils.isNotEmpty(currentAssessInfo.getCurrentYear())) {
                currentYear = currentAssessInfo.getCurrentYear();
            }else {
                return;
            }
        }
        String url = "http://localhost:" + port + "/jmreport/exportAllExcelStream" + "?token=" + TokenUtils.getTokenByRequest(request);
        JSONObject queryParam = new JSONObject();
        queryParam.put("currentYear", currentYear);
        Map<String, Object> param = new JSONObject();
        param.put("excelConfigId", "1053903768778424320");
        param.put("queryParam", queryParam);

        String fileName = "年度干部选拔任用工作民主评议结果排名.xlsx";
        assessCommonApi.getExportExcel(url, fileName, param, response);
    }

    @GetMapping("/getLeaderWorkRankByQuestion1")
    public JSONObject getLeaderWorkRankByQuestion1(@RequestParam(name = "currentYear", required = true) String currentYear) {
        List<ReportLeaderWorkRankDTO> list = summaryService.getLeaderWorkRankByQuestion1(currentYear);
        JSONObject json = new JSONObject();
        json.put("data", list);
        return json;
    }

    @GetMapping("/getLeaderWorkRankByQuestion2")
    public JSONObject getLeaderWorkRankByQuestion2(@RequestParam(name = "currentYear", required = true) String currentYear) {
        List<ReportLeaderWorkRankDTO> list = summaryService.getLeaderWorkRankByQuestion2(currentYear);
        JSONObject json = new JSONObject();
        json.put("data", list);
        return json;
    }

    @GetMapping("/getLeaderWorkRankByQuestion3")
    public JSONObject getLeaderWorkRankByQuestion3(@RequestParam(name = "currentYear", required = true) String currentYear) {
        List<ReportLeaderWorkRankDTO> list = summaryService.getLeaderWorkRankByQuestion3(currentYear);
        JSONObject json = new JSONObject();
        json.put("data", list);
        return json;
    }

    @GetMapping("/exportLeaderWorkDetails")
    public void exportLeaderWorkDetails(@RequestParam(name = "currentYear", required = false) String currentYear,@Value("${server.port}") String port, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isEmpty(currentYear)){
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("report");
            if (currentAssessInfo != null&& StringUtils.isNotEmpty(currentAssessInfo.getCurrentYear())) {
                currentYear = currentAssessInfo.getCurrentYear();
            }else {
                return;
            }
        }
        String url = "http://localhost:" + port + "/jmreport/exportAllExcelStream" + "?token=" + TokenUtils.getTokenByRequest(request);
        JSONObject queryParam = new JSONObject();
        queryParam.put("currentYear", currentYear);
        Map<String, Object> param = new JSONObject();
        param.put("excelConfigId", "1054545886735745024");
        param.put("queryParam", queryParam);

        String fileName = "干部选拔任用工作民主评议结果汇总表.xlsx";
        assessCommonApi.getExportExcel(url, fileName, param, response);
    }

    @GetMapping("/getLeaderWorkDetails")
    public JSONObject getLeaderWorkDetails(@RequestParam(name = "currentYear", required = true) String currentYear) {
        List<ReportLeaderWorkDetailsDTO> list = summaryService.getLeaderWorkDetailsData(currentYear);
        JSONObject json = new JSONObject();
        json.put("data", list);
        return json;
    }

    @GetMapping("/exportLeaderDataByDepart")
    public void exportLeaderDataByDepart(@RequestParam(name = "currentYear", required = false) String currentYear,@Value("${server.port}") String port, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isEmpty(currentYear)){
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("report");
            if (currentAssessInfo != null&& StringUtils.isNotEmpty(currentAssessInfo.getCurrentYear())) {
                currentYear = currentAssessInfo.getCurrentYear();
            }else {
                return;
            }
        }
        String url = "http://localhost:" + port + "/jmreport/exportAllExcelStream" + "?token=" + TokenUtils.getTokenByRequest(request);
        List<Map<String, Object>> params=new LinkedList<>();
        LambdaQueryWrapper<AssessReportEvaluationSummary> lqw = new LambdaQueryWrapper<>();
        lqw.select(AssessReportEvaluationSummary::getId,AssessReportEvaluationSummary::getCurrentYear,AssessReportEvaluationSummary::getDepart);
        lqw.eq(AssessReportEvaluationSummary::getCurrentYear, currentYear);
        List<AssessReportEvaluationSummary> list=summaryService.list(lqw);
        for (AssessReportEvaluationSummary assessReportEvaluationSummary:
                list ) {
            JSONObject queryParam = new JSONObject();
            queryParam.put("currentYear", currentYear);
            queryParam.put("depart", assessReportEvaluationSummary.getDepart());
            Map<String, Object> param = new JSONObject();
            param.put("excelConfigId", "1057510019395469312");
            param.put("queryParam", queryParam);
            params.add(param);
        }
        String fileName = currentYear+"年度各单位新提拔干部民主测评明细.zip";
        assessCommonApi.exportZipWithExcel(url, fileName, params, response);
    }

    @GetMapping("/getLeaderDataByDepart")
    public JSONObject getLeaderDataByDepart(@RequestParam(name = "currentYear", required = true) String currentYear,@RequestParam(name = "depart", required = true) String depart) {
        List<ReportDepartLeaderDataDTO> list = summaryService.getLeaderDataByDepart(currentYear,depart);
        JSONObject json = new JSONObject();
        json.put("data", list);
        return json;
    }

    @PutMapping("/rerank")
    public Result<?> reRank() {
        summaryService.reRanking();
        return Result.OK("重新计算成功！");
    }


}
