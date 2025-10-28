package org.jeecg.modules.business.controller;

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
import org.jeecg.common.system.vo.SysDepartModel;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.business.entity.BusinessIndexLeader;
import org.jeecg.modules.business.service.IAssessBusinessDepartFillService;
import org.jeecg.modules.business.service.IAssessBusinessFillItemService;
import org.jeecg.modules.business.vo.AssessBusinessDepartFillPage;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.dto.AssessBusinessDepartFillDTO;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.business.AssessBusinessDepartFill;
import org.jeecg.modules.sys.entity.business.AssessBusinessFillItem;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 业务考核填报列表
 * @Author: jeecg-boot
 * @Date: 2024-08-22
 * @Version: V1.0
 */
@Api(tags = "业务考核填报列表")
@RestController
@RequestMapping("/modules/businessAssessDepartFill")
@Slf4j
public class AssessBusinessDepartFillController {
    @Autowired
    private IAssessBusinessDepartFillService fillService;
    @Autowired
    private IAssessBusinessFillItemService fillItemService;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private DepartCommonApi departCommonApi;

    /**
     * 分页列表查询
     *
     * @param assessBusinessDepartFill
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "业务考核填报列表-分页列表查询")
    @ApiOperation(value = "业务考核填报列表-分页列表查询", notes = "业务考核填报列表-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessBusinessDepartFill>> queryPageList(AssessBusinessDepartFill assessBusinessDepartFill,
                                                                 @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                 HttpServletRequest req) {
        QueryWrapper<AssessBusinessDepartFill> queryWrapper = QueryGenerator.initQueryWrapper(assessBusinessDepartFill, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Map<String, String[]> parameterMap = req.getParameterMap();
        if (parameterMap.get("currentYear") == null) {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("business");
            if (currentAssessInfo != null) {
                queryWrapper.eq("current_year", currentAssessInfo.getCurrentYear());
            }
        }
        Page<AssessBusinessDepartFill> page = new Page<AssessBusinessDepartFill>(pageNo, pageSize);
        IPage<AssessBusinessDepartFill> pageList = fillService.listByPageDistRole(page, queryWrapper);
        return Result.OK(pageList);
    }

    @ApiOperation(value = "业务考核填报列表-单位填报人分页列表查询", notes = "业务考核填报列表-单位填报人分页列表查询")
    @GetMapping(value = "/listToReporter")
    public Result<IPage<AssessBusinessDepartFill>> queryPageListToHeader(AssessBusinessDepartFill assessBusinessDepartFill,
                                                                         @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                         @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                         HttpServletRequest req) {
        QueryWrapper<AssessBusinessDepartFill> queryWrapper = QueryGenerator.initQueryWrapper(assessBusinessDepartFill, req.getParameterMap());
        Page<AssessBusinessDepartFill> page = new Page<AssessBusinessDepartFill>(pageNo, pageSize);
        IPage<AssessBusinessDepartFill> pageList = fillService.listByPageToReporter(page, queryWrapper);
        return Result.OK(pageList);
    }

    @ApiOperation(value = "业务考核填报列表-部门负责人分页列表查询", notes = "业务考核填报列表-部门负责人分页列表查询")
    @GetMapping(value = "/listToHeader")
    public Result<IPage<AssessBusinessDepartFill>> queryPageListToReporter(AssessBusinessDepartFill assessBusinessDepartFill,
                                                                           @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                           @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                           HttpServletRequest req) {
        QueryWrapper<AssessBusinessDepartFill> queryWrapper = QueryGenerator.initQueryWrapper(assessBusinessDepartFill, req.getParameterMap());
        Page<AssessBusinessDepartFill> page = new Page<AssessBusinessDepartFill>(pageNo, pageSize);
        IPage<AssessBusinessDepartFill> pageList = fillService.listByPageToHeader(page, queryWrapper);
        return Result.OK(pageList);
    }

    @ApiOperation(value = "业务考核填报列表-分角色分页列表查询", notes = "业务考核填报列表-分角色分页列表查询")
    @GetMapping(value = "/listDistRole")
    public Result<IPage<AssessBusinessDepartFill>> queryPageListDistRole(AssessBusinessDepartFill assessBusinessDepartFill,
                                                                         @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                         @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                         HttpServletRequest req) {
        QueryWrapper<AssessBusinessDepartFill> queryWrapper = QueryGenerator.initQueryWrapper(assessBusinessDepartFill, req.getParameterMap());
        boolean cadre = assessCommonApi.isCadre();
        if (!cadre) {
            Map<String, String> currentUserDepart = departCommonApi.getCurrentUserDepart();
            queryWrapper.eq("report_depart", currentUserDepart.get("departId"));
        }
        queryWrapper.orderByDesc("create_time");

        Map<String, String[]> parameterMap = req.getParameterMap();
        if (parameterMap.get("currentYear") == null) {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("business");
            if (currentAssessInfo != null) {
                queryWrapper.eq("current_year", currentAssessInfo.getCurrentYear());
            }
        }

        Page<AssessBusinessDepartFill> page = new Page<AssessBusinessDepartFill>(pageNo, pageSize);
        IPage<AssessBusinessDepartFill> pageList = fillService.listByPageDistRole(page, queryWrapper);

        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param assessBusinessDepartFillPage
     * @return
     */
    @AutoLog(value = "业务考核填报列表-添加")
    @ApiOperation(value = "业务考核填报列表-添加", notes = "业务考核填报列表-添加")
    //@RequiresPermissions("org.jeecg:business_assess_depart_fill:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessBusinessDepartFillPage assessBusinessDepartFillPage) {
        AssessBusinessDepartFill assessBusinessDepartFill = new AssessBusinessDepartFill();
        BeanUtils.copyProperties(assessBusinessDepartFillPage, assessBusinessDepartFill);
        fillService.saveMain(assessBusinessDepartFill, assessBusinessDepartFillPage.getAssessBusinessFillItemList());
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param assessBusinessDepartFillPage
     * @return
     */
    @AutoLog(value = "业务考核填报列表-编辑")
    @ApiOperation(value = "业务考核填报列表-编辑", notes = "业务考核填报列表-编辑")
    //@RequiresPermissions("org.jeecg:business_assess_depart_fill:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessBusinessDepartFillPage assessBusinessDepartFillPage) {
        List<AssessBusinessFillItem> businessFillItems=assessBusinessDepartFillPage.getAssessBusinessFillItemList();
        if (fillService.assessNone(businessFillItems)){
            return Result.error("请至少测评一个处室（单位）！");
        }
        Map<String, String> currentUserDepart = departCommonApi.getCurrentUserDepart();
        AssessBusinessDepartFill assessBusinessDepartFill = new AssessBusinessDepartFill();
        BeanUtils.copyProperties(assessBusinessDepartFillPage, assessBusinessDepartFill);
        AssessBusinessDepartFill assessBusinessDepartFillEntity = fillService.getById(assessBusinessDepartFill.getId());
        if (assessBusinessDepartFillEntity == null) {
            return Result.error("未找到对应数据");
        }
        if (currentUserDepart == null) assessBusinessDepartFill.setReportBy("填报专员");
        else assessBusinessDepartFill.setReportBy(currentUserDepart.get("alias") + "填报专员");
        return fillService.updateMain(assessBusinessDepartFill, assessBusinessDepartFillPage.getAssessBusinessFillItemList());
    }


    @AutoLog(value = "业务考核填报列表-编辑")
    @ApiOperation(value = "业务考核填报列表-编辑", notes = "业务考核填报列表-编辑")
    //@RequiresPermissions("org.jeecg:business_assess_depart_fill:edit")
    @RequestMapping(value = "/save", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> save(@RequestBody AssessBusinessDepartFillPage assessBusinessDepartFillPage) {
        AssessBusinessDepartFill assessBusinessDepartFill = new AssessBusinessDepartFill();
        BeanUtils.copyProperties(assessBusinessDepartFillPage, assessBusinessDepartFill);
        AssessBusinessDepartFill assessBusinessDepartFillEntity = fillService.getById(assessBusinessDepartFill.getId());
        if (assessBusinessDepartFillEntity == null) {
            return Result.error("未找到对应数据");
        }
        if (assessBusinessDepartFill.getStatus() == 3) {
            return Result.error("审核已通过，不可修改！");
        }
        fillService.saveTemp(assessBusinessDepartFill, assessBusinessDepartFillPage.getAssessBusinessFillItemList());
        return Result.OK("保存成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "业务考核填报列表-通过id删除")
    @ApiOperation(value = "业务考核填报列表-通过id删除", notes = "业务考核填报列表-通过id删除")
    //@RequiresPermissions("org.jeecg:business_assess_depart_fill:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        fillService.delMain(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "业务考核填报列表-批量删除")
    @ApiOperation(value = "业务考核填报列表-批量删除", notes = "业务考核填报列表-批量删除")
    //@RequiresPermissions("org.jeecg:business_assess_depart_fill:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.fillService.delBatchMain(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "业务考核填报列表-通过id查询")
    @ApiOperation(value = "业务考核填报列表-通过id查询", notes = "业务考核填报列表-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessBusinessDepartFill> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessBusinessDepartFill assessBusinessDepartFill = fillService.getById(id);
        if (assessBusinessDepartFill == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessBusinessDepartFill);

    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "业务考核填报项目通过主表ID查询")
    @ApiOperation(value = "业务考核填报项目主表ID查询", notes = "业务考核填报项目-通主表ID查询")
    @GetMapping(value = "/queryAssessBusinessFillItemByMainId")
    public Result<IPage<AssessBusinessFillItem>> queryAssessBusinessFillItemListByMainId(@RequestParam(name = "id", required = true) String id) {
        List<AssessBusinessFillItem> assessBusinessFillItemList = fillItemService.selectByMainId(id);
        List<SysDepartModel> allDepart = departCommonApi.queryAllDepart();
        Map<String, Integer> departMap = allDepart.stream().collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getDepartOrder));
        for (AssessBusinessFillItem item : assessBusinessFillItemList) {
            item.setSortNo(departMap.get(item.getDepartCode()));
        }
        List<AssessBusinessFillItem> sortedList = assessBusinessFillItemList.stream().sorted(Comparator.comparing(AssessBusinessFillItem::getSortNo)).collect(Collectors.toList());//升序排序

        AssessBusinessDepartFill byId = fillService.getById(id);
        if (1 != byId.getStatus()) {
            // 如果score为空，排在后面
            sortedList.sort((o1, o2) -> {
                        if (o1.getScore() == null && o2.getScore() == null) {
                            return 0;
                        }
                        if (o1.getScore() == null) {
                            return 1;
                        }
                        if (o2.getScore() == null) {
                            return -1;
                        }
                        return o2.getScore().compareTo(o1.getScore());
                    }
            );
        }

        IPage<AssessBusinessFillItem> pageList = new Page<>();
        pageList.setRecords(sortedList);
        return Result.OK(pageList);
    }



    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "查询必须填报部门", notes = "业务考核填报项目-查询必须填报部门")
    @GetMapping(value = "/queryAssessBusinessMustFill")
    public Result<IPage<AssessBusinessFillItem>> queryAssessBusinessMustFill(@RequestParam(name = "id", required = true) String id) {
        QueryWrapper<AssessBusinessFillItem> assessBusinessFillItemQueryWrapper = new QueryWrapper<>();
        assessBusinessFillItemQueryWrapper.eq("fill_id",id);
        assessBusinessFillItemQueryWrapper.eq("must_fill",1);
        List<AssessBusinessFillItem> assessBusinessFillItemList = fillItemService.list(assessBusinessFillItemQueryWrapper);
        AssessBusinessDepartFill byId = fillService.getById(id);
        if (1 != byId.getStatus()) {
            // 如果score为空，排在后面
            assessBusinessFillItemList.sort((o1, o2) -> {
                        if (o1.getScore() == null && o2.getScore() == null) {
                            return 0;
                        }
                        if (o1.getScore() == null) {
                            return 1;
                        }
                        if (o2.getScore() == null) {
                            return -1;
                        }
                        return o2.getScore().compareTo(o1.getScore());
                    }
            );
        }
        IPage<AssessBusinessFillItem> pageList = new Page<>();
        pageList.setRecords(assessBusinessFillItemList);
        return Result.OK(pageList);
    }






    /**
     * 查询业务考核首页信息
     */
    @ApiOperation(value = "查询业务考核首页信息", notes = "查询业务考核首页信息")
    @GetMapping(value = "getIndexInfo")
    public Result<BusinessIndexLeader> getAllItems() {
        return Result.OK(fillService.getAllItems());
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessBusinessDepartFill
     */
    //@RequiresPermissions("org.jeecg:business_assess_depart_fill:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessBusinessDepartFill assessBusinessDepartFill) {
        // Step.1 组装查询条件查询数据
        QueryWrapper<AssessBusinessDepartFill> queryWrapper = QueryGenerator.initQueryWrapper(assessBusinessDepartFill, request.getParameterMap());
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        //配置选中数据查询条件
        String selections = request.getParameter("selections");
        if (oConvertUtils.isNotEmpty(selections)) {
            List<String> selectionList = Arrays.asList(selections.split(","));
            queryWrapper.in("id", selectionList);
        }
        //Step.2 获取导出数据
        List<AssessBusinessDepartFill> assessBusinessDepartFillList = fillService.list(queryWrapper);

        // Step.3 组装pageList
        List<AssessBusinessDepartFillPage> pageList = new ArrayList<AssessBusinessDepartFillPage>();
        for (AssessBusinessDepartFill main : assessBusinessDepartFillList) {
            AssessBusinessDepartFillPage vo = new AssessBusinessDepartFillPage();
            BeanUtils.copyProperties(main, vo);
            List<AssessBusinessFillItem> assessBusinessFillItemList = fillItemService.selectByMainId(main.getId());
            vo.setAssessBusinessFillItemList(assessBusinessFillItemList);
            pageList.add(vo);
        }

        // Step.4 AutoPoi 导出Excel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        mv.addObject(NormalExcelConstants.FILE_NAME, "业务考核填报列表列表");
        mv.addObject(NormalExcelConstants.CLASS, AssessBusinessDepartFillPage.class);
        mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("业务考核填报列表数据", "导出人:" + sysUser.getRealname(), "业务考核填报列表"));
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
    //@RequiresPermissions("org.jeecg:business_assess_depart_fill:importExcel")
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
                List<AssessBusinessDepartFillPage> list = ExcelImportUtil.importExcel(file.getInputStream(), AssessBusinessDepartFillPage.class, params);
                for (AssessBusinessDepartFillPage page : list) {
                    AssessBusinessDepartFill po = new AssessBusinessDepartFill();
                    BeanUtils.copyProperties(page, po);
                    fillService.saveMain(po, page.getAssessBusinessFillItemList());
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


    @AutoLog(value = "业务考核填报列表-添加")
    @ApiOperation(value = "业务考核填报列表-添加", notes = "业务考核填报列表-添加")
    //@RequiresPermissions("org.jeecg:business_assess_depart_fill:add")
    @PostMapping(value = "/init")
    public Result<String> initAssess(@RequestBody AssessBusinessDepartFillDTO assessBusinessDepartFillDTO) {
        fillService.initAssess(assessBusinessDepartFillDTO);
        return Result.OK("添加成功！");
    }

    @AutoLog(value = "业务考核填报列表-审核通过")
    @ApiOperation(value = "业务考核填报列表-审核通过", notes = "业务考核填报列表-审核通过")
    //@RequiresPermissions("org.jeecg:business_assess_depart_fill:add")
    @PutMapping(value = "/passAudit")
    public Result<String> passTheAudit(@RequestParam String assessId, @RequestParam String name) {
        AssessBusinessDepartFill assessBusinessDepartFillEntity = fillService.getById(assessId);
        if (assessBusinessDepartFillEntity == null) {
            return Result.error("未找到对应数据");
        }

        List<AssessBusinessFillItem> itemList = fillItemService.selectByMainId(assessId);

        for (AssessBusinessFillItem entity : itemList) {
            if (entity.getScore() != null && entity.getScore().compareTo(new BigDecimal(100)) < 0) {
                if (entity.getReasonOfDeduction() == null || entity.getReasonOfDeduction().trim().isEmpty() || entity.getReasonOfDeduction().isEmpty()) {
                    return Result.error("填报中存在分数小于100分，并且未填写扣分原因，请核对后重新提交！");
                }
            }
        }

        return fillService.passAudit(assessId, name, itemList);
    }

    @AutoLog(value = "业务考核填报列表-保存并审核通过")
    @ApiOperation(value = "业务考核填报列表-保存并审核通过", notes = "业务考核填报列表-保存并审核通过")
    //@RequiresPermissions("org.jeecg:business_assess_depart_fill:edit")
    @RequestMapping(value = "/saveAndPass", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> saveAndPassAudit(@RequestBody AssessBusinessDepartFillPage assessBusinessDepartFillPage, @RequestParam String name) {
        AssessBusinessDepartFill assessBusinessDepartFill = new AssessBusinessDepartFill();
        BeanUtils.copyProperties(assessBusinessDepartFillPage, assessBusinessDepartFill);
        AssessBusinessDepartFill assessBusinessDepartFillEntity = fillService.getById(assessBusinessDepartFill.getId());
        if (assessBusinessDepartFillEntity == null) {
            return Result.error("未找到对应数据");
        }

        if (assessBusinessDepartFillEntity.getStatus() == 3) {
            return Result.error("审核已通过，不可修改！");
        }
        Result<String> result = fillService.updateMain(assessBusinessDepartFill, assessBusinessDepartFillPage.getAssessBusinessFillItemList());
        if (result.isSuccess()) {
            return fillService.passAudit(assessBusinessDepartFill.getId(), name, assessBusinessDepartFillPage.getAssessBusinessFillItemList());
        }

        return result;
    }


    @ApiOperation(value = "业务考核填报列表-通过成绩id查询填报详情", notes = "业务考核填报列表-通过成绩id查询填报详情")
    @GetMapping(value = "/getFillItem")
    public Result<IPage<AssessBusinessFillItem>> getFillItemByGradeId(@RequestParam String gradeId) {
        return Result.OK(fillItemService.getFillItemByGradeId(gradeId));
    }

    @PostMapping("/addMustAssessItem")
    public Result<?> addMustAssessItem(@RequestBody List<Map<String, String>> array) {
        fillItemService.addMustAssessItem(array);
        return Result.OK("添加成功！");
    }

}
