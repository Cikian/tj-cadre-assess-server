package org.jeecg.modules.annual.controller;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.sys.entity.annual.AssessAccountabilityPeriodConfig;
import org.jeecg.modules.annual.service.IAssessAccountabilityPeriodConfigService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.sys.AssessCommonApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

/**
 * @Description: 问责影响期配置
 * @Author: jeecg-boot
 * @Date: 2024-09-18
 * @Version: V1.0
 */
@Api(tags = "问责影响期配置")
@RestController
@RequestMapping("/annual/period/config")
@Slf4j
public class AssessAccountabilityPeriodConfigController extends JeecgController<AssessAccountabilityPeriodConfig, IAssessAccountabilityPeriodConfigService> {
    @Autowired
    private IAssessAccountabilityPeriodConfigService periodConfigService;
    @Autowired
    private AssessCommonApi assessCommonApi;

    /**
     * 分页列表查询
     *
     * @param assessAccountabilityPeriodConfig
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "问责影响期配置-分页列表查询")
    @ApiOperation(value = "问责影响期配置-分页列表查询", notes = "问责影响期配置-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessAccountabilityPeriodConfig>> queryPageList(AssessAccountabilityPeriodConfig assessAccountabilityPeriodConfig,
                                                                         @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                         @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                         HttpServletRequest req) {
        if (pageNo == 1) {
            periodConfigService.checkAll();
        }
        QueryWrapper<AssessAccountabilityPeriodConfig> queryWrapper = QueryGenerator.initQueryWrapper(assessAccountabilityPeriodConfig, req.getParameterMap());
        queryWrapper.orderByAsc("type");
        Page<AssessAccountabilityPeriodConfig> page = new Page<>(pageNo, pageSize);
        IPage<AssessAccountabilityPeriodConfig> pageList = periodConfigService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param assessAccountabilityPeriodConfig
     * @return
     */
    @AutoLog(value = "问责影响期配置-添加")
    @ApiOperation(value = "问责影响期配置-添加", notes = "问责影响期配置-添加")
    //@RequiresPermissions("org.jeecg:assess_accountability_period_config:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessAccountabilityPeriodConfig assessAccountabilityPeriodConfig) {
        periodConfigService.save(assessAccountabilityPeriodConfig);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param assessAccountabilityPeriodConfig
     * @return
     */
    @AutoLog(value = "问责影响期配置-编辑")
    @ApiOperation(value = "问责影响期配置-编辑", notes = "问责影响期配置-编辑")
    //@RequiresPermissions("org.jeecg:assess_accountability_period_config:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessAccountabilityPeriodConfig assessAccountabilityPeriodConfig) {
        periodConfigService.updateById(assessAccountabilityPeriodConfig);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "问责影响期配置-通过id删除")
    @ApiOperation(value = "问责影响期配置-通过id删除", notes = "问责影响期配置-通过id删除")
    //@RequiresPermissions("org.jeecg:assess_accountability_period_config:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        periodConfigService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "问责影响期配置-批量删除")
    @ApiOperation(value = "问责影响期配置-批量删除", notes = "问责影响期配置-批量删除")
    //@RequiresPermissions("org.jeecg:assess_accountability_period_config:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.periodConfigService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "问责影响期配置-通过id查询")
    @ApiOperation(value = "问责影响期配置-通过id查询", notes = "问责影响期配置-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessAccountabilityPeriodConfig> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessAccountabilityPeriodConfig assessAccountabilityPeriodConfig = periodConfigService.getById(id);
        if (assessAccountabilityPeriodConfig == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessAccountabilityPeriodConfig);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessAccountabilityPeriodConfig
     */
    //@RequiresPermissions("org.jeecg:assess_accountability_period_config:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessAccountabilityPeriodConfig assessAccountabilityPeriodConfig) {
        return super.exportXls(request, assessAccountabilityPeriodConfig, AssessAccountabilityPeriodConfig.class, "问责影响期配置");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresPermissions("assess_accountability_period_config:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AssessAccountabilityPeriodConfig.class);
    }

    @Deprecated
    @GetMapping("/getEndDate")
    public Result<String> getAccountabilityEndDate(@RequestParam String type, @RequestParam String startDate) {
        String endDateById = periodConfigService.getEndDateById(type, startDate);
        return Result.OK(endDateById);
    }

}
