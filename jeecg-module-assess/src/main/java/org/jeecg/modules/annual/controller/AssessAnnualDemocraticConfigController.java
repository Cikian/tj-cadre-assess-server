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
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.sys.entity.annual.AssessAnnualDemocraticConfig;
import org.jeecg.modules.sys.entity.annual.AssessAnnualDemocraticItem;
import org.jeecg.modules.annual.service.IAssessAnnualDemocraticConfigService;
import org.jeecg.modules.annual.service.IAssessAnnualDemocraticItemService;
import org.jeecg.modules.annual.vo.AssessAnnualDemocraticConfigPage;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: 民主测评指标
 * @Author: jeecg-boot
 * @Date: 2024-09-11
 * @Version: V1.0
 */
@Api(tags = "民主测评指标")
@RestController
@RequestMapping("/modules/democraticConfig")
@Slf4j
public class AssessAnnualDemocraticConfigController {
    @Autowired
    private IAssessAnnualDemocraticConfigService democraticConfigService;
    @Autowired
    private IAssessAnnualDemocraticItemService democraticItemService;

    /**
     * 分页列表查询
     *
     * @param assessAnnualDemocraticConfig
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "民主测评指标-分页列表查询")
    @ApiOperation(value = "民主测评指标-分页列表查询", notes = "民主测评指标-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessAnnualDemocraticConfig>> queryPageList(AssessAnnualDemocraticConfig assessAnnualDemocraticConfig,
                                                                     @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                     @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                     HttpServletRequest req) {
        QueryWrapper<AssessAnnualDemocraticConfig> queryWrapper = QueryGenerator.initQueryWrapper(assessAnnualDemocraticConfig, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<AssessAnnualDemocraticConfig> page = new Page<AssessAnnualDemocraticConfig>(pageNo, pageSize);
        IPage<AssessAnnualDemocraticConfig> pageList = democraticConfigService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param assessAnnualDemocraticConfigPage
     * @return
     */
    @AutoLog(value = "民主测评指标-添加")
    @ApiOperation(value = "民主测评指标-添加", notes = "民主测评指标-添加")
    //@RequiresPermissions("org.jeecg:assess_annual_democratic_config:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessAnnualDemocraticConfigPage assessAnnualDemocraticConfigPage) {
        AssessAnnualDemocraticConfig assessAnnualDemocraticConfig = new AssessAnnualDemocraticConfig();
        BeanUtils.copyProperties(assessAnnualDemocraticConfigPage, assessAnnualDemocraticConfig);
        democraticConfigService.saveMain(assessAnnualDemocraticConfig, assessAnnualDemocraticConfigPage.getAssessAnnualDemocraticItemList());
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param assessAnnualDemocraticConfigPage
     * @return
     */
    @AutoLog(value = "民主测评指标-编辑")
    @ApiOperation(value = "民主测评指标-编辑", notes = "民主测评指标-编辑")
    //@RequiresPermissions("org.jeecg:assess_annual_democratic_config:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessAnnualDemocraticConfigPage assessAnnualDemocraticConfigPage) {
        AssessAnnualDemocraticConfig assessAnnualDemocraticConfig = new AssessAnnualDemocraticConfig();
        BeanUtils.copyProperties(assessAnnualDemocraticConfigPage, assessAnnualDemocraticConfig);
        AssessAnnualDemocraticConfig assessAnnualDemocraticConfigEntity = democraticConfigService.getById(assessAnnualDemocraticConfig.getId());
        if (assessAnnualDemocraticConfigEntity == null) {
            return Result.error("未找到对应数据");
        }
        democraticConfigService.updateMain(assessAnnualDemocraticConfig, assessAnnualDemocraticConfigPage.getAssessAnnualDemocraticItemList());
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "民主测评指标-通过id删除")
    @ApiOperation(value = "民主测评指标-通过id删除", notes = "民主测评指标-通过id删除")
    //@RequiresPermissions("org.jeecg:assess_annual_democratic_config:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        democraticConfigService.delMain(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "民主测评指标-批量删除")
    @ApiOperation(value = "民主测评指标-批量删除", notes = "民主测评指标-批量删除")
    //@RequiresPermissions("org.jeecg:assess_annual_democratic_config:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.democraticConfigService.delBatchMain(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "民主测评指标-通过id查询")
    @ApiOperation(value = "民主测评指标-通过id查询", notes = "民主测评指标-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessAnnualDemocraticConfig> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessAnnualDemocraticConfig assessAnnualDemocraticConfig = democraticConfigService.getById(id);
        if (assessAnnualDemocraticConfig == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessAnnualDemocraticConfig);

    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "民主测评指标项通过主表ID查询")
    @ApiOperation(value = "民主测评指标项主表ID查询", notes = "民主测评指标项-通主表ID查询")
    @GetMapping(value = "/queryAssessAnnualDemocraticItemByMainId")
    public Result<List<AssessAnnualDemocraticItem>> queryAssessAnnualDemocraticItemListByMainId(@RequestParam(name = "id", required = true) String id) {
        List<AssessAnnualDemocraticItem> assessAnnualDemocraticItemList = democraticItemService.selectByMainId(id);
        return Result.OK(assessAnnualDemocraticItemList);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessAnnualDemocraticConfig
     */
    //@RequiresPermissions("org.jeecg:assess_annual_democratic_config:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessAnnualDemocraticConfig assessAnnualDemocraticConfig) {
        // Step.1 组装查询条件查询数据
        QueryWrapper<AssessAnnualDemocraticConfig> queryWrapper = QueryGenerator.initQueryWrapper(assessAnnualDemocraticConfig, request.getParameterMap());
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        //配置选中数据查询条件
        String selections = request.getParameter("selections");
        if (oConvertUtils.isNotEmpty(selections)) {
            List<String> selectionList = Arrays.asList(selections.split(","));
            queryWrapper.in("id", selectionList);
        }
        //Step.2 获取导出数据
        List<AssessAnnualDemocraticConfig> assessAnnualDemocraticConfigList = democraticConfigService.list(queryWrapper);

        // Step.3 组装pageList
        List<AssessAnnualDemocraticConfigPage> pageList = new ArrayList<AssessAnnualDemocraticConfigPage>();
        for (AssessAnnualDemocraticConfig main : assessAnnualDemocraticConfigList) {
            AssessAnnualDemocraticConfigPage vo = new AssessAnnualDemocraticConfigPage();
            BeanUtils.copyProperties(main, vo);
            List<AssessAnnualDemocraticItem> assessAnnualDemocraticItemList = democraticItemService.selectByMainId(main.getId());
            vo.setAssessAnnualDemocraticItemList(assessAnnualDemocraticItemList);
            pageList.add(vo);
        }

        // Step.4 AutoPoi 导出Excel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        mv.addObject(NormalExcelConstants.FILE_NAME, "民主测评指标列表");
        mv.addObject(NormalExcelConstants.CLASS, AssessAnnualDemocraticConfigPage.class);
        mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("民主测评指标数据", "导出人:" + sysUser.getRealname(), "民主测评指标"));
        mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
        return mv;
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresPermissions("org.jeecg:assess_annual_democratic_config:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<AssessAnnualDemocraticConfigPage> list = ExcelImportUtil.importExcel(file.getInputStream(), AssessAnnualDemocraticConfigPage.class, params);
                for (AssessAnnualDemocraticConfigPage page : list) {
                    AssessAnnualDemocraticConfig po = new AssessAnnualDemocraticConfig();
                    BeanUtils.copyProperties(page, po);
                    democraticConfigService.saveMain(po, page.getAssessAnnualDemocraticItemList());
                }
                return Result.OK("文件导入成功！数据行数:" + list.size());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Result.error("文件导入失败:" + e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Result.OK("文件导入失败！");
    }

    @GetMapping("/getItemByType")
    public Result<List<AssessAnnualDemocraticItem>> getItemByType(@RequestParam(name = "type", required = true) String type) {
        List<AssessAnnualDemocraticItem> list = democraticItemService.getItemByType(type);

        return Result.ok(list);
    }

    @GetMapping("/getOptionsByType")
    public Result<Map<String, BigDecimal>> getOptionsByType(@RequestParam(name = "type", required = true) String type) {
        Map<String, BigDecimal> optionsByType = democraticConfigService.getOptionsByType(type);

        return Result.ok(optionsByType);
    }

}
