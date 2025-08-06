package org.jeecg.modules.report.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.jeecg.modules.sys.entity.report.AssessReportArrange;
import org.jeecg.modules.report.service.IAssessReportArrangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 一报告两评议工作安排
 * @Author: jeecg-boot
 * @Date: 2024-09-24
 * @Version: V1.0
 */
@Api(tags = "一报告两评议工作安排")
@RestController
@RequestMapping("/modules/report/assessReportArrange")
@Slf4j
public class AssessReportArrangeController extends JeecgController<AssessReportArrange, IAssessReportArrangeService> {
    @Autowired
    private IAssessReportArrangeService assessReportArrangeService;

    /**
     * 分页列表查询
     *
     * @param assessReportArrange
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "一报告两评议工作安排-分页列表查询")
    @ApiOperation(value = "一报告两评议工作安排-分页列表查询", notes = "一报告两评议工作安排-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessReportArrange>> queryPageList(AssessReportArrange assessReportArrange,
                                                            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                            HttpServletRequest req) {
        QueryWrapper<AssessReportArrange> queryWrapper = QueryGenerator.initQueryWrapper(assessReportArrange, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<AssessReportArrange> page = new Page<AssessReportArrange>(pageNo, pageSize);
        IPage<AssessReportArrange> pageList = assessReportArrangeService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param assessReportArrange
     * @return
     */
    @AutoLog(value = "一报告两评议工作安排-添加")
    @ApiOperation(value = "一报告两评议工作安排-添加", notes = "一报告两评议工作安排-添加")
    //@RequiresPermissions("org.jeecg:assess_report_arrange:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessReportArrange assessReportArrange) {
        assessReportArrangeService.save(assessReportArrange);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param assessReportArrange
     * @return
     */
    @AutoLog(value = "一报告两评议工作安排-编辑")
    @ApiOperation(value = "一报告两评议工作安排-编辑", notes = "一报告两评议工作安排-编辑")
    //@RequiresPermissions("org.jeecg:assess_report_arrange:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessReportArrange assessReportArrange) {
        assessReportArrangeService.updateById(assessReportArrange);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "一报告两评议工作安排-通过id删除")
    @ApiOperation(value = "一报告两评议工作安排-通过id删除", notes = "一报告两评议工作安排-通过id删除")
    //@RequiresPermissions("org.jeecg:assess_report_arrange:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        assessReportArrangeService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "一报告两评议工作安排-批量删除")
    @ApiOperation(value = "一报告两评议工作安排-批量删除", notes = "一报告两评议工作安排-批量删除")
    //@RequiresPermissions("org.jeecg:assess_report_arrange:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.assessReportArrangeService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "一报告两评议工作安排-通过id查询")
    @ApiOperation(value = "一报告两评议工作安排-通过id查询", notes = "一报告两评议工作安排-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessReportArrange> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessReportArrange assessReportArrange = assessReportArrangeService.getById(id);
        if (assessReportArrange == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessReportArrange);
    }

    @ApiOperation(value = "一报告两评议工作安排-通过id查询", notes = "一报告两评议工作安排-通过id查询")
    @GetMapping(value = "/queryByFillId")
    public Result<IPage<AssessReportArrange>> queryByFillId(@RequestParam(name = "fillId", required = true) String fillId) {
        AssessReportArrange assessReportArrange = assessReportArrangeService.getByFillId(fillId);
        if (assessReportArrange == null) {
            return Result.error("未找到对应数据");
        }
        IPage<AssessReportArrange> pageList = new Page<>();
        List<AssessReportArrange> assessReportArrangeList = new ArrayList<>();
        assessReportArrangeList.add(assessReportArrange);
        pageList.setRecords(assessReportArrangeList);
        return Result.OK(pageList);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessReportArrange
     */
    //@RequiresPermissions("org.jeecg:assess_report_arrange:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessReportArrange assessReportArrange) {
        return super.exportXls(request, assessReportArrange, AssessReportArrange.class, "一报告两评议工作安排");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresPermissions("assess_report_arrange:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AssessReportArrange.class);
    }

    @GetMapping("/getScope")
    public Result<String> getScope() {
        // 获取最新的一条
        LambdaQueryWrapper<AssessReportArrange> lqw = new LambdaQueryWrapper<>();
        lqw.orderByDesc(AssessReportArrange::getId).last("limit 1");
        AssessReportArrange assessReportArrange = assessReportArrangeService.getOne(lqw);
        String scope = "";
        if (assessReportArrange == null || assessReportArrange.getScope() == null || assessReportArrange.getScope().isEmpty()) {
            scope = "原则上应与上年度一致，一般为参加领导班子和领导干部年度总结考核会议人员，基层党员干部群众代表原则上不少于10%；在职干部人数少于50人的单位，一般应组织全体干部参加。各单位参评人员实到人数一般不少于应到人数80%。";
        }
        return Result.OK(scope);
    }

}
