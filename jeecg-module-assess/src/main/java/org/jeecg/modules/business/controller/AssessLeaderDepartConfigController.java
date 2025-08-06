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
import org.jeecg.modules.sys.entity.business.AssessLeaderDepartConfig;
import org.jeecg.modules.business.service.IAssessLeaderDepartConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @Description: 领导分管处室（单位）配置
 * @Author: jeecg-boot
 * @Date: 2024-08-26
 * @Version: V1.0
 */
@Api(tags = "领导分管处室（单位）配置")
@RestController
@RequestMapping("/modules/assessLeaderDepartConfig")
@Slf4j
public class AssessLeaderDepartConfigController extends JeecgController<AssessLeaderDepartConfig, IAssessLeaderDepartConfigService> {
    @Autowired
    private IAssessLeaderDepartConfigService assessLeaderDepartConfigService;

    /**
     * 分页列表查询
     *
     * @param assessLeaderDepartConfig
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "领导分管处室（单位）配置-分页列表查询")
    @ApiOperation(value = "领导分管处室（单位）配置-分页列表查询", notes = "领导分管处室（单位）配置-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessLeaderDepartConfig>> queryPageList(AssessLeaderDepartConfig assessLeaderDepartConfig,
                                                                 @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                 HttpServletRequest req) {
        QueryWrapper<AssessLeaderDepartConfig> queryWrapper = QueryGenerator.initQueryWrapper(assessLeaderDepartConfig, req.getParameterMap());
        Page<AssessLeaderDepartConfig> page = new Page<AssessLeaderDepartConfig>(pageNo, pageSize);
        IPage<AssessLeaderDepartConfig> pageList = assessLeaderDepartConfigService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param assessLeaderDepartConfig
     * @return
     */
    @AutoLog(value = "领导分管处室（单位）配置-添加")
    @ApiOperation(value = "领导分管处室（单位）配置-添加", notes = "领导分管处室（单位）配置-添加")
    //@RequiresPermissions("org.jeecg:assess_leader_depart_config:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessLeaderDepartConfig assessLeaderDepartConfig) {
        return assessLeaderDepartConfigService.saveDistinct(assessLeaderDepartConfig);
    }

    /**
     * 编辑
     *
     * @param assessLeaderDepartConfig
     * @return
     */
    @AutoLog(value = "领导分管处室（单位）配置-编辑")
    @ApiOperation(value = "领导分管处室（单位）配置-编辑", notes = "领导分管处室（单位）配置-编辑")
    //@RequiresPermissions("org.jeecg:assess_leader_depart_config:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessLeaderDepartConfig assessLeaderDepartConfig) {
        assessLeaderDepartConfigService.updateById(assessLeaderDepartConfig);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "领导分管处室（单位）配置-通过id删除")
    @ApiOperation(value = "领导分管处室（单位）配置-通过id删除", notes = "领导分管处室（单位）配置-通过id删除")
    //@RequiresPermissions("org.jeecg:assess_leader_depart_config:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        assessLeaderDepartConfigService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "领导分管处室（单位）配置-批量删除")
    @ApiOperation(value = "领导分管处室（单位）配置-批量删除", notes = "领导分管处室（单位）配置-批量删除")
    //@RequiresPermissions("org.jeecg:assess_leader_depart_config:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.assessLeaderDepartConfigService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "领导分管处室（单位）配置-通过id查询")
    @ApiOperation(value = "领导分管处室（单位）配置-通过id查询", notes = "领导分管处室（单位）配置-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessLeaderDepartConfig> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessLeaderDepartConfig assessLeaderDepartConfig = assessLeaderDepartConfigService.getById(id);
        if (assessLeaderDepartConfig == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessLeaderDepartConfig);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessLeaderDepartConfig
     */
    //@RequiresPermissions("org.jeecg:assess_leader_depart_config:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessLeaderDepartConfig assessLeaderDepartConfig) {
        return super.exportXls(request, assessLeaderDepartConfig, AssessLeaderDepartConfig.class, "领导分管处室（单位）配置");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresPermissions("assess_leader_depart_config:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AssessLeaderDepartConfig.class);
    }

}
