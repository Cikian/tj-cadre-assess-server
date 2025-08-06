package org.jeecg.modules.business.controller;

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
import org.jeecg.modules.business.service.IAssessBusinessCommendService;
import org.jeecg.modules.business.service.IAssessBusinessDenounceService;
import org.jeecg.modules.sys.entity.business.AssessBusiness;
import org.jeecg.modules.business.service.IAssessBusinessService;
import org.jeecg.modules.sys.entity.business.AssessBusinessCommend;
import org.jeecg.modules.sys.entity.business.AssessBusinessDenounce;
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
 * @Description: 业务考核发起
 * @Author: jeecg-boot
 * @Date: 2024-08-22
 * @Version: V1.0
 */
@Api(tags = "业务考核发起")
@RestController
@RequestMapping("/modules/assessBusiness")
@Slf4j
public class AssessBusinessController extends JeecgController<AssessBusiness, IAssessBusinessService> {
    @Autowired
    private IAssessBusinessService assessBusinessService;
    @Autowired
    private IAssessBusinessCommendService commendService;
    @Autowired
    private IAssessBusinessDenounceService denounceService;

    /**
     * 分页列表查询
     *
     * @param assessBusiness
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "业务考核发起-分页列表查询")
    @ApiOperation(value = "业务考核发起-分页列表查询", notes = "业务考核发起-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessBusiness>> queryPageList(AssessBusiness assessBusiness,
                                                       @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                       HttpServletRequest req) {
        QueryWrapper<AssessBusiness> queryWrapper = QueryGenerator.initQueryWrapper(assessBusiness, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<AssessBusiness> page = new Page<AssessBusiness>(pageNo, pageSize);
        IPage<AssessBusiness> pageList = assessBusinessService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param assessBusiness
     * @return
     */
    @AutoLog(value = "业务考核发起-添加")
    @ApiOperation(value = "业务考核发起-添加", notes = "业务考核发起-添加")
    //@RequiresPermissions("org.jeecg:assess_business:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessBusiness assessBusiness) {
        assessBusinessService.save(assessBusiness);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param assessBusiness
     * @return
     */
    @AutoLog(value = "业务考核发起-编辑")
    @ApiOperation(value = "业务考核发起-编辑", notes = "业务考核发起-编辑")
    //@RequiresPermissions("org.jeecg:assess_business:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessBusiness assessBusiness) {
        assessBusinessService.updateById(assessBusiness);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "业务考核发起-通过id删除")
    @ApiOperation(value = "业务考核发起-通过id删除", notes = "业务考核发起-通过id删除")
    //@RequiresPermissions("org.jeecg:assess_business:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        assessBusinessService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "业务考核发起-批量删除")
    @ApiOperation(value = "业务考核发起-批量删除", notes = "业务考核发起-批量删除")
    //@RequiresPermissions("org.jeecg:assess_business:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.assessBusinessService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "业务考核发起-通过id查询")
    @ApiOperation(value = "业务考核发起-通过id查询", notes = "业务考核发起-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessBusiness> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessBusiness assessBusiness = assessBusinessService.getById(id);
        if (assessBusiness == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessBusiness);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessBusiness
     */
    //@RequiresPermissions("org.jeecg:assess_business:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessBusiness assessBusiness) {
        return super.exportXls(request, assessBusiness, AssessBusiness.class, "业务考核发起");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresPermissions("assess_business:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AssessBusiness.class);
    }

    @GetMapping("/current/cd")
    public Result<?> getCurrentCommendAndDenounce(@RequestParam String year, @RequestParam String depart) {
        List<AssessBusinessCommend> currentCommend = commendService.getCurrentCommend(year, depart);
        List<AssessBusinessDenounce> currentDenounce = denounceService.getCurrentDenounce(year, depart);
        Map<String, Object> map = new HashMap<>();
        map.put("commend", currentCommend);
        map.put("denounce", currentDenounce);
        return Result.OK(map);
    }

}
