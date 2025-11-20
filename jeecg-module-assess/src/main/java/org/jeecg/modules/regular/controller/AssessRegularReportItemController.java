package org.jeecg.modules.regular.controller;

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
import org.jeecg.modules.regular.entity.AssessRegularReport;
import org.jeecg.modules.regular.entity.AssessRegularReportItem;
import org.jeecg.modules.regular.service.IAssessRegularReportItemService;
import org.jeecg.modules.regular.service.IAssessRegularReportService;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 平时考核填报明细表
 * @Author: jeecg-boot
 * @Date: 2025-11-02
 * @Version: V1.0
 */
@Api(tags = "平时考核填报明细表")
@RestController
@RequestMapping("/regular/item")
@Slf4j
public class AssessRegularReportItemController extends JeecgController<AssessRegularReportItem, IAssessRegularReportItemService> {
    @Autowired
    private IAssessRegularReportItemService regularReportItemService;
    @Autowired
    private IAssessRegularReportService assessRegularReportService;
    @Autowired
    private AssessCommonApi assessCommonApi;

    /**
     * 分页列表查询
     *
     * @param assessRegularReportItem
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "平时考核填报明细表-分页列表查询")
    @ApiOperation(value = "平时考核填报明细表-分页列表查询", notes = "平时考核填报明细表-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessRegularReportItem>> queryPageList(AssessRegularReportItem assessRegularReportItem,
                                                                @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                HttpServletRequest req) {
        QueryWrapper<AssessRegularReportItem> queryWrapper = QueryGenerator.initQueryWrapper(assessRegularReportItem, req.getParameterMap());
        queryWrapper.isNotNull("main_id");
        AssessCurrentAssess assess = assessCommonApi.getCurrentAssessInfo("regular");
        if (assess != null) {
            queryWrapper.eq("current_year", assess.getCurrentYear());
        }
        Page<AssessRegularReportItem> page = new Page<AssessRegularReportItem>(pageNo, pageSize);
        IPage<AssessRegularReportItem> pageList = regularReportItemService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param assessRegularReportItem
     * @return
     */
    @AutoLog(value = "平时考核填报明细表-添加")
    @ApiOperation(value = "平时考核填报明细表-添加", notes = "平时考核填报明细表-添加")
    //@RequiresPermissions("org.jeecg.modules:assess_regular_report_item:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessRegularReportItem assessRegularReportItem) {
        regularReportItemService.save(assessRegularReportItem);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param assessRegularReportItem
     * @return
     */
    @AutoLog(value = "平时考核填报明细表-编辑")
    @ApiOperation(value = "平时考核填报明细表-编辑", notes = "平时考核填报明细表-编辑")
    //@RequiresPermissions("org.jeecg.modules:assess_regular_report_item:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessRegularReportItem assessRegularReportItem) {
        AssessRegularReportItem byId = regularReportItemService.getById(assessRegularReportItem.getId());
        if (byId == null) {
            return Result.error("未找到对应数据");
        }
        if (!byId.getDepartmentCode().equals(assessRegularReportItem.getDepartmentCode())) {
            this.changeDepart(assessRegularReportItem.getId(), assessRegularReportItem.getDepartmentCode());
        }
        regularReportItemService.updateById(assessRegularReportItem);
        return Result.OK("编辑成功!");
    }

    private void changeDepart(String id, String departId) {
        LambdaQueryWrapper<AssessRegularReportItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssessRegularReportItem::getId, id);
        List<AssessRegularReportItem> regularReportItemList = regularReportItemService.list(queryWrapper);

        AssessRegularReportItem assessRegularReportItem = regularReportItemList.get(0);
        String mainId = assessRegularReportItem.getMainId();
        AssessRegularReport assessRegularReport = assessRegularReportService.getById(mainId);
        String currentYear = assessRegularReport.getCurrentYear();
        String quarter = assessRegularReport.getCurrentQuarter();

        LambdaQueryWrapper<AssessRegularReport> regularWrapper = new LambdaQueryWrapper<>();
        regularWrapper.eq(AssessRegularReport::getCurrentYear, currentYear).eq(AssessRegularReport::getCurrentQuarter, quarter).eq(AssessRegularReport::getDepartmentCode, departId);
        List<AssessRegularReport> assessRegularReportList = assessRegularReportService.list(regularWrapper);
        if (!assessRegularReportList.isEmpty()) {
            AssessRegularReport destAssessRegularReport = assessRegularReportList.get(0);
            String destMainId = destAssessRegularReport.getId();
            assessRegularReportItem.setMainId(destMainId);
            assessRegularReportItem.setDepartmentCode(departId);
            regularReportItemService.updateById(assessRegularReportItem);
        }
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "平时考核填报明细表-通过id删除")
    @ApiOperation(value = "平时考核填报明细表-通过id删除", notes = "平时考核填报明细表-通过id删除")
    //@RequiresPermissions("org.jeecg.modules:assess_regular_report_item:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        regularReportItemService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "平时考核填报明细表-批量删除")
    @ApiOperation(value = "平时考核填报明细表-批量删除", notes = "平时考核填报明细表-批量删除")
    //@RequiresPermissions("org.jeecg.modules:assess_regular_report_item:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.regularReportItemService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "平时考核填报明细表-通过id查询")
    @ApiOperation(value = "平时考核填报明细表-通过id查询", notes = "平时考核填报明细表-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessRegularReportItem> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessRegularReportItem assessRegularReportItem = regularReportItemService.getById(id);
        if (assessRegularReportItem == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessRegularReportItem);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessRegularReportItem
     */
    //@RequiresPermissions("org.jeecg.modules:assess_regular_report_item:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessRegularReportItem assessRegularReportItem) {
        return super.exportXls(request, assessRegularReportItem, AssessRegularReportItem.class, "平时考核填报明细表");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresPermissions("assess_regular_report_item:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AssessRegularReportItem.class);
    }

    @DeleteMapping("/remove")
    public Result<?> remove(@RequestParam(name = "id", required = true) String id) {
        regularReportItemService.dontAssess(id);
        return Result.OK("修改成功!");
    }

    @PutMapping("/goon")
    public Result<?> goon(@RequestParam(name = "id", required = true) String id) {
        regularReportItemService.goon(id);
        return Result.OK("修改成功!");
    }

}
