package org.jeecg.modules.annual.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.annual.service.IAssessAnnualNegativeListService;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.AssessAnnualNegativeList;
import org.jeecg.modules.user.UserCommonApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 年度考核负面清单
 * @Author: jeecg-boot
 * @Date:   2024-11-03
 * @Version: V1.0
 */
@Api(tags="年度考核负面清单")
@RestController
@RequestMapping("/modules/annual/assessAnnualNegativeList")
@Slf4j
public class AssessAnnualNegativeListController extends JeecgController<AssessAnnualNegativeList, IAssessAnnualNegativeListService> {
	@Autowired
	private IAssessAnnualNegativeListService listService;
	@Autowired
	private UserCommonApi userCommonApi;
	@Autowired
	private AssessCommonApi assessCommonApi;


	/**
	 * 分页列表查询
	 *
	 * @param assessAnnualNegativeList
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "年度考核负面清单-分页列表查询")
	@ApiOperation(value="年度考核负面清单-分页列表查询", notes="年度考核负面清单-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<AssessAnnualNegativeList>> queryPageList(AssessAnnualNegativeList assessAnnualNegativeList,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<AssessAnnualNegativeList> queryWrapper = QueryGenerator.initQueryWrapper(assessAnnualNegativeList, req.getParameterMap());
		queryWrapper.orderByDesc("create_time");

		Map<String, String[]> parameterMap = new HashMap<>(req.getParameterMap());
		AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
		if (parameterMap.get("currentYear") == null) {
			if (currentAssessInfo != null) {
				queryWrapper.eq("current_year", currentAssessInfo.getCurrentYear());
			}
		}

		queryWrapper.ne("status", 1);

		Page<AssessAnnualNegativeList> page = new Page<>(pageNo, pageSize);
		IPage<AssessAnnualNegativeList> pageList = listService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	/**
	 *   添加
	 *
	 * @return
	 */
	@AutoLog(value = "年度考核负面清单-添加")
	@ApiOperation(value="年度考核负面清单-添加", notes="年度考核负面清单-添加")
	//@RequiresPermissions("org.jeecg:assess_annual_negative_list:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody AssessAnnualNegativeList entry) {

		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

		List<String> roles = userCommonApi.getUserRolesByUserName(sysUser.getUsername());
		if (roles.contains("department_cadre_admin")) entry.setStatus("3");
		else entry.setStatus("1");

		listService.save(entry);
		listService.updateAnnualSummarySuggestion(entry);
		return Result.OK("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param assessAnnualNegativeList
	 * @return
	 */
	@AutoLog(value = "年度考核负面清单-编辑")
	@ApiOperation(value="年度考核负面清单-编辑", notes="年度考核负面清单-编辑")
	//@RequiresPermissions("org.jeecg:assess_annual_negative_list:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody AssessAnnualNegativeList assessAnnualNegativeList) {
		listService.updateById(assessAnnualNegativeList);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "年度考核负面清单-通过id删除")
	@ApiOperation(value="年度考核负面清单-通过id删除", notes="年度考核负面清单-通过id删除")
	//@RequiresPermissions("org.jeecg:assess_annual_negative_list:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		listService.removeById(id);
		return Result.OK("删除成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "年度考核负面清单-批量删除")
	@ApiOperation(value="年度考核负面清单-批量删除", notes="年度考核负面清单-批量删除")
	//@RequiresPermissions("org.jeecg:assess_annual_negative_list:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.listService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "年度考核负面清单-通过id查询")
	@ApiOperation(value="年度考核负面清单-通过id查询", notes="年度考核负面清单-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<AssessAnnualNegativeList> queryById(@RequestParam(name="id",required=true) String id) {
		AssessAnnualNegativeList assessAnnualNegativeList = listService.getById(id);
		if(assessAnnualNegativeList==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(assessAnnualNegativeList);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param assessAnnualNegativeList
    */
    //@RequiresPermissions("org.jeecg:assess_annual_negative_list:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessAnnualNegativeList assessAnnualNegativeList) {
        return super.exportXls(request, assessAnnualNegativeList, AssessAnnualNegativeList.class, "年度考核负面清单");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    //@RequiresPermissions("assess_annual_negative_list:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AssessAnnualNegativeList.class);
    }

	@PutMapping("/passOrReject")
	public Result<String> pass(@RequestBody AssessAnnualNegativeList assessAnnualNegativeList) {
		listService.passOrReject(assessAnnualNegativeList.getId(), assessAnnualNegativeList.getStatus());
		listService.updateAnnualSummarySuggestion(assessAnnualNegativeList);
		return Result.OK("操作成功！");
	}

	@GetMapping("/getByDep")
	public Result<IPage<AssessAnnualNegativeList>> getCurrentByDep() {
		List<AssessAnnualNegativeList> byCurrentDep = listService.getByCurrentDep();
		IPage<AssessAnnualNegativeList> page = new Page<>();
		page.setRecords(byCurrentDep);
		return Result.OK(page);
	}

	@GetMapping("/passOrDelete")
	public Result<?> passOrDelete(@RequestParam(name="id",required=true) String id, @RequestParam(name="var",required=true) Integer var) {
		boolean b = listService.passOrDelete(id, var);
		if(!b) {
			return Result.error("操作失败！");
		}
		return Result.OK("操作成功！");
	}

}
