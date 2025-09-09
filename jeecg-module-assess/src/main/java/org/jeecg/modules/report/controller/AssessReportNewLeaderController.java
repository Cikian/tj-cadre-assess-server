package org.jeecg.modules.report.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import freemarker.template.utility.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.modules.report.service.IAssessReportFillService;
import org.jeecg.modules.report.service.IAssessReportNewLeaderService;
import org.jeecg.modules.report.vo.AssessReportDemocracyNewLeaderVO;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.dto.ReportNewLeaderDTO;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.report.AssessReportFill;
import org.jeecg.modules.sys.entity.report.AssessReportNewLeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: 新提拔干部名册
 * @Author: jeecg-boot
 * @Date: 2024-09-24
 * @Version: V1.0
 */
@Api(tags = "新提拔干部名册")
@RestController
@RequestMapping("/modules/report/assessReportNewLeader")
@Slf4j
public class AssessReportNewLeaderController extends JeecgController<AssessReportNewLeader, IAssessReportNewLeaderService> {
    @Autowired
    private IAssessReportNewLeaderService newLeaderService;

    @Autowired
    private IAssessReportFillService assessReportFillService;

    @Autowired
    private ISysBaseAPI sysBaseAPI;

    @Autowired
    private AssessCommonApi assessCommonApi;

    /**
     * 分页列表查询
     *
     * @param assessReportNewLeader
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "新提拔干部名册-分页列表查询")
    @ApiOperation(value = "新提拔干部名册-分页列表查询", notes = "新提拔干部名册-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessReportNewLeader>> queryPageList(AssessReportNewLeader assessReportNewLeader,
                                                              @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                              @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                              HttpServletRequest req) {
        QueryWrapper<AssessReportNewLeader> queryWrapper = QueryGenerator.initQueryWrapper(assessReportNewLeader, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<AssessReportNewLeader> page = new Page<AssessReportNewLeader>(pageNo, pageSize);
        IPage<AssessReportNewLeader> pageList = newLeaderService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param assessReportNewLeader
     * @return
     */
    @AutoLog(value = "新提拔干部名册-添加")
    @ApiOperation(value = "新提拔干部名册-添加", notes = "新提拔干部名册-添加")
    //@RequiresPermissions("org.jeecg:assess_report_new_leader:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessReportNewLeader assessReportNewLeader) {
        newLeaderService.save(assessReportNewLeader);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param assessReportNewLeader
     * @return
     */
    @AutoLog(value = "新提拔干部名册-编辑")
    @ApiOperation(value = "新提拔干部名册-编辑", notes = "新提拔干部名册-编辑")
    //@RequiresPermissions("org.jeecg:assess_report_new_leader:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessReportNewLeader assessReportNewLeader) {
        newLeaderService.updateById(assessReportNewLeader);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "新提拔干部名册-通过id删除")
    @ApiOperation(value = "新提拔干部名册-通过id删除", notes = "新提拔干部名册-通过id删除")
    //@RequiresPermissions("org.jeecg:assess_report_new_leader:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        newLeaderService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "新提拔干部名册-批量删除")
    @ApiOperation(value = "新提拔干部名册-批量删除", notes = "新提拔干部名册-批量删除")
    //@RequiresPermissions("org.jeecg:assess_report_new_leader:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.newLeaderService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "新提拔干部名册-通过id查询")
    @ApiOperation(value = "新提拔干部名册-通过id查询", notes = "新提拔干部名册-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessReportNewLeader> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessReportNewLeader assessReportNewLeader = newLeaderService.getById(id);
        if (assessReportNewLeader == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessReportNewLeader);
    }

    @ApiOperation(value = "新提拔干部名册-通过fillId查询", notes = "新提拔干部名册-通过fillId查询")
    @GetMapping(value = "/getNewLeaders")
    public Result<IPage<AssessReportNewLeader>> getNewLeaders(@RequestParam(name = "fillId", required = true) String fillId) {
        List<AssessReportNewLeader> assessReportNewLeader = newLeaderService.getByFillId(fillId);
        if (assessReportNewLeader == null) {
            return Result.error("未找到对应数据");
        }
        IPage<AssessReportNewLeader> page = new Page<>();
        page.setRecords(assessReportNewLeader);
        return Result.OK(page);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessReportNewLeader
     */
    //@RequiresPermissions("org.jeecg:assess_report_new_leader:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessReportNewLeader assessReportNewLeader) {
        return super.exportXls(request, assessReportNewLeader, AssessReportNewLeader.class, "新提拔干部名册");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresPermissions("assess_report_new_leader:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AssessReportNewLeader.class);
    }

    @GetMapping("/getDemocracyItemByFillId")
    public Result<?> getDemocracyItemByFillId(@RequestParam(name = "fillId", required = true) String fillId) {
        List<AssessReportDemocracyNewLeaderVO> democracyItemByFillId = newLeaderService.getDemocracyItemByFillId(fillId);
        return Result.OK(democracyItemByFillId);
    }


    /**
     * 新提拔干部信息 Excel
     *
     * @param port 端口号
     * @return
     */
    @PostMapping("/exportExcel")
    public void exportExcel(@RequestParam(name = "currentYear",defaultValue = "0") String currentYear, @Value("${server.port}") String port, HttpServletRequest request, HttpServletResponse response) {
        // 构建导出 Excel 的 URL，包括服务端口和 token 参数
        String url = "http://localhost:" + port + "/jmreport/exportAllExcelStream" + "?token=" + TokenUtils.getTokenByRequest(request);

        JSONObject queryParam = new JSONObject(); // 创建一个空的 JSON 对象用于查询参数
        queryParam.put("currentYear",currentYear);
        Map<String, Object> param = new JSONObject(); // 创建一个 Map 用于存放请求参数
        param.put("excelConfigId", "1013695717132115968"); // 添加 Excel 配置 ID 参数
        param.put("queryParam", queryParam); // 将查询参数放入请求参数中
        String filename = "新提拔干部信息.xlsx"; // 指定导出的 Excel 文件名

        // 调用方法导出 Excel
        assessCommonApi.getExportExcel(url, filename, param, response);
    }

    /**
     * 导出新提拔干部信息 Excel
     *
     * @param currentYear 导出年份
     * @return
     */
    @GetMapping("/getNewLeaderbyYear")
    public JSONObject getNewLeaderbyYear(@RequestParam(name = "currentYear",defaultValue = "0") String currentYear, HttpServletRequest request, HttpServletResponse response) {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("report");
        if (currentAssessInfo == null) return null;
        if ("0".equals(currentYear)) {
            currentYear = currentAssessInfo.getCurrentYear();
        }
        LambdaQueryWrapper<AssessReportNewLeader> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AssessReportNewLeader::getCurrentYear,currentYear);
        List<AssessReportNewLeader> list = newLeaderService.list(lambdaQueryWrapper);
        for (AssessReportNewLeader assessReportNewLeader: list) {
            assessReportNewLeader.setOriginalPosition(assessReportNewLeader.getOriginalPosition()+" "+assessReportNewLeader.getOriginalDate());
            assessReportNewLeader.setCurrentPosition(assessReportNewLeader.getCurrentPosition()+" "+assessReportNewLeader.getCurDate());

            DateFormat df = new SimpleDateFormat("yyyy.MM");
            String dateStr = df.format(assessReportNewLeader.getBirthday());
            assessReportNewLeader.setStrBirthday(dateStr);

            AssessReportFill assessReportFill= assessReportFillService.getById(assessReportNewLeader.getFillId());
            assessReportNewLeader.setDepart(assessReportFill.getDepart());
        }
        JSONObject json = new JSONObject();
        json.put("data", list);
        return json;
    }


}
