package org.jeecg.modules.annual.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.system.vo.SysDepartModel;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.modules.annual.dto.LeadershipStatisticsDTO;
import org.jeecg.modules.annual.dto.RecommendTempDTO;
import org.jeecg.modules.annual.entity.FinalRecord;
import org.jeecg.modules.annual.service.IAssessAnnualFillService;
import org.jeecg.modules.annual.service.IAssessAnnualSummaryService;
import org.jeecg.modules.annual.vo.*;
import org.jeecg.modules.business.service.IAssessLeaderDepartConfigService;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.*;
import org.jeecg.modules.sys.mapper.annual.*;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.user.UserCommonApi;
import org.jeecg.modules.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 年度考核成绩汇总
 * @Author: jeecg-boot
 * @Date: 2024-09-10
 * @Version: V1.0
 */
@Api(tags = "年度考核成绩汇总")
@RestController
@RequestMapping("/modules/assessAnnualSummary")
@Slf4j
public class AssessAnnualSummaryController extends JeecgController<AssessAnnualSummary, IAssessAnnualSummaryService> {
    @Autowired
    private IAssessAnnualSummaryService summaryService;
    @Autowired
    private IAssessAnnualFillService fillService;
    @Autowired
    private DepartCommonApi departCommonApi;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private AssessAnnualSummaryMapper annualSummaryMapper;
    @Autowired
    private AssessDemocraticEvaluationSummaryMapper evaluationSummaryMapper;
    @Autowired
    private AssessAnnualExcellentNumMapper excellentNumMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private UserCommonApi userCommonApi;
    @Autowired
    private IAssessLeaderDepartConfigService leaderDepartConfigService;


    /**
     * 分页列表查询
     *
     * @param assessAnnualSummary
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "年度考核成绩汇总-分页列表查询")
    @ApiOperation(value = "年度考核成绩汇总-分页列表查询", notes = "年度考核成绩汇总-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<?>> queryPageList(AssessAnnualSummary assessAnnualSummary,
                                          @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                          @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                          @RequestParam(name = "summaryType", required = false) String summaryType,
                                          HttpServletRequest req) {
        Map<String, String[]> parameterMap = new HashMap<>(req.getParameterMap());

        QueryWrapper<AssessAnnualSummary> queryWrapper = QueryGenerator.initQueryWrapper(assessAnnualSummary, parameterMap);
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
        if (parameterMap.get("currentYear") == null) {
            if (currentAssessInfo != null && currentAssessInfo.isAssessing()) {
                queryWrapper.eq("current_year", currentAssessInfo.getCurrentYear());
            }
        }

        if (parameterMap.get("leader") != null) {
            String[] leaders = parameterMap.get("leader");
            if (leaders.length > 0) {
                String leader = leaders[0];
                List<String> departsIds = leaderDepartConfigService.getDepartIdsByLeaderIds(Arrays.asList(leader.split(",")));
                queryWrapper.in("depart", departsIds);
            }
        }

        if (parameterMap.get("departType") != null) {
            String s = parameterMap.get("departType")[0];
            List<String> list = Arrays.asList(s.split(","));
            queryWrapper.in("depart_type", list);
        }


        if ("group".equals(summaryType)) {
            queryWrapper.eq("type", summaryType);
        } else {
            queryWrapper.ne("type", "group");
        }


        queryWrapper.orderByAsc("person_order + 0");

        Page<AssessAnnualSummary> page = new Page<>(pageNo, pageSize);
        IPage<AssessAnnualSummary> pageList = summaryService.page(page, queryWrapper);

        if ("group".equals(summaryType)) {
            List<AnnualGroupSummaryVO> summary = summaryService.getGroupSummaryInfo(pageList.getRecords(), currentAssessInfo.getCurrentYear());
            IPage<AnnualGroupSummaryVO> pageGroup = new Page<>();
            pageGroup.setRecords(summary);
            pageGroup.setTotal(pageList.getTotal());
            pageGroup.setCurrent(pageList.getCurrent());
            pageGroup.setSize(pageList.getSize());
            pageGroup.setPages(pageList.getPages());
            return Result.OK(pageGroup);
        }


        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param assessAnnualSummary
     * @return
     */
    @AutoLog(value = "年度考核成绩汇总-添加")
    @ApiOperation(value = "年度考核成绩汇总-添加", notes = "年度考核成绩汇总-添加")
    //@RequiresPermissions("org.jeecg:assess_annual_summary:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessAnnualSummary assessAnnualSummary) {
        summaryService.save(assessAnnualSummary);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param assessAnnualSummary
     * @return
     */
    @AutoLog(value = "年度考核成绩汇总-编辑")
    @ApiOperation(value = "年度考核成绩汇总-编辑", notes = "年度考核成绩汇总-编辑")
    //@RequiresPermissions("org.jeecg:assess_annual_summary:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessAnnualSummary assessAnnualSummary) {
//        AssessAnnualSummary annualSummary = summaryService.getById(assessAnnualSummary.getId());
//        if (!annualSummary.getLevel().equals(assessAnnualSummary.getLevel())) {
//            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
//            if (!currentAssessInfo.getCurrentYear().equals(annualSummary.getCurrentYear()) || !currentAssessInfo.isAssessing()) {
//                assessAnnualSummary.setRedoFlag(true);
//            }
//        }
        summaryService.updateById(assessAnnualSummary);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "年度考核成绩汇总-通过id删除")
    @ApiOperation(value = "年度考核成绩汇总-通过id删除", notes = "年度考核成绩汇总-通过id删除")
    //@RequiresPermissions("org.jeecg:assess_annual_summary:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        summaryService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "年度考核成绩汇总-批量删除")
    @ApiOperation(value = "年度考核成绩汇总-批量删除", notes = "年度考核成绩汇总-批量删除")
    //@RequiresPermissions("org.jeecg:assess_annual_summary:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.summaryService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "年度考核成绩汇总-通过id查询")
    @ApiOperation(value = "年度考核成绩汇总-通过id查询", notes = "年度考核成绩汇总-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessAnnualSummary> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessAnnualSummary assessAnnualSummary = summaryService.getById(id);
        if (assessAnnualSummary == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessAnnualSummary);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessAnnualSummary
     */
    //@RequiresPermissions("org.jeecg:assess_annual_summary:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessAnnualSummary assessAnnualSummary) {
        return super.exportXls(request, assessAnnualSummary, AssessAnnualSummary.class, "年度考核成绩汇总");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresPermissions("assess_annual_summary:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AssessAnnualSummary.class);
    }

    @GetMapping("/leaderRecommend")
    public Result<?> leaderRecommend(@RequestParam String summaryId) {
        summaryService.leaderRecommend(summaryId);
        return Result.OK(1);
    }


    @GetMapping("/recommendList")
    public Result<?> recommendList(@RequestParam String year) {
        List<AssessAnnualSummary> assessAnnualSummaries = summaryService.recommendList(year);
        IPage<AssessAnnualSummary> page = new Page<>();
        page.setRecords(assessAnnualSummaries);
        return Result.OK(page);
    }

    @PutMapping("/recommend")
    public Result<?> recommend(@RequestBody RecommendVO params) {
        summaryService.recommend(params);
        return Result.OK("推优成功！");
    }

    @PutMapping("/merit")
    public Result<?> submitMerit(@RequestBody JSONObject params) {
        String year = (String) params.get("year");
        String list = params.get("list").toString();
        // 转换为List
        List<String> idList = JSONArray.parseArray(list, String.class);
        summaryService.submitMerit(year, idList);
        return Result.OK("修改成功！");
    }

    @PutMapping("/partyAssess")
    public Result<?> submitPartyAssess(@RequestBody JSONObject params) {
        String year = (String) params.get("year");
        String list = params.get("list").toString();
        // 转换为List
        List<String> idList = JSONArray.parseArray(list, String.class);
        List<String> distanceIds = summaryService.getDistancePartyAssessIdList(year, idList, "party");

        summaryService.submitPartyAssess(year, idList);

        List<AssessAnnualSummary> lists = summaryService.queryByIds(distanceIds, year);
        fillService.getEvaluationSuggestion(lists);

        for (AssessAnnualSummary l : lists) {
            summaryService.updateById(l);
        }

        return Result.OK("修改成功！");
    }

    @PutMapping("/inspection")
    public Result<?> submitInspection(@RequestBody JSONObject params) {
        String year = (String) params.get("year");
        String list = params.get("list").toString();
        // 转换为List
        List<String> idList = JSONArray.parseArray(list, String.class);

        List<String> distanceIds = summaryService.getDistancePartyAssessIdList(year, idList, "dis");

        summaryService.submitInspection(year, idList);

        List<AssessAnnualSummary> lists = summaryService.queryByIds(distanceIds, year);
        fillService.getEvaluationSuggestion(lists);
        for (AssessAnnualSummary l : lists) {
            summaryService.updateById(l);
        }
        return Result.OK("修改成功！");
    }

    @GetMapping("/getMerit")
    public Result<?> getMerit(@RequestParam(name = "depart", required = false, defaultValue = "0") String depart) {
        List<AssessAnnualSummary> merit = summaryService.getMerit(depart);
        IPage<AssessAnnualSummary> page = new Page<>();
        page.setRecords(merit);
        return Result.OK(page);
    }

    @GetMapping("/getPartyOrganization")
    public Result<?> getPartyOrganization() {
        List<AssessAnnualSummary> p = summaryService.getPartyOrganization();
        IPage<AssessAnnualSummary> page = new Page<>();
        page.setRecords(p);
        return Result.OK(page);
    }

    @GetMapping("/getByFillId")
    public Result<?> getByFillId(@RequestParam String fillId) {
        List<AssessAnnualSummary> list = summaryService.queryPersonByFillId(fillId);
        IPage<AssessAnnualSummary> page = new Page<>();
        page.setRecords(list);
        return Result.OK(page);
    }

    // 通过fillId获取领导班子汇总
    @GetMapping("/getGroupSummary")
    public Result<?> getGroupSummary(@RequestParam String fillId) {
        AssessAnnualSummary item = summaryService.getGroupSummary(fillId);
        return Result.OK(item);
    }

    @GetMapping("/getRecommendList")
    public Result<?> getRecommendList(@RequestParam String year) {
        List<AssessAnnualSummary> list = summaryService.recommendList(year);
        IPage<AssessAnnualSummary> page = new Page<>();
        page.setRecords(list);
        return Result.OK(page);
    }

    @GetMapping("/getLeaderRecommendList")
    public Result<?> getLeaderRecommendList() {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
        if (currentAssessInfo == null || !currentAssessInfo.isAssessing()) {
            return Result.error("当前不在考核期内，无法获取推荐名单");
        }

        List<AssessLeaderRecItem> leaderRecommendList = summaryService.getLeaderRecommendList(currentAssessInfo.getCurrentYear());

        Map<String, List<AssessLeaderRecItem>> map = new HashMap<>();

        List<AssessLeaderRecItem> groupList = leaderRecommendList.stream().filter(item -> "group".equals(item.getRecommendType())).collect(Collectors.toList());
        List<AssessLeaderRecItem> bureauList = leaderRecommendList.stream().filter(item -> "bureau".equals(item.getRecommendType())).collect(Collectors.toList());
        List<AssessLeaderRecItem> basicList = leaderRecommendList.stream().filter(item -> "basic".equals(item.getRecommendType())).collect(Collectors.toList());
        List<AssessLeaderRecItem> institutionList = leaderRecommendList.stream().filter(item -> "institution".equals(item.getRecommendType())).collect(Collectors.toList());

        map.put("group", groupList);
        map.put("bureau", bureauList);
        map.put("basic", basicList);
        map.put("institution", institutionList);

        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        LambdaQueryWrapper<AssessAnnualExcellentNum> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualExcellentNum::getCurrentYear, currentAssessInfo.getCurrentYear());
        lqw.eq(AssessAnnualExcellentNum::getLeader, sysUser.getId());
        AssessAnnualExcellentNum eNum = excellentNumMapper.selectOne(lqw);

        if (eNum.getInspectionNum() != null && eNum.getInspectionNum() > 0) {
            String msg = "以上局机关领导干部、分局+参公、事业单位中不包含分管纪检工作领导，共" + eNum.getInspectionNum() + "人";
            return Result.OK(msg, map);
        }

        return Result.OK(map);
    }

    @GetMapping("/getLeaderRecommendListByMainId")
    public Result<?> getLeaderRecommendList(@RequestParam String mainId) {

        List<AssessLeaderRecItem> leaderRecommendList = summaryService.getLeaderRecommendListByMainId(mainId);

        Map<String, List<AssessLeaderRecItem>> map = new HashMap<>();

        List<AssessLeaderRecItem> groupList = leaderRecommendList.stream().filter(item -> "group".equals(item.getRecommendType())).collect(Collectors.toList());
        List<AssessLeaderRecItem> bureauList = leaderRecommendList.stream().filter(item -> "bureau".equals(item.getRecommendType())).collect(Collectors.toList());
        List<AssessLeaderRecItem> basicList = leaderRecommendList.stream().filter(item -> "basic".equals(item.getRecommendType())).collect(Collectors.toList());
        List<AssessLeaderRecItem> institutionList = leaderRecommendList.stream().filter(item -> "institution".equals(item.getRecommendType())).collect(Collectors.toList());

        map.put("group", groupList);
        map.put("bureau", bureauList);
        map.put("basic", basicList);
        map.put("institution", institutionList);

        return Result.OK(map);
    }

    @GetMapping("/getRecommendData4Excel")
    public JSONObject getRecommendData4Excel(@RequestParam(name = "year", required = false) String year,
                                             @RequestParam(name = "leader", required = true) String leader,
                                             @RequestParam(name = "type", required = true) String type) {

        List<AssessLeaderRecItem> leaderRecommendList = summaryService.getLeaderRecommendList(year, leader);

        for (AssessLeaderRecItem leaderRecommendListVO : leaderRecommendList) {
            if (leaderRecommendListVO.getDisciplineRecommend()) {
                leaderRecommendListVO.setInfo("已被纪检组推荐");
            }
            if (!leaderRecommendListVO.getCanExcellent()) {
                leaderRecommendListVO.setInfo("不可以评优");
            }
        }

        List<AssessLeaderRecItem> groupList = leaderRecommendList.stream().filter(item -> "group".equals(item.getRecommendType())).collect(Collectors.toList());
        List<AssessLeaderRecItem> bureauList = leaderRecommendList.stream().filter(item -> "bureau".equals(item.getRecommendType())).collect(Collectors.toList());
        List<AssessLeaderRecItem> basicList = leaderRecommendList.stream().filter(item -> "basic".equals(item.getRecommendType())).collect(Collectors.toList());
        List<AssessLeaderRecItem> institutionList = leaderRecommendList.stream().filter(item -> "institution".equals(item.getRecommendType())).collect(Collectors.toList());

        JSONObject json = new JSONObject();
        switch (type) {
            case "group":
                json.put("data", groupList);
                break;
            case "bureau":
                json.put("data", bureauList);
                break;
            case "basic":
                json.put("data", basicList);
                break;
            case "institution":
                json.put("data", institutionList);
                break;
            default:
                break;
        }

        return json;
    }

    @GetMapping("/getExcellentInfo")
    public JSONObject getExcellentInfo(@RequestParam(name = "leader", required = true) String leader) {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");

        LambdaQueryWrapper<AssessAnnualExcellentNum> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualExcellentNum::getCurrentYear, currentAssessInfo.getCurrentYear());
        lqw.eq(AssessAnnualExcellentNum::getLeader, leader);
        List<AssessAnnualExcellentNum> eNum = excellentNumMapper.selectList(lqw);
        JSONObject json = new JSONObject();
        json.put("data", eNum);
        return json;
    }

    @GetMapping("/annualAssessRes")
    public JSONObject getAnnualAssessRes(@RequestParam(name = "year", required = false, defaultValue = "0") String year,
                                         @RequestParam(name = "type", required = true) String type) {
        if (year.equals("0")) {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
            year = currentAssessInfo.getCurrentYear();
        }

        List<AssessAnnualSummary> assessResByType = summaryService.getAssessResByType(year, type);

        JSONObject json = new JSONObject();
        json.put("data", assessResByType);
        return json;
    }

    @PostMapping("/exportRecommendExcel")
    public void exportExcel(@RequestParam(name = "year", required = false) String year,
                            @RequestParam(name = "leader", required = true) String leader,
                            @RequestParam(name = "type", required = true) String type,
                            @Value("${server.port}") String port,
                            HttpServletRequest request,
                            HttpServletResponse response) {
        String url = "http://localhost:" + port + "/jmreport/exportAllExcelStream" + "?token=" + TokenUtils.getTokenByRequest(request);


        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");

        if (currentAssessInfo == null) {
            year = currentAssessInfo.getCurrentYear();
        }

        List<Map<String, Object>> paramList = new ArrayList<>();
        List<String> entryNames = new ArrayList<>();

        List<SysUser> allLeader = userCommonApi.getAllLeader();
        Map<String, String> leaderMap = allLeader.stream().collect(Collectors.toMap(SysUser::getId, SysUser::getRealname));

        for (SysUser user : allLeader) {
            leader = user.getId();
            leaderMap.put(user.getId(), user.getRealname());
            for (int i = 0; i < 4; i++) {
                JSONObject queryParam = new JSONObject();
                queryParam.put("leader", leader);
                queryParam.put("year", year);
                Map<String, Object> param = new JSONObject();

                if (i == 0) {
                    param.put("excelConfigId", "1026143371854688256");
                    queryParam.put("type", "group");
                    entryNames.add(year + "年" + user.getRealname() + "基层单位领导班子年度考核推荐表.xlsx");
                } else if (i == 1) {
                    param.put("excelConfigId", "1026122611215904768");
                    queryParam.put("type", "bureau");
                    entryNames.add(year + "年" + user.getRealname() + "局机关年度考核推荐表.xlsx");
                } else if (i == 2) {
                    param.put("excelConfigId", "1026152800071266304");
                    queryParam.put("type", "basic");
                    entryNames.add(year + "年" + user.getRealname() + "行政基层单位（含参公）年度考核推荐表.xlsx");
                } else {
                    param.put("excelConfigId", "1026157857919545344");
                    queryParam.put("type", "institution");
                    entryNames.add(year + "年" + user.getRealname() + "基层事业单位年度考核推荐表.xlsx");
                }
                param.put("queryParam", queryParam);
                paramList.add(param);
            }
        }

        assessCommonApi.getExportRecExlZipWithExcel(url, year + "纸质推按表.zip", entryNames, paramList, leaderMap, response);

    }

    @GetMapping("/getRecommendTemp")
    public Result<IPage<RecommendTempVO>> getRecommendTemp() {
        List<RecommendTempVO> recommendTemp = summaryService.getRecommendTemp();
        IPage<RecommendTempVO> page = new Page<>();
        page.setRecords(recommendTemp);
        return Result.OK(page);
    }

    @GetMapping("/getRecommendTempExl")
    public JSONObject getRecommendTempExl() {
        List<AssessAnnualRecommendTemp> reportQueries = summaryService.getRecommendTempExl();

        List<SysDepartModel> sysDepartModels = departCommonApi.queryAllDepart();
        Map<String, String> departMap = sysDepartModels.stream()
                .collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getDepartName));

        LambdaQueryWrapper<SysUser> sysUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        List<SysUser> sysUsers = sysUserMapper.selectList(sysUserLambdaQueryWrapper);
        Map<String, String> sysUserMap = sysUsers.stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getRealname));

        List<RecommendTempDTO> newList = new ArrayList<>();

        // 遍历每条记录
        for (int index = 0; index < reportQueries.size(); index++) {
            AssessAnnualRecommendTemp reportQuery = reportQueries.get(index);
            RecommendTempDTO dto = new RecommendTempDTO();

            // 设置领导姓名
            dto.setName(sysUserMap.get(reportQuery.getLeader()));

            String[] entries = reportQuery.getGroupRec().split(",");
            StringBuilder result = new StringBuilder();

            for (String entry : entries) {
                if (result.length() > 0) {
                    result.append("、");
                }
                result.append(departMap.get(entry));
            }

            String finalResult = result.toString();
            dto.setGrassrootsLeadership(finalResult);

            // 提取其他字段
            String basicRec = extractNames(reportQuery.getBasicRec());
            String bureauRec = extractNames(reportQuery.getBureauRec());
            String institutionRec = extractNames(reportQuery.getInstitutionRec());

            dto.setBureauCadres(bureauRec);
            dto.setPublicBureau(basicRec);
            dto.setInstitution(institutionRec);

            int serialNumber = index + 1; // 序号从1开始
            dto.setSerialNumber(serialNumber);
            newList.add(dto);
        }

        // 将新列表放入 JSON 对象中
        JSONObject json = new JSONObject();
        json.put("data", newList);
        return json;
    }

    @PostMapping("/exportExl")
    public void exportExl(@Value("${server.port}") String port, HttpServletRequest request, HttpServletResponse response) {
        String url = "http://localhost:" + port + "/jmreport/exportAllExcelStream" + "?token=" + TokenUtils.getTokenByRequest(request);
        JSONObject queryParam = new JSONObject();
        Map<String, Object> param = new JSONObject();
        param.put("excelConfigId", "1022275182707777536");
        param.put("queryParam", queryParam);
        String filename = "局领导推荐优秀情况汇总.xlsx";
        assessCommonApi.getExportExcel(url, filename, param, response);
    }

    @PostMapping("/downLoadAnnualRes")
    public void downLoadAnnualRes(@RequestParam String year, @Value("${server.port}") String port, HttpServletRequest request, HttpServletResponse response) {
        String url = "http://localhost:" + port + "/jmreport/exportAllExcelStream" + "?token=" + TokenUtils.getTokenByRequest(request);

        if (year == null || year.equals("0")) {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
            if (currentAssessInfo != null) {
                year = currentAssessInfo.getCurrentYear();
            }
        }

        List<Map<String, Object>> paramList = new ArrayList<>();

        List<String> entryNames = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            JSONObject queryParam = new JSONObject();
            queryParam.put("year", year);
            Map<String, Object> param = new JSONObject();

            if (i == 0) {
                param.put("excelConfigId", "1059711867758690304");
                entryNames.add(year + "年度考核局机关处室考核结果.xlsx");
                queryParam.put("type", "bureau");
            } else if (i == 1) {
                param.put("excelConfigId", "1059711867758690304");
                entryNames.add(year + "年度考核分局、参公考核结果.xlsx");
                queryParam.put("type", "basic");
            } else if (i == 2) {
                param.put("excelConfigId", "1059711867758690304");
                entryNames.add(year + "年度考核事业单位考核结果.xlsx");
                queryParam.put("type", "institution");
            } else {
                param.put("excelConfigId", "1059718812771139584");
                entryNames.add(year + "年度考核领导班子考核结果.xlsx");
                queryParam.put("type", "group");
            }
            param.put("queryParam", queryParam);
            paramList.add(param);
        }

        assessCommonApi.getExportZipWithExcel(url, "年度考核结果.zip", entryNames, paramList, response);
    }


    @PostMapping("/calculateLeadershipStatistics")
    public JSONObject calculateLeadershipStatistics() {
        LeadershipStatisticsDTO statsDto = new LeadershipStatisticsDTO();

        List<SysDepartModel> sysDepartModels = departCommonApi.queryAllDepart();
        Map<String, String> departMap = sysDepartModels.stream()
                .collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getDepartName));

        // 统计局正级领导干部
        LambdaQueryWrapper<AssessAnnualSummary> chiefQuery = new LambdaQueryWrapper<>();
        chiefQuery.eq(AssessAnnualSummary::getRecommendType, "bureau")
                .eq(AssessAnnualSummary::getLeaderRecommend, true)
                .eq(AssessAnnualSummary::getType, "chief");
        List<AssessAnnualSummary> chiefList = annualSummaryMapper.selectList(chiefQuery);
        statsDto.setBureauChiefLevelCount(chiefList.size());
        statsDto.setBureauChiefLevelNames(chiefList.stream()
                .map(summary -> departMap.get(summary.getDepart()) + " " + summary.getPerson())
                .collect(Collectors.joining(",")));
        statsDto.setBureauChiefLevelPosition(chiefList.stream()
                .map(summary -> summary.getDuties() + " " + summary.getPerson())
                .collect(Collectors.joining(",")));

        // 统计副处级人数
        LambdaQueryWrapper<AssessAnnualSummary> deputyQuery = new LambdaQueryWrapper<>();
        deputyQuery.eq(AssessAnnualSummary::getRecommendType, "bureau")
                .ne(AssessAnnualSummary::getType, "chief")
                .eq(AssessAnnualSummary::getLeaderRecommend, true);
        List<AssessAnnualSummary> deputyList = annualSummaryMapper.selectList(deputyQuery);
        statsDto.setDeputyDirectorLevelCount(deputyList.size());
        statsDto.setDeputyDirectorLevelNames(deputyList.stream()
                .map(summary -> departMap.get(summary.getDepart()) + " " + summary.getPerson())
                .collect(Collectors.joining(",")));

        // 统计领导班子人数
        LambdaQueryWrapper<AssessAnnualSummary> leadershipQuery = new LambdaQueryWrapper<>();
        leadershipQuery.eq(AssessAnnualSummary::getRecommendType, "group")
                .eq(AssessAnnualSummary::getLeaderRecommend, true);
        List<AssessAnnualSummary> leadershipList = annualSummaryMapper.selectList(leadershipQuery);
        statsDto.setLeadershipTeamCount(leadershipList.size());
        statsDto.setLeadershipTeamNames(leadershipList.stream()
                .map(AssessAnnualSummary::getDepart)
                .collect(Collectors.joining(",")));

        // 统计行政参公人数
        LambdaQueryWrapper<AssessAnnualSummary> administrativeQuery = new LambdaQueryWrapper<>();
        administrativeQuery.eq(AssessAnnualSummary::getRecommendType, "basic")
                .eq(AssessAnnualSummary::getLeaderRecommend, true);
        List<AssessAnnualSummary> administrativePublicList = annualSummaryMapper.selectList(administrativeQuery);
        statsDto.setAdministrativePublicCount(administrativePublicList.size());

        // 统计行政参公正级人数
        LambdaQueryWrapper<AssessAnnualSummary> administrativeChiefLevelQuery = new LambdaQueryWrapper<>();
        administrativeChiefLevelQuery.eq(AssessAnnualSummary::getRecommendType, "basic")
                .eq(AssessAnnualSummary::getLeaderRecommend, true)
                .eq(AssessAnnualSummary::getType, "chief")
                .eq(AssessAnnualSummary::getInspectionWork, false);
        List<AssessAnnualSummary> administrativeChiefLevelList = annualSummaryMapper.selectList(administrativeChiefLevelQuery);
        statsDto.setAdministrativeChiefLevelCount(administrativeChiefLevelList.size());
        statsDto.setAdministrativeChiefLevelNames(administrativeChiefLevelList.stream()
                .map(summary -> departMap.get(summary.getDepart()) + " " + summary.getPerson())
                .collect(Collectors.joining(",")));

        // 统计行政参公副级人数
        LambdaQueryWrapper<AssessAnnualSummary> administrativeDeputyLevelQuery = new LambdaQueryWrapper<>();
        administrativeDeputyLevelQuery.eq(AssessAnnualSummary::getRecommendType, "basic")
                .eq(AssessAnnualSummary::getLeaderRecommend, true)
                .ne(AssessAnnualSummary::getType, "chief")
                .eq(AssessAnnualSummary::getInspectionWork, false);
        List<AssessAnnualSummary> administrativeDeputyLevelList = annualSummaryMapper.selectList(administrativeDeputyLevelQuery);
        statsDto.setAdministrativeDeputyLevelCount(administrativeDeputyLevelList.size());
        statsDto.setAdministrativeDeputyLevelNames(administrativeDeputyLevelList.stream()
                .map(summary -> departMap.get(summary.getDepart()) + " " + summary.getPerson())
                .collect(Collectors.joining(",")));

        // 统计纪检干部人数
        LambdaQueryWrapper<AssessAnnualSummary> disciplineInspectionQuery = new LambdaQueryWrapper<>();
        disciplineInspectionQuery.eq(AssessAnnualSummary::getRecommendType, "basic")
                .eq(AssessAnnualSummary::getLeaderRecommend, true)
                .ne(AssessAnnualSummary::getType, "chief")
                .eq(AssessAnnualSummary::getInspectionWork, true);
        List<AssessAnnualSummary> disciplineInspectionList = annualSummaryMapper.selectList(disciplineInspectionQuery);
        statsDto.setDisciplineInspectionCadreCount(disciplineInspectionList.size());
        statsDto.setDisciplineInspectionCadreNames(disciplineInspectionList.stream()
                .map(summary -> departMap.get(summary.getDepart()) + " " + summary.getPerson())
                .collect(Collectors.joining(",")));

        // 统计事业单位人数
        LambdaQueryWrapper<AssessAnnualSummary> publicInstitutionQuery = new LambdaQueryWrapper<>();
        publicInstitutionQuery.eq(AssessAnnualSummary::getRecommendType, "institution")
                .eq(AssessAnnualSummary::getLeaderRecommend, true);
        List<AssessAnnualSummary> publicInstitutionList = annualSummaryMapper.selectList(publicInstitutionQuery);
        statsDto.setPublicInstitutionCount(publicInstitutionList.size());
        statsDto.setPublicInstitutionNames(publicInstitutionList.stream()
                .map(summary -> departMap.get(summary.getDepart()) + " " + summary.getPerson())
                .collect(Collectors.joining(",")));

        List<LeadershipStatisticsDTO> newList = new ArrayList<>();
        newList.add(statsDto);

        JSONObject json = new JSONObject();
        json.put("data", newList);
        return json;
    }

    @PostMapping("/calculateLeadershipStatisticsExl")
    public void calculateLeadershipStatisticsExl(@Value("${server.port}") String port, HttpServletRequest request, HttpServletResponse response) {
        String url = "http://localhost:" + port + "/jmreport/exportPdfStream" + "?token=" + TokenUtils.getTokenByRequest(request);
        JSONObject queryParam = new JSONObject();
        Map<String, Object> param = new JSONObject();
        param.put("excelConfigId", "1022312656142245888");
        param.put("queryParam", queryParam);
        String filename = "局领导推荐优秀情况汇总.pdf";
        assessCommonApi.getExportPDF(url, filename, param, response);
    }


    public String extractNames(String input) {
        // 分割字符串
        String[] entries = input.split(",");

        // 使用 StringBuilder 来构建新的字符串
        StringBuilder result = new StringBuilder();

        for (String entry : entries) {
            String name = CommonUtils.getNameByHashId(entry);

            if (name != null) {
                // 添加到结果中
                if (result.length() > 0) {
                    result.append("、"); // 添加逗号分隔
                }
                result.append(name);
            }
        }

        // 生成最终的字符串
        return result.toString();
    }


    @GetMapping("/notExcellent")
    public List<String> notExcellent() {

        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
        if (currentAssessInfo == null || !currentAssessInfo.isAssessing()) {
            return null;
        }

        return summaryService.notExcellent(currentAssessInfo.getCurrentYear());
    }

    @PutMapping("/return")
    public Result<?> returnLeaderRecommend(@RequestParam String id) {
        summaryService.returnLeaderRecommend(id);
        return Result.OK();
    }


    @PostMapping("/DetailExport")
    public void exportExcel(@RequestBody Map<String, String> params, HttpServletResponse response) {
        String year = params.get("year");

        // 查询 BB 表中符合条件的记录
        List<AssessDemocraticEvaluationSummary> bbRecords = evaluationSummaryMapper.selectList(
                new QueryWrapper<AssessDemocraticEvaluationSummary>()
                        .eq("current_year", year)
                        .isNotNull("appraisee")
        );

        // 查询 AA 表中符合条件的记录
        List<AssessAnnualSummary> aaRecords = annualSummaryMapper.selectList(
                new QueryWrapper<AssessAnnualSummary>()
                        .eq("inspection_work", true)
                        .eq("current_year", year)
        );

        // 生成最终记录
        List<FinalRecord> finalRecords = generateFinalRecords(bbRecords, aaRecords);

        // 创建 Excel 文件
        HSSFWorkbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("考核明细");

        // 创建表头
        createHeader(sheet);

        // 填充数据
        fillData(sheet, finalRecords);

        // 设置响应头
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=纪检明细.xls");

        // 写入响应
        try {
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/getInspection")
    public Result<?> getInspection(@RequestParam String year) {
        List<InspectionVO> list = summaryService.getInspection(year);
        IPage<InspectionVO> page = new Page<>();
        page.setRecords(list);
        return Result.OK(page);
    }

    @GetMapping("/getDetailInfo")
    public JSONObject getDetailInfo(@RequestParam(name = "year", required = false) String year,
                                    @RequestParam(name = "depart", required = false) String depart) {
        List<Map<String, String>> detailInfo = summaryService.getDetailInfo(year, depart);

        JSONObject json = new JSONObject();
        json.put("data", detailInfo);
        return json;
    }

    @GetMapping("/getDetailFill")
    public JSONObject getDetailFill(@RequestParam(name = "year", required = false) String year,
                                    @RequestParam(name = "depart", required = false) String depart) {
        List<Map<String, String>> detailInfo = summaryService.getDetailFill(year, depart);

        JSONObject json = new JSONObject();
        json.put("data", detailInfo);
        return json;
    }

    @GetMapping("/getDetailPerson")
    public JSONObject getDetailPerson(@RequestParam(name = "year", required = false) String year,
                                      @RequestParam(name = "depart", required = false) String depart) {
        List<Map<String, String>> detailInfo = summaryService.getDetailPerson(year, depart);

        JSONObject json = new JSONObject();
        json.put("data", detailInfo);
        return json;
    }

    @GetMapping("/getDetailAccount")
    public JSONObject getDetailAccount(@RequestParam(name = "year", required = false) String year,
                                       @RequestParam(name = "depart", required = false) String depart) {
        List<Map<String, String>> detailInfo = summaryService.getDetailAccount(year, depart);

        JSONObject json = new JSONObject();
        json.put("data", detailInfo);
        return json;
    }

    @GetMapping("/getDetailVacation")
    public JSONObject getDetailVacation(@RequestParam(name = "year", required = false) String year,
                                        @RequestParam(name = "depart", required = false) String depart) {
        List<Map<String, String>> detailInfo = summaryService.getDetailVacation(year, depart);

        JSONObject json = new JSONObject();
        json.put("data", detailInfo);
        return json;
    }

    @PostMapping("/downLoadFill")
    public void downLoadFill(@RequestParam String year, @Value("${server.port}") String port, HttpServletRequest request, HttpServletResponse response) {
        String url = "http://localhost:" + port + "/jmreport/exportPdfStream" + "?token=" + TokenUtils.getTokenByRequest(request);

        if (year == null || year.equals("0")) {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
            if (currentAssessInfo != null) {
                year = currentAssessInfo.getCurrentYear();
            }
        }

        Map<String, String> currentUserDepart = departCommonApi.getCurrentUserDepart();
        String departId = currentUserDepart.get("departId");

        JSONObject queryParam = new JSONObject();
        queryParam.put("year", year);
        queryParam.put("depart", departId);
        Map<String, Object> param = new JSONObject();
        param.put("excelConfigId", "1060067316131545088");
        param.put("queryParam", queryParam);

        assessCommonApi.getExportPDF(url, year + "年度考核填报情况.pdf", param, response);
    }

    @PostMapping("/editPerson")
    public Result<?> editPerson(@RequestBody AssessAnnualSummary person) {

        summaryService.editPerson(person);

        return Result.ok("1");
    }

    @PostMapping("/editDepart")
    public Result<?> editDepart(@RequestBody Map<String, String> param) {
        String hashId = param.get("hashId");
        String depart = param.get("depart");

        summaryService.editDepart(hashId, depart);

        return Result.ok("调整成功");
    }

    @GetMapping("/departRes")
    public Result<?> departRes(@RequestParam(name = "year", required = false) String year) {

        List<DepartResVO> list = summaryService.getDepartRes(year);
        return Result.ok(list);
    }

    private List<FinalRecord> generateFinalRecords(List<AssessDemocraticEvaluationSummary> bbRecords, List<AssessAnnualSummary> aaRecords) {
        // 生成最终记录
        Map<String, AssessAnnualSummary> aaMap = aaRecords.stream()
                .collect(Collectors.toMap(AssessAnnualSummary::getHashId, aa -> aa));

        List<FinalRecord> finalRecords = new ArrayList<>();
        for (AssessDemocraticEvaluationSummary bbRecord : bbRecords) {
            String appraisee = bbRecord.getAppraisee();
            String name = appraisee.split("@")[0]; // 获取姓名
            String depart = bbRecord.getDepart();
            BigDecimal score = bbRecord.getScore();

            // 查找与 hashId 匹配的 AA 记录
            AssessAnnualSummary aaRecord = aaMap.get(appraisee);
            if (aaRecord != null) {
                FinalRecord finalRecord = new FinalRecord();
                finalRecord.setDepart(depart);
                finalRecord.setName(name);
                finalRecord.setScore(score);
                finalRecord.setRecommendation(null); // 默认 null
                finalRecord.setRemark(null); // 默认 null

                finalRecords.add(finalRecord);
            }
        }

        // 计算班子内排名和总排名
        calculateRankings(finalRecords);

        return finalRecords;
    }

    private void calculateRankings(List<FinalRecord> finalRecords) {
        // 确保分数为 null 时设置为 0
        for (FinalRecord record : finalRecords) {
            if (record.getScore() == null) {
                record.setScore(BigDecimal.ZERO); // 将分数设置为 0
            }
        }

        // 计算总排名
        finalRecords.sort(Comparator.comparing(FinalRecord::getScore, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        for (int i = 0; i < finalRecords.size(); i++) {
            finalRecords.get(i).setTotalRanking(String.valueOf(i + 1));
        }

        // 计算班子内排名
        Map<String, List<FinalRecord>> departGroups = finalRecords.stream()
                .collect(Collectors.groupingBy(FinalRecord::getDepart));

        for (List<FinalRecord> group : departGroups.values()) {
            group.sort(Comparator.comparing(FinalRecord::getScore, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
            for (int j = 0; j < group.size(); j++) {
                group.get(j).setTeamRanking((j + 1) + "/" + group.size());
            }
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 14);
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setAlignment(HorizontalAlignment.CENTER); // 表头居中
        return headerStyle;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle dataStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        dataStyle.setFont(font);
        dataStyle.setAlignment(HorizontalAlignment.CENTER); // 数据居中
        return dataStyle;
    }

    private void createHeader(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());

        headerRow.createCell(0).setCellValue("序号");
        headerRow.createCell(1).setCellValue("单位");
        headerRow.createCell(2).setCellValue("姓名");
        headerRow.createCell(3).setCellValue("测评分数");
        headerRow.createCell(4).setCellValue("班子内排名");
        headerRow.createCell(5).setCellValue("总排名");
        headerRow.createCell(6).setCellValue("考核推荐等次");
        headerRow.createCell(7).setCellValue("备注");

        // 应用表头样式
        for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }

        // 设置表头行高
        headerRow.setHeightInPoints(40);
    }

    private void fillData(Sheet sheet, List<FinalRecord> finalRecords) {
        int rowNum = 1;
        CellStyle dataStyle = createDataStyle(sheet.getWorkbook());

        for (FinalRecord record : finalRecords) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(rowNum - 1); // 序号
            row.createCell(1).setCellValue(record.getDepart()); // 单位
            row.createCell(2).setCellValue(record.getName()); // 姓名

            // 检查分数是否为 null，若为 null 则设置为 0
            BigDecimal score = record.getScore() != null ? record.getScore() : BigDecimal.ZERO;
            row.createCell(3).setCellValue(score.toString()); // 测评分数

            row.createCell(4).setCellValue(record.getTeamRanking() != null ? record.getTeamRanking() : ""); // 班子内排名
            row.createCell(5).setCellValue(record.getTotalRanking() != null ? record.getTotalRanking() : ""); // 总排名
            row.createCell(6).setCellValue(record.getRecommendation() != null ? record.getRecommendation() : ""); // 考核推荐等次
            row.createCell(7).setCellValue(record.getRemark() != null ? record.getRemark() : ""); // 备注

            // 应用数据样式
            for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }

            // 设置行高
            row.setHeightInPoints(40);
        }

        // 设置列宽
        for (int i = 0; i < 8; i++) { // 假设有 8 列
            sheet.setColumnWidth(i, 15 * 256); // 设置列宽
        }
    }

}
