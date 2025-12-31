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
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.*;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
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
    public Result<IPage<AssessDemocraticEvaluationSummary>> queryPageList(
            AssessDemocraticEvaluationSummary assessDemocraticEvaluationSummary,
            String year,
            HttpServletRequest req) {

        QueryWrapper<AssessDemocraticEvaluationSummary> queryWrapper = new QueryWrapper<>();
        Map<String, String[]> parameter = req.getParameterMap();
        Map<String, String[]> parameterMap = new HashMap<>();

        parameter.forEach((key, value) -> {
            if (!(value.length == 1 && value[0].isEmpty())) {
                parameterMap.put(key, value);
            }
        });

        // 1. 提取currentYear处理逻辑
        if (year != null) queryWrapper.eq("current_year", year);
        else handleCurrentYearCondition(queryWrapper, parameterMap);
        // 2. 提取type条件处理
        handleTypeCondition(queryWrapper, parameterMap);
        // 3. 提取departType条件处理
        handleDepartTypeCondition(queryWrapper, parameterMap);
        // 4. 提取depart条件处理
        handleDepartCondition(queryWrapper, parameterMap);
        // 5. 提取leader相关处理
        List<AssessDemocraticEvaluationSummary> jgList = handleLeaderCondition(queryWrapper, parameterMap);
        // 6. 主查询及结果合并
        List<AssessDemocraticEvaluationSummary> data = democraticSummaryService.list(queryWrapper);
        if (!jgList.isEmpty()) {
            data.addAll(jgList);
        }
        // 7. 统一排序（空值置后）
        data.sort(Comparator.comparing(
                AssessDemocraticEvaluationSummary::getRanking,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));
        // 8. 封装分页结果
        Page<AssessDemocraticEvaluationSummary> page = new Page<>();
        page.setRecords(data);
        return Result.OK(page);
    }

    // currentYear条件处理
    private void handleCurrentYearCondition(QueryWrapper<AssessDemocraticEvaluationSummary> queryWrapper,
                                            Map<String, String[]> parameterMap) {
        if (parameterMap.containsKey("currentYear")) {
            queryWrapper.eq("current_year", parameterMap.get("currentYear")[0]);
        } else {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
            if (currentAssessInfo != null) {
                queryWrapper.eq("current_year", currentAssessInfo.getCurrentYear());
            }
        }
    }

    // type条件处理
    private void handleTypeCondition(QueryWrapper<AssessDemocraticEvaluationSummary> queryWrapper,
                                     Map<String, String[]> parameterMap) {
        if (parameterMap.containsKey("type")) {
            String type = parameterMap.get("type")[0];
            if ("z".equals(type)) {
                queryWrapper.in("type", Arrays.asList("23", "22", "32"));
            } else if ("f".equals(type)) {
                queryWrapper.in("type", Arrays.asList("21", "31"));
            }
        }
    }

    // departType条件处理
    private void handleDepartTypeCondition(QueryWrapper<AssessDemocraticEvaluationSummary> queryWrapper,
                                           Map<String, String[]> parameterMap) {
        if (parameterMap.containsKey("departType")) {
            String departType = parameterMap.get("departType")[0];
            queryWrapper.eq("bureau".equals(departType), "depart_type", "bureau")
                    .ne(!"bureau".equals(departType), "depart_type", "bureau");
        }
    }

    // depart条件处理
    private void handleDepartCondition(QueryWrapper<AssessDemocraticEvaluationSummary> queryWrapper,
                                       Map<String, String[]> parameterMap) {
        if (parameterMap.containsKey("depart")) {
            String departs = parameterMap.get("depart")[0];
            List<String> deps = Arrays.stream(departs.split(","))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            if (!deps.isEmpty()) {
                queryWrapper.in("depart", deps);
            }
        }
    }

    // leader条件处理
    private List<AssessDemocraticEvaluationSummary> handleLeaderCondition(
            QueryWrapper<AssessDemocraticEvaluationSummary> queryWrapper,
            Map<String, String[]> parameterMap) {
        List<AssessDemocraticEvaluationSummary> jgList = new ArrayList<>();

        if (!parameterMap.containsKey("leader")) {
            return jgList;
        }

        String leader = parameterMap.get("leader")[0];
        String year = null;
        if (parameterMap.containsKey("currentYear")) {
            year = parameterMap.get("currentYear")[0];
        }

        // 优化：直接获取部门ID列表
        List<String> departIds = userCommonApi.getHistoryUnitDeptIds(Collections.singletonList(leader), year);
        queryWrapper.in("depart", departIds);
        if (parameterMap.containsKey("departType")) {

        }
        if ((parameterMap.containsKey("type") && !"z".equals(parameterMap.get("type")[0]))
                || (parameterMap.containsKey("departType") && !"bureau".equals(parameterMap.get("departType")[0]))) {
            return jgList;
        }


        // 优化：避免重复逻辑
        if (shouldFetchJGList(parameterMap)) {
            jgList = fetchJGList(parameterMap, departIds);
        }
        return jgList;
    }

    // 子方法：判断是否需要获取JG列表
    private boolean shouldFetchJGList(Map<String, String[]> parameterMap) {
        return !parameterMap.containsKey("type") || "z".equals(parameterMap.get("type")[0]);
    }

    // 子方法：获取并过滤JG列表
    private List<AssessDemocraticEvaluationSummary> fetchJGList(
            Map<String, String[]> parameterMap,
            List<String> departIds) {

        LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw = new LambdaQueryWrapper<>();
        // 复用currentYear条件
        if (parameterMap.containsKey("currentYear")) {
            lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, parameterMap.get("currentYear")[0]);
        } else {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
            if (currentAssessInfo != null) {
                lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, currentAssessInfo.getCurrentYear());
            }
        }

        lqw.like(AssessDemocraticEvaluationSummary::getDepart, "JG")
                .isNotNull(AssessDemocraticEvaluationSummary::getScore);

        return democraticSummaryService.list(lqw).stream()
                .filter(a -> {
                    String depart = a.getDepart();
                    return depart.length() > 2 && departIds.contains(depart.substring(2));
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/listPro2Excel")
    public JSONObject queryPageList4Pro2Excel(AssessDemocraticEvaluationSummary params,
                                              @RequestParam(name = "startYear", defaultValue = "0", required = false) String startYear,
                                              @RequestParam(name = "endYear", defaultValue = "0", required = false) String endYear,
                                              HttpServletRequest req) {
        if ("0".equals(endYear) || endYear == null) {
            AssessCurrentAssess info = assessCommonApi.getCurrentAssessInfo("annual");
            endYear = info.getCurrentYear();
        }

        if ("0".equals(startYear) || startYear == null) {
            startYear = Integer.parseInt(endYear) - 2 + "";
        }

        Map<String, String[]> parameterMap = req.getParameterMap();
        String desc4Excel = getDesc4Excel(parameterMap);

        List<AssessDemocraticEvaluationSummary> data = new ArrayList<>();

        Result<IPage<AssessDemocraticEvaluationSummary>> iPageResult = this.queryPageList4Pro(params, startYear, req);

        IPage<AssessDemocraticEvaluationSummary> result1 = iPageResult.getResult();
        if (result1 != null) {
            data = result1.getRecords();
        }

        if (data.isEmpty()){
            Result<IPage<AssessDemocraticEvaluationSummary>> iPageResul = this.queryPageList4Pro(params, endYear, req);
            IPage<AssessDemocraticEvaluationSummary> result = iPageResul.getResult();
            if (result != null) {
                data = result.getRecords();
            }
        }


        List<JSONObject> resData = new ArrayList<>();

        Map<String, String> id2NameMap = departCommonApi.getId2NameMap();

        int i = 1;
        for (AssessDemocraticEvaluationSummary item : data) {
            JSONObject json = new JSONObject();
            json.put("id", i);
            json.put("name", CommonUtils.getNameByHashId(item.getAppraisee()));
            json.put("depart", id2NameMap.get(item.getDepart()));
            json.put("year", item.getCurrentYear());
            json.put("score", item.getScore());
            json.put("ranking", item.getRanking());
            json.put("scopRamking", item.getScopeRanking());
            json.put("currentYear", item.getCurrentYear());
            json.put("desc", desc4Excel + "人员名单");
            resData.add(json);
            i++;
        }


        JSONObject json = new JSONObject();
        json.put("data", resData);
        return json;
    }

    @PostMapping("/people2Excel")
    public void exportPeopleExcel(@Value("${server.port}") String port,
                                  @RequestBody JSONObject params,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {

        String url = "http://localhost:" + port + "/jmreport/exportAllExcelStream" + "?token=" + TokenUtils.getTokenByRequest(request);
        JSONObject queryParam = new JSONObject();

        // 遍历params
        for (String key : params.keySet()) {
            queryParam.put(key, params.get(key));
        }

        Map<String, Object> param = new JSONObject();
        param.put("excelConfigId", "1127770269838737408");
        param.put("queryParam", queryParam);
        String filename = this.getDesc4Excel(params) + ".xlsx";
        assessCommonApi.getExportExcel(url, filename, param, response);
    }

    private String getDesc4Excel(Map<String, String[]> parameterMap) {
        String desc = "";

        String endYear = "";
        String startYear = "";
        if (!parameterMap.containsKey("endYear")
                || parameterMap.get("endYear") == null
                || "0".equals(parameterMap.get("endYear")[0])
                || parameterMap.get("endYear")[0].isEmpty()
                || "undefined".equals(parameterMap.get("endYear")[0])) {

            AssessCurrentAssess info = assessCommonApi.getCurrentAssessInfo("annual");
            endYear = info.getCurrentYear();

        } else {
            endYear = parameterMap.get("endYear")[0];
        }

        if (!parameterMap.containsKey("startYear")
                || parameterMap.get("startYear") == null
                || "0".equals(parameterMap.get("startYear")[0])
                || parameterMap.get("startYear")[0].isEmpty()
                || "undefined".equals(parameterMap.get("startYear")[0])) {
            startYear = Integer.parseInt(endYear) - 2 + "";
        } else {
            startYear = parameterMap.get("startYear")[0];
        }

        desc += startYear + "-" + endYear + "年度";

        if (parameterMap.containsKey("trial") && "true".equals(parameterMap.get("trial")[0])) {
            desc += "试用期人员，";
        } else {
            if (parameterMap.containsKey("leader") && !parameterMap.get("leader")[0].isEmpty()) {
                String leader = parameterMap.get("leader")[0];
                List<LoginUser> userByIds = userCommonApi.getUserByIds(new String[]{leader});
                if (userByIds != null && !userByIds.isEmpty()) {
                    desc += userByIds.get(0).getRealname() + "考核单元内";
                }
            }

            if (parameterMap.containsKey("departType") && !parameterMap.get("departType")[0].isEmpty()) {
                String departType = parameterMap.get("departType")[0];
                if ("bureau".equals(departType)) departType = "局机关处室";
                else departType = "基层单位";
                desc += departType;
            }

            if (parameterMap.containsKey("type") && !parameterMap.get("type")[0].isEmpty()) {
                String type = parameterMap.get("type")[0];
                if ("z".equals(type)) type = "正职";
                else type = "副职";
                desc += type;
            }
        }

        if (parameterMap.containsKey("nums") && !parameterMap.get("nums")[0].isEmpty()) {
            String nums = parameterMap.get("nums")[0];
            desc += "后" + nums + "%";
        }

        return desc;
    }

    private String getDesc4Excel(JSONObject parameterMap) {
        String desc = "";

        String endYear = "";
        String startYear = "";
        if (!parameterMap.containsKey("endYear")
                || parameterMap.get("endYear") == null
                || "0".equals(parameterMap.get("endYear"))
                || "undefined".equals(parameterMap.get("endYear"))) {

            AssessCurrentAssess info = assessCommonApi.getCurrentAssessInfo("annual");
            endYear = info.getCurrentYear();

        } else {
            endYear = parameterMap.getString("endYear");
        }

        if (!parameterMap.containsKey("startYear")
                || parameterMap.get("startYear") == null
                || "0".equals(parameterMap.get("startYear"))
                || "undefined".equals(parameterMap.get("startYear"))) {
            startYear = Integer.parseInt(endYear) - 2 + "";
        } else {
            startYear = parameterMap.getString("startYear");
        }

        desc += startYear + "-" + endYear + "年度";

        if (parameterMap.containsKey("trial") && parameterMap.getBoolean("trial")) {
            desc += "试用期人员，";
        } else {
            if (parameterMap.containsKey("leader")) {
                String leader = parameterMap.getString("leader");
                List<LoginUser> userByIds = userCommonApi.getUserByIds(new String[]{leader});
                if (userByIds != null && !userByIds.isEmpty()) {
                    desc += userByIds.get(0).getRealname() + "考核单元内";
                }
            }

            if (parameterMap.containsKey("departType")) {
                String departType = parameterMap.getString("departType");
                if ("bureau".equals(departType)) departType = "局机关处室";
                else departType = "基层单位";
                desc += departType;
            }

            if (parameterMap.containsKey("type")) {
                String type = parameterMap.getString("type");
                if ("z".equals(type)) type = "正职";
                else type = "副职";
                desc += type;
            }
        }

        if (parameterMap.containsKey("nums")) {
            String nums = parameterMap.getString("nums");
            desc += "后" + nums + "%";
        }

        return desc;
    }

    @GetMapping(value = "/listPro")
    public Result<IPage<AssessDemocraticEvaluationSummary>> queryPageList4Pro(AssessDemocraticEvaluationSummary params,
                                                                              @RequestParam(name = "startYear", required = false, defaultValue = "0") String startYear,
                                                                              HttpServletRequest req) {
        List<AssessDemocraticEvaluationSummary> data = new ArrayList<>();

        Boolean trial = params.getTrial();
        if (Boolean.TRUE.equals(trial)) {
            List<AssessTrialPeople> trials = trialService.list();
            if (trials != null && !trials.isEmpty()) {

                if (startYear == null || "0".equals(startYear)) {
                    AssessCurrentAssess annual = assessCommonApi.getCurrentAssessInfo("annual");
                    startYear = annual.getCurrentYear();
                }

                // 将hashId取出组合成List
                LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw = new LambdaQueryWrapper<>();
                List<String> trialIds = trials.stream().map(AssessTrialPeople::getHashId).collect(Collectors.toList());
                lqw.in(AssessDemocraticEvaluationSummary::getAppraisee, trialIds);
                lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, startYear);
                lqw.orderByAsc(AssessDemocraticEvaluationSummary::getRanking);
                data = democraticSummaryService.list(lqw);
            }
        } else {
            Result<IPage<AssessDemocraticEvaluationSummary>> iPageResult = this.queryPageList(params, "0".equals(startYear) ? null : startYear, req);
            IPage<AssessDemocraticEvaluationSummary> result = iPageResult.getResult();
            if (result != null) {
                data = result.getRecords();
            }
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

//    @GetMapping(value = "/listPro")
//    public Result<IPage<AssessDemocraticEvaluationSummary>> queryPageList4Pro(AssessDemocraticEvaluationSummary params,
//                                                                              @RequestParam(name = "startYear", required = false, defaultValue = "0") String startYear,
//                                                                              HttpServletRequest req) {
//        // QueryWrapper<AssessDemocraticEvaluationSummary> queryWrapper = QueryGenerator.initQueryWrapper(assessDemocraticEvaluationSummary, req.getParameterMap());
//        QueryWrapper<AssessDemocraticEvaluationSummary> queryWrapper = new QueryWrapper<>();
//        Map<String, String[]> parameterMap = req.getParameterMap();
//
//        if ("0".equals(startYear) || startYear == null) {
//            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
//            if (currentAssessInfo != null) {
//                startYear = currentAssessInfo.getCurrentYear();
//            }
//        }
//
//        queryWrapper.eq("current_year", startYear);
//
//
//        Boolean trial = params.getTrial();
//        List<AssessDemocraticEvaluationSummary> jgList = new ArrayList<>();
//
//        if (trial == null || !trial) {
//            if (parameterMap.get("type") != null) {
//                String type = parameterMap.get("type")[0];
//                if ("z".equals(type)) {
//                    queryWrapper.in("type", Arrays.asList("23", "22", "32"));
//                } else if ("f".equals(type)) {
//                    queryWrapper.in("type", Arrays.asList("21", "31"));
//                }
//            }
//
//            if (parameterMap.get("departType") != null) {
//                String departType = parameterMap.get("departType")[0];
//                if ("bureau".equals(departType)) {
//                    queryWrapper.eq("depart_type", "bureau");
//                } else {
//                    queryWrapper.ne("depart_type", "bureau");
//                }
//            }
//
//            if (parameterMap.get("leader") != null) {
//                String leader = parameterMap.get("leader")[0];
//                List<String> departIds = userCommonApi.getHistoryUnitDeptIds(Collections.singletonList(leader), startYear);
//
//                if (!departIds.isEmpty()) {
//                    queryWrapper.in("depart", departIds);
//                }
//
//                if (parameterMap.get("type") == null || "z".equals(parameterMap.get("type")[0])) {
//                    LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw = new LambdaQueryWrapper<>();
//                    lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, startYear);
//                    lqw.like(AssessDemocraticEvaluationSummary::getDepart, "JG");
//                    lqw.isNotNull(AssessDemocraticEvaluationSummary::getScore);
//                    jgList = democraticSummaryService.list(lqw);
//
//                    if (!jgList.isEmpty()) {
//                        Iterator<AssessDemocraticEvaluationSummary> iterator = jgList.iterator();
//                        while (iterator.hasNext()) {
//                            AssessDemocraticEvaluationSummary a = iterator.next();
//                            String depart = a.getDepart();
//                            String substring = depart.substring(2);
//                            if (!departIds.contains(substring)) {
//                                iterator.remove();
//                            }
//                        }
//                    }
//                }
//            }
//        } else {
//            List<AssessTrialPeople> trials = trialService.list();
//            if (trials != null && !trials.isEmpty()) {
//                // 将hashId取出组合成List
//                List<String> trialIds = trials.stream().map(AssessTrialPeople::getHashId).collect(Collectors.toList());
//                queryWrapper.in("appraisee", trialIds);
//            }
//        }
//
//        queryWrapper.orderByAsc("ranking");
//
//        List<AssessDemocraticEvaluationSummary> data = democraticSummaryService.getData(queryWrapper);
//
//        if (!jgList.isEmpty()) {
//            data.addAll(jgList);
//        }
//
//        // 以ranking降序排序，如果是null，排在后面
//        data.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getRanking, Comparator.nullsLast(Comparator.reverseOrder())));
//
//        Integer nums = params.getNums();
//        if (!Boolean.TRUE.equals(trial) && nums != null) {
//            // nums除以100，保留两位小数
//            BigDecimal divide = new BigDecimal(nums).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
//            // data的size乘divide，向下取整
//            int size = (int) (data.size() * divide.doubleValue());
//            // 截取size个元素
//            data = data.subList(0, size);
//
//        }
//
//        data.sort(Comparator.comparing(AssessDemocraticEvaluationSummary::getRanking, Comparator.nullsLast(Comparator.naturalOrder())));
//
//
//        Page<AssessDemocraticEvaluationSummary> page = new Page<>();
//        page.setRecords(data);
//        return Result.OK(page);
//    }

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

        LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, year);

        List<AssessDemocraticEvaluationSummary> summaryLists = democraticSummaryService.list(lqw);
        if (summaryLists == null || summaryLists.isEmpty()) {
            return Result.error("目前不存在进行中的年度考核民主测评，可在页面上方搜索其他年度查看。");
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
                if (!currentAssessInfo.isAssessing()) {
                    return Result.ok(new Page<DepartProgressVO>().setRecords(new ArrayList<>()));
                }
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

//        List<SysUser> allLeader = userCommonApi.getAllLeader();
        List<SysUser> allLeader = userCommonApi.getHistoryAssessUnit(currentYear);
        Map<String, String> leaderMap = allLeader.stream().collect(Collectors.toMap(SysUser::getId, SysUser::getRealname));

        for (SysUser user : allLeader) {
            leader = user.getId();
            leaderMap.put(user.getId(), user.getRealname());
            for (int i = 0; i < 5; i++) {
                JSONObject queryParam = new JSONObject();
                queryParam.put("year", currentYear);
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
                queryParam.put("year", currentYear);
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
            queryParam.put("year", currentYear);
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
            queryParam.put("year", currentYear);
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

        if ("0".equals(year) || year == null) {
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
    public Result<Map<String, Object>> getData1(
            AssessDemocraticEvaluationSummary params,
            HttpServletRequest req,
            @RequestParam(name = "startYear", defaultValue = "0", required = false) String startYear,
            @RequestParam(name = "endYear", defaultValue = "0", required = false) String endYear) {

        if ("0".equals(endYear) || endYear == null) {
            AssessCurrentAssess info = assessCommonApi.getCurrentAssessInfo("annual");
            endYear = info.getCurrentYear();
        }

        if ("0".equals(startYear) || startYear == null) {
            startYear = Integer.parseInt(endYear) - 2 + "";
        }

        int startYearInt = Integer.parseInt(startYear);
        int endYearInt = Integer.parseInt(endYear);

        List<Map<String, Object>> dataMapList = new ArrayList<>();
        for (int i = startYearInt; i <= endYearInt; i++) {
            Map<String, Object> dataMap = getDataMap(params, req, String.valueOf(i));
            dataMapList.add(dataMap);
        }

        return getRes(dataMapList, params);
    }

    @GetMapping("/data2")
    public Result<Map<String, Object>> data2(AssessDemocraticEvaluationSummary params, HttpServletRequest req,
                                             @RequestParam(name = "startYear", defaultValue = "0", required = false) String startYear,
                                             @RequestParam(name = "endYear", defaultValue = "0", required = false) String endYear) {

        if ("0".equals(endYear) || endYear == null) {
            AssessCurrentAssess info = assessCommonApi.getCurrentAssessInfo("annual");
            endYear = info.getCurrentYear();
        }

        if ("0".equals(startYear) || startYear == null) {
            startYear = Integer.parseInt(endYear) - 2 + "";
        }

        int startYearInt = Integer.parseInt(startYear);
        int endYearInt = Integer.parseInt(endYear);

        List<Map<String, Object>> dataMapList = new ArrayList<>();
        for (int i = startYearInt; i <= endYearInt; i++) {
            Map<String, Object> dataMap = getItem1DataMap(params, req, String.valueOf(i));
            dataMapList.add(dataMap);
        }

        return getRes(dataMapList, params);

    }

    @GetMapping("/data3")
    public Result<Map<String, Object>> data3(AssessDemocraticEvaluationSummary params, HttpServletRequest req,
                                             @RequestParam(name = "startYear", defaultValue = "0", required = false) String startYear,
                                             @RequestParam(name = "endYear", defaultValue = "0", required = false) String endYear) {
        if ("0".equals(endYear) || endYear == null) {
            AssessCurrentAssess info = assessCommonApi.getCurrentAssessInfo("annual");
            endYear = info.getCurrentYear();
        }

        if ("0".equals(startYear) || startYear == null) {
            startYear = Integer.parseInt(endYear) - 2 + "";
        }

        int startYearInt = Integer.parseInt(startYear);
        int endYearInt = Integer.parseInt(endYear);

        List<Map<String, Object>> dataMapList = new ArrayList<>();
        for (int i = startYearInt; i <= endYearInt; i++) {
            Map<String, Object> dataMap = getItem2DataMap(params, req, String.valueOf(i));
            dataMapList.add(dataMap);
        }

        return getRes(dataMapList, params);

    }

    @GetMapping("/data4")
    public Result<Map<String, Object>> data4(AssessDemocraticEvaluationSummary params, HttpServletRequest req,
                                             @RequestParam(name = "startYear", defaultValue = "0", required = false) String startYear,
                                             @RequestParam(name = "endYear", defaultValue = "0", required = false) String endYear) {
        if ("0".equals(endYear) || endYear == null) {
            AssessCurrentAssess info = assessCommonApi.getCurrentAssessInfo("annual");
            endYear = info.getCurrentYear();
        }

        if ("0".equals(startYear) || startYear == null) {
            startYear = Integer.parseInt(endYear) - 2 + "";
        }

        int startYearInt = Integer.parseInt(startYear);
        int endYearInt = Integer.parseInt(endYear);

        List<Map<String, Object>> dataMapList = new ArrayList<>();
        for (int i = startYearInt; i <= endYearInt; i++) {
            Map<String, Object> dataMap = getItem3DataMap(params, req, String.valueOf(i));
            dataMapList.add(dataMap);
        }

        return getRes(dataMapList, params);
    }

    private Result<Map<String, Object>> getRes(List<Map<String, Object>> dataMapList, AssessDemocraticEvaluationSummary params) {
        Integer min = (Integer) dataMapList.get(0).get("data");
        int markPointBottom = 9999;
        int markPointTop = 0;
        List<Integer> xAxisData = new ArrayList<>();
        List<Integer> seriesData = new ArrayList<>();
        for (Map<String, Object> dataMap : dataMapList) {
            Integer data = (Integer) dataMap.get("data");
            if (data < min) {
                min = data;
            }
            Boolean underLine = (Boolean) dataMap.get("underLine");
            if (underLine && data < markPointBottom) {
                markPointBottom = data;
            }
            if (!underLine && data > markPointTop) {
                markPointTop = data;
            }
            xAxisData.add(Integer.parseInt((String) dataMap.get("year")));
            seriesData.add(data);
        }

        Map<String, Object> resMap = new HashMap<>();

        int markLineData = 0;

        if (markPointBottom != markPointTop) {
            markLineData = (markPointBottom - markPointTop) / 2 + markPointTop;
        } else {
            markLineData = markPointBottom;
        }

        resMap.put("xAxisData", xAxisData);
        resMap.put("yAxisMin", Math.max(min - 10, 0));
        resMap.put("seriesData", seriesData);
        resMap.put("markLineData", markLineData);
        resMap.put("markLineName", params.getNums() != null ? params.getNums() + "%线" : "0");

        return Result.ok(resMap);
    }


    private Map<String, Object> getDataMap(AssessDemocraticEvaluationSummary params,
                                           HttpServletRequest req, String year) {
        // 1. 构建查询条件
        QueryWrapper<AssessDemocraticEvaluationSummary> queryWrapper = buildQueryWrapper(req, year);

        // 2. 获取领导配置数据
        List<String> leaderDepartIds = getLeaderDepartIds(req, year);

        // 3. 获取JG列表
        List<AssessDemocraticEvaluationSummary> jgList = getJGList(req, year, leaderDepartIds,
                y -> {
                    LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw = new LambdaQueryWrapper<>();
                    lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, y)
                            .like(AssessDemocraticEvaluationSummary::getDepart, "JG")
                            .isNotNull(AssessDemocraticEvaluationSummary::getScore);
                    return democraticSummaryService.list(lqw);
                },
                // 部门提取函数
                AssessDemocraticEvaluationSummary::getDepart
        );

        // 4. 执行主查询
        List<AssessDemocraticEvaluationSummary> data = executeMainQuery(queryWrapper, jgList,
                qw -> democraticSummaryService.getData(qw),
                // 排名提取函数
                AssessDemocraticEvaluationSummary::getRanking);

        // 5. 处理结果
        return processResult(params, data, req, year);
    }

    // 辅助方法1：构建查询条件
    private <T> QueryWrapper<T> buildQueryWrapper(HttpServletRequest req, String year) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        Map<String, String[]> parameterMap = req.getParameterMap();

        queryWrapper.eq("current_year", year);

        // 处理type参数
        if (parameterMap.containsKey("type")) {
            String type = parameterMap.get("type")[0];
            if ("z".equals(type)) {
                queryWrapper.in("type", Arrays.asList("23", "22", "32"));
            } else if ("f".equals(type)) {
                queryWrapper.in("type", Arrays.asList("21", "31"));
            }
        }

        // 处理departType参数
        if (parameterMap.containsKey("departType")) {
            String departType = parameterMap.get("departType")[0];
            if ("bureau".equals(departType)) {
                queryWrapper.eq("depart_type", "bureau");
            } else {
                queryWrapper.ne("depart_type", "bureau");
            }
        }

        return queryWrapper;
    }


    // 辅助方法2：获取领导配置的部门ID
    private List<String> getLeaderDepartIds(HttpServletRequest req, String year) {
        Map<String, String[]> parameterMap = req.getParameterMap();
        if (!parameterMap.containsKey("leader")) {
            return Collections.emptyList();
        }

        String leader = parameterMap.get("leader")[0];

        return userCommonApi.getHistoryUnitDeptIds(Collections.singletonList(leader), year);
    }

    // 辅助方法3：获取JG列表
    private <T> List<T> getJGList(HttpServletRequest req, String year,
                                  List<String> leaderDepartIds,
                                  Function<String, List<T>> queryFunction,
                                  Function<T, String> departExtractor) {

        Map<String, String[]> parameterMap = req.getParameterMap();
        if (leaderDepartIds.isEmpty() ||
                (parameterMap.containsKey("type") && !"z".equals(parameterMap.get("type")[0]))) {
            return Collections.emptyList();
        }

        // 执行查询
        List<T> jgItems = queryFunction.apply(year);

        return jgItems.stream()
                .filter(item -> {
                    String depart = departExtractor.apply(item);
                    if (depart == null || depart.length() <= 2) return false;
                    String departId = depart.substring(2);
                    return leaderDepartIds.contains(departId);
                })
                .collect(Collectors.toList());
    }

    // 辅助方法4：执行主查询
    private <T> List<T> executeMainQuery(
            QueryWrapper<T> queryWrapper,
            List<T> jgList,
            Function<QueryWrapper<T>, List<T>> dataFetcher,
            Function<T, Integer> rankingExtractor) {

        queryWrapper.orderByAsc("ranking");
        List<T> data = dataFetcher.apply(queryWrapper);

        if (!jgList.isEmpty()) {
            data.addAll(jgList);
        }

        // 按ranking升序排序，null值排在最后
        data.sort(Comparator.comparing(
                rankingExtractor,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));

        return data;
    }

    // 辅助方法5：处理结果
    private Map<String, Object> processResult(AssessDemocraticEvaluationSummary params,
                                              List<AssessDemocraticEvaluationSummary> data,
                                              HttpServletRequest req,
                                              String year) {
        Map<String, Object> res = new HashMap<>();
        String hashId = params.getAppraisee();

        // 查找目标记录
        Optional<AssessDemocraticEvaluationSummary> summaryOpt = data.stream()
                .filter(item -> hashId.equals(item.getAppraisee()))
                .findFirst();

        if (!summaryOpt.isPresent()) {
            res.put("underLine", false);
            res.put("year", year);
            res.put("data", 0);
            return res;
        }

        AssessDemocraticEvaluationSummary summary = summaryOpt.get();
        int index = data.indexOf(summary) + 1;

        Integer nums = params.getNums();
        if (nums == null || nums == 0) {
            res.put("underLine", false);
            res.put("year", year);
            res.put("data", req.getParameter("leader") != null || Boolean.TRUE.equals(params.getTrial()) ?
                    summary.getScopeRanking() : summary.getRanking());
            return res;
        }

        // 计算分数线
        BigDecimal percentage = new BigDecimal(nums).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        int threshold = (int) (data.size() * (1 - percentage.doubleValue()));

        res.put("underLine", index > threshold);
        res.put("year", data.get(0).getCurrentYear()); // 假设所有记录年份相同
        // todo: 使用哪个排名存疑
        res.put("data", req.getParameter("leader") != null || Boolean.TRUE.equals(params.getTrial()) ?
                summary.getScopeRanking() : summary.getRanking());

        return res;
    }

    private <T> void calculateRankings(List<T> data,
                                       Function<T, BigDecimal> scoreExtractor,
                                       BiConsumer<T, Integer> rankingSetter) {
        if (data.isEmpty()) return;

        int rank = 1;
        BigDecimal previousScore = scoreExtractor.apply(data.get(0));

        for (int i = 0; i < data.size(); i++) {
            T current = data.get(i);
            BigDecimal currentScore = scoreExtractor.apply(current);

            // 如果当前分数与前一个不同，更新排名
            if (i > 0 && currentScore.compareTo(previousScore) != 0) {
                rank = i + 1;
                previousScore = currentScore;
            }

            rankingSetter.accept(current, rank);
        }
    }

    private Map<String, Object> getItem1DataMap(AssessDemocraticEvaluationSummary params,
                                                HttpServletRequest req, String year) {
        QueryWrapper<VmDemocraticAvgA> queryWrapper = buildQueryWrapper(req, year);

        List<String> leaderDepartIds = getLeaderDepartIds(req, year);

        List<VmDemocraticAvgA> jgList = getJGList(
                req, year, leaderDepartIds,
                // 查询函数
                y -> {
                    LambdaQueryWrapper<VmDemocraticAvgA> lqw = new LambdaQueryWrapper<>();
                    lqw.eq(VmDemocraticAvgA::getCurrentYear, y)
                            .like(VmDemocraticAvgA::getDepart, "JG")
                            .isNotNull(VmDemocraticAvgA::getScore);
                    return avgAService.list(lqw);
                },
                // 部门提取函数
                VmDemocraticAvgA::getDepart
        );

        queryWrapper.orderByAsc("ranking");

        List<VmDemocraticAvgA> data = executeMainQuery(
                queryWrapper,
                jgList,
                // 数据获取函数
                qw -> avgAService.getData(qw),
                // 排名提取函数
                VmDemocraticAvgA::getRanking
        );

        // 对data进行排序，按照分数降序排序
        data.sort(Comparator.comparing(VmDemocraticAvgA::getScore, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        calculateRankings(data, VmDemocraticAvgA::getScore, VmDemocraticAvgA::setRanking);

        return processResultItem(params, data, year, VmDemocraticAvgA::getAppraisee, VmDemocraticAvgA::getRanking);
    }

    private <T> Map<String, Object> processResultItem(AssessDemocraticEvaluationSummary params,
                                                      List<T> data, String year,
                                                      Function<T, String> appraiseeExtractor,
                                                      Function<T, Integer> rankingExtractor) {

        Map<String, Object> res = new HashMap<>();
        // 获取当前人员
        String hashId = params.getAppraisee();

        // 查找目标记录
        Optional<T> summaryOpt = data.stream()
                .filter(item -> hashId.equals(appraiseeExtractor.apply(item)))
                .findFirst();

        // 如果未找到，则...
        if (!summaryOpt.isPresent()) {
            res.put("underLine", false);
            res.put("year", year);
            res.put("data", 0);
            return res;
        }

        // 获取当前人员
        T summary = summaryOpt.get();
        int index = data.indexOf(summary) + 1;

        // 获取百分比，如果获取不到，则无线
        Integer nums = params.getNums();
        if (nums == null || nums == 0) {
            res.put("underLine", false);
            res.put("year", year);
            res.put("data", rankingExtractor.apply(summary));
            return res;
        }

        // 计算分数线
        BigDecimal percentage = new BigDecimal(nums).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        int threshold = (int) (data.size() * (1 - percentage.doubleValue()));

        res.put("underLine", index > threshold);
        res.put("year", year);
        res.put("data", rankingExtractor.apply(summary));

        return res;
    }

    private Map<String, Object> getItem2DataMap(AssessDemocraticEvaluationSummary params,
                                                HttpServletRequest req, String year) {

        QueryWrapper<VmDemocraticAvgB> queryWrapper = buildQueryWrapper(req, year);

        List<String> leaderDepartIds = getLeaderDepartIds(req, year);

        List<VmDemocraticAvgB> jgList = getJGList(
                req, year, leaderDepartIds,
                y -> {
                    LambdaQueryWrapper<VmDemocraticAvgB> lqw = new LambdaQueryWrapper<>();
                    lqw.eq(VmDemocraticAvgB::getCurrentYear, y)
                            .like(VmDemocraticAvgB::getDepart, "JG")
                            .isNotNull(VmDemocraticAvgB::getScore);
                    return avgBService.list(lqw);
                },
                VmDemocraticAvgB::getDepart
        );

        List<VmDemocraticAvgB> data = executeMainQuery(
                queryWrapper,
                jgList,
                qw -> avgBService.getData(qw),
                VmDemocraticAvgB::getRanking
        );

        // 对data进行排序，按照分数降序排序
        data.sort(Comparator.comparing(VmDemocraticAvgB::getScore, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        calculateRankings(data, VmDemocraticAvgB::getScore, VmDemocraticAvgB::setRanking);

        return processResultItem(params, data, year, VmDemocraticAvgB::getAppraisee, VmDemocraticAvgB::getRanking);
    }

    private Map<String, Object> getItem3DataMap(AssessDemocraticEvaluationSummary params,
                                                HttpServletRequest req, String year) {
        QueryWrapper<VmDemocraticAvgC> queryWrapper = buildQueryWrapper(req, year);

        List<String> leaderDepartIds = getLeaderDepartIds(req, year);

        List<VmDemocraticAvgC> jgList = getJGList(
                req, year, leaderDepartIds,
                y -> {
                    LambdaQueryWrapper<VmDemocraticAvgC> lqw = new LambdaQueryWrapper<>();
                    lqw.eq(VmDemocraticAvgC::getCurrentYear, y)
                            .like(VmDemocraticAvgC::getDepart, "JG")
                            .isNotNull(VmDemocraticAvgC::getScore);
                    return avgCService.list(lqw);
                },
                VmDemocraticAvgC::getDepart
        );

        List<VmDemocraticAvgC> data = executeMainQuery(
                queryWrapper,
                jgList,
                qw -> avgCService.getData(qw),
                VmDemocraticAvgC::getRanking
        );

        // 对data进行排序，按照分数降序排序
        data.sort(Comparator.comparing(VmDemocraticAvgC::getScore, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        calculateRankings(data, VmDemocraticAvgC::getScore, VmDemocraticAvgC::setRanking);

        return processResultItem(params, data, year, VmDemocraticAvgC::getAppraisee, VmDemocraticAvgC::getRanking);
    }

    @PostMapping("/svg2Excel")
    public void svg2Excel(@RequestParam("svgs") List<MultipartFile> svgFiles,
                          @RequestParam("hashId") String hashId,
                          @RequestParam("desc") String desc,
                          @RequestParam(name = "startYear", defaultValue = "0", required = false) String startYear,
                          @RequestParam(name = "endYear", defaultValue = "0", required = false) String endYear,
                          HttpServletResponse response) throws Exception {
        if ("0".equals(endYear) || "undefined".equals(endYear) || endYear == null) {
            AssessCurrentAssess info = assessCommonApi.getCurrentAssessInfo("annual");
            endYear = info.getCurrentYear();
        }

        if ("0".equals(startYear) || "undefined".equals(startYear) || startYear == null) {
            startYear = Integer.parseInt(endYear) - 2 + "";
        }

        int startYearInt = Integer.parseInt(startYear);
        int endYearInt = Integer.parseInt(endYear);


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
        Map<String, String> peopleInfo = userCommonApi.getPeopleInfo(hashId, startYear);
        if (peopleInfo == null) peopleInfo = userCommonApi.getPeopleInfo(hashId, endYear);

        Map<String, String> textMap = new HashMap<>();
        String text = CommonUtils.getNameByHashId(hashId) + startYear + "-" + endYear + "年度民主测评排名变化";
        textMap.put("${text_1}", text);
        textMap.put("${desc}", desc);

        textMap.put("${p_name}", peopleInfo.get("name") == null ? "" : peopleInfo.get("name"));
        textMap.put("${depart_type}", peopleInfo.get("departType") == null ? "" : peopleInfo.get("departType"));
        textMap.put("${p_depart}", peopleInfo.get("depart") == null ? "" : peopleInfo.get("depart"));
        textMap.put("${p_type}", peopleInfo.get("type") == null ? "" : peopleInfo.get("type"));
        textMap.put("${p_unit}", peopleInfo.get("unit") == null ? "" : peopleInfo.get("unit"));
        textMap.put("${time}", new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(new Date()));

        ExcelTemplateUtils.replaceText(sheet, textMap);

        // 6. 输出 Excel
        String filename = text + ".xlsx";
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        workbook.write(response.getOutputStream());
        workbook.close();
    }


    /**
     * 将 SVG 字节数组转换为 PNG 字节数组
     */
    public byte[] convertSvgToPng(byte[] svgBytes) throws Exception {
        // 使用 Batik  transcoder 示例
        PNGTranscoder transcoder = new PNGTranscoder();

        // 设置高分辨率（300 DPI）
        transcoder.addTranscodingHint(PNGTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, 25.4f / 300);

        // 或者直接设置输出尺寸为2倍大小
        transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, 590f); // 295 * 2
        transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, 680f); // 340 * 2

        TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(svgBytes));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(outputStream);

        transcoder.transcode(input, output);
        return outputStream.toByteArray();
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