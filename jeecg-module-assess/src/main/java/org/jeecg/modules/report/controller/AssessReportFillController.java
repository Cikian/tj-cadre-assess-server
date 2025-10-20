package org.jeecg.modules.report.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.SysDepartModel;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.report.entity.ReportIndexLeader;
import org.jeecg.modules.report.service.IAssessReportArrangeService;
import org.jeecg.modules.report.service.IAssessReportFillService;
import org.jeecg.modules.report.service.IAssessReportNewLeaderService;
import org.jeecg.modules.report.service.IAssessReportObjectInfoService;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.dto.ReportAssessInitDTO;
import org.jeecg.modules.sys.dto.ReportFillDTO;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.report.AssessReportArrange;
import org.jeecg.modules.sys.entity.report.AssessReportFill;
import org.jeecg.modules.sys.entity.report.AssessReportNewLeader;
import org.jeecg.modules.sys.entity.report.AssessReportObjectInfo;
import org.jeecg.modules.user.UserCommonApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 一报告两评议填报
 * @Author: jeecg-boot
 * @Date: 2024-09-24
 * @Version: V1.0
 */
@Api(tags = "一报告两评议填报")
@RestController
@RequestMapping("/modules/report/assessReportFill")
@Slf4j
public class AssessReportFillController extends JeecgController<AssessReportFill, IAssessReportFillService> {
    @Autowired
    private IAssessReportFillService reportFillService;
    @Autowired
    private IAssessReportArrangeService reportArrangeService;
    @Autowired
    private IAssessReportNewLeaderService reportNewLeaderService;
    @Autowired
    private IAssessReportObjectInfoService reportObjectInfoService;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private DepartCommonApi departCommonApi;
    @Autowired
    private UserCommonApi userCommonApi;

    /**
     * 分页列表查询
     *
     * @param assessReportFill
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "一报告两评议填报-分页列表查询")
    @ApiOperation(value = "一报告两评议填报-分页列表查询", notes = "一报告两评议填报-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessReportFill>> queryPageList(AssessReportFill assessReportFill,
                                                         @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                         @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                         HttpServletRequest req) {
        QueryWrapper<AssessReportFill> queryWrapper = QueryGenerator.initQueryWrapper(assessReportFill, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        boolean cadre = assessCommonApi.isCadre();
        if (!cadre) {
            Map<String, String> currentUserDepart = departCommonApi.getCurrentUserDepart();

            queryWrapper.eq("depart", currentUserDepart.get("departId"));
        }
        Map<String, String[]> parameterMap = req.getParameterMap();
        if (parameterMap.get("currentYear") == null){
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("report");
            if (currentAssessInfo != null) {
                queryWrapper.eq("current_year", currentAssessInfo.getCurrentYear());
            }
        }
        Page<AssessReportFill> page = new Page<AssessReportFill>(pageNo, pageSize);
        IPage<AssessReportFill> pageList = reportFillService.page(page, queryWrapper);
        List<AssessReportFill> assessReportFillList=pageList.getRecords();

        List<SysDepartModel> allDepart = departCommonApi.queryAllDepart();
        Map<String, Integer> departMap = allDepart.stream().collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getDepartOrder));
        for (AssessReportFill item : assessReportFillList) {
            item.setSortNo(departMap.get(item.getDepart()));
        }
        List<AssessReportFill> sortedList = assessReportFillList.stream().sorted(Comparator.comparing(AssessReportFill::getSortNo)).collect(Collectors.toList());//升序排序
        pageList.setRecords(sortedList);
        return Result.OK(pageList);
    }

    @AutoLog(value = "一报告两评议-发起")
    @ApiOperation(value = "一报告两评议-发起", notes = "一报告两评议-发起")
    @PostMapping(value = "/init")
    public Result<?> initAssess(@RequestBody ReportAssessInitDTO initDTO) {
        boolean b = reportFillService.initAssess(initDTO);

        return Result.OK("添加成功！");
    }

    /**
     * 添加
     *
     * @param assessReportFill
     * @return
     */
    @AutoLog(value = "一报告两评议填报-添加")
    @ApiOperation(value = "一报告两评议填报-添加", notes = "一报告两评议填报-添加")
    //@RequiresPermissions("org.jeecg:assess_report_fill:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessReportFill assessReportFill) {
        reportFillService.save(assessReportFill);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param assessReportFill
     * @return
     */
    @AutoLog(value = "一报告两评议填报-编辑")
    @ApiOperation(value = "一报告两评议填报-编辑", notes = "一报告两评议填报-编辑")
    //@RequiresPermissions("org.jeecg:assess_report_fill:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessReportFill assessReportFill) {
        Map<String, String> currentUserDepart = departCommonApi.getCurrentUserDepart();
        assessReportFill.setStatus("3");
        assessReportFill.setReportBy((currentUserDepart.get("departName")+"填报专员"));
        reportFillService.updateById(assessReportFill);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "一报告两评议填报-通过id删除")
    @ApiOperation(value = "一报告两评议填报-通过id删除", notes = "一报告两评议填报-通过id删除")
    //@RequiresPermissions("org.jeecg:assess_report_fill:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        reportFillService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "一报告两评议填报-批量删除")
    @ApiOperation(value = "一报告两评议填报-批量删除", notes = "一报告两评议填报-批量删除")
    //@RequiresPermissions("org.jeecg:assess_report_fill:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.reportFillService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "一报告两评议填报-通过id查询")
    @ApiOperation(value = "一报告两评议填报-通过id查询", notes = "一报告两评议填报-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessReportFill> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessReportFill assessReportFill = reportFillService.getById(id);
        if (assessReportFill == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessReportFill);
    }

    /**
     * 查询一报告两评议首页信息
     *
     */
    @ApiOperation(value = "查询一报告两评议首页信息", notes = "查询一报告两评议首页信息")
    @GetMapping(value = "getIndexInfo")
    public Result<ReportIndexLeader> getAllItems() {
        return Result.OK(reportFillService.getAllItems());
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessReportFill
     */
    //@RequiresPermissions("org.jeecg:assess_report_fill:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessReportFill assessReportFill) {
        return super.exportXls(request, assessReportFill, AssessReportFill.class, "一报告两评议填报");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresPermissions("assess_report_fill:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AssessReportFill.class);
    }

    @RequestMapping("/submitFill")
    public Result<?> submitFill(@RequestBody ReportFillDTO fillDTO, @RequestParam(value = "pass", defaultValue = "false") String pass) {
        boolean passFlag = Boolean.parseBoolean(pass);
        reportFillService.submitFill(fillDTO, true, passFlag);
        return Result.OK("提交成功！");
    }

    @RequestMapping("saveFill")
    public Result<?> saveFill(@RequestBody ReportFillDTO fillDTO,HttpServletRequest req) throws IOException{
        reportFillService.submitFill(fillDTO, false, false);
        return Result.OK("保存成功！");
    }

    @PutMapping(value = "/passAudit")
    public Result<String> passTheAudit(@RequestParam String assessId, @RequestParam String name) {
        AssessReportFill fill = reportFillService.getById(assessId);
        if (fill == null) {
            return Result.error("未找到对应数据");
        }

        // 审核人录入
        fill.setAuditBy(name);

        ReportFillDTO fillDTO = new ReportFillDTO();
        fillDTO.setReportFill(fill);

        AssessReportArrange arrange = reportArrangeService.getByFillId(assessId);
        fillDTO.setReportArrange(arrange);

        List<AssessReportNewLeader> newLeaders = reportNewLeaderService.getByFillId(assessId);
        fillDTO.setReportNewLeader(newLeaders);

        List<AssessReportObjectInfo> objectInfos = reportObjectInfoService.getByFillId(assessId);
        fillDTO.setReportObjectInfo(objectInfos);

        reportFillService.submitFill(fillDTO, true, true);

        return Result.OK("审核通过！");

    }

}
