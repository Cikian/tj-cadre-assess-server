package org.jeecg.modules.business.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.business.service.IAssessBusinessGradeService;
import org.jeecg.modules.sys.entity.business.AssessBusinessLevelRatio;
import org.jeecg.modules.business.service.IAssessBusinessLevelRatioService;

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
 * @Description: 业务考核等次默认比例
 * @Author: jeecg-boot
 * @Date: 2024-10-29
 * @Version: V1.0
 */
@Api(tags = "业务考核等次默认比例")
@RestController
@RequestMapping("/modules/business/assessBusinessLevelRatio")
@Slf4j
public class AssessBusinessLevelRatioController extends JeecgController<AssessBusinessLevelRatio, IAssessBusinessLevelRatioService> {
    @Autowired
    private IAssessBusinessLevelRatioService ratioService;
    @Autowired
    private IAssessBusinessGradeService gradeService;

    /**
     * 分页列表查询
     *
     * @param assessBusinessLevelRatio
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "业务考核等次默认比例-分页列表查询")
    @ApiOperation(value = "业务考核等次默认比例-分页列表查询", notes = "业务考核等次默认比例-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessBusinessLevelRatio>> queryPageList(AssessBusinessLevelRatio assessBusinessLevelRatio,
                                                                 @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                 HttpServletRequest req) {
        QueryWrapper<AssessBusinessLevelRatio> queryWrapper = QueryGenerator.initQueryWrapper(assessBusinessLevelRatio, req.getParameterMap());
        queryWrapper.clear();
        Page<AssessBusinessLevelRatio> page = new Page<AssessBusinessLevelRatio>(pageNo, pageSize);
        IPage<AssessBusinessLevelRatio> pageList = ratioService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param assessBusinessLevelRatio
     * @return
     */
    @AutoLog(value = "业务考核等次默认比例-添加")
    @ApiOperation(value = "业务考核等次默认比例-添加", notes = "业务考核等次默认比例-添加")
    //@RequiresPermissions("org.jeecg:assess_business_level_ratio:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessBusinessLevelRatio ratio) {
        // 计算总和
        BigDecimal added = ratio.getExcellent().add(ratio.getFine()).add(ratio.getCommon());
        // 如果和不为100，抛出异常
        if (added.compareTo(new BigDecimal(100)) != 0) {
            throw new JeecgBootException("当前比例之和不为为100%");
        }
        ratioService.save(ratio);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param assessBusinessLevelRatio
     * @return
     */
    @AutoLog(value = "业务考核等次默认比例-编辑")
    @ApiOperation(value = "业务考核等次默认比例-编辑", notes = "业务考核等次默认比例-编辑")
    //@RequiresPermissions("org.jeecg:assess_business_level_ratio:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessBusinessLevelRatio ratio) {
        // 计算总和
        BigDecimal added = ratio.getExcellent().add(ratio.getFine()).add(ratio.getCommon());
        // 如果和不为100，抛出异常
        if (added.compareTo(new BigDecimal(100)) != 0) {
            throw new JeecgBootException("当前比例之和不为为100%");
        }
        ratioService.updateById(ratio);
        gradeService.updateBusinessRanking();
        gradeService.updateBusinessLevel();
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "业务考核等次默认比例-通过id删除")
    @ApiOperation(value = "业务考核等次默认比例-通过id删除", notes = "业务考核等次默认比例-通过id删除")
    //@RequiresPermissions("org.jeecg:assess_business_level_ratio:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        ratioService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "业务考核等次默认比例-批量删除")
    @ApiOperation(value = "业务考核等次默认比例-批量删除", notes = "业务考核等次默认比例-批量删除")
    //@RequiresPermissions("org.jeecg:assess_business_level_ratio:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.ratioService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "业务考核等次默认比例-通过id查询")
    @ApiOperation(value = "业务考核等次默认比例-通过id查询", notes = "业务考核等次默认比例-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessBusinessLevelRatio> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessBusinessLevelRatio assessBusinessLevelRatio = ratioService.getById(id);
        if (assessBusinessLevelRatio == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessBusinessLevelRatio);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessBusinessLevelRatio
     */
    //@RequiresPermissions("org.jeecg:assess_business_level_ratio:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessBusinessLevelRatio assessBusinessLevelRatio) {
        return super.exportXls(request, assessBusinessLevelRatio, AssessBusinessLevelRatio.class, "业务考核等次默认比例");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresPermissions("assess_business_level_ratio:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AssessBusinessLevelRatio.class);
    }

    @PostMapping(value = "/changeEnable")
    public Result<?> changeEnable(@RequestParam("cid") String cid) {
        ratioService.changeEnable(cid);
        return Result.OK("操作成功");
    }

}
