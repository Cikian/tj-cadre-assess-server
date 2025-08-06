package org.jeecg.modules.annual.controller;

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
import org.jeecg.modules.business.service.IAssessLeaderConfigService;
import org.jeecg.modules.sys.entity.annual.AssessLeaderConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @Description: 考核领导配置
 * @Author: jeecg-boot
 * @Date: 2024-12-09
 * @Version: V1.0
 */
@Api(tags = "考核领导配置")
@RestController
@RequestMapping("/assess/annual/assessLeaderConfig")
@Slf4j
public class AssessLeaderConfigController extends JeecgController<AssessLeaderConfig, IAssessLeaderConfigService> {
    @Autowired
    private IAssessLeaderConfigService leaderConfigService;

    /**
     * 分页列表查询
     *
     * @param assessLeaderConfig
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "考核领导配置-分页列表查询")
    @ApiOperation(value = "考核领导配置-分页列表查询", notes = "考核领导配置-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessLeaderConfig>> queryPageList(AssessLeaderConfig assessLeaderConfig,
                                                           @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                           @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                           HttpServletRequest req) {
        QueryWrapper<AssessLeaderConfig> queryWrapper = QueryGenerator.initQueryWrapper(assessLeaderConfig, req.getParameterMap());
        Page<AssessLeaderConfig> page = new Page<AssessLeaderConfig>(pageNo, pageSize);
        IPage<AssessLeaderConfig> pageList = leaderConfigService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param assessLeaderConfig
     * @return
     */
    @AutoLog(value = "考核领导配置-添加")
    @ApiOperation(value = "考核领导配置-添加", notes = "考核领导配置-添加")
    //@RequiresPermissions("org.jeecg:assess_leader_config:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessLeaderConfig assessLeaderConfig) {
        leaderConfigService.save(assessLeaderConfig);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param assessLeaderConfig
     * @return
     */
    @AutoLog(value = "考核领导配置-编辑")
    @ApiOperation(value = "考核领导配置-编辑", notes = "考核领导配置-编辑")
    //@RequiresPermissions("org.jeecg:assess_leader_config:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessLeaderConfig assessLeaderConfig) {
        leaderConfigService.updateById(assessLeaderConfig);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "考核领导配置-通过id删除")
    @ApiOperation(value = "考核领导配置-通过id删除", notes = "考核领导配置-通过id删除")
    //@RequiresPermissions("org.jeecg:assess_leader_config:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        leaderConfigService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "考核领导配置-批量删除")
    @ApiOperation(value = "考核领导配置-批量删除", notes = "考核领导配置-批量删除")
    //@RequiresPermissions("org.jeecg:assess_leader_config:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.leaderConfigService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "考核领导配置-通过id查询")
    @ApiOperation(value = "考核领导配置-通过id查询", notes = "考核领导配置-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessLeaderConfig> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessLeaderConfig assessLeaderConfig = leaderConfigService.getById(id);
        if (assessLeaderConfig == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessLeaderConfig);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessLeaderConfig
     */
    //@RequiresPermissions("org.jeecg:assess_leader_config:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessLeaderConfig assessLeaderConfig) {
        return super.exportXls(request, assessLeaderConfig, AssessLeaderConfig.class, "考核领导配置");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresPermissions("assess_leader_config:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AssessLeaderConfig.class);
    }

}
