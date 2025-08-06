package org.jeecg.modules.business.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.handler.IFillRuleHandler;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.SysDepartModel;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.modules.annual.service.IAssessAnnualFillService;
import org.jeecg.modules.annual.service.IAssessAnnualSummaryService;
import org.jeecg.modules.business.service.IAssessBusinessCommendService;
import org.jeecg.modules.business.service.IAssessBusinessDenounceService;
import org.jeecg.modules.business.service.IAssessBusinessGradeService;
import org.jeecg.modules.business.service.impl.AssessBusinessFillItemServiceImpl;
import org.jeecg.modules.business.vo.ExportGradeVO;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.dto.AssessBusinessDepartGradeDTO;
import org.jeecg.modules.sys.dto.AssessBusinessGradeDTO;
import org.jeecg.modules.sys.dto.GradeExportDTO;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.business.AssessBusinessCommend;
import org.jeecg.modules.sys.entity.business.AssessBusinessDenounce;
import org.jeecg.modules.sys.entity.business.AssessBusinessFillItem;
import org.jeecg.modules.sys.entity.business.AssessBusinessGrade;
import org.jeecg.modules.sys.mapper.business.AssessBusinessGradeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 业务考核成绩汇总
 * @Author: jeecg-boot
 * @Date: 2024-08-28
 * @Version: V1.0
 */
@Api(tags = "业务考核成绩汇总")
@RestController
@RequestMapping("/modules/assessBusinessGrade")
@Slf4j
public class AssessBusinessGradeController extends JeecgController<AssessBusinessGrade, IAssessBusinessGradeService> {
    @Autowired
    private IAssessBusinessGradeService gradeService;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private DepartCommonApi departCommonApi;
    @Autowired
    private AssessBusinessGradeMapper gradeMapper;
    @Autowired
    private IAssessBusinessCommendService assessBusinessCommendService;
    @Autowired
    private IAssessBusinessDenounceService assessBusinessDenounceService;
    @Autowired
    private AssessBusinessFillItemServiceImpl assessBusinessFillItemService;
    @Autowired
    private IAssessAnnualFillService annualFillService;


    /**
     * 分页列表查询
     *
     * @param assessBusinessGrade
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "业务考核成绩汇总-分页列表查询")
    @ApiOperation(value = "业务考核成绩汇总-分页列表查询", notes = "业务考核成绩汇总-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessBusinessGrade>> queryPageList(AssessBusinessGrade assessBusinessGrade,
                                                            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                            HttpServletRequest req) {
        QueryWrapper<AssessBusinessGrade> queryWrapper = QueryGenerator.initQueryWrapper(assessBusinessGrade, req.getParameterMap());
        Map<String, String[]> parameterMap = req.getParameterMap();
        if (parameterMap.get("currentYear") == null) {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("business");
            if (currentAssessInfo != null && currentAssessInfo.isAssessing()) {
                queryWrapper.eq("current_year", currentAssessInfo.getCurrentYear());
            }
        }
        queryWrapper.orderByDesc("current_year").orderByAsc("ranking");
        Page<AssessBusinessGrade> page = new Page<AssessBusinessGrade>(pageNo, pageSize);
        IPage<AssessBusinessGrade> pageList = gradeService.page(page, queryWrapper);
        pageList.setRecords(gradeService.sort(pageList.getRecords()));
        return Result.OK(pageList);
    }


    /**
     * 根据部门查询
     *
     * @param assessBusinessGrade
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "业务考核成绩汇总-根据部门查询", notes = "业务考核成绩汇总-根据部门查询")
    @GetMapping(value = "/searchByDeparts")
    public Result<IPage<AssessBusinessGrade>> searchByDeparts(AssessBusinessGrade assessBusinessGrade,
                                                            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                            HttpServletRequest req) {
        QueryWrapper<AssessBusinessGrade> queryWrapper = QueryGenerator.initQueryWrapper(assessBusinessGrade, req.getParameterMap());
        Map<String, String[]> parameterMap = req.getParameterMap();
        if (parameterMap.get("currentYear") == null) {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("business");
            if (currentAssessInfo != null && currentAssessInfo.isAssessing()) {
                queryWrapper.eq("current_year", currentAssessInfo.getCurrentYear());
            }
        }
        queryWrapper.orderByDesc("current_year").orderByAsc("ranking");

        List<AssessBusinessGrade> assessBusinessGrades = gradeService.list(queryWrapper);
        int ranking = 1;
        int ranking1 = 1;
        for(int i=0;i<assessBusinessGrades.size();i++){
            AssessBusinessGrade assessBusinessGrade1 = assessBusinessGrades.get(i);
            if(i>0){
                AssessBusinessGrade assessBusinessGrade2= assessBusinessGrades.get(i - 1);
                if(assessBusinessGrade1.getAssessScore().compareTo(assessBusinessGrade2.getAssessScore())==0){
                    assessBusinessGrade1.setRanking(ranking);
                }else{
                    ranking=ranking1;
                    assessBusinessGrade1.setRanking(ranking1);
                }
            }else{
                assessBusinessGrade1.setRanking(ranking);
            }
            ranking1++;
        }
        IPage<AssessBusinessGrade> page = new Page<AssessBusinessGrade>(pageNo, pageSize,assessBusinessGrades.size());
        page.setRecords(assessBusinessGrades);
        return Result.OK(page);
    }


    /**
     * 添加
     *
     * @param assessBusinessGrade
     * @return
     */
    @AutoLog(value = "业务考核成绩汇总-添加")
    @ApiOperation(value = "业务考核成绩汇总-添加", notes = "业务考核成绩汇总-添加")
    //@RequiresPermissions("org.jeecg:assess_business_grade:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessBusinessGrade assessBusinessGrade) {
        gradeService.save(assessBusinessGrade);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param assessBusinessGrade
     * @return
     */
    @AutoLog(value = "业务考核成绩汇总-编辑")
    @ApiOperation(value = "业务考核成绩汇总-编辑", notes = "业务考核成绩汇总-编辑")
    //@RequiresPermissions("org.jeecg:assess_business_grade:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessBusinessGrade assessBusinessGrade) {
        Result<String> res = gradeService.edit(assessBusinessGrade);
        annualFillService.getEvaluationSuggestionByBusiness(assessBusinessGrade.getDepartId(), assessBusinessGrade.getCurrentYear());

        return res;
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "业务考核成绩汇总-通过id删除")
    @ApiOperation(value = "业务考核成绩汇总-通过id删除", notes = "业务考核成绩汇总-通过id删除")
    //@RequiresPermissions("org.jeecg:assess_business_grade:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        gradeService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "业务考核成绩汇总-批量删除")
    @ApiOperation(value = "业务考核成绩汇总-批量删除", notes = "业务考核成绩汇总-批量删除")
    //@RequiresPermissions("org.jeecg:assess_business_grade:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.gradeService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "业务考核成绩汇总-通过id查询")
    @ApiOperation(value = "业务考核成绩汇总-通过id查询", notes = "业务考核成绩汇总-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessBusinessGrade> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessBusinessGrade assessBusinessGrade = gradeService.getById(id);
        if (assessBusinessGrade == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessBusinessGrade);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessBusinessGrade
     */
    //@RequiresPermissions("org.jeecg:assess_business_grade:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessBusinessGrade assessBusinessGrade) {
        return super.exportXls(request, assessBusinessGrade, AssessBusinessGrade.class, "业务考核成绩汇总");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresPermissions("assess_business_grade:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AssessBusinessGrade.class);
    }

    @GetMapping("/proportion")
    public Result<Map<String, BigDecimal>> getProportionCount(@RequestParam String year, @RequestParam String type) {
        Map<String, BigDecimal> proportionCount = gradeService.getProportionCount(year, type);
        if (proportionCount == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(proportionCount);
    }

    @GetMapping("/getByDepartIdAndYear")
    public Result<IPage<AssessBusinessGrade>> getByDepartIdAndYear(@RequestParam String departId, @RequestParam String year) {
        List<AssessBusinessGrade> byDepartIdAndYear = gradeService.getByDepartIdAndYear(departId, year);
        IPage<AssessBusinessGrade> page = new Page<>();
        page.setRecords(byDepartIdAndYear);
        return Result.OK(page);
    }

    @GetMapping("/reportQuery")
    public JSONObject getExportSummaryData(@RequestParam(defaultValue = "0") String currentYear, @RequestParam(defaultValue = "0") String exportScope) {
        List<AssessBusinessGradeDTO> reportQuery = gradeService.getReportQuery(currentYear, Arrays.asList(exportScope.split(",")));
        JSONObject json = new JSONObject();
        json.put("data", reportQuery);
        return json;
    }

    @GetMapping("/departGradeQuery")
    public JSONObject getDepartGradeExportData(@RequestParam(defaultValue = "0") String currentYear, @RequestParam(defaultValue = "0") String departId,@RequestParam(defaultValue = "0") String exportDepart) {
        AssessBusinessDepartGradeDTO reportQuery = gradeService.getDepartReportQuery(currentYear, departId,exportDepart);
        List<AssessBusinessDepartGradeDTO> list = new ArrayList<>();
        list.add(reportQuery);
        JSONObject json = new JSONObject();
        json.put("data", list);
        return json;
    }

    @PostMapping("/exportPdf")
    public void exportPdf(@RequestBody ExportGradeVO options, @Value("${server.port}") String port, HttpServletRequest request, HttpServletResponse response) {
        String url = "http://localhost:" + port + "/jmreport/exportPdfStream" + "?token=" + TokenUtils.getTokenByRequest(request);
        String currentYear = options.getCurrentYear();
        boolean exportDepart = options.isExportDepart();
        boolean merge = options.isMerge();
        List<AssessBusinessGradeDTO> departmentIdsList=gradeMapper.getGradeListByRank(currentYear);
        Map<String, String> folders = new HashMap<>();
        folders.put("1", "局机关各处室");
        folders.put("2", "分局");
        folders.put("3", "事业单位");
        folders.put("4", "参公");
        String isExportDepart=exportDepart?"1":"0";
        List<Map<String, Object>> paramList = new LinkedList<>();
        for (AssessBusinessGradeDTO assessBusinessGradeDTO : departmentIdsList) {
            String departId=assessBusinessGradeDTO.getDepartId();
            Map<String, Object> param = new JSONObject();
            JSONObject queryParam = new JSONObject();
            queryParam.put("departId", departId);
            queryParam.put("currentYear", currentYear);
            queryParam.put("exportDepart", isExportDepart);
            param.put("excelConfigId", "1006442863012007936");
            param.put("queryParam", queryParam);
            paramList.add(param);
        }

        if (merge) {
            String filename = currentYear + "年度业务考核各单位考核结果（合并）.pdf";
            assessCommonApi.getMergePdf(url, filename, paramList, response);
        } else {
            String filename = currentYear + "年度业务考核各单位考核结果.zip";
            assessCommonApi.getExportZip(url, filename, "年度业务考核结果.pdf", paramList, folders, response);
        }
    }

    @GetMapping("/hasBusnessGrade")
    public Result<String> hasBusnessGrade(@RequestParam(defaultValue = "0") String currentYear) {
        if ("0".equals(currentYear)){
            return Result.error("请选择导出的年份！");
        }
        LambdaQueryWrapper<AssessBusinessGrade> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AssessBusinessGrade::getCurrentYear, currentYear);
        if (gradeService.count(lambdaQueryWrapper)==0){
            return Result.error("当前年度无业务考核数据！");
        }
        return Result.OK("success！");
    }

    @GetMapping("/hasBusnessGradeBetween")
    public Result<String> hasBusnessGradeBetween(@RequestParam(defaultValue = "0",name = "startYear") String startYear,@RequestParam(defaultValue = "0",name = "endYear") String endYear) {
        if ("0".equals(startYear)||"0".equals(endYear)){
            return Result.error("请选择导出的年份！");
        }
        List<AssessBusinessGrade> gradesList = gradeService.getGradesBetweenYears(startYear, endYear);
        if (gradesList.size()==0){
            return Result.error("当前年度无业务考核数据！");
        }
        return Result.OK("success！");
    }

    @GetMapping("/getNonAssess")
    public Result<List<SysDepartModel>> getNonAssessDepart(@RequestParam String year) {
        List<SysDepartModel> nonAssessDepart = gradeService.getNonAssessDepart(year);
        return Result.OK(nonAssessDepart);
    }


    /**
     * 业务考核导出汇总
     */
    @AutoLog(value = "业务考核导出汇总")
    @ApiOperation(value = "业务考核导出汇总", notes = "业务考核导出汇总")
    @GetMapping(value = "/getSummary")
    public JSONObject getSummary(@RequestParam String type,@RequestParam String year) {

        // 表彰
        LambdaQueryWrapper<AssessBusinessCommend> commendLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commendLambdaQueryWrapper.eq(AssessBusinessCommend::getCurrentYear, year);
        List<AssessBusinessCommend> commends = assessBusinessCommendService.list(commendLambdaQueryWrapper);

        // 通报
        LambdaQueryWrapper<AssessBusinessDenounce> denounceLambdaQueryWrapper = new LambdaQueryWrapper<>();
        denounceLambdaQueryWrapper.eq(AssessBusinessDenounce::getCurrentYear, year);
        List<AssessBusinessDenounce> denounces = assessBusinessDenounceService.list(denounceLambdaQueryWrapper);

        // 将type按，分给为list
        List<String> typeList = Arrays.asList(type.split(","));

        List<GradeExportDTO> summaryList = gradeMapper.getSummary(year, typeList);

        //排序
        List<SysDepartModel> allDepart = departCommonApi.queryAllDepart();
        Map<String, Integer> departMap = allDepart.stream().collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getDepartOrder));
        for (GradeExportDTO item : summaryList) {
            item.setDepartOrder(departMap.get(item.getDepartId()));
        }
        List<GradeExportDTO> sortedList = summaryList.stream().sorted(Comparator.comparing(GradeExportDTO::getRanking).thenComparing(GradeExportDTO::getDepartOrder)).collect(Collectors.toList());//升序排序

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");

        List<GradeExportDTO> list = new ArrayList<>();

        // 遍历 summaryList 并进行匹配
        int index = 1;
        for (GradeExportDTO summary : sortedList) {
            String departId = summary.getDepartId();
            switch (summary.getLevel()) {
                case "1":
                    summary.setLevel("优秀");
                    break;
                case "2":
                    summary.setLevel("良好");
                    break;
                case "3":
                    summary.setLevel("一般");
                    break;
                default:
                    summary.setLevel(""); // 为空
                    break;
            }
            // 匹配表彰
            StringBuilder commendBuilder = new StringBuilder();
            for (AssessBusinessCommend commend : commends) {
                if (commend.getDepartmentCode().equals(departId)) {
                    if (commendBuilder.length() > 0) {
                        commendBuilder.append("\n");
                    }
                    commendBuilder.append(sdf.format(commend.getCommendTime()) + summary.getAlias() + "受" + commend.getCommendUnit() + commend.getCommendProject() + "的表彰");
                }
            }
            summary.setCommend(commendBuilder.toString());

            // 匹配通报
            StringBuilder denounceBuilder = new StringBuilder();
            for (AssessBusinessDenounce denounce : denounces) {
                if (denounce.getDepartmentCode().equals(departId)) {
                    if (denounceBuilder.length() > 0) {
                        denounceBuilder.append("\n");
                    }
                    denounceBuilder.append(sdf.format(denounce.getDenounceTime()) + summary.getAlias() + "受" + denounce.getDenounceUnit() + denounce.getDenounceProject() + "的通报");
                }
            }
            summary.setDenounce(denounceBuilder.toString());

            // 设置 id,将 index 转为字符串并设置为 id
            summary.setId(String.valueOf(index++));
            list.add(summary);
        }

        // 将处理后的 summaryList 放入 JSON 对象中
        JSONObject json = new JSONObject();
        json.put("data", list);
        return json;
    }

    @GetMapping("/exportExcelByParam")
    public void exportExcelByParam(@Value("${server.port}") String port,@RequestParam(name = "currentYear") String currentYear,@RequestParam(name = "departIds") String departIds, HttpServletRequest request, HttpServletResponse response) {
        String url = "http://localhost:" + port + "/jmreport/exportAllExcelStream" + "?token=" + TokenUtils.getTokenByRequest(request);
        JSONObject queryParam = new JSONObject();
        queryParam.put("currentYear",currentYear);
        queryParam.put("departId",departIds);
        Map<String, Object> param = new JSONObject();
        param.put("excelConfigId", "1032220034073288704");
        param.put("queryParam", queryParam);
        String filename = "业务考核成绩.xlsx";
        assessCommonApi.getExportExcel(url, filename, param, response);
    }

    @GetMapping("/reportQueryByParam")
    public JSONObject reportQueryByParam(AssessBusinessGrade assessBusinessGrade,@RequestParam(name = "currentYear") String currentYear,@RequestParam(name = "departId") String departIds,HttpServletRequest req) {
        LambdaQueryWrapper<AssessBusinessGrade> lambdaQueryWrapper=new LambdaQueryWrapper();
        List<String> list=Arrays.asList(departIds.split(","));
        lambdaQueryWrapper.eq(AssessBusinessGrade::getCurrentYear,currentYear).in(AssessBusinessGrade::getDepartId,list);
        lambdaQueryWrapper.orderByAsc(AssessBusinessGrade::getRanking);
        List<AssessBusinessGrade> reportQuery = gradeMapper.selectList(lambdaQueryWrapper);
        JSONObject json = new JSONObject();
        json.put("data", reportQuery);
        return json;
    }

    @GetMapping("/exportExcel")
    public void exportExcel(@Value("${server.port}") String port,@RequestParam(name = "year") String year, HttpServletRequest request, HttpServletResponse response) {
        String url = "http://localhost:" + port + "/jmreport/exportAllExcelStream" + "?token=" + TokenUtils.getTokenByRequest(request);
        List<Map<String, Object>> paramList = new ArrayList<>();
        List<String> entryNames = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            JSONObject queryParam = new JSONObject();
            Map<String, Object> param = new JSONObject();
            queryParam.put("year", year);
            param.put("excelConfigId", "1023479759155142656");
            if (i == 0) {
                entryNames.add("局机关各处室业务工作测评成绩.xlsx");
                queryParam.put("type", "1");
            } else if (i == 1) {
                entryNames.add("分局业务工作测评成绩.xlsx");
                queryParam.put("type", "2");
            } else {
                entryNames.add("事业单位+参公业务工作测评成绩.xlsx");
                queryParam.put("type", "3,4");
            }
            param.put("queryParam", queryParam);
            paramList.add(param);
        }
        assessCommonApi.getExportZipWithExcel(url, "业务工作测评成绩汇总（含加减分）.zip", entryNames, paramList, response);

    }

    private String getCommendInfo(String departId, String year) {
        List<AssessBusinessCommend> commends = assessBusinessCommendService.listByDepartmentCodeAndYear(departId, year);
        return commends.stream()
                .map(commend -> commend.getCommendTime() + "受到" + commend.getCommendUnit() + "来自" + commend.getCommendProject() + "的表彰")
                .collect(Collectors.joining("\n"));
    }

    private String getDenounceInfo(String departId, String year) {
        List<AssessBusinessDenounce> denounces = assessBusinessDenounceService.listByDepartmentCodeAndYear(departId, year);
        return denounces.stream()
                .map(denounce -> denounce.getDenounceTime() + "受到" + denounce.getDenounceUnit() + "来自" + denounce.getDenounceProject() + "的通报")
                .collect(Collectors.joining("\n"));
    }

    private CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }


    /**
     * 考核明细导出模板
     */
    @AutoLog(value = "考核明细导出模板")
    @ApiOperation(value = "考核明细导出模板", notes = "考核明细导出模板")
    @PostMapping("/assessmentDetailExport")
    public void assessmentDetailExport(@RequestBody Map<String, String> params, HttpServletResponse response) {
        String year = params.get("year");
        // 机关或基层
        Map<String,List<AssessBusinessGrade>> dataMap=new LinkedHashMap<>();
        List<AssessBusinessGrade> jgGradeList = gradeService.getGradeListByType("1",year);
        List<AssessBusinessGrade> jcGradeList = gradeService.getGradeListByTypeNotEqualTo("1",year);
        List<AssessBusinessGrade> jgSortGradeList = gradeService.sort(jgGradeList);
        List<AssessBusinessGrade> jcSortGradeList = gradeService.sort(jcGradeList);
        dataMap.put("1",jgSortGradeList);
        dataMap.put("2",jcSortGradeList);
        // 区分处室
        List<String> jgDepartAliases = gradeService.getDepartAliases("机关");
        List<String> jcDepartAliases = gradeService.getDepartAliases("基层");

        // 创建 Excel 文件
        HSSFWorkbook workbook = new HSSFWorkbook();

        for (String type:
        dataMap.keySet()) {
            List<AssessBusinessGrade> gradeList =dataMap.get(type);
            List<String> departAliases=null;
            String sheetName=null;
            if ("1".equals(type)){
                sheetName="机关";
                departAliases=jgDepartAliases;
            }else {
                sheetName="基层";
                departAliases=jcDepartAliases;
            }
            HSSFSheet sheet = workbook.createSheet(sheetName);

            // 创建表头，传递部门
            createHeader(sheet, departAliases,year);

            // 填充数据
            fillData(sheet, gradeList, departAliases);
        }

        // 写入响应
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("业务测评明细.xls", "UTF-8"));
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createHeader(HSSFSheet sheet, List<String> departAliases,String year) {
        // 设置所有单元格的高度和宽度

        // 设置行高
        sheet.setDefaultRowHeightInPoints(40);
        // 设置前面固定表头的列宽为10
        int width=6;
        for (int i = 0; i < 6; i++) { // 0到5的列
            if (i==2){
                width=14;//处室列宽度大一点
            }else if (i==4){
                width=7;//分数列宽度大一点
            }else {
                width=6;
            }
            sheet.setColumnWidth(i, width * 256); // 设置宽度为10个字符
        }

        // 设置后面循环出来的表头宽度为4
        int startColumnForDetails = 6;
        for (int i = 0; i < departAliases.size(); i++) {
            sheet.setColumnWidth(startColumnForDetails + i, 4 * 256); // 设置宽度为4个字符
        }

        // 创建字体
        HSSFWorkbook workbook = sheet.getWorkbook();
        HSSFFont font = workbook.createFont();
        font.setFontName("黑体");
        font.setFontHeightInPoints((short) 12);

        // 创建居中样式
        CellStyle centeredStyle = workbook.createCellStyle();
        centeredStyle.setFont(font);
        // 水平居中
        centeredStyle.setAlignment(HorizontalAlignment.CENTER);
        // 垂直居中
        centeredStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        centeredStyle.setBorderBottom(BorderStyle.THIN);//下边框
        centeredStyle.setBorderTop(BorderStyle.THIN);//上边框
        centeredStyle.setBorderLeft(BorderStyle.THIN);//左边框
        centeredStyle.setBorderRight(BorderStyle.THIN);//右边框


        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setFontName("黑体");
        headerFont.setFontHeightInPoints((short) 16);
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
        headerStyle.setBorderBottom(BorderStyle.THIN);//下边框
        headerStyle.setBorderTop(BorderStyle.THIN);//上边框
        headerStyle.setBorderLeft(BorderStyle.THIN);//左边框
        headerStyle.setBorderRight(BorderStyle.THIN);//右边框
        // 合并单元格
        Row titleRow = sheet.createRow(0);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, departAliases.size() + 5));
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(year+"年度业务测评结果明细");
        titleCell.setCellStyle(headerStyle);
        titleRow.setHeight((short) 800);

        // 创建表头
        Row row1 = sheet.createRow(1);
        Row row2 = sheet.createRow(2);
        row1.setHeight((short) 400);
        row2.setHeight((short) 1400);

        // 合并单元格并设置表头内容
        // A1:A2
        sheet.addMergedRegion(new CellRangeAddress(1, 2, 0, 0));
        Cell cell01 = row1.createCell(0);
        Cell cell02 = row2.createCell(0);
        cell01.setCellValue("序号"); // 修改为 "序号"
        cell01.setCellStyle(centeredStyle);
        cell02.setCellStyle(centeredStyle);

        // B1:B2
        sheet.addMergedRegion(new CellRangeAddress(1, 2,1, 1));
        Cell cell11 = row1.createCell(1);
        Cell cell12 = row2.createCell(1);
        cell11.setCellValue("年度");
        cell11.setCellStyle(centeredStyle);
        cell12.setCellStyle(centeredStyle);

        // C1:C2
        sheet.addMergedRegion(new CellRangeAddress(1, 2,2, 2));
        Cell cell21 = row1.createCell(2);
        Cell cell22 = row2.createCell(2);
        cell21.setCellValue("处室(单位)");
        cell21.setCellStyle(centeredStyle);
        cell22.setCellStyle(centeredStyle);

        // D1:D2
        sheet.addMergedRegion(new CellRangeAddress(1, 2, 3, 3));
        Cell cell31 = row1.createCell(3);
        Cell cell32 = row2.createCell(3);
        cell31.setCellValue("排名");
        cell31.setCellStyle(centeredStyle);
        cell32.setCellStyle(centeredStyle);

        // E1:E2
        sheet.addMergedRegion(new CellRangeAddress(1, 2,4, 4));
        Cell cell41 = row1.createCell(4);
        Cell cell42 = row2.createCell(4);
        cell41.setCellValue("分数");
        cell41.setCellStyle(centeredStyle);
        cell42.setCellStyle(centeredStyle);

        // F1:F2
        sheet.addMergedRegion(new CellRangeAddress(1, 2, 5, 5));
        Cell cell51 = row1.createCell(5);
        Cell cell52 = row2.createCell(5);
        cell51.setCellValue("等次");
        cell51.setCellStyle(centeredStyle);
        cell52.setCellStyle(centeredStyle);

        // 添加打分明细标题并合并单元格，打分明细开始列
        // G1到最后一列
        sheet.addMergedRegion(new CellRangeAddress(1, 1, startColumnForDetails, startColumnForDetails + departAliases.size() - 1));
        for(int i=startColumnForDetails;i<departAliases.size()+startColumnForDetails;i++){
            Cell cellDetails = row1.createCell(i);
            cellDetails.setCellValue("打分明细");
            // 设置居中样式
            cellDetails.setCellStyle(centeredStyle);
        }
        // 设置单元格样式
        CellStyle aliasStyle = workbook.createCellStyle();
        aliasStyle.cloneStyleFrom(centeredStyle);
        // 设置自动换行
        aliasStyle.setWrapText(true);
        for (int i = 0; i < departAliases.size(); i++) {
            Cell cellAlias = row2.createCell(startColumnForDetails + i);
            String alias = departAliases.get(i);
            cellAlias.setCellValue(alias);
            cellAlias.setCellStyle(aliasStyle);
        }
    }

    private void fillData(HSSFSheet sheet, List<AssessBusinessGrade> gradeList, List<String> departAliases) {
        // 从第三行开始填充数据
        int rowNum = 3;

        List<SysDepartModel> sysDepartModels = departCommonApi.queryAllDepart();

        Map<String, String> departMap = new HashMap<>();
        for (SysDepartModel a : sysDepartModels) {
            departMap.put(a.getId(), a.getAlias());
        }

        // 创建一个居中样式
        HSSFWorkbook workbook = sheet.getWorkbook();
        CellStyle centeredStyle = workbook.createCellStyle();
        // 水平居中
        centeredStyle.setAlignment(HorizontalAlignment.CENTER);
        // 垂直居中
        centeredStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        centeredStyle.setBorderBottom(BorderStyle.THIN);//下边框
        centeredStyle.setBorderTop(BorderStyle.THIN);//上边框
        centeredStyle.setBorderLeft(BorderStyle.THIN);//左边框
        centeredStyle.setBorderRight(BorderStyle.THIN);//右边框
        centeredStyle.setWrapText(true);//自动换行

        // 根据 grade 的 id 获取 fill 数据
        for (AssessBusinessGrade grade : gradeList) {
            Row row = sheet.createRow(rowNum++);

            // 填充序号
            Cell cellIndex = row.createCell(0);
            cellIndex.setCellValue(rowNum - 2); // 填充序号
            cellIndex.setCellStyle(centeredStyle); // 设置居中样式

            // 年度
            Cell cellYear = row.createCell(1);
            cellYear.setCellValue(String.valueOf(grade.getCurrentYear()));
            cellYear.setCellStyle(centeredStyle);

            // 处室
            Cell cellDepart = row.createCell(2);
            cellDepart.setCellValue(departMap.get(grade.getDepartId()));
            cellDepart.setCellStyle(centeredStyle);

            // 排名
            Cell cellRanking = row.createCell(3);
            if (grade.getRanking()!=null){
                cellRanking.setCellValue(grade.getRanking());
            }
            cellRanking.setCellStyle(centeredStyle);

            // 分数
            Cell cellScore = row.createCell(4);
            cellScore.setCellValue(String.valueOf(grade.getScore()));
            cellScore.setCellStyle(centeredStyle);

            // 等次
            Cell cellLevel = row.createCell(5);
            switch (grade.getLevel()) {
                case "1":
                    cellLevel.setCellValue("优秀");
                    break;
                case "2":
                    cellLevel.setCellValue("良好");
                    break;
                case "3":
                    cellLevel.setCellValue("一般");
                    break;
                default:
                    cellLevel.setCellValue(""); // 为空
                    break;
            }
            cellLevel.setCellStyle(centeredStyle);

            // 查询 fill 数据
            List<AssessBusinessFillItem> fillItems = assessBusinessFillItemService.getFillItemsByGatherId(grade.getId());

            // 填充打分明细
            for (int i = 0; i < departAliases.size(); i++) {
                String departAlias=departAliases.get(i);
                Cell cellScoreDetail = row.createCell(6 + i);
                cellScoreDetail.setCellStyle(centeredStyle);
                for (AssessBusinessFillItem fillItem : fillItems) {
                    String reportDepart = fillItem.getReportDepart();
                    // 匹配 reportDepart 与 departAlias
                    if (reportDepart != null && departMap.get(reportDepart).equals(departAlias)) {
                        // 去掉小数部分
                        BigDecimal score = fillItem.getScore().setScale(0, RoundingMode.DOWN);
                        // 转换为字符串填充单元格
                        String string = score.toString();
                        cellScoreDetail.setCellValue(string);
                        break;
                    }
                }

            }

        }
    }


    @PostMapping("/downloadDuty")
    public ResponseEntity<byte[]> downloadDuty(@RequestBody Map<String, Object> params) {
        String startYear = (String) params.get("startYear");
        String endYear = (String) params.get("endYear");

        // 检查开始时间是否大于结束时间
        if (Integer.parseInt(startYear) > Integer.parseInt(endYear)) {
            throw new JeecgBootException("开始年度不能大于结束年度");
        }

        // 查询成绩数据
        List<AssessBusinessGrade> gradesList = gradeService.getGradesBetweenYears(startYear, endYear);
        // 排序
        List<SysDepartModel> allDepart = departCommonApi.queryAllDepart();
        Map<String, Integer> departMapData = allDepart.stream().collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getDepartOrder));
        for (AssessBusinessGrade item : gradesList) {
            item.setDepartOrder(departMapData.get(item.getDepartId()));
        }
        List<AssessBusinessGrade> allGrades = gradesList.stream().sorted(Comparator.comparing(AssessBusinessGrade::getDepartOrder)).collect(Collectors.toList());//升序排序
        Map<String, List<AssessBusinessGrade>> groupMap=gradeService.groupByDepartType(allGrades);
        if (groupMap.containsKey("4")){
            List<AssessBusinessGrade> data4=groupMap.get("4");
            if (groupMap.containsKey("3")){
                List<AssessBusinessGrade> data3=groupMap.get("3");
                data3.addAll(data4);
                groupMap.replace("3",data3);
                groupMap.remove("4");
            }
        }
        // 生成 Excel
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Workbook workbook = new XSSFWorkbook()) {
            // 创建字体样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setFontName("宋体");
            headerFont.setFontHeightInPoints((short) 16);
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
            headerStyle.setBorderBottom(BorderStyle.THIN);//下边框
            headerStyle.setBorderTop(BorderStyle.THIN);//上边框
            headerStyle.setBorderLeft(BorderStyle.THIN);//左边框
            headerStyle.setBorderRight(BorderStyle.THIN);//右边框

            CellStyle subHeaderStyle = workbook.createCellStyle();
            Font subHeaderFont = workbook.createFont();
            subHeaderFont.setFontName("宋体");
            subHeaderFont.setFontHeightInPoints((short) 14);
            subHeaderFont.setBold(true);
            subHeaderStyle.setFont(subHeaderFont);
            subHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
            subHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
            subHeaderStyle.setBorderBottom(BorderStyle.THIN);//下边框
            subHeaderStyle.setBorderTop(BorderStyle.THIN);//上边框
            subHeaderStyle.setBorderLeft(BorderStyle.THIN);//左边框
            subHeaderStyle.setBorderRight(BorderStyle.THIN);//右边框

            CellStyle cellStyle = workbook.createCellStyle();
            Font cellFont = workbook.createFont();
            cellFont.setFontName("宋体");
            cellFont.setFontHeightInPoints((short) 12);
            cellStyle.setFont(cellFont);
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
            cellStyle.setBorderBottom(BorderStyle.THIN);//下边框
            cellStyle.setBorderTop(BorderStyle.THIN);//上边框
            cellStyle.setBorderLeft(BorderStyle.THIN);//左边框
            cellStyle.setBorderRight(BorderStyle.THIN);//右边框

            for (String departType: groupMap.keySet()) {
                List<AssessBusinessGrade> grades=groupMap.get(departType);
                String sheetName =null;
                switch (departType) {
                    case "1":
                        sheetName="机关";
                        break;
                    case "2":
                        sheetName="分局";
                        break;
                    case "3":
                        sheetName="事业单位、参公";
                        break;
                    default:
                        sheetName="其他";
                        break;
                }
                Sheet sheet = workbook.createSheet(sheetName);
                int yearCount = Integer.parseInt(endYear) - Integer.parseInt(startYear) + 1;
                // 合并单元格
                Row titleRow = sheet.createRow(0);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, yearCount*2 + 1));
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("各处室（单位）业务测评历史成绩");
                titleCell.setCellStyle(headerStyle);
                titleRow.setHeight((short) 800);

                // 创建表头
                Row row1 = sheet.createRow(1);
                Row row2 = sheet.createRow(2);
                row1.setHeight((short) 500);
                row2.setHeight((short) 500);

                sheet.addMergedRegion(new CellRangeAddress(1, 2, 0, 0));
                Cell cell1 = row1.createCell(0);
                cell1.setCellValue("序号"); //
                cell1.setCellStyle(cellStyle);

                sheet.addMergedRegion(new CellRangeAddress(1, 2, 1, 1));
                Cell cell2 = row1.createCell(1);
                cell2.setCellValue("处室（单位）"); //
                cell2.setCellStyle(cellStyle);

                // 动态生成年度列，添加“年”字样
                int startColumnForDetails = 2;
                for (int i = 0; i < yearCount; i++) {
                    sheet.addMergedRegion(new CellRangeAddress(1, 1, startColumnForDetails*(i+1), startColumnForDetails*(i+1)+ 1));
                    Cell cellYearTitle = row1.createCell(startColumnForDetails*(i+1));
                    Cell cellYearTitle2 = row1.createCell(startColumnForDetails*(i+1)+1);
                    cellYearTitle.setCellValue((Integer.parseInt(startYear) + i) + "年");
                    // 设置居中样式
                    cellYearTitle.setCellStyle(cellStyle);
                    cellYearTitle2.setCellStyle(cellStyle);

                    Cell cellScoreTitle = row2.createCell(startColumnForDetails*(i+1));
                    Cell cellLevelTitle = row2.createCell(startColumnForDetails*(i+1)+1);
                    cellScoreTitle.setCellValue("分数");
                    cellLevelTitle.setCellValue("等次");
                    // 设置居中样式
                    cellScoreTitle.setCellStyle(subHeaderStyle);
                    cellLevelTitle.setCellStyle(subHeaderStyle);
                }

                // 设置表头的样式和宽度
                for (int i = 0; i < row1.getPhysicalNumberOfCells(); i++) {
                    Cell cell = row1.getCell(i);
                    cell.setCellStyle(subHeaderStyle);
                    if (i==0){
                        sheet.setColumnWidth(i, 10 * 256); // 设置序号单元格的宽度为 10
                    }else {
                        sheet.setColumnWidth(i, 15 * 256); // 设置每个单元格的宽度为 15
                    }
                }

                List<SysDepartModel> sysDepartModels = departCommonApi.queryAllDepart();
                Map<String, String> departMap = sysDepartModels.stream()
                        .collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getDepartName));

                // 填充数据
                Map<String, List<AssessBusinessGrade>> departmentGradesMap = new LinkedHashMap<>();
                for (AssessBusinessGrade grade : grades) {
                    departmentGradesMap
                            .computeIfAbsent(departMap.get(grade.getDepartId()), k -> new LinkedList<>())
                            .add(grade);
                }

                int rowIndex = 3; // 从第四行开始填充数据
                for (String departId : departmentGradesMap.keySet()) {
                    Row row = sheet.createRow(rowIndex++);
                    row.setHeight((short) 400);
                    Cell indexCell=row.createCell(0);
                    Cell departCell=row.createCell(1);
                    indexCell.setCellValue(rowIndex - 3); // 序号
                    row.createCell(1).setCellValue(departId);
                    indexCell.setCellStyle(cellStyle);
                    departCell.setCellStyle(cellStyle);
                    for (int i = 0; i <= Integer.parseInt(endYear) - Integer.parseInt(startYear); i++) {
                        int currentYear = Integer.parseInt(startYear) + i;
                        AssessBusinessGrade currentGrade = departmentGradesMap.get(departId).stream()
                                .filter(g -> g.getCurrentYear().equals(String.valueOf(currentYear)))
                                .findFirst()
                                .orElse(null);

                        Cell scoreCell=row.createCell(i*2 + 2);
                        Cell levelCell=row.createCell(i*2 + 3);
                        scoreCell.setCellStyle(cellStyle);
                        levelCell.setCellStyle(cellStyle);
                        // 根据 level 值设置内容
                        if (currentGrade==null){
                            continue;
                        }
                        String score=String.valueOf(currentGrade.getScore());
                        scoreCell.setCellValue(score);
                        String level = (currentGrade != null) ? currentGrade.getLevel() : ""; // 默认为空
                        switch (level) {
                            case "1":
                                levelCell.setCellValue("优秀");
                                break;
                            case "2":
                                levelCell.setCellValue("良好");
                                break;
                            case "3":
                                levelCell.setCellValue("一般");
                                break;
                            default:
                                levelCell.setCellValue(""); // 为空
                                break;
                        }
                    }
                }
            }
            workbook.write(out);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "成绩汇总.xlsx");
        return ResponseEntity.ok().headers(headers).body(out.toByteArray());
    }

    /**
     * 重新计算排名
     * @return
     */
    @AutoLog(value = "重新计算排名")
    @ApiOperation(value = "重新计算排名", notes = "重新计算排名")
    //@RequiresPermissions("org.jeecg:assess_business_grade:reCalBusnessRank")
    @GetMapping(value = "/reCalBusnessRank")
    public Result<String> reCalBusnessRank() {
        gradeService.updateBusinessRanking();
        gradeService.updateBusinessLevel();
        return Result.OK("操作成功");
    }

}
