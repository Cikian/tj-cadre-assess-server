package org.jeecg.modules.regular.controller;

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
import org.jeecg.modules.regular.entity.AssessRegularReportItem;
import org.jeecg.modules.regular.service.IAssessRegularResultService;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;

/**
 * @Description: 平时考核结果
 * @Author: admin
 * @Date:   2024-10-09
 * @Version: V1.0
 */
@Api(tags="平时考核结果")
@RestController
@RequestMapping("/regular/result")
@Slf4j
public class AssessRegularResultController extends JeecgController<AssessRegularReportItem, IAssessRegularResultService> {
	@Autowired
	private IAssessRegularResultService assessRegularResultService;
	@Autowired
    private AssessCommonApi assessCommonApi;

	/**
	 * 分页列表查询
	 *
	 * @param assessRegularResult
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "平时考核结果-分页列表查询")
	@ApiOperation(value="平时考核结果-分页列表查询", notes="平时考核结果-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<AssessRegularReportItem>> queryPageList(AssessRegularReportItem assessRegularResult,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   @RequestParam(name="adminDepartment") String adminDepartment,
								   HttpServletRequest req) {
		QueryWrapper<AssessRegularReportItem> queryWrapper = QueryGenerator.initQueryWrapper(assessRegularResult, req.getParameterMap());
		// 根据当前用户的部门添加条件
		if ("人事处".equals(adminDepartment)) {
			queryWrapper.eq("admin_department", "人事处");
		} else if ("干部处".equals(adminDepartment)) {
			queryWrapper.eq("admin_department", "干部处");
		}
		Map<String, String[]> parameterMap = req.getParameterMap();
		if (parameterMap.get("currentYear") == null){
			AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("regular");
			if (currentAssessInfo != null) {
				queryWrapper.eq("current_year", currentAssessInfo.getCurrentYear());
			}
		}
		Page<AssessRegularReportItem> page = new Page<>(pageNo, pageSize);
		IPage<AssessRegularReportItem> pageList = assessRegularResultService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	/**
	 *   添加
	 *
	 * @param assessRegularResult
	 * @return
	 */
	@AutoLog(value = "平时考核结果-添加")
	@ApiOperation(value="平时考核结果-添加", notes="平时考核结果-添加")
	//@RequiresPermissions("org.jeecg.modules:assess_regular_result:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody AssessRegularReportItem assessRegularResult) {
		assessRegularResultService.save(assessRegularResult);
		return Result.OK("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param assessRegularResult
	 * @return
	 */
	@AutoLog(value = "平时考核结果-编辑")
	@ApiOperation(value="平时考核结果-编辑", notes="平时考核结果-编辑")
	//@RequiresPermissions("org.jeecg.modules:assess_regular_result:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody AssessRegularReportItem assessRegularResult) {
		assessRegularResultService.updateById(assessRegularResult);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "平时考核结果-通过id删除")
	@ApiOperation(value="平时考核结果-通过id删除", notes="平时考核结果-通过id删除")
	//@RequiresPermissions("org.jeecg.modules:assess_regular_result:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		assessRegularResultService.removeById(id);
		return Result.OK("删除成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "平时考核结果-批量删除")
	@ApiOperation(value="平时考核结果-批量删除", notes="平时考核结果-批量删除")
	//@RequiresPermissions("org.jeecg.modules:assess_regular_result:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.assessRegularResultService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "平时考核结果-通过id查询")
	@ApiOperation(value="平时考核结果-通过id查询", notes="平时考核结果-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<AssessRegularReportItem> queryById(@RequestParam(name="id",required=true) String id) {
		AssessRegularReportItem assessRegularResult = assessRegularResultService.getById(id);
		if(assessRegularResult==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(assessRegularResult);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param assessRegularResult
    */
    //@RequiresPermissions("org.jeecg.modules:assess_regular_result:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessRegularReportItem assessRegularResult) {
        return super.exportXls(request, assessRegularResult, AssessRegularReportItem.class, "平时考核结果");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    //@RequiresPermissions("assess_regular_result:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AssessRegularReportItem.class);
    }

	@GetMapping("/detail")
	public Result<?> detailList(){
		Map<String, Object> detail = assessRegularResultService.getCurrentDetail();

		return Result.OK(detail);
	}

	@GetMapping("/leaderPro")
    public Result<?> leaderPro(){
		AssessCurrentAssess regular = assessCommonApi.getCurrentAssessInfo("regular");
		if (regular == null || !regular.isAssessing()) {
			return Result.error("当前没有正在进行的考核");
		}

		Map<String, Boolean> leaderPro = assessRegularResultService.getLeaderPro(regular.getCurrentYear(), regular.getQuarter());

		return Result.OK(leaderPro);
	}

}
