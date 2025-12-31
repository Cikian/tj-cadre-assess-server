package org.jeecg.modules.annual.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.system.vo.SysDepartModel;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.annual.entity.AnnualIndexLeader;
import org.jeecg.modules.annual.service.IAssessAnnualArrangeService;
import org.jeecg.modules.annual.service.IAssessAnnualDutyReportService;
import org.jeecg.modules.annual.service.IAssessAnnualFillService;
import org.jeecg.modules.annual.service.IAssessDemocraticEvaluationSummaryService;
import org.jeecg.modules.annual.vo.AssessAnnualFillPage;
import org.jeecg.modules.annual.vo.AssessAnnualListVo;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.regular.vo.AdjustDivisionsUnitsVO;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.dto.AnnualAssessDepartFillDTO;
import org.jeecg.modules.sys.dto.AssessAnnualArrangeDTO;
import org.jeecg.modules.sys.dto.ConfirmVoteNumDTO;
import org.jeecg.modules.sys.dto.RegularGradeDTO;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.*;
import org.jeecg.modules.sys.mapper.annual.AssessAnnualSummaryMapper;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.utils.CommonUtils;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 年度考核填报列表
 * @Author: jeecg-boot
 * @Date: 2024-09-04
 * @Version: V1.0
 */
@Api(tags = "年度考核填报列表")
@RestController
@RequestMapping("/modules/annual/assessAnnualFill")
@Slf4j
public class AssessAnnualFillController {

    @Autowired
    private IAssessAnnualFillService fillService;
    @Autowired
    private IAssessAnnualDutyReportService dutyReportService;
    @Autowired
    private IAssessAnnualArrangeService arrangeService;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private DepartCommonApi departCommonApi;
    @Autowired
    private AssessAnnualSummaryMapper annualSummaryMapper;
    @Autowired
    private IAssessDemocraticEvaluationSummaryService democraticEvaluationSummaryService;

    /**
     * 分页列表查询
     *
     * @param assessAnnualFill
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "年度考核填报列表-分页列表查询")
    @ApiOperation(value = "年度考核填报列表-分页列表查询", notes = "年度考核填报列表-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessAnnualFill>> queryPageList(AssessAnnualFill assessAnnualFill,
                                                         @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                         @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                         HttpServletRequest req) {
        QueryWrapper<AssessAnnualFill> queryWrapper = QueryGenerator.initQueryWrapper(assessAnnualFill, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        boolean cadre = assessCommonApi.isCadre();
        if (!cadre) {
            Map<String, String> currentUserDepart = departCommonApi.getCurrentUserDepart();
            queryWrapper.eq("depart", currentUserDepart.get("departId"));
        }
        Map<String, String[]> parameterMap = req.getParameterMap();
        if (parameterMap.get("currentYear") == null) {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
            if (currentAssessInfo != null) {
                queryWrapper.eq("current_year", currentAssessInfo.getCurrentYear());
            }
        }
        queryWrapper.orderByDesc("create_time");

        Page<AssessAnnualFill> page = new Page<>(pageNo, pageSize);
        IPage<AssessAnnualFill> pageList = fillService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 分页列表查询
     *
     * @param assessAnnualFill
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "年度考核填报列表-分页列表查询")
    @ApiOperation(value = "年度考核汇总列表", notes = "年度考汇总列表")
    @GetMapping(value = "/summaryList")
    @Deprecated
    public Result<IPage<AssessAnnualListVo>> queryPageSummaryList(AssessAnnualFill assessAnnualFill,
                                                                  @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                  HttpServletRequest req) {
        QueryWrapper<AssessAnnualFill> queryWrapper = QueryGenerator.initQueryWrapper(assessAnnualFill, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        queryWrapper.eq("status", 3);
        Page<AssessAnnualFill> page = new Page<AssessAnnualFill>(pageNo, pageSize);
        IPage<AssessAnnualFill> pageList = fillService.page(page, queryWrapper);
        List<AssessAnnualListVo> summaryList = fillService.getSummaryList(pageList);
        IPage<AssessAnnualListVo> summaryPage = new Page<>();
        summaryPage.setRecords(summaryList);
        summaryPage.setTotal(pageList.getTotal());
        return Result.OK(summaryPage);
    }

//    /**
//     * 添加
//     *
//     * @param assessAnnualFillPage
//     * @return
//     */
//    @AutoLog(value = "年度考核填报列表-添加")
//    @ApiOperation(value = "年度考核填报列表-添加", notes = "年度考核填报列表-添加")
//    //@RequiresPermissions("org.jeecg:assess_annual_fill:add")
//    @PostMapping(value = "/add")
//    public Result<String> add(@RequestBody AssessAnnualFillPage assessAnnualFillPage) {
//        AssessAnnualFill assessAnnualFill = new AssessAnnualFill();
//        BeanUtils.copyProperties(assessAnnualFillPage, assessAnnualFill);
//        assessAnnualFillService.saveMain(assessAnnualFill, assessAnnualFillPage.getAssessAnnualDutyReportList(), assessAnnualFillPage.getAssessAnnualArrangeList());
//        return Result.OK("添加成功！");
//    }

    /**
     * 编辑
     *
     * @param assessAnnualFillPage
     * @return
     */
    @AutoLog(value = "年度考核填报列表-编辑")
    @ApiOperation(value = "年度考核填报列表-编辑", notes = "年度考核填报列表-编辑")
    //@RequiresPermissions("org.jeecg:assess_annual_fill:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody @Validated AssessAnnualFillPage assessAnnualFillPage, BindingResult bindingResult) {
        List<FieldError> errors = bindingResult.getFieldErrors();
        if (!errors.isEmpty()) {
            return Result.error(errors.get(0).getDefaultMessage());
        }
        Map<String, String> currentUserDepart = departCommonApi.getCurrentUserDepart();
        AssessAnnualFill annualFill = new AssessAnnualFill();
        BeanUtils.copyProperties(assessAnnualFillPage, annualFill);

        AssessAnnualFill assessAnnualFillEntity = fillService.getById(annualFill.getId());
        if (assessAnnualFillEntity == null) {
            return Result.error("未找到对应数据");
        }

        if (currentUserDepart == null) annualFill.setReportBy("填报专员");
        else annualFill.setReportBy(currentUserDepart.get("departName")+"填报专员");
        fillService.updateMain(
                annualFill,
                assessAnnualFillPage.getAssessAnnualArrangeList(),
                assessAnnualFillPage.getSummaryList(),
                false
        );

        return Result.OK("提交成功!");
    }

    @AutoLog(value = "年度考核填报列表-审核通过")
    @ApiOperation(value = "年度考核填报列表-审核通过", notes = "年度考核填报列表-审核通过")
    //@RequiresPermissions("org.jeecg:business_assess_depart_fill:add")
    @PutMapping(value = "/passAudit")
    public Result<String> passTheAudit(@RequestParam String assessId, @RequestParam String name) {
        AssessAnnualFill byId = fillService.getById(assessId);
        if (byId == null) {
            return Result.error("未找到对应数据");
        }

//        List<AssessBusinessFillItem> itemList = fillItemService.selectByMainId(assessId);
        // todo：验证关联的表


        return fillService.passAudit(assessId, name);
    }

    /**
     * 编辑
     *
     * @param assessAnnualFillPage
     * @return
     */
    @AutoLog(value = "年度考核填报列表-保存")
    @ApiOperation(value = "年度考核填报列表-保存", notes = "年度考核填报列表-保存")
    //@RequiresPermissions("org.jeecg:assess_annual_fill:edit")
    @RequestMapping(value = "/save", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> save(@RequestBody AssessAnnualFillPage assessAnnualFillPage) {
        AssessAnnualFill annualFill = new AssessAnnualFill();
        BeanUtils.copyProperties(assessAnnualFillPage, annualFill);

        AssessAnnualFill assessAnnualFillEntity = fillService.getById(annualFill.getId());
        if (assessAnnualFillEntity == null) {
            return Result.error("未找到对应数据");
        }

        fillService.updateMain(
                annualFill,
                assessAnnualFillPage.getAssessAnnualArrangeList(),
                assessAnnualFillPage.getSummaryList(),
                true
        );

        return Result.OK("保存成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "年度考核填报列表-通过id删除")
    @ApiOperation(value = "年度考核填报列表-通过id删除", notes = "年度考核填报列表-通过id删除")
    //@RequiresPermissions("org.jeecg:assess_annual_fill:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        fillService.delMain(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "年度考核填报列表-批量删除")
    @ApiOperation(value = "年度考核填报列表-批量删除", notes = "年度考核填报列表-批量删除")
    //@RequiresPermissions("org.jeecg:assess_annual_fill:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.fillService.delBatchMain(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "年度考核填报列表-通过id查询")
    @ApiOperation(value = "年度考核填报列表-通过id查询", notes = "年度考核填报列表-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessAnnualFill> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessAnnualFill assessAnnualFill = fillService.getById(id);
        if (assessAnnualFill == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessAnnualFill);

    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "述职报告通过主表ID查询")
    @ApiOperation(value = "述职报告主表ID查询", notes = "述职报告-通主表ID查询")
    @GetMapping(value = "/queryAssessAnnualDutyReportByMainId")
    public Result<List<AssessAnnualDutyReport>> queryAssessAnnualDutyReportListByMainId(@RequestParam(name = "id", required = true) String id) {
        List<AssessAnnualDutyReport> assessAnnualDutyReportList = dutyReportService.selectByMainId(id);
        return Result.OK(assessAnnualDutyReportList);
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "年度考核工作安排通过主表ID查询")
    @ApiOperation(value = "年度考核工作安排主表ID查询", notes = "年度考核工作安排-通主表ID查询")
    @GetMapping(value = "/queryAssessAnnualArrangeByMainId")
    public Result<IPage<AssessAnnualArrange>> queryAssessAnnualArrangeListByMainId(@RequestParam(name = "id", required = true) String id) {
        List<AssessAnnualArrange> assessAnnualArrangeList = arrangeService.selectByMainId(id);
        IPage<AssessAnnualArrange> page = new Page<>();
        page.setRecords(assessAnnualArrangeList);
        return Result.OK(page);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessAnnualFill
     */
    //@RequiresPermissions("org.jeecg:assess_annual_fill:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessAnnualFill assessAnnualFill) {
        // Step.1 组装查询条件查询数据
        QueryWrapper<AssessAnnualFill> queryWrapper = QueryGenerator.initQueryWrapper(assessAnnualFill, request.getParameterMap());
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        //配置选中数据查询条件
        String selections = request.getParameter("selections");
        if (oConvertUtils.isNotEmpty(selections)) {
            List<String> selectionList = Arrays.asList(selections.split(","));
            queryWrapper.in("id", selectionList);
        }
        //Step.2 获取导出数据
        List<AssessAnnualFill> assessAnnualFillList = fillService.list(queryWrapper);

        // Step.3 组装pageList
        List<AssessAnnualFillPage> pageList = new ArrayList<AssessAnnualFillPage>();
        for (AssessAnnualFill main : assessAnnualFillList) {
            AssessAnnualFillPage vo = new AssessAnnualFillPage();
            BeanUtils.copyProperties(main, vo);
            List<AssessAnnualDutyReport> assessAnnualDutyReportList = dutyReportService.selectByMainId(main.getId());
            List<AssessAnnualArrange> assessAnnualArrangeList = arrangeService.selectByMainId(main.getId());
            vo.setAssessAnnualArrangeList(assessAnnualArrangeList);
            pageList.add(vo);
        }

        // Step.4 AutoPoi 导出Excel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        mv.addObject(NormalExcelConstants.FILE_NAME, "年度考核填报列表列表");
        mv.addObject(NormalExcelConstants.CLASS, AssessAnnualFillPage.class);
        mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("年度考核填报列表数据", "导出人:" + sysUser.getRealname(), "年度考核填报列表"));
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
    //@RequiresPermissions("org.jeecg:assess_annual_fill:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        List<AssessAnnualSummary> itemList = new ArrayList<>(); // 用于存储解析后的数据
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(0); // 设置标题行数（如果有标题行）
            params.setHeadRows(1); // 设置表头行数
            try {
                // 使用 AutoPoi 读取 Excel 文件，映射到 AssessAnnualSummary 类
                List<AssessAnnualSummary> list = ExcelImportUtil.importExcel(file.getInputStream(), AssessAnnualSummary.class, params);
                for (AssessAnnualSummary item : list) {
                    log.info("导入的考核项: {}", item);
                    itemList.add(item); // 将每个条目加入到列表中
                }
                return Result.OK("文件导入成功！数据行数:" + list.size(), itemList); // 返回成功消息和数据
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
        return Result.error("没有文件被上传！");
    }
/*    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
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
                List<AssessAnnualFillPage> list = ExcelImportUtil.importExcel(file.getInputStream(), AssessAnnualFillPage.class, params);
                for (AssessAnnualFillPage page : list) {
                    AssessAnnualFill po = new AssessAnnualFill();
                    BeanUtils.copyProperties(page, po);
                    assessAnnualFillService.saveMain(po, page.getAssessAnnualArrangeList());
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
    }*/

    @AutoLog(value = "年度考核-发起")
    @ApiOperation(value = "年度考核-发起", notes = "年度考核-发起")
    @PostMapping(value = "/init")
    public Result<String> initAssess(@RequestBody AnnualAssessDepartFillDTO departFillDTO) {
        String currentYear = departFillDTO.getCurrentYear();
        // 如果截至日期小于当前日期，则不允许填报


        fillService.initAssess(departFillDTO);

        assessCommonApi.updateLdh(currentYear);
        return Result.OK("添加成功！");
    }

    @GetMapping("/getDuty")
    public Result<IPage<AssessAnnualDutyReport>> getDuty(@RequestParam(name = "fillId", required = true) String fillId) {
        List<AssessAnnualDutyReport> dutyList = dutyReportService.queryByFillId(fillId);
        IPage<AssessAnnualDutyReport> page = new Page<>();
        page.setRecords(dutyList);
        return Result.OK(page);
    }


    // todo： 获取平时成绩，此方法应该在平时成绩的Controller中
    @GetMapping("/getRegularGrade")
    public Result<List<RegularGradeDTO>> getRegularGradeByUserIds(@RequestParam(name = "userIds", required = true) String userIds, @RequestParam(name = "year", required = true) String year) {
        List<String> list = Arrays.asList(userIds.split(","));
        List<RegularGradeDTO> regularGradeDTOS = fillService.queryRegularGrade(list, year);
        return Result.OK(regularGradeDTOS);
    }

    // 获取平时成绩，和处室领导在一起

    @GetMapping("/assessAnnualFillQuery")
    public JSONObject getAssessAnnualFillExport(@RequestParam(defaultValue = "0") String depart) {
        AssessAnnualArrangeDTO reportQuery = arrangeService.getAssessAnnualFillQuery(depart);
        List<AssessAnnualArrangeDTO> list = new ArrayList<>();
        list.add(reportQuery);
        JSONObject json = new JSONObject();
        json.put("data", list);
        return json;
    }


    /**
     * 年度考核工作安排表PDF
     *
     * @param port 请求参数
     * @return
     */
    @PostMapping("/exportExcel")
    public void exportExcel(@Value("${server.port}") String port, HttpServletRequest request, HttpServletResponse response) {
        String url = "http://localhost:" + port + "/jmreport/exportAllExcelStream" + "?token=" + TokenUtils.getTokenByRequest(request);
        JSONObject queryParam = new JSONObject();
        Map<String, Object> param = new JSONObject();
        param.put("excelConfigId", "1013733530527506432");
        param.put("queryParam", queryParam);
        String filename = "年度考核工作安排表.xlsx";
        assessCommonApi.getExportExcel(url, filename, param, response);
    }

    @PostMapping("/getExcelData")
    public Result<?> importExcel(HttpServletRequest request) {

        String year = request.getHeader("year");

        List<AssessAnnualSummary> itemList = assessCommonApi.importExcel(request, AssessAnnualSummary.class, "annual_" + year);

        IPage<AssessAnnualSummary> page = new Page<>();
        page.setRecords(itemList);

        if (itemList != null) {
            return Result.OK(page);
        }

        return Result.error("解析失败！");
    }

    @GetMapping("/getArrange")
    public Result<?> getArrange(@RequestParam(name = "fillId", required = true) String fillId) {
        AssessAnnualArrange arrangeByFillId = arrangeService.getArrangeByFillId(fillId);
        return Result.OK(arrangeByFillId);
    }


    /**
     * 查询年度考核首页信息
     */
    @ApiOperation(value = "查询年度考核首页信息", notes = "查询年度考核首页信息")
    @GetMapping(value = "getIndexInfo")
    public Result<AnnualIndexLeader> getAllItems() {
        return Result.OK(fillService.getAllItems());
    }


    /**
     * 查询调整项
     */
    @AutoLog(value = "查询调整项")
    @ApiOperation(value = "查询调整项", notes = "查询调整项")
    @GetMapping(value = "/selectAdjustment")
    public Result<List<AssessAnnualSummary>> selectAdjustment() {
        // 是否存在当前年度考核
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
        if (currentAssessInfo == null) {
            throw new JeecgBootException("当前考核未开始！");
        }
        LambdaQueryWrapper<AssessAnnualSummary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssessAnnualSummary::getCurrentYear, currentAssessInfo.getCurrentYear()).isNotNull(AssessAnnualSummary::getHashId);
        List<AssessAnnualSummary> annualSummaryList = annualSummaryMapper.selectList(queryWrapper);
        annualSummaryList.forEach(annualSummary -> {
            String departId = annualSummary.getDepart();
            SysDepart sysDepart = departCommonApi.queryDepartById(departId);
            annualSummary.setDepart(sysDepart.getDepartName());
        });

        return Result.OK("查询成功！", annualSummaryList);
    }

    /**
     * 新增调整项
     */
    @AutoLog(value = "新增调整项")
    @ApiOperation(value = "新增调整项", notes = "新增调整项")
    @PostMapping(value = "/addAdjustmentItem")
    public Result<String> addAdjustmentItem(@RequestBody AssessAnnualSummary annualSummary) {
        // 是否存在当前年度考核
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
        if (currentAssessInfo == null) {
            throw new JeecgBootException("当前考核未开始！");
        }
        LambdaQueryWrapper<AssessAnnualFill> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssessAnnualFill::getDepart, annualSummary.getDepart());
        queryWrapper.eq(AssessAnnualFill::getCurrentYear, currentAssessInfo.getCurrentYear());
        AssessAnnualFill annualFill = fillService.getOne(queryWrapper);
        // 解析字符串为ZonedDateTime
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(annualSummary.getBirth());

        // 定义输出格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        // 格式化为所需的字符串格式
        String output = zonedDateTime.format(formatter);
        String fillId = IdWorker.getIdStr();
        annualSummary.setId(fillId);
        annualSummary.setFillId(annualFill.getId());
        annualSummary.setBirth(output);
        annualSummary.setCurrentYear(currentAssessInfo.getCurrentYear());
        String sex = Objects.equals(annualSummary.getSex(), "1") ? "男" : "女";
        annualSummary.setHashId(CommonUtils.hashId(annualSummary.getPerson(), sex, output));
        annualSummary.setLeaderRecommend(false);
        String departType = departCommonApi.getDepartTypeById(annualSummary.getDepart());
        switch (departType) {
            case "1":
                annualSummary.setRecommendType("bureau");
                annualSummary.setDepartType("bureau");
                break;
            case "4":
                annualSummary.setRecommendType("institution");
                annualSummary.setDepartType("institution");
                break;
            default:
                annualSummary.setRecommendType("basic");
                annualSummary.setDepartType("basic");
        }
        annualSummaryMapper.insert(annualSummary);
//        BeanUtils.copyProperties(annualSummary);
        return Result.OK("添加成功！");
    }

    /**
     * 删除调整项
     */
    @AutoLog(value = "删除调整项")
    @ApiOperation(value = "删除调整项", notes = "删除调整项")
    @PutMapping(value = "/deleteAdjustment")
    public Result<String> deleteAdjustment(@RequestBody List<String> idsToDelete) {
        // 检查 ID 列表是否为空
        if (idsToDelete == null || idsToDelete.isEmpty()) {
            return Result.OK("ID 列表不能为空！");
        }
        // 执行批量删除操作
        int deletedCount = annualSummaryMapper.deleteBatchIds(idsToDelete);

        // 根据删除结果返回相应的消息
        if (deletedCount > 0) {
            return Result.OK("删除成功！共删除 " + deletedCount + " 条记录。");
        } else {
            return Result.OK("删除失败，可能没有匹配的记录！");
        }
    }

    /**
     * 调整处室（单位）
     */
    @AutoLog(value = "新增调整项")
    @ApiOperation(value = "新增调整项", notes = "新增调整项")
    @PostMapping(value = "/adjustDivisionsUnits")
    public Result<String> adjustDivisionsUnits(@RequestBody(required = false) AdjustDivisionsUnitsVO adjustVO) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(adjustVO.getBirthDate());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String output = zonedDateTime.format(formatter);

        String uniqueValue = CommonUtils.hashId(adjustVO.getName(), adjustVO.getSex(), output);

        LambdaQueryWrapper<AssessAnnualSummary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssessAnnualSummary::getHashId, uniqueValue);
        List<AssessAnnualSummary> annualSummaries = annualSummaryMapper.selectList(queryWrapper);

        LambdaQueryWrapper<AssessAnnualFill> fillLambdaQueryWrapper = new LambdaQueryWrapper<>();
        List<AssessAnnualFill> assessRegularReportList = fillService.list(fillLambdaQueryWrapper);

        LambdaQueryWrapper<AssessDemocraticEvaluationSummary> wrapper = new LambdaQueryWrapper<>();
        List<AssessDemocraticEvaluationSummary> democraticEvaluationSummaryList = democraticEvaluationSummaryService.list(wrapper);

        for (AssessAnnualSummary summary : annualSummaries) {

            // 查找符合条件的AssessRegularReport记录
            AssessAnnualFill matchingReport = assessRegularReportList.stream()
                    .filter(report -> summary.getCurrentYear().equals(report.getCurrentYear()) &&
                            report.getDepart().equals(adjustVO.getDepartmentCode()))
                    .findFirst()
                    .orElse(null);
            AssessDemocraticEvaluationSummary matchingDemocraticEvaluationSummary = democraticEvaluationSummaryList.stream()
                    .filter(report -> summary.getId().equals(report.getAnnualSummaryId()))
                    .findFirst()
                    .orElse(null);

            if(matchingDemocraticEvaluationSummary!=null){
                matchingDemocraticEvaluationSummary.setDepart(adjustVO.getDepartmentCode());
                democraticEvaluationSummaryService.updateById(matchingDemocraticEvaluationSummary);
            }

            // 如果找到了匹配的记录，则更新regularReportItem的main_id
            if (matchingReport != null) {
                summary.setFillId(matchingReport.getId());
                summary.setDepart(adjustVO.getDepartmentCode());
                //
                annualSummaryMapper.updateById(summary);
            }

        }

        return Result.OK("调整成功");
    }

    @GetMapping("/checkProgress")
    public Result<Boolean> checkProgress() {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
        String currentYear = "";
        Boolean b = false;
        if (currentAssessInfo != null) {
            currentYear = currentAssessInfo.getCurrentYear();
            b = fillService.checkDepartFillOver(currentYear);
        }

        return Result.OK(b);
    }

    @GetMapping("/getVoteNum")
    public Result<?> getVoteNum() {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
        if (currentAssessInfo == null || !currentAssessInfo.isAssessing()) {
            return Result.error("当前没有进行中的年度考核");
        }

        List<?> voteNum = arrangeService.getVoteNum(currentAssessInfo.getCurrentYear());

        return null;
    }

    @GetMapping("/getExcellentDeputyNum")
    public Result<?> getExcellentNum(@RequestParam(name = "year", required = true) String year, @RequestParam(name = "depart", required = true) String depart) {
        Integer num = arrangeService.getExcellentDeputyNum(year, depart);
        return Result.OK(num);
    }

    @GetMapping("/excellentDeputyNumList")
    public Result<?> getExcellentNumList(@RequestParam(name = "year", required = true) String year){
        QueryWrapper<AssessAnnualFill> qw = new QueryWrapper<>();
        qw.eq("current_year", year);
        List<AssessAnnualFill> list = fillService.list(qw);

        List<SysDepartModel> departs = departCommonApi.queryDepartByType(new String[]{"2", "3", "4"});
        List<String> departIds = departs.stream().map(SysDepartModel::getId).collect(Collectors.toList());

        // 过滤出list中depart在departIds中的元素
        List<AssessAnnualFill> filteredList = list.stream()
                .filter(item -> departIds.contains(item.getDepart()))
                .collect(Collectors.toList());

        IPage<AssessAnnualFill> page = new Page<>();
        page.setRecords(filteredList);
        return Result.ok(page);
    }

    @PutMapping("/excellentDeputyNumChange")
    public Result<?> editExcellentDeputy(@RequestBody List<AssessAnnualFill> map){
        boolean b = fillService.updateBatchById(map);
        if (b) return Result.ok("修改成功！");
        else return Result.error("修改失败！");
    }

    @GetMapping("/getVoteNumList")
    public Result<?> getVoteNumList(@RequestParam(name = "year", required = true) String year) {
        List<ConfirmVoteNumDTO> voteNums = arrangeService.getVoteNumList(year);
        return Result.ok(voteNums);
    }

    @PutMapping("/editVoteNum")
    public Result<?> editVoteNum(@RequestBody Map<String, Object> map) {
        String id = (String) map.get("id");
        Integer voteA = (Integer) map.get("voteA");
        Integer voteB = (Integer) map.get("voteB");
        Integer voteC = (Integer) map.get("voteC");

        arrangeService.changeVoteNum(id, voteA, voteB, voteC);

        return Result.ok("修改成功！");
    }

}
