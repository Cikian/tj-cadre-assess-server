package org.jeecg.modules.annual.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.poi.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.annual.dto.Dem4jjGradeDTO;
import org.jeecg.modules.annual.dto.DemGradeDTO;
import org.jeecg.modules.annual.dto.DemGradeItemsDTO;
import org.jeecg.modules.annual.service.*;
import org.jeecg.modules.annual.vo.*;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.dto.DemocraticInitDTO;
import org.jeecg.modules.sys.dto.VmDemocraticAvgA;
import org.jeecg.modules.sys.dto.VmDemocraticAvgB;
import org.jeecg.modules.sys.dto.VmDemocraticAvgC;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.AssessDemocraticEvaluation;
import org.jeecg.modules.sys.entity.annual.AssessDemocraticEvaluationSummary;
import org.jeecg.modules.sys.entity.annual.AssessTrialPeople;
import org.jeecg.modules.sys.entity.business.AssessLeaderDepartConfig;
import org.jeecg.modules.sys.mapper.business.AssessLeaderDepartConfigMapper;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.user.UserCommonApi;
import org.jeecg.modules.utils.CommonUtils;
import org.jeecg.modules.utils.ExcelTemplateUtils;
import org.jeecg.modules.utils.ImageResizeUtils;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 民主测评汇总
 * @Author: jeecg-boot
 * @Date: 2024-09-12
 * @Version: V1.0
 */
@Api(tags = "民主测评汇总")
@RestController
@RequestMapping("/modules/annual/democraticSummary")
@Slf4j
public class AssessDemocraticEvaluationSummaryController {
    @Autowired
    private IAssessDemocraticEvaluationSummaryService democraticSummaryService;
    @Autowired
    private IVmDemocraticAvgAService avgAService;
    @Autowired
    private IVmDemocraticAvgBService avgBService;
    @Autowired
    private IVmDemocraticAvgCService avgCService;
    @Autowired
    private IAssessDemocraticEvaluationService democraticEvaluationService;
    //    @Qualifier("userCommonApi")
    @Autowired
    private UserCommonApi userCommonApi;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private AssessLeaderDepartConfigMapper deaderDepartConfigMapper;
    @Autowired
    private DepartCommonApi departCommonApi;
    @Autowired
    private IAssessTrialPeopleService trialService;
    @Value("${jeecg.path.upload}")
    private String uploadPath;
    /**
     * 分页列表查询
     *
     * @param assessDemocraticEvaluationSummary
     * @param req
     * @return
     */
    //@AutoLog(value = "民主测评汇总-分页列表查询")
    @ApiOperation(value = "民主测评汇总-分页列表查询", notes = "民主测评汇总-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessDemocraticEvaluationSummary>> queryPageList(AssessDemocraticEvaluationSummary assessDemocraticEvaluationSummary,
                                                                          HttpServletRequest req) {
        // QueryWrapper<AssessDemocraticEvaluationSummary> queryWrapper = QueryGenerator.initQueryWrapper(assessDemocraticEvaluationSummary, req.getParameterMap());
        QueryWrapper<AssessDemocraticEvaluationSummary> queryWrapper = new QueryWrapper<>();
        Map<String, String[]> parameterMap = req.getParameterMap();

        AssessCurrentAssess currentAssessInfo = null;
        if (parameterMap.get("currentYear") == null) {
            currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
            if (currentAssessInfo != null && currentAssessInfo.isAssessing()) {
                queryWrapper.eq("current_year", currentAssessInfo.getCurrentYear());
            }
        } else {
            queryWrapper.eq("current_year", parameterMap.get("currentYear")[0]);
        }

        if (parameterMap.get("type") != null) {
            String type = parameterMap.get("type")[0];
            if ("z".equals(type)) {
                queryWrapper.in("type", Arrays.asList("23", "22", "32"));
            } else if ("f".equals(type)) {
                queryWrapper.in("type", Arrays.asList("21", "31"));
            }
        }

        if (parameterMap.get("departType") != null) {
            String departType = parameterMap.get("departType")[0];
            if ("bureau".equals(departType)) {
                queryWrapper.eq("depart_type", "bureau");
            } else {
                queryWrapper.ne("depart_type", "bureau");
            }
        }

        List<AssessDemocraticEvaluationSummary> jgList = new ArrayList<>();
        if (parameterMap.get("leader") != null) {
            String leader = parameterMap.get("leader")[0];
            LambdaQueryWrapper<AssessLeaderDepartConfig> qw = new LambdaQueryWrapper<>();
            qw.eq(AssessLeaderDepartConfig::getLeaderId, leader);
            List<AssessLeaderDepartConfig> leaderConfig = deaderDepartConfigMapper.selectList(qw);
            StringBuilder ids = new StringBuilder();
            for (AssessLeaderDepartConfig config : leaderConfig) {
                ids.append(config.getDepartId()).append(",");
            }
            // 去除最后逗号
            if (ids.length() > 0) {
                ids.deleteCharAt(ids.length() - 1);
            }

            // 转换为List
            List<String> departIds = Arrays.asList(ids.toString().split(","));

            if (!departIds.isEmpty()) {
                queryWrapper.in("depart", departIds);
            }

            if (parameterMap.get("type") == null || "z".equals(parameterMap.get("type")[0])) {
                LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw = new LambdaQueryWrapper<>();
                if (parameterMap.get("currentYear") == null) {
                    lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, currentAssessInfo.getCurrentYear());
                } else {
                    lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, parameterMap.get("currentYear")[0]);
                }
                lqw.like(AssessDemocraticEvaluationSummary::getDepart, "JG");
                lqw.isNotNull(AssessDemocraticEvaluationSummary::getScore);
                jgList = democraticSummaryService.list(lqw);
                if (!jgList.isEmpty()) {
                    Iterator<AssessDemocraticEvaluationSummary> iterator = jgList.iterator();
                    while (iterator.hasNext()) {
                        AssessDemocraticEvaluationSummary a = iterator.next();
                        String depart = a.getDepart();
                        String substring = depart.substring(2);
                        if (!departIds.contains(substring)) {
                            iterator.remove();
                        }
                    }
                }
            }
        }

        queryWrapper.orderByAsc("ranking");

        List<AssessDemocraticEvaluationSummary> data = democraticSummaryService.getData(queryWrapper);
        if (!jgList.isEmpty()) {
            data.addAll(jgList);
        }

        // 以ranking升序排序，如果是null，排在后面
        data.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getRanking, Comparator.nullsLast(Comparator.naturalOrder())));

        Page<AssessDemocraticEvaluationSummary> page = new Page<>();
        page.setRecords(data);
        return Result.OK(page);
    }

    @GetMapping(value = "/listPro")
    public Result<IPage<AssessDemocraticEvaluationSummary>> queryPageList4Pro(AssessDemocraticEvaluationSummary params,
                                                                              HttpServletRequest req) {
        // QueryWrapper<AssessDemocraticEvaluationSummary> queryWrapper = QueryGenerator.initQueryWrapper(assessDemocraticEvaluationSummary, req.getParameterMap());
        QueryWrapper<AssessDemocraticEvaluationSummary> queryWrapper = new QueryWrapper<>();
        Map<String, String[]> parameterMap = req.getParameterMap();

        AssessCurrentAssess currentAssessInfo = null;
        if (parameterMap.get("currentYear") == null) {
            currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
            if (currentAssessInfo != null && currentAssessInfo.isAssessing()) {
                queryWrapper.eq("current_year", currentAssessInfo.getCurrentYear());
            }
        } else {
            queryWrapper.eq("current_year", parameterMap.get("currentYear")[0]);
        }


        Boolean trial = params.getTrial();
        List<AssessDemocraticEvaluationSummary> jgList = new ArrayList<>();

        if (trial == null || !trial) {
            if (parameterMap.get("type") != null) {
                String type = parameterMap.get("type")[0];
                if ("z".equals(type)) {
                    queryWrapper.in("type", Arrays.asList("23", "22", "32"));
                } else if ("f".equals(type)) {
                    queryWrapper.in("type", Arrays.asList("21", "31"));
                }
            }

            if (parameterMap.get("departType") != null) {
                String departType = parameterMap.get("departType")[0];
                if ("bureau".equals(departType)) {
                    queryWrapper.eq("depart_type", "bureau");
                } else {
                    queryWrapper.ne("depart_type", "bureau");
                }
            }

            if (parameterMap.get("leader") != null) {
                String leader = parameterMap.get("leader")[0];
                LambdaQueryWrapper<AssessLeaderDepartConfig> qw = new LambdaQueryWrapper<>();
                qw.eq(AssessLeaderDepartConfig::getLeaderId, leader);
                List<AssessLeaderDepartConfig> leaderConfig = deaderDepartConfigMapper.selectList(qw);
                StringBuilder ids = new StringBuilder();
                for (AssessLeaderDepartConfig config : leaderConfig) {
                    ids.append(config.getDepartId()).append(",");
                }
                // 去除最后逗号
                if (ids.length() > 0) {
                    ids.deleteCharAt(ids.length() - 1);
                }

                // 转换为List
                List<String> departIds = Arrays.asList(ids.toString().split(","));

                if (!departIds.isEmpty()) {
                    queryWrapper.in("depart", departIds);
                }

                if (parameterMap.get("type") == null || "z".equals(parameterMap.get("type")[0])) {
                    LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw = new LambdaQueryWrapper<>();
                    if (parameterMap.get("currentYear") == null) {
                        lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, currentAssessInfo.getCurrentYear());
                    } else {
                        lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, parameterMap.get("currentYear")[0]);
                    }
                    lqw.like(AssessDemocraticEvaluationSummary::getDepart, "JG");
                    lqw.isNotNull(AssessDemocraticEvaluationSummary::getScore);
                    jgList = democraticSummaryService.list(lqw);
                    if (!jgList.isEmpty()) {
                        Iterator<AssessDemocraticEvaluationSummary> iterator = jgList.iterator();
                        while (iterator.hasNext()) {
                            AssessDemocraticEvaluationSummary a = iterator.next();
                            String depart = a.getDepart();
                            String substring = depart.substring(2);
                            if (!departIds.contains(substring)) {
                                iterator.remove();
                            }
                        }
                    }
                }
            }
        } else {
            List<AssessTrialPeople> trials = trialService.list();
            if (trials != null && !trials.isEmpty()) {
                // 将hashId取出组合成List
                List<String> trialIds = trials.stream().map(AssessTrialPeople::getHashId).collect(Collectors.toList());
                queryWrapper.in("appraisee", trialIds);
            }
        }

        queryWrapper.orderByAsc("ranking");

        List<AssessDemocraticEvaluationSummary> data = democraticSummaryService.getData(queryWrapper);

        if (!jgList.isEmpty()) {
            data.addAll(jgList);
        }

        // 以ranking降序排序，如果是null，排在后面
        data.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getRanking, Comparator.nullsLast(Comparator.reverseOrder())));

        Integer nums = params.getNums();
        if (!Boolean.TRUE.equals(trial) && nums != null) {
            // nums除以100，保留两位小数
            BigDecimal divide = new BigDecimal(nums).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            // data的size乘divide，向下取整
            int size = (int) (data.size() * divide.doubleValue());
            // 截取size个元素
            data = data.subList(0, size);

        }

        data.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getRanking, Comparator.nullsLast(Comparator.naturalOrder())));


        Page<AssessDemocraticEvaluationSummary> page = new Page<>();
        page.setRecords(data);
        return Result.OK(page);
    }

    /**
     * 添加
     *
     * @param assessDemocraticEvaluationSummaryPage
     * @return
     */
    @AutoLog(value = "民主测评汇总-添加")
    @ApiOperation(value = "民主测评汇总-添加", notes = "民主测评汇总-添加")
    //@RequiresPermissions("org.jeecg:assess_democratic_evaluation_summary:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessDemocraticEvaluationSummaryPage assessDemocraticEvaluationSummaryPage) {
        AssessDemocraticEvaluationSummary assessDemocraticEvaluationSummary = new AssessDemocraticEvaluationSummary();
        BeanUtils.copyProperties(assessDemocraticEvaluationSummaryPage, assessDemocraticEvaluationSummary);
        democraticSummaryService.saveMain(assessDemocraticEvaluationSummary, assessDemocraticEvaluationSummaryPage.getAssessDemocraticEvaluationList());
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param assessDemocraticEvaluationSummaryPage
     * @return
     */
    @AutoLog(value = "民主测评汇总-编辑")
    @ApiOperation(value = "民主测评汇总-编辑", notes = "民主测评汇总-编辑")
    //@RequiresPermissions("org.jeecg:assess_democratic_evaluation_summary:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessDemocraticEvaluationSummaryPage assessDemocraticEvaluationSummaryPage) {
        AssessDemocraticEvaluationSummary assessDemocraticEvaluationSummary = new AssessDemocraticEvaluationSummary();
        BeanUtils.copyProperties(assessDemocraticEvaluationSummaryPage, assessDemocraticEvaluationSummary);
        AssessDemocraticEvaluationSummary assessDemocraticEvaluationSummaryEntity = democraticSummaryService.getById(assessDemocraticEvaluationSummary.getId());
        if (assessDemocraticEvaluationSummaryEntity == null) {
            return Result.error("未找到对应数据");
        }
        democraticSummaryService.updateMain(assessDemocraticEvaluationSummary, assessDemocraticEvaluationSummaryPage.getAssessDemocraticEvaluationList());
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "民主测评汇总-通过id删除")
    @ApiOperation(value = "民主测评汇总-通过id删除", notes = "民主测评汇总-通过id删除")
    //@RequiresPermissions("org.jeecg:assess_democratic_evaluation_summary:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        democraticSummaryService.delMain(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "民主测评汇总-批量删除")
    @ApiOperation(value = "民主测评汇总-批量删除", notes = "民主测评汇总-批量删除")
    //@RequiresPermissions("org.jeecg:assess_democratic_evaluation_summary:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.democraticSummaryService.delBatchMain(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "民主测评汇总-通过id查询")
    @ApiOperation(value = "民主测评汇总-通过id查询", notes = "民主测评汇总-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessDemocraticEvaluationSummary> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessDemocraticEvaluationSummary assessDemocraticEvaluationSummary = democraticSummaryService.getById(id);
        if (assessDemocraticEvaluationSummary == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessDemocraticEvaluationSummary);

    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "民主测评填报通过主表ID查询")
    @ApiOperation(value = "民主测评填报主表ID查询", notes = "民主测评填报-通主表ID查询")
    @GetMapping(value = "/queryAssessDemocraticEvaluationByMainId")
    public Result<List<AssessDemocraticEvaluation>> queryAssessDemocraticEvaluationListByMainId(@RequestParam(name = "id", required = true) String id) {
        List<AssessDemocraticEvaluation> assessDemocraticEvaluationList = democraticEvaluationService.selectByMainId(id);
        return Result.OK(assessDemocraticEvaluationList);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessDemocraticEvaluationSummary
     */
    //@RequiresPermissions("org.jeecg:assess_democratic_evaluation_summary:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessDemocraticEvaluationSummary assessDemocraticEvaluationSummary) {
        // Step.1 组装查询条件查询数据
        QueryWrapper<AssessDemocraticEvaluationSummary> queryWrapper = QueryGenerator.initQueryWrapper(assessDemocraticEvaluationSummary, request.getParameterMap());
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        // 配置选中数据查询条件
        String selections = request.getParameter("selections");
        if (oConvertUtils.isNotEmpty(selections)) {
            List<String> selectionList = Arrays.asList(selections.split(","));
            queryWrapper.in("id", selectionList);
        }
        // Step.2 获取导出数据
        List<AssessDemocraticEvaluationSummary> assessDemocraticEvaluationSummaryList = democraticSummaryService.list(queryWrapper);

        // Step.3 组装pageList
        List<AssessDemocraticEvaluationSummaryPage> pageList = new ArrayList<AssessDemocraticEvaluationSummaryPage>();
        for (AssessDemocraticEvaluationSummary main : assessDemocraticEvaluationSummaryList) {
            AssessDemocraticEvaluationSummaryPage vo = new AssessDemocraticEvaluationSummaryPage();
            BeanUtils.copyProperties(main, vo);
            List<AssessDemocraticEvaluation> assessDemocraticEvaluationList = democraticEvaluationService.selectByMainId(main.getId());
            vo.setAssessDemocraticEvaluationList(assessDemocraticEvaluationList);
            pageList.add(vo);
        }

        // Step.4 AutoPoi 导出Excel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        mv.addObject(NormalExcelConstants.FILE_NAME, "民主测评汇总列表");
        mv.addObject(NormalExcelConstants.CLASS, AssessDemocraticEvaluationSummaryPage.class);
        mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("民主测评汇总数据", "导出人:" + sysUser.getRealname(), "民主测评汇总"));
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
    //@RequiresPermissions("org.jeecg:assess_democratic_evaluation_summary:importExcel")
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
                List<AssessDemocraticEvaluationSummaryPage> list = ExcelImportUtil.importExcel(file.getInputStream(), AssessDemocraticEvaluationSummaryPage.class, params);
                for (AssessDemocraticEvaluationSummaryPage page : list) {
                    AssessDemocraticEvaluationSummary po = new AssessDemocraticEvaluationSummary();
                    BeanUtils.copyProperties(page, po);
                    democraticSummaryService.saveMain(po, page.getAssessDemocraticEvaluationList());
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

    @AutoLog(value = "年度考核民主测评-发起")
    @ApiOperation(value = "年度考核民主测评-发起", notes = "年度考核民主测评-发起")
    @PostMapping(value = "/init")
    public Result<?> initAssess(@RequestBody DemocraticInitDTO democraticDTO) {
        boolean b = democraticSummaryService.initDemocratic(democraticDTO);
        if (b) {
            List<SysUser> sysUsers = userCommonApi.generateAnonymousAccount("annual", democraticDTO.getCurrentYear());
            IPage<SysUser> page = new Page<>();
            page.setRecords(sysUsers);
            return Result.OK("发起成功！", page);
        }
        return Result.error("发起失败！");
    }

    @AutoLog(value = "根据年度考核id获取民主测评成绩")
    @ApiOperation(value = "根据年度考核id获取民主测评成绩", notes = "根据年度考核id获取民主测评成绩")
    @GetMapping(value = "/getByAnnualId")
    public Result<IPage<AssessDemocraticEvaluationSummary>> getByAnnualId(@RequestParam(name = "annualId", required = true) String annualId) {
        List<AssessDemocraticEvaluationSummary> summaryList = democraticSummaryService.getByAnnualId(annualId);
        IPage<AssessDemocraticEvaluationSummary> page = new Page<>();
        page.setRecords(summaryList);
        return Result.OK(page);
    }

    /**
     * 匿名账号信息 Excel
     *
     * @param port 端口号
     * @return
     */
    @PostMapping("/exportExcel")
    public void exportExcel(@Value("${server.port}") String port, HttpServletRequest request, HttpServletResponse response) {
        // 构建导出 Excel 的 URL，包括服务端口和 token 参数
        String url = "http://localhost:" + port + "/jmreport/exportAllExcelStream" + "?token=" + TokenUtils.getTokenByRequest(request);

        JSONObject queryParam = new JSONObject(); // 创建一个空的 JSON 对象用于查询参数
        Map<String, Object> param = new JSONObject(); // 创建一个 Map 用于存放请求参数
        param.put("excelConfigId", "1017226591127642112"); // 添加 Excel 配置 ID 参数
        param.put("queryParam", queryParam); // 将查询参数放入请求参数中
        String filename = "年度民主测评匿名账号.xlsx"; // 指定导出的 Excel 文件名

        // 调用方法导出 Excel
        assessCommonApi.getExportExcel(url, filename, param, response);
    }

    @GetMapping("/getLeaderProgress")
    public Result<IPage<LeaderProgressVO>> getLeaderProgress() {
        List<LeaderProgressVO> leaderProgress = democraticSummaryService.getLeaderProgress();
        IPage<LeaderProgressVO> page = new Page<>();
        page.setRecords(leaderProgress);
        return Result.OK(page);
    }

    @GetMapping("/getDemocraticProgressByYear")
    public Result<IPage<DepartProgressVO>> getDemocraticProgressByYear(@RequestParam(name = "year", required = false, defaultValue = "0") String year) {
        if ("0".equals(year) || "undefined".equals(year)) {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
            if (currentAssessInfo != null) {
                year = currentAssessInfo.getCurrentYear();
            }
        }
        List<DepartProgressVO> progress = democraticSummaryService.getDepartProgressByYear(year);
        IPage<DepartProgressVO> page = new Page<>();
        page.setRecords(progress);
        return Result.OK(page);
    }

    @GetMapping("/getDepartProgressByYear")
    public Result<IPage<DepartProgressVO>> getDepartProgressByYear(@RequestParam(name = "year", required = false, defaultValue = "0") String year) {
        if ("0".equals(year) || "undefined".equals(year)) {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
            if (currentAssessInfo != null) {
                year = currentAssessInfo.getCurrentYear();
            }
        }
        List<DepartProgressVO> progress = democraticSummaryService.getDepartProgressByYear(year);

        Map<String, String> currentUserDepart = departCommonApi.getCurrentUserDepart();
        if (currentUserDepart != null && currentUserDepart.get("departId") != null) {
            progress = progress.stream().filter(item -> currentUserDepart.get("departId").equals(item.getDepart())).collect(Collectors.toList());
        } else {
            return Result.error("未获取到当前用户部门信息");
        }

        IPage<DepartProgressVO> page = new Page<>();
        page.setRecords(progress);
        return Result.OK(page);
    }

    @PostMapping("/export")
    public void exportPdf(@RequestBody ExportDemVO options, @Value("${server.port}") String port, HttpServletRequest request, HttpServletResponse response) {
        String url = "http://localhost:" + port + "/jmreport/exportAllExcelStream" + "?token=" + TokenUtils.getTokenByRequest(request);
        String currentYear = options.getYear();
        if (currentYear == null || currentYear.isEmpty()) {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
            if (currentAssessInfo != null) {
                currentYear = currentAssessInfo.getCurrentYear();
            }
        }
        String leader;

        List<Map<String, Object>> paramList = new ArrayList<>();

        List<String> entryNames = new ArrayList<>();

        List<SysUser> allLeader = userCommonApi.getAllLeader();
        Map<String, String> leaderMap = allLeader.stream().collect(Collectors.toMap(SysUser::getId, SysUser::getRealname));

        for (SysUser user : allLeader) {
            leader = user.getId();
            leaderMap.put(user.getId(), user.getRealname());
            for (int i = 0; i < 5; i++) {
                JSONObject queryParam = new JSONObject();
                queryParam.put("currentYear", currentYear);
                queryParam.put("leader", leader == null ? "0" : leader);
                Map<String, Object> param = new JSONObject();

                if (i <= 1) {
                    param.put("excelConfigId", "1041346362018705408");
                    entryNames.add(i == 1 ? currentYear + "年度民主测评局机关处室正职成绩.xlsx" : currentYear + "年度民主测评局机关处室副职成绩.xlsx");
                    queryParam.put("departScope", "2");
                    queryParam.put("personType", i == 1 ? "2" : "3");
                } else if (i == 2) {
                    param.put("excelConfigId", "1041217442082570240");
                    entryNames.add(currentYear + "年度民主测评基层正职成绩.xlsx");
                    queryParam.put("departScope", "3");
                    queryParam.put("personType", "2");
                } else if (i == 3) {
                    param.put("excelConfigId", "1041344771584765952");
                    entryNames.add(currentYear + "年度民主测评基层副职成绩.xlsx");
                    queryParam.put("departScope", "3");
                    queryParam.put("personType", "3");
                } else {
                    param.put("excelConfigId", "1041347306785681408");
                    entryNames.add(currentYear + "年度民主测评领导班子成绩.xlsx");
                    queryParam.put("departScope", "3");
                    queryParam.put("personType", "4");
                }
                param.put("queryParam", queryParam);
                paramList.add(param);
            }

            for (int i = 0; i < 5; i++) {
                JSONObject queryParam = new JSONObject();
                queryParam.put("currentYear", currentYear);
                queryParam.put("leader", leader == null ? "0" : leader);
                Map<String, Object> param = new JSONObject();

                if (i == 0) {
                    param.put("excelConfigId", "1041859875900325888");
                    entryNames.add(currentYear + "年度民主测评局机关处室正职分项成绩.xlsx");
                    queryParam.put("departScope", "2");
                    queryParam.put("personType", "2");
                } else if (i == 1) {
                    param.put("excelConfigId", "1041859875900325888");
                    entryNames.add(currentYear + "年度民主测评基层单位正职分项成绩.xlsx");
                    queryParam.put("departScope", "3");
                    queryParam.put("personType", "2");
                } else if (i == 2) {
                    param.put("excelConfigId", "1041957751343120384");
                    entryNames.add(currentYear + "年度民主测评局机关处室副职分项成绩.xlsx");
                    queryParam.put("departScope", "2");
                    queryParam.put("personType", "3");
                } else if (i == 3) {
                    param.put("excelConfigId", "1041957751343120384");
                    entryNames.add(currentYear + "年度民主测评基层单位副职分项成绩.xlsx");
                    queryParam.put("departScope", "3");
                    queryParam.put("personType", "3");
                } else {
                    param.put("excelConfigId", "1041959172981489664");
                    entryNames.add(currentYear + "年度民主测评领导班子分项成绩.xlsx");
                    queryParam.put("departScope", "3");
                    queryParam.put("personType", "4");
                }
                param.put("queryParam", queryParam);
                paramList.add(param);
            }
        }

        for (int i = 0; i < 5; i++) {
            JSONObject queryParam = new JSONObject();
            queryParam.put("currentYear", currentYear);
            queryParam.put("leader", "0");
            Map<String, Object> param = new JSONObject();

            if (i <= 1) {
                param.put("excelConfigId", "1041346362018705408");
                entryNames.add(i == 1 ? currentYear + "年度民主测评局机关处室正职成绩.xlsx" : currentYear + "年度民主测评局机关处室副职成绩.xlsx");
                queryParam.put("departScope", "2");
                queryParam.put("personType", i == 1 ? "2" : "3");
            } else if (i == 2) {
                param.put("excelConfigId", "1041217442082570240");
                entryNames.add(currentYear + "年度民主测评基层正职成绩.xlsx");
                queryParam.put("departScope", "3");
                queryParam.put("personType", "2");
            } else if (i == 3) {
                param.put("excelConfigId", "1041344771584765952");
                entryNames.add(currentYear + "年度民主测评基层副职成绩.xlsx");
                queryParam.put("departScope", "3");
                queryParam.put("personType", "3");
            } else {
                param.put("excelConfigId", "1041347306785681408");
                entryNames.add(currentYear + "年度民主测评领导班子成绩.xlsx");
                queryParam.put("departScope", "3");
                queryParam.put("personType", "4");
            }
            param.put("queryParam", queryParam);
            paramList.add(param);
        }

        for (int i = 0; i < 5; i++) {
            JSONObject queryParam = new JSONObject();
            queryParam.put("currentYear", currentYear);
            queryParam.put("leader", "0");
            Map<String, Object> param = new JSONObject();

            if (i == 0) {
                param.put("excelConfigId", "1041859875900325888");
                entryNames.add(currentYear + "年度民主测评局机关处室正职分项成绩.xlsx");
                queryParam.put("departScope", "2");
                queryParam.put("personType", "2");
            } else if (i == 1) {
                param.put("excelConfigId", "1041859875900325888");
                entryNames.add(currentYear + "年度民主测评基层单位正职分项成绩.xlsx");
                queryParam.put("departScope", "3");
                queryParam.put("personType", "2");
            } else if (i == 2) {
                param.put("excelConfigId", "1041957751343120384");
                entryNames.add(currentYear + "年度民主测评局机关处室副职分项成绩.xlsx");
                queryParam.put("departScope", "2");
                queryParam.put("personType", "3");
            } else if (i == 3) {
                param.put("excelConfigId", "1041957751343120384");
                entryNames.add(currentYear + "年度民主测评基层单位副职分项成绩.xlsx");
                queryParam.put("departScope", "3");
                queryParam.put("personType", "3");
            } else {
                param.put("excelConfigId", "1041959172981489664");
                entryNames.add(currentYear + "年度民主测评领导班子分项成绩.xlsx");
                queryParam.put("departScope", "3");
                queryParam.put("personType", "4");
            }
            param.put("queryParam", queryParam);
            paramList.add(param);
        }


        assessCommonApi.getExportDemZipWithExcel(url, "年度民主测评成绩.zip", entryNames, paramList, leaderMap, response);


    }

    @PostMapping("/export4jj")
    public void export4jj(@RequestBody Map<String, String> options, @Value("${server.port}") String port, HttpServletRequest request, HttpServletResponse response) {
        String url = "http://localhost:" + port + "/jmreport/exportAllExcelStream" + "?token=" + TokenUtils.getTokenByRequest(request);
        String currentYear = options.get("year");
        if (currentYear == null || currentYear.isEmpty()) {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
            if (currentAssessInfo != null) {
                currentYear = currentAssessInfo.getCurrentYear();
            }
        }

        JSONObject queryParam = new JSONObject();
        queryParam.put("year", currentYear);
        Map<String, Object> param = new JSONObject();
        param.put("excelConfigId", "1047376810406641664");

        param.put("queryParam", queryParam);

        String filename = currentYear + "年度考核工作基层单位分管纪检工作处级领导干部测评信息表.xlsx";
        assessCommonApi.getExportExcel(url, filename, param, response);

    }

    @GetMapping("/query4Excel")
    public JSONObject getExportSummaryData(@RequestParam(defaultValue = "0") String year,
                                           @RequestParam(defaultValue = "0") String departScope,
                                           @RequestParam(defaultValue = "0") String personType,
                                           @RequestParam(defaultValue = "0") String leader) {

        if ("0".equals(year)) {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
            if (currentAssessInfo != null) {
                year = currentAssessInfo.getCurrentYear();
            }
        }

        List<DemGradeDTO> list = democraticSummaryService.getSummary4Excel(year, departScope, personType, leader);
        JSONObject json = new JSONObject();
        json.put("data", list);
        return json;
    }

    @GetMapping("/queryItems4Excel")
    public JSONObject getExportItemsData(@RequestParam(defaultValue = "0") String year,
                                         @RequestParam(defaultValue = "0") String departScope,
                                         @RequestParam(defaultValue = "0") String personType,
                                         @RequestParam(defaultValue = "0") String leader) {

        if ("0".equals(year)) {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
            if (currentAssessInfo != null) {
                year = currentAssessInfo.getCurrentYear();
            }
        }

        List<DemGradeItemsDTO> list = democraticSummaryService.getSummaryItems4Excel(year, departScope, personType, leader);
        JSONObject json = new JSONObject();
        json.put("data", list);
        // json.put("data",null);
        return json;
    }

    @GetMapping("/queryItems4jj")
    public JSONObject getExportItemsData4jj(@RequestParam(defaultValue = "0") String year) {

        if ("0".equals(year)) {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
            if (currentAssessInfo != null) {
                year = currentAssessInfo.getCurrentYear();
            }
        }

        List<Dem4jjGradeDTO> list = democraticSummaryService.getSummaryItems4jjExcel(year);
        JSONObject json = new JSONObject();
        json.put("data", list);
        // json.put("data",null);
        return json;
    }

    @PutMapping("/rerank")
    public Result<?> reRanking() {
        democraticSummaryService.reRanking();
        return Result.ok("更新分数及排名成功！");
    }

    /**
     * 排名变化曲线
     *
     * @return
     */
    @GetMapping("/data1")
    public Result<Map<String, Object>> getData1(AssessDemocraticEvaluationSummary params, HttpServletRequest req) {

        // 获取当前年度
        AssessCurrentAssess info = assessCommonApi.getCurrentAssessInfo("annual");
        String currentYear = info.getCurrentYear();
        int currentYearInt = Integer.parseInt(currentYear);
        int lastYearInt = currentYearInt - 1;
        int last2YearInt = currentYearInt - 2;

        Map<String, Object> currentYearDataMap = getDataMap(params, req, currentYear);
        Map<String, Object> lastYearDataMap = getDataMap(params, req, Integer.toString(lastYearInt));
        Map<String, Object> last2YearDataMap = getDataMap(params, req, Integer.toString(last2YearInt));

        return getRes(currentYearDataMap,
                lastYearDataMap,
                last2YearDataMap,
                currentYearInt,
                lastYearInt,
                last2YearInt,
                params);
    }

    @GetMapping("/data2")
    public Result<Map<String, Object>> data2(AssessDemocraticEvaluationSummary params, HttpServletRequest req) {
        // 获取当前年度
        AssessCurrentAssess info = assessCommonApi.getCurrentAssessInfo("annual");
        String currentYear = info.getCurrentYear();
        int currentYearInt = Integer.parseInt(currentYear);
        int lastYearInt = currentYearInt - 1;
        int last2YearInt = currentYearInt - 2;

        Map<String, Object> currentYearDataMap = getItem1DataMap(params, req, currentYear);
        Map<String, Object> lastYearDataMap = getItem1DataMap(params, req, Integer.toString(lastYearInt));
        Map<String, Object> last2YearDataMap = getItem1DataMap(params, req, Integer.toString(last2YearInt));

        return getRes(currentYearDataMap,
                lastYearDataMap,
                last2YearDataMap,
                currentYearInt,
                lastYearInt,
                last2YearInt,
                params);

    }

    @GetMapping("/data3")
    public Result<Map<String, Object>> data3(AssessDemocraticEvaluationSummary params, HttpServletRequest req) {
        // 获取当前年度
        AssessCurrentAssess info = assessCommonApi.getCurrentAssessInfo("annual");
        String currentYear = info.getCurrentYear();
        int currentYearInt = Integer.parseInt(currentYear);
        int lastYearInt = currentYearInt - 1;
        int last2YearInt = currentYearInt - 2;

        Map<String, Object> currentYearDataMap = getItem2DataMap(params, req, currentYear);
        Map<String, Object> lastYearDataMap = getItem2DataMap(params, req, Integer.toString(lastYearInt));
        Map<String, Object> last2YearDataMap = getItem2DataMap(params, req, Integer.toString(last2YearInt));

        return getRes(currentYearDataMap,
                lastYearDataMap,
                last2YearDataMap,
                currentYearInt,
                lastYearInt,
                last2YearInt,
                params);

    }

    @GetMapping("/data4")
    public Result<Map<String, Object>> data4(AssessDemocraticEvaluationSummary params, HttpServletRequest req) {
        // 获取当前年度
        AssessCurrentAssess info = assessCommonApi.getCurrentAssessInfo("annual");
        String currentYear = info.getCurrentYear();
        int currentYearInt = Integer.parseInt(currentYear);
        int lastYearInt = currentYearInt - 1;
        int last2YearInt = currentYearInt - 2;

        Map<String, Object> currentYearDataMap = getItem3DataMap(params, req, currentYear);
        Map<String, Object> lastYearDataMap = getItem3DataMap(params, req, Integer.toString(lastYearInt));
        Map<String, Object> last2YearDataMap = getItem3DataMap(params, req, Integer.toString(last2YearInt));

        return getRes(currentYearDataMap,
                lastYearDataMap,
                last2YearDataMap,
                currentYearInt,
                lastYearInt,
                last2YearInt,
                params);

    }

    private Result<Map<String, Object>> getRes(Map<String, Object> currentYearDataMap,
                                               Map<String, Object> lastYearDataMap,
                                               Map<String, Object> last2YearDataMap,
                                               int currentYearInt,
                                               int lastYearInt,
                                               int last2YearInt,
                                               AssessDemocraticEvaluationSummary params) {
        Map<String, Object> resMap = new HashMap<>();
        Integer data1 = (Integer) last2YearDataMap.get("data");
        Boolean underline1 = (Boolean) last2YearDataMap.get("underLine");

        Integer data2 = (Integer) lastYearDataMap.get("data");
        Boolean underline2 = (Boolean) lastYearDataMap.get("underLine");

        Integer data3 = (Integer) currentYearDataMap.get("data");
        Boolean underline3 = (Boolean) currentYearDataMap.get("underLine");

        // data1、2、3中找出最小的
        Integer min = data1;
        if (data2 < min) {
            min = data2;
        }
        if (data3 < min) {
            min = data3;
        }

        int over1 = 0;
        int over2 = 0;
        int over3 = 0;

        int under1 = 9999;
        int under2 = 9999;
        int under3 = 9999;

        if (underline1) under1 = data1;
        else over1 = data1;

        if (underline2) under2 = data2;
        else over2 = data2;

        if (underline3) under3 = data3;
        else over3 = data3;

        int markLineMin = Math.max(over1, Math.max(over2, over3));
        int markLineMax = Math.min(under1, Math.min(under2, under3));

        int markLineData = 0;

        if (markLineMin != markLineMax) {
            markLineData = (markLineMax - markLineMin) / 2 + markLineMin;
        } else {
            markLineData = markLineMin;
        }

        resMap.put("xAxisData", new Integer[]{last2YearInt, lastYearInt, currentYearInt});
        resMap.put("yAxisMin", Math.max(min - 10, 0));
        resMap.put("seriesData", new Integer[]{data1, data2, data3});
        resMap.put("markLineData", markLineData);
        resMap.put("markLineName", params.getNums() != null ? params.getNums() + "%线" : "0");

        return Result.ok(resMap);
    }


    private Map<String, Object> getDataMap(AssessDemocraticEvaluationSummary params,
                                           HttpServletRequest req, String year) {
        QueryWrapper<AssessDemocraticEvaluationSummary> queryWrapper = new QueryWrapper<>();
        Map<String, String[]> parameterMap = req.getParameterMap();

        queryWrapper.eq("current_year", year);

        if (parameterMap.get("type") != null) {
            String type = parameterMap.get("type")[0];
            if ("z".equals(type)) {
                queryWrapper.in("type", Arrays.asList("23", "22", "32"));
            } else if ("f".equals(type)) {
                queryWrapper.in("type", Arrays.asList("21", "31"));
            }
        }

        if (parameterMap.get("departType") != null) {
            String departType = parameterMap.get("departType")[0];
            if ("bureau".equals(departType)) {
                queryWrapper.eq("depart_type", "bureau");
            } else {
                queryWrapper.ne("depart_type", "bureau");
            }
        }

        List<AssessDemocraticEvaluationSummary> jgList = new ArrayList<>();
        if (parameterMap.get("leader") != null) {
            String leader = parameterMap.get("leader")[0];
            LambdaQueryWrapper<AssessLeaderDepartConfig> qw = new LambdaQueryWrapper<>();
            qw.eq(AssessLeaderDepartConfig::getLeaderId, leader);
            List<AssessLeaderDepartConfig> leaderConfig = deaderDepartConfigMapper.selectList(qw);
            StringBuilder ids = new StringBuilder();
            for (AssessLeaderDepartConfig config : leaderConfig) {
                ids.append(config.getDepartId()).append(",");
            }
            // 去除最后逗号
            if (ids.length() > 0) {
                ids.deleteCharAt(ids.length() - 1);
            }

            // 转换为List
            List<String> departIds = Arrays.asList(ids.toString().split(","));

            if (!departIds.isEmpty()) {
                queryWrapper.in("depart", departIds);
            }

            if (parameterMap.get("type") == null || "z".equals(parameterMap.get("type")[0])) {
                LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw = new LambdaQueryWrapper<>();
                lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year);
                lqw.like(AssessDemocraticEvaluationSummary::getDepart, "JG");
                lqw.isNotNull(AssessDemocraticEvaluationSummary::getScore);
                jgList = democraticSummaryService.list(lqw);
                if (!jgList.isEmpty()) {
                    Iterator<AssessDemocraticEvaluationSummary> iterator = jgList.iterator();
                    while (iterator.hasNext()) {
                        AssessDemocraticEvaluationSummary a = iterator.next();
                        String depart = a.getDepart();
                        String substring = depart.substring(2);
                        if (!departIds.contains(substring)) {
                            iterator.remove();
                        }
                    }
                }
            }
        }

        queryWrapper.orderByAsc("ranking");

        List<AssessDemocraticEvaluationSummary> data = democraticSummaryService.getData(queryWrapper);
        if (!jgList.isEmpty()) {
            data.addAll(jgList);
        }

        // 以ranking降序排序，如果是null，排在后面
        data.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getRanking, Comparator.nullsLast(Comparator.naturalOrder())));

        String hashId = params.getAppraisee();
        // 找出Appraisee属性为hashId的索引
        AssessDemocraticEvaluationSummary summary = data.stream().filter(item -> item.getAppraisee().equals(hashId)).findFirst().orElse(null);
        int i1 = data.indexOf(summary) + 1;

        Map<String, Object> res = new HashMap<>();

        Integer nums = params.getNums();
        if (nums != null && i1 != -1) {
            // nums除以100，保留两位小数
            BigDecimal divide = new BigDecimal(nums).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            // data的size乘divide，向下取整
            int size = (int) (data.size() * divide.doubleValue());

            int i = data.size() - size;

            if (i1 > i) res.put("underLine", true);
            else res.put("underLine", false);
            res.put("year", year);
            if (summary == null) res.put("data", 0);
            else res.put("data", (parameterMap.get("leader") != null) || Boolean.TRUE.equals(params.getTrial()) ? summary.getScopeRanking() : summary.getRanking());
        }

        return res;
    }

    private Map<String, Object> getItem1DataMap(AssessDemocraticEvaluationSummary params,
                                                HttpServletRequest req, String year) {
        QueryWrapper<VmDemocraticAvgA> queryWrapper = new QueryWrapper<>();
        Map<String, String[]> parameterMap = req.getParameterMap();

        queryWrapper.eq("current_year", year);

        if (parameterMap.get("type") != null) {
            String type = parameterMap.get("type")[0];
            if ("z".equals(type)) {
                queryWrapper.in("type", Arrays.asList("23", "22", "32"));
            } else if ("f".equals(type)) {
                queryWrapper.in("type", Arrays.asList("21", "31"));
            }
        }

        if (parameterMap.get("departType") != null) {
            String departType = parameterMap.get("departType")[0];
            if ("bureau".equals(departType)) {
                queryWrapper.eq("depart_type", "bureau");
            } else {
                queryWrapper.ne("depart_type", "bureau");
            }
        }

        List<VmDemocraticAvgA> jgList = new ArrayList<>();
        if (parameterMap.get("leader") != null) {
            String leader = parameterMap.get("leader")[0];
            LambdaQueryWrapper<AssessLeaderDepartConfig> qw = new LambdaQueryWrapper<>();
            qw.eq(AssessLeaderDepartConfig::getLeaderId, leader);
            List<AssessLeaderDepartConfig> leaderConfig = deaderDepartConfigMapper.selectList(qw);
            StringBuilder ids = new StringBuilder();
            for (AssessLeaderDepartConfig config : leaderConfig) {
                ids.append(config.getDepartId()).append(",");
            }
            // 去除最后逗号
            if (ids.length() > 0) {
                ids.deleteCharAt(ids.length() - 1);
            }

            // 转换为List
            List<String> departIds = Arrays.asList(ids.toString().split(","));

            if (!departIds.isEmpty()) {
                queryWrapper.in("depart", departIds);
            }

            if (parameterMap.get("type") == null || "z".equals(parameterMap.get("type")[0])) {
                LambdaQueryWrapper<VmDemocraticAvgA> lqw = new LambdaQueryWrapper<>();
                lqw.eq(VmDemocraticAvgA::getCurrentYear, year);
                lqw.like(VmDemocraticAvgA::getDepart, "JG");
                lqw.isNotNull(VmDemocraticAvgA::getScore);
                jgList = avgAService.list(lqw);
                if (!jgList.isEmpty()) {
                    Iterator<VmDemocraticAvgA> iterator = jgList.iterator();
                    while (iterator.hasNext()) {
                        VmDemocraticAvgA a = iterator.next();
                        String depart = a.getDepart();
                        String substring = depart.substring(2);
                        if (!departIds.contains(substring)) {
                            iterator.remove();
                        }
                    }
                }
            }
        }

        queryWrapper.orderByAsc("ranking");

        List<VmDemocraticAvgA> data = avgAService.getData(queryWrapper);
        if (!jgList.isEmpty()) {
            data.addAll(jgList);
        }

        // 以scope降序排序，如果是null，排在后面
        data.sort(Comparator.comparing(VmDemocraticAvgA::getScore, Comparator.nullsLast(Comparator.reverseOrder())));
        // 间隙排名，相同分数并列，例如100，100，95，他们的排名是:1,1,3
        int rank = 1;
        for (int i = 0; i < data.size(); i++) {
            VmDemocraticAvgA a = data.get(i);
            if (i > 0 && data.get(i - 1).getScore().compareTo(a.getScore()) != 0) {
                rank = i + 1;
            }
            a.setRanking(rank);
        }


        String hashId = params.getAppraisee();
        // 找出Appraisee属性为hashId的索引
        VmDemocraticAvgA summary = data.stream().filter(item -> item.getAppraisee().equals(hashId)).findFirst().orElse(null);
        int i1 = data.indexOf(summary) + 1;

        Map<String, Object> res = new HashMap<>();

        Integer nums = params.getNums();
        if (nums != null) {
            // nums除以100，保留两位小数
            BigDecimal divide = new BigDecimal(nums).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            // data的size乘divide，向下取整
            int size = (int) (data.size() * divide.doubleValue());

            int i = data.size() - size;

            if (i1 > i) res.put("underLine", true);
            else res.put("underLine", false);
            res.put("year", year);
            if (summary == null) res.put("data", 0);
            else res.put("data", summary.getRanking());
        }

        return res;
    }

    private Map<String, Object> getItem2DataMap(AssessDemocraticEvaluationSummary params,
                                                HttpServletRequest req, String year) {
        QueryWrapper<VmDemocraticAvgB> queryWrapper = new QueryWrapper<>();
        Map<String, String[]> parameterMap = req.getParameterMap();

        queryWrapper.eq("current_year", year);

        if (parameterMap.get("type") != null) {
            String type = parameterMap.get("type")[0];
            if ("z".equals(type)) {
                queryWrapper.in("type", Arrays.asList("23", "22", "32"));
            } else if ("f".equals(type)) {
                queryWrapper.in("type", Arrays.asList("21", "31"));
            }
        }

        if (parameterMap.get("departType") != null) {
            String departType = parameterMap.get("departType")[0];
            if ("bureau".equals(departType)) {
                queryWrapper.eq("depart_type", "bureau");
            } else {
                queryWrapper.ne("depart_type", "bureau");
            }
        }

        List<VmDemocraticAvgB> jgList = new ArrayList<>();
        if (parameterMap.get("leader") != null) {
            String leader = parameterMap.get("leader")[0];
            LambdaQueryWrapper<AssessLeaderDepartConfig> qw = new LambdaQueryWrapper<>();
            qw.eq(AssessLeaderDepartConfig::getLeaderId, leader);
            List<AssessLeaderDepartConfig> leaderConfig = deaderDepartConfigMapper.selectList(qw);
            StringBuilder ids = new StringBuilder();
            for (AssessLeaderDepartConfig config : leaderConfig) {
                ids.append(config.getDepartId()).append(",");
            }
            // 去除最后逗号
            if (ids.length() > 0) {
                ids.deleteCharAt(ids.length() - 1);
            }

            // 转换为List
            List<String> departIds = Arrays.asList(ids.toString().split(","));

            if (!departIds.isEmpty()) {
                queryWrapper.in("depart", departIds);
            }

            if (parameterMap.get("type") == null || "z".equals(parameterMap.get("type")[0])) {
                LambdaQueryWrapper<VmDemocraticAvgB> lqw = new LambdaQueryWrapper<>();
                lqw.eq(VmDemocraticAvgB::getCurrentYear, year);
                lqw.like(VmDemocraticAvgB::getDepart, "JG");
                lqw.isNotNull(VmDemocraticAvgB::getScore);
                jgList = avgBService.list(lqw);
                if (!jgList.isEmpty()) {
                    Iterator<VmDemocraticAvgB> iterator = jgList.iterator();
                    while (iterator.hasNext()) {
                        VmDemocraticAvgB a = iterator.next();
                        String depart = a.getDepart();
                        String substring = depart.substring(2);
                        if (!departIds.contains(substring)) {
                            iterator.remove();
                        }
                    }
                }
            }
        }

        queryWrapper.orderByAsc("ranking");

        List<VmDemocraticAvgB> data = avgBService.getData(queryWrapper);
        if (!jgList.isEmpty()) {
            data.addAll(jgList);
        }

        // 以scope降序排序，如果是null，排在后面
        data.sort(Comparator.comparing(VmDemocraticAvgB::getScore, Comparator.nullsLast(Comparator.reverseOrder())));
        // 间隙排名，相同分数并列，例如100，100，95，他们的排名是:1,1,3
        int rank = 1;
        for (int i = 0; i < data.size(); i++) {
            VmDemocraticAvgB a = data.get(i);
            if (i > 0 && data.get(i - 1).getScore().compareTo(a.getScore()) != 0) {
                rank = i + 1;
            }
            a.setRanking(rank);
        }


        String hashId = params.getAppraisee();
        // 找出Appraisee属性为hashId的索引
        VmDemocraticAvgB summary = data.stream().filter(item -> item.getAppraisee().equals(hashId)).findFirst().orElse(null);
        int i1 = data.indexOf(summary) + 1;

        Map<String, Object> res = new HashMap<>();

        Integer nums = params.getNums();
        if (nums != null) {
            // nums除以100，保留两位小数
            BigDecimal divide = new BigDecimal(nums).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            // data的size乘divide，向下取整
            int size = (int) (data.size() * divide.doubleValue());

            int i = data.size() - size;

            if (i1 > i) res.put("underLine", true);
            else res.put("underLine", false);
            res.put("year", year);
            if (summary == null) res.put("data", 0);
            else res.put("data", summary.getRanking());
        }

        return res;
    }

    private Map<String, Object> getItem3DataMap(AssessDemocraticEvaluationSummary params,
                                                HttpServletRequest req, String year) {
        QueryWrapper<VmDemocraticAvgC> queryWrapper = new QueryWrapper<>();
        Map<String, String[]> parameterMap = req.getParameterMap();

        queryWrapper.eq("current_year", year);

        if (parameterMap.get("type") != null) {
            String type = parameterMap.get("type")[0];
            if ("z".equals(type)) {
                queryWrapper.in("type", Arrays.asList("23", "22", "32"));
            } else if ("f".equals(type)) {
                queryWrapper.in("type", Arrays.asList("21", "31"));
            }
        }

        if (parameterMap.get("departType") != null) {
            String departType = parameterMap.get("departType")[0];
            if ("bureau".equals(departType)) {
                queryWrapper.eq("depart_type", "bureau");
            } else {
                queryWrapper.ne("depart_type", "bureau");
            }
        }

        List<VmDemocraticAvgC> jgList = new ArrayList<>();
        if (parameterMap.get("leader") != null) {
            String leader = parameterMap.get("leader")[0];
            LambdaQueryWrapper<AssessLeaderDepartConfig> qw = new LambdaQueryWrapper<>();
            qw.eq(AssessLeaderDepartConfig::getLeaderId, leader);
            List<AssessLeaderDepartConfig> leaderConfig = deaderDepartConfigMapper.selectList(qw);
            StringBuilder ids = new StringBuilder();
            for (AssessLeaderDepartConfig config : leaderConfig) {
                ids.append(config.getDepartId()).append(",");
            }
            // 去除最后逗号
            if (ids.length() > 0) {
                ids.deleteCharAt(ids.length() - 1);
            }

            // 转换为List
            List<String> departIds = Arrays.asList(ids.toString().split(","));

            if (!departIds.isEmpty()) {
                queryWrapper.in("depart", departIds);
            }

            if (parameterMap.get("type") == null || "z".equals(parameterMap.get("type")[0])) {
                LambdaQueryWrapper<VmDemocraticAvgC> lqw = new LambdaQueryWrapper<>();
                lqw.eq(VmDemocraticAvgC::getCurrentYear, year);
                lqw.like(VmDemocraticAvgC::getDepart, "JG");
                lqw.isNotNull(VmDemocraticAvgC::getScore);
                jgList = avgCService.list(lqw);
                if (!jgList.isEmpty()) {
                    Iterator<VmDemocraticAvgC> iterator = jgList.iterator();
                    while (iterator.hasNext()) {
                        VmDemocraticAvgC a = iterator.next();
                        String depart = a.getDepart();
                        String substring = depart.substring(2);
                        if (!departIds.contains(substring)) {
                            iterator.remove();
                        }
                    }
                }
            }
        }

        queryWrapper.orderByAsc("ranking");

        List<VmDemocraticAvgC> data = avgCService.getData(queryWrapper);
        if (!jgList.isEmpty()) {
            data.addAll(jgList);
        }

        // 以scope降序排序，如果是null，排在后面
        data.sort(Comparator.comparing(VmDemocraticAvgC::getScore, Comparator.nullsLast(Comparator.reverseOrder())));
        // 间隙排名，相同分数并列，例如100，100，95，他们的排名是:1,1,3
        int rank = 1;
        for (int i = 0; i < data.size(); i++) {
            VmDemocraticAvgC a = data.get(i);
            if (i > 0 && data.get(i - 1).getScore().compareTo(a.getScore()) != 0) {
                rank = i + 1;
            }
            a.setRanking(rank);
        }


        String hashId = params.getAppraisee();
        // 找出Appraisee属性为hashId的索引
        VmDemocraticAvgC summary = data.stream().filter(item -> item.getAppraisee().equals(hashId)).findFirst().orElse(null);
        int i1 = data.indexOf(summary) + 1;

        Map<String, Object> res = new HashMap<>();

        Integer nums = params.getNums();
        if (nums != null) {
            // nums除以100，保留两位小数
            BigDecimal divide = new BigDecimal(nums).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            // data的size乘divide，向下取整
            int size = (int) (data.size() * divide.doubleValue());

            int i = data.size() - size;

            if (i1 > i) res.put("underLine", true);
            else res.put("underLine", false);
            res.put("year", year);
            if (summary == null) res.put("data", 0);
            else res.put("data", summary.getRanking());
        }

        return res;
    }

    @PostMapping("/svg2Excel")
    public void svg2Excel(@RequestParam("svgs") List<MultipartFile> svgFiles,
                          @RequestParam("name") String name,
                          @RequestParam("desc") String desc,
                          HttpServletResponse response) throws Exception {
        String templatePath = uploadPath + "/template/" + "scoreChange.xlsx";
//        templatePath = templatePath.replace("//", "\\");

        // 1. 读取模板
        Workbook workbook = ExcelTemplateUtils.readTemplate(templatePath);
        Sheet sheet = workbook.getSheetAt(0);
        // 2. 转换 SVG 为 PNG
        Map<String, byte[]> imageMap = new HashMap<>();
        for (int i = 0; i < svgFiles.size(); i++) {
            byte[] pngBytes = convertSvgToPng(svgFiles.get(i).getBytes());
            byte[] resizedBytes = ImageResizeUtils.resizeHighQuality(pngBytes, 295, 340);
            imageMap.put("${image_" + (i + 1) + "}", resizedBytes);
        }

        //将map元素顺序倒置
        Map<String, byte[]> imageMapReverse = new LinkedHashMap<>();
        List<Map.Entry<String, byte[]>> list = new ArrayList<>(imageMap.entrySet());
        Collections.reverse(list);
        for (Map.Entry<String, byte[]> entry : list) {
            imageMapReverse.put(entry.getKey(), entry.getValue());
        }

        // 3. 插入图片
        ExcelTemplateUtils.insertImage(workbook, sheet, imageMapReverse);

        // 4. 替换文本
        Map<String, String> textMap = new HashMap<>();
        textMap.put("${text_1}", CommonUtils.getNameByHashId(name) + "近三年民主测评排名变化");
        textMap.put("${desc}", desc);
        ExcelTemplateUtils.replaceText(sheet, textMap);

        // 5. 输出 Excel
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=report.xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }


    /**
     * 将 SVG 字节数组转换为 PNG 字节数组
     */
    private byte[] convertSvgToPng(byte[] svgBytes) throws Exception {
        PNGTranscoder transcoder = new PNGTranscoder();

        // 关键设置：提高分辨率（默认 96 DPI，此处设为 300 DPI）
        transcoder.addTranscodingHint(PNGTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, 25.4f / 300);

        try (ByteArrayInputStream svgStream = new ByteArrayInputStream(svgBytes);
             ByteArrayOutputStream pngStream = new ByteArrayOutputStream()) {
            TranscoderInput input = new TranscoderInput(svgStream);
            TranscoderOutput output = new TranscoderOutput(pngStream);
            transcoder.transcode(input, output);
            return pngStream.toByteArray();
        }
    }

    /**
     * 将图片插入 Excel 的指定单元格
     */
    private void insertImageToSheet(
            Workbook workbook,
            Sheet sheet,
            byte[] imageBytes,
            int r,
            int c
    ) {
        // 1. 将图片添加到工作簿
        int pictureIdx = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG); // 需要先转为 PNG
        CreationHelper helper = workbook.getCreationHelper();
        Drawing<?> drawing = sheet.createDrawingPatriarch();

        // 5. 定位到当前行（例如每张图占一行）
        int rowNum = r;  // 可根据需求调整行号
        Row row = sheet.createRow(rowNum);

        // 6. 设置行高（关键修改点）
        row.setHeightInPoints(100);  // 设置行高为 100 磅

        // 7. 定义图片位置（示例：从 A 列开始）
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(c);   // A列
        anchor.setRow1(r);
        anchor.setDx1(0);
        anchor.setDy1(0);
        anchor.setDx2(1023); // 占满列宽
        anchor.setDy2(255);  // 占满行高

        // 8. 插入图片到指定行
        drawing.createPicture(anchor, pictureIdx);

        // 9. 调整列宽（可选）
        sheet.setColumnWidth(0, 30 * 256); // 设置 A 列宽为 30 字符
    }


}