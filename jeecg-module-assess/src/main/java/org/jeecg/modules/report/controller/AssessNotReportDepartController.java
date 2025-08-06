package org.jeecg.modules.report.controller;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.sys.entity.report.AssessNotReportDepart;
import org.jeecg.modules.report.service.IAssessNotReportDepartService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

 /**
 * @Description: 不参加一报告两评议单位配置表
 * @Author: jeecg-boot
 * @Date:   2025-01-13
 * @Version: V1.0
 */
@Api(tags="不参加一报告两评议单位配置表")
@RestController
@RequestMapping("/assess/notReportDepart")
@Slf4j
public class AssessNotReportDepartController extends JeecgController<AssessNotReportDepart, IAssessNotReportDepartService> {
	@Autowired
	private IAssessNotReportDepartService assessNotReportDepartService;
	
	/**
	 * 分页列表查询
	 *
	 * @param assessNotReportDepart
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "不参加一报告两评议单位配置表-分页列表查询")
	@ApiOperation(value="不参加一报告两评议单位配置表-分页列表查询", notes="不参加一报告两评议单位配置表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<AssessNotReportDepart>> queryPageList(AssessNotReportDepart assessNotReportDepart,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<AssessNotReportDepart> queryWrapper = QueryGenerator.initQueryWrapper(assessNotReportDepart, req.getParameterMap());
		Page<AssessNotReportDepart> page = new Page<AssessNotReportDepart>(pageNo, pageSize);
		IPage<AssessNotReportDepart> pageList = assessNotReportDepartService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param assessNotReportDepart
	 * @return
	 */
	@AutoLog(value = "不参加一报告两评议单位配置表-添加")
	@ApiOperation(value="不参加一报告两评议单位配置表-添加", notes="不参加一报告两评议单位配置表-添加")
	//@RequiresPermissions("org.jeecg.modules:assess_not_report_depart:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody AssessNotReportDepart assessNotReportDepart) {
		assessNotReportDepartService.save(assessNotReportDepart);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param assessNotReportDepart
	 * @return
	 */
	@AutoLog(value = "不参加一报告两评议单位配置表-编辑")
	@ApiOperation(value="不参加一报告两评议单位配置表-编辑", notes="不参加一报告两评议单位配置表-编辑")
	//@RequiresPermissions("org.jeecg.modules:assess_not_report_depart:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody AssessNotReportDepart assessNotReportDepart) {
		assessNotReportDepartService.updateById(assessNotReportDepart);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "不参加一报告两评议单位配置表-通过id删除")
	@ApiOperation(value="不参加一报告两评议单位配置表-通过id删除", notes="不参加一报告两评议单位配置表-通过id删除")
	//@RequiresPermissions("org.jeecg.modules:assess_not_report_depart:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		assessNotReportDepartService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "不参加一报告两评议单位配置表-批量删除")
	@ApiOperation(value="不参加一报告两评议单位配置表-批量删除", notes="不参加一报告两评议单位配置表-批量删除")
	//@RequiresPermissions("org.jeecg.modules:assess_not_report_depart:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.assessNotReportDepartService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "不参加一报告两评议单位配置表-通过id查询")
	@ApiOperation(value="不参加一报告两评议单位配置表-通过id查询", notes="不参加一报告两评议单位配置表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<AssessNotReportDepart> queryById(@RequestParam(name="id",required=true) String id) {
		AssessNotReportDepart assessNotReportDepart = assessNotReportDepartService.getById(id);
		if(assessNotReportDepart==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(assessNotReportDepart);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param assessNotReportDepart
    */
    //@RequiresPermissions("org.jeecg.modules:assess_not_report_depart:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessNotReportDepart assessNotReportDepart) {
        return super.exportXls(request, assessNotReportDepart, AssessNotReportDepart.class, "不参加一报告两评议单位配置表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    //@RequiresPermissions("assess_not_report_depart:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AssessNotReportDepart.class);
    }

}
