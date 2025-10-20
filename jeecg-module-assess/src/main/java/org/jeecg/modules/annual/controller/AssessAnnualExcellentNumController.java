package org.jeecg.modules.annual.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.SysDepartModel;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.AssessAnnualExcellentNum;
import org.jeecg.modules.annual.service.IAssessAnnualExcellentNumService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.sys.entity.annual.AssessAssistConfig;
import org.jeecg.modules.sys.mapper.annual.AssessAssistConfigMapper;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.user.UserCommonApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

/**
 * @Description: 年度考核分管领导优秀名额
 * @Author: jeecg-boot
 * @Date: 2024-09-18
 * @Version: V1.0
 */
@Api(tags = "年度考核分管领导优秀名额")
@RestController
@RequestMapping("/modules/annual/assessAnnualExcellentNum")
@Slf4j
public class AssessAnnualExcellentNumController extends JeecgController<AssessAnnualExcellentNum, IAssessAnnualExcellentNumService> {
    @Autowired
    private IAssessAnnualExcellentNumService excellentNumService;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private DepartCommonApi departCommonApi;
    @Autowired
    private AssessAssistConfigMapper assistConfigMapper;
    @Autowired
    private UserCommonApi userCommonApi;

    /**
     * 分页列表查询
     *
     * @param assessAnnualExcellentNum
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "年度考核分管领导优秀名额-分页列表查询")
    @ApiOperation(value = "年度考核分管领导优秀名额-分页列表查询", notes = "年度考核分管领导优秀名额-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessAnnualExcellentNum>> queryPageList(AssessAnnualExcellentNum assessAnnualExcellentNum,
                                                                 @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                 HttpServletRequest req) {
        QueryWrapper<AssessAnnualExcellentNum> queryWrapper = QueryGenerator.initQueryWrapper(assessAnnualExcellentNum, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<AssessAnnualExcellentNum> page = new Page<AssessAnnualExcellentNum>(pageNo, pageSize);
        IPage<AssessAnnualExcellentNum> pageList = excellentNumService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param assessAnnualExcellentNum
     * @return
     */
    @AutoLog(value = "年度考核分管领导优秀名额-添加")
    @ApiOperation(value = "年度考核分管领导优秀名额-添加", notes = "年度考核分管领导优秀名额-添加")
    //@RequiresPermissions("org.jeecg:assess_annual_excellent_num:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessAnnualExcellentNum assessAnnualExcellentNum) {
        excellentNumService.save(assessAnnualExcellentNum);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param assessAnnualExcellentNum
     * @return
     */
    @AutoLog(value = "年度考核分管领导优秀名额-编辑")
    @ApiOperation(value = "年度考核分管领导优秀名额-编辑", notes = "年度考核分管领导优秀名额-编辑")
    //@RequiresPermissions("org.jeecg:assess_annual_excellent_num:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessAnnualExcellentNum assessAnnualExcellentNum) {
        excellentNumService.updateById(assessAnnualExcellentNum);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "年度考核分管领导优秀名额-通过id删除")
    @ApiOperation(value = "年度考核分管领导优秀名额-通过id删除", notes = "年度考核分管领导优秀名额-通过id删除")
    //@RequiresPermissions("org.jeecg:assess_annual_excellent_num:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        excellentNumService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "年度考核分管领导优秀名额-批量删除")
    @ApiOperation(value = "年度考核分管领导优秀名额-批量删除", notes = "年度考核分管领导优秀名额-批量删除")
    //@RequiresPermissions("org.jeecg:assess_annual_excellent_num:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.excellentNumService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "年度考核分管领导优秀名额-通过id查询")
    @ApiOperation(value = "年度考核分管领导优秀名额-通过id查询", notes = "年度考核分管领导优秀名额-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessAnnualExcellentNum> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessAnnualExcellentNum assessAnnualExcellentNum = excellentNumService.getById(id);
        if (assessAnnualExcellentNum == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessAnnualExcellentNum);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessAnnualExcellentNum
     */
    //@RequiresPermissions("org.jeecg:assess_annual_excellent_num:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessAnnualExcellentNum assessAnnualExcellentNum) {
        return super.exportXls(request, assessAnnualExcellentNum, AssessAnnualExcellentNum.class, "年度考核分管领导优秀名额");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresPermissions("assess_annual_excellent_num:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AssessAnnualExcellentNum.class);
    }

    @GetMapping("/getExcellentNum")
    public Result<IPage<AssessAnnualExcellentNum>> getEnableItemByMainId(@RequestParam(name = "year", required = true) String year) {
        boolean b = excellentNumService.checkNo(year);
        List<AssessAnnualExcellentNum> excellentNumList = excellentNumService.getByCurrentYear(year);

        int num = 0;
        for (AssessAnnualExcellentNum excellentNum : excellentNumList) {
            num += excellentNum.getInspectionNum();
        }

        String msg1 = "以上局机关领导干部、分局+参公、事业单位中不包含分管纪检工作领导，共" + num + "人；";

        List<AssessAssistConfig> assessAssistConfigs = assistConfigMapper.selectList(null);
        List<SysUser> allLeader = userCommonApi.getCurrentLeader();
        // 以id为k，name为v，allLeader转为map
        Map<String, String> leaderMap = allLeader.stream().collect(Collectors.toMap(SysUser::getId, SysUser::getRealname));


        StringBuilder sb = new StringBuilder();
        sb.append("以上人员中，");
        for (AssessAssistConfig config : assessAssistConfigs) {
            String people = config.getPeople();
            sb.append(people);
            sb.append("辅助");
            String leader = config.getLeader();
            List<String> peopleList = Arrays.asList(leader.split(","));
            for (int i = 0; i < peopleList.size(); i++) {
                String s1 = peopleList.get(i);
                s1 = leaderMap.get(s1);
                sb.append(s1);
                if (i == peopleList.size() - 1) sb.append("；");
                else sb.append("、");
            }
        }

        sb.append("共重复计算").append(assessAssistConfigs.size()).append("人次。");

        String msg2 = sb.toString();

        String msg = msg1 + msg2;

        IPage<AssessAnnualExcellentNum> page = new Page<>();
        page.setRecords(excellentNumList);
        return Result.OK(msg, page);
    }

    @GetMapping("/getExcellentNum4Excel")
    public JSONObject getEnableItemByMainId4Excel(@RequestParam(name = "year", required = true) String year) {
        List<AssessAnnualExcellentNum> excellentNumList = excellentNumService.getByCurrentYear(year);

        List<SysDepartModel> departs = departCommonApi.queryAllDepart();
        // 转为map，id作为key，alias作为value
        Map<String, String> departMap = departs.stream().collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getAlias));

        for (AssessAnnualExcellentNum excellentNum : excellentNumList) {
            List<String> list = Arrays.asList(excellentNum.getDepart().split(","));
            StringBuilder sb = new StringBuilder();
            for (String departId : list) {
                sb.append(departMap.get(departId)).append(",");
            }
            excellentNum.setDepart(sb.toString());

            excellentNum.setGroupRatio(excellentNum.getGroupRatio().multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP));
            excellentNum.setBureauRatio(excellentNum.getBureauRatio().multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP));
            excellentNum.setBasicRatio(excellentNum.getBasicRatio().multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP));
            excellentNum.setInstitutionRatio(excellentNum.getInstitutionRatio().multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP));
        }

        JSONObject json = new JSONObject();
        json.put("data", excellentNumList);
        return json;
    }

    @PostMapping("/exportExcel")
    public void exportExcel(@RequestParam String year, @Value("${server.port}") String port, HttpServletRequest request, HttpServletResponse response) {
        String url = "http://localhost:" + port + "/jmreport/exportAllExcelStream" + "?token=" + TokenUtils.getTokenByRequest(request);

        JSONObject queryParam = new JSONObject();
        queryParam.put("year", year);

        Map<String, Object> param = new JSONObject();
        param.put("excelConfigId", "1019618676355444736");
        param.put("queryParam", queryParam);

        String filename = year + "年度局领导年度考核优秀名额分配.xlsx";

        assessCommonApi.getExportExcel(url, filename, param, response);

    }

    @PutMapping("/updateExcellentNum")
    public Result<String> updateExcellentNum(@RequestBody List<AssessAnnualExcellentNum> excellentNumList) {
        excellentNumService.updateBatchById(excellentNumList);
        return Result.OK("更新成功!");
    }

    @GetMapping("/getRecommendData")
    public Result<AssessAnnualExcellentNum> getRecommendData(@RequestParam(name = "year", required = true) String year) {
        AssessAnnualExcellentNum byLeaderId = excellentNumService.getByLeaderId(year);
        return Result.OK(byLeaderId);
    }


}
