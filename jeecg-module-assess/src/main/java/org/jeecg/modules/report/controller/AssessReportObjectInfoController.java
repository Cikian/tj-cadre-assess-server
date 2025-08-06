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
import org.jeecg.modules.sys.entity.report.AssessReportObjectInfo;
import org.jeecg.modules.report.service.IAssessReportObjectInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 评议对象有关信息汇总表
 * @Author: jeecg-boot
 * @Date: 2024-09-24
 * @Version: V1.0
 */
@Api(tags = "评议对象有关信息汇总表")
@RestController
@RequestMapping("/modules/report/assessReportObjectInfo")
@Slf4j
public class AssessReportObjectInfoController extends JeecgController<AssessReportObjectInfo, IAssessReportObjectInfoService> {
    @Autowired
    private IAssessReportObjectInfoService assessReportObjectInfoService;

    /**
     * 分页列表查询
     *
     * @param assessReportObjectInfo
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "评议对象有关信息汇总表-分页列表查询")
    @ApiOperation(value = "评议对象有关信息汇总表-分页列表查询", notes = "评议对象有关信息汇总表-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessReportObjectInfo>> queryPageList(AssessReportObjectInfo assessReportObjectInfo,
                                                               @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                               @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                               HttpServletRequest req) {
        QueryWrapper<AssessReportObjectInfo> queryWrapper = QueryGenerator.initQueryWrapper(assessReportObjectInfo, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<AssessReportObjectInfo> page = new Page<AssessReportObjectInfo>(pageNo, pageSize);
        IPage<AssessReportObjectInfo> pageList = assessReportObjectInfoService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param assessReportObjectInfo
     * @return
     */
    @AutoLog(value = "评议对象有关信息汇总表-添加")
    @ApiOperation(value = "评议对象有关信息汇总表-添加", notes = "评议对象有关信息汇总表-添加")
    //@RequiresPermissions("org.jeecg:assess_report_object_info:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessReportObjectInfo assessReportObjectInfo) {
        assessReportObjectInfoService.save(assessReportObjectInfo);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param assessReportObjectInfo
     * @return
     */
    @AutoLog(value = "评议对象有关信息汇总表-编辑")
    @ApiOperation(value = "评议对象有关信息汇总表-编辑", notes = "评议对象有关信息汇总表-编辑")
    //@RequiresPermissions("org.jeecg:assess_report_object_info:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessReportObjectInfo assessReportObjectInfo) {
        assessReportObjectInfoService.updateById(assessReportObjectInfo);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "评议对象有关信息汇总表-通过id删除")
    @ApiOperation(value = "评议对象有关信息汇总表-通过id删除", notes = "评议对象有关信息汇总表-通过id删除")
    //@RequiresPermissions("org.jeecg:assess_report_object_info:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        assessReportObjectInfoService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "评议对象有关信息汇总表-批量删除")
    @ApiOperation(value = "评议对象有关信息汇总表-批量删除", notes = "评议对象有关信息汇总表-批量删除")
    //@RequiresPermissions("org.jeecg:assess_report_object_info:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.assessReportObjectInfoService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "评议对象有关信息汇总表-通过id查询")
    @ApiOperation(value = "评议对象有关信息汇总表-通过id查询", notes = "评议对象有关信息汇总表-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessReportObjectInfo> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessReportObjectInfo assessReportObjectInfo = assessReportObjectInfoService.getById(id);
        if (assessReportObjectInfo == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessReportObjectInfo);
    }

    @ApiOperation(value = "评议对象有关信息汇总表-通过fillId查询", notes = "评议对象有关信息汇总表-通过fillId查询")
    @GetMapping(value = "/getAssessObj")
    public Result<IPage<AssessReportObjectInfo>> getAssessObj(@RequestParam(name = "fillId", required = true) String fillId) {
        List<AssessReportObjectInfo> assessReportObjectInfo = assessReportObjectInfoService.getByFillId(fillId);
        if (assessReportObjectInfo == null) {
            return Result.error("未找到对应数据");
        }
        IPage<AssessReportObjectInfo> page = new Page<>();
        page.setRecords(assessReportObjectInfo);
        return Result.OK(page);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessReportObjectInfo
     */
    //@RequiresPermissions("org.jeecg:assess_report_object_info:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessReportObjectInfo assessReportObjectInfo) {
        return super.exportXls(request, assessReportObjectInfo, AssessReportObjectInfo.class, "评议对象有关信息汇总表");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresPermissions("assess_report_object_info:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AssessReportObjectInfo.class);
    }

}
