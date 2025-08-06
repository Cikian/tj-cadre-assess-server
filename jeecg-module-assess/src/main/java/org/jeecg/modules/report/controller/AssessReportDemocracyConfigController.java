package org.jeecg.modules.report.controller;

import com.alibaba.fastjson.JSONObject;
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
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.report.service.IAssessReportDemocracyConfigItemService;
import org.jeecg.modules.report.service.IAssessReportDemocracyConfigService;
import org.jeecg.modules.report.vo.AssessReportDemocracyConfigPage;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.report.AssessReportDemocracyConfig;
import org.jeecg.modules.sys.entity.report.AssessReportDemocracyConfigItem;
import org.jeecg.modules.sys.entity.report.AssessReportEvaluationItem;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: 一报告两评议民主测评配置
 * @Author: jeecg-boot
 * @Date: 2024-10-07
 * @Version: V1.0
 */
@Api(tags = "一报告两评议民主测评配置")
@RestController
@RequestMapping("/modules/report/assessReportDemocracyConfig")
@Slf4j
public class AssessReportDemocracyConfigController {
    @Autowired
    private IAssessReportDemocracyConfigService configService;
    @Autowired
    private IAssessReportDemocracyConfigItemService itemService;
    @Autowired
    private AssessCommonApi assessCommonApi;

    /**
     * 分页列表查询
     *
     * @param assessReportDemocracyConfig
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "一报告两评议民主测评配置-分页列表查询")
    @ApiOperation(value = "一报告两评议民主测评配置-分页列表查询", notes = "一报告两评议民主测评配置-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessReportDemocracyConfig>> queryPageList(AssessReportDemocracyConfig assessReportDemocracyConfig,
                                                                    @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                    @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                    HttpServletRequest req) {
        QueryWrapper<AssessReportDemocracyConfig> queryWrapper = QueryGenerator.initQueryWrapper(assessReportDemocracyConfig, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<AssessReportDemocracyConfig> page = new Page<AssessReportDemocracyConfig>(pageNo, pageSize);
        IPage<AssessReportDemocracyConfig> pageList = configService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param assessReportDemocracyConfigPage
     * @return
     */
    @AutoLog(value = "一报告两评议民主测评配置-添加")
    @ApiOperation(value = "一报告两评议民主测评配置-添加", notes = "一报告两评议民主测评配置-添加")
    //@RequiresPermissions("org.jeecg:assess_report_democracy_config:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessReportDemocracyConfigPage assessReportDemocracyConfigPage) {
        AssessReportDemocracyConfig assessReportDemocracyConfig = new AssessReportDemocracyConfig();
        BeanUtils.copyProperties(assessReportDemocracyConfigPage, assessReportDemocracyConfig);
        configService.saveMain(assessReportDemocracyConfig, assessReportDemocracyConfigPage.getConfigItems(), assessReportDemocracyConfigPage.getMultiItems());
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param configPage
     * @return
     */
    @AutoLog(value = "一报告两评议民主测评配置-编辑")
    @ApiOperation(value = "一报告两评议民主测评配置-编辑", notes = "一报告两评议民主测评配置-编辑")
    //@RequiresPermissions("org.jeecg:assess_report_democracy_config:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessReportDemocracyConfigPage configPage) {
        AssessReportDemocracyConfig config = new AssessReportDemocracyConfig();
        BeanUtils.copyProperties(configPage, config);
        AssessReportDemocracyConfig assessReportDemocracyConfigEntity = configService.getById(config.getId());
        if (assessReportDemocracyConfigEntity == null) {
            return Result.error("未找到对应数据");
        }
        configService.updateMain(config, configPage.getConfigItems(), configPage.getMultiItems());
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "一报告两评议民主测评配置-通过id删除")
    @ApiOperation(value = "一报告两评议民主测评配置-通过id删除", notes = "一报告两评议民主测评配置-通过id删除")
    //@RequiresPermissions("org.jeecg:assess_report_democracy_config:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        configService.delMain(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "一报告两评议民主测评配置-批量删除")
    @ApiOperation(value = "一报告两评议民主测评配置-批量删除", notes = "一报告两评议民主测评配置-批量删除")
    //@RequiresPermissions("org.jeecg:assess_report_democracy_config:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.configService.delBatchMain(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "一报告两评议民主测评配置-通过id查询")
    @ApiOperation(value = "一报告两评议民主测评配置-通过id查询", notes = "一报告两评议民主测评配置-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessReportDemocracyConfigPage> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessReportDemocracyConfigPage page = configService.selectById(id);
        if (page == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(page);

    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "一报告两评议民主测评项通过主表ID查询")
    @ApiOperation(value = "一报告两评议民主测评项主表ID查询", notes = "一报告两评议民主测评项-通主表ID查询")
    @GetMapping(value = "/queryAssessReportDemocracyConfigItemByMainId")
    public Result<List<AssessReportDemocracyConfigItem>> queryAssessReportDemocracyConfigItemListByMainId(@RequestParam(name = "id", required = true) String id) {
        List<AssessReportDemocracyConfigItem> assessReportDemocracyConfigItemList = itemService.selectByMainId(id);
        return Result.OK(assessReportDemocracyConfigItemList);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessReportDemocracyConfig
     */
    //@RequiresPermissions("org.jeecg:assess_report_democracy_config:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessReportDemocracyConfig assessReportDemocracyConfig) {
        // Step.1 组装查询条件查询数据
        QueryWrapper<AssessReportDemocracyConfig> queryWrapper = QueryGenerator.initQueryWrapper(assessReportDemocracyConfig, request.getParameterMap());
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        //配置选中数据查询条件
        String selections = request.getParameter("selections");
        if (oConvertUtils.isNotEmpty(selections)) {
            List<String> selectionList = Arrays.asList(selections.split(","));
            queryWrapper.in("id", selectionList);
        }
        //Step.2 获取导出数据
        List<AssessReportDemocracyConfig> assessReportDemocracyConfigList = configService.list(queryWrapper);

        // Step.3 组装pageList
        List<AssessReportDemocracyConfigPage> pageList = new ArrayList<AssessReportDemocracyConfigPage>();
        for (AssessReportDemocracyConfig main : assessReportDemocracyConfigList) {
            AssessReportDemocracyConfigPage vo = new AssessReportDemocracyConfigPage();
            BeanUtils.copyProperties(main, vo);
            List<AssessReportDemocracyConfigItem> assessReportDemocracyConfigItemList = itemService.selectByMainId(main.getId());
            vo.setConfigItems(assessReportDemocracyConfigItemList);
            pageList.add(vo);
        }

        // Step.4 AutoPoi 导出Excel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        mv.addObject(NormalExcelConstants.FILE_NAME, "一报告两评议民主测评配置列表");
        mv.addObject(NormalExcelConstants.CLASS, AssessReportDemocracyConfigPage.class);
        mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("一报告两评议民主测评配置数据", "导出人:" + sysUser.getRealname(), "一报告两评议民主测评配置"));
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
    //@RequiresPermissions("org.jeecg:assess_report_democracy_config:importExcel")
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
                List<AssessReportDemocracyConfigPage> list = ExcelImportUtil.importExcel(file.getInputStream(), AssessReportDemocracyConfigPage.class, params);
                for (AssessReportDemocracyConfigPage page : list) {
                    AssessReportDemocracyConfig po = new AssessReportDemocracyConfig();
                    BeanUtils.copyProperties(page, po);
                    configService.saveMain(po, page.getConfigItems(), page.getMultiItems());
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

    @PutMapping("/updateEnable")
    public Result<String> updateEnable(@RequestParam(name = "id", required = true) String id) {
        boolean b = configService.updateEnable(id);
        if (b) {
            return Result.OK("启用成功！");
        } else {
            return Result.error("启用失败！");
        }
    }

    @GetMapping("/getItems")
    public Result<?> getItems() {
        List<AssessReportEvaluationItem> fillItems = configService.getFillItems();

        return Result.OK(fillItems);
    }

    /**
     * 匿名账号信息 Excel
     *
     * @param port 端口号
     * @return
     */
    @PostMapping("/exportExcel")
    public void exportExcel(@Value("${server.port}") String port, HttpServletRequest request, HttpServletResponse response) {
        // 构建导出 Excel 的 URL，包括服务端口和 token 参数
        String url = "http://localhost:" + port + "/jmreport/exportAllExcelStream" + "?token=" + TokenUtils.getTokenByRequest(request);

        JSONObject queryParam = new JSONObject(); // 创建一个空的 JSON 对象用于查询参数
        Map<String, Object> param = new JSONObject(); // 创建一个 Map 用于存放请求参数
        param.put("excelConfigId", "1017224638880456704"); // 添加 Excel 配置 ID 参数
        param.put("queryParam", queryParam); // 将查询参数放入请求参数中
        String filename = "一报告两评议民主评议匿名账号.xlsx"; // 指定导出的 Excel 文件名

        // 调用方法导出 Excel
        assessCommonApi.getExportExcel(url, filename, param, response);
    }


}
