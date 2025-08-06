package org.jeecg.modules.annual.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.AssessDemocraticEvaluation;
import org.jeecg.modules.sys.entity.annual.AssessDemocraticEvaluationItem;
import org.jeecg.modules.sys.entity.annual.AssessDemocraticEvaluationSummary;
import org.jeecg.modules.annual.service.IAssessDemocraticEvaluationItemService;
import org.jeecg.modules.annual.service.IAssessDemocraticEvaluationService;
import org.jeecg.modules.annual.service.IAssessDemocraticEvaluationSummaryService;
import org.jeecg.modules.annual.vo.AssessDemocraticEvaluationPage;
import org.jeecg.modules.annual.vo.DemocraticFillListVO;
import org.jeecg.modules.annual.vo.DemocraticSubmitPage;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 民主测评填报
 * @Author: jeecg-boot
 * @Date: 2024-09-11
 * @Version: V1.0
 */
@Api(tags = "民主测评填报")
@RestController
@RequestMapping("/modules/annual/democraticEvaluation")
@Slf4j
public class AssessDemocraticEvaluationController {
    @Autowired
    private IAssessDemocraticEvaluationService evaluationService;
    @Autowired
    private IAssessDemocraticEvaluationItemService evaluationItemService;
    @Autowired
    private IAssessDemocraticEvaluationSummaryService summaryService;
    @Autowired
    private AssessCommonApi assessCommonApi;

    /**
     * 分页列表查询
     */
    //@AutoLog(value = "民主测评填报-分页列表查询")
    @ApiOperation(value = "民主测评填报-分页列表查询", notes = "民主测评填报-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<DemocraticFillListVO>> queryList() {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
        if (currentAssessInfo == null) return Result.error("当前不在考核期间");
        else if (!currentAssessInfo.isAssessing()) return Result.error("当前不在考核期间");

        List<DemocraticFillListVO> anonymousList = evaluationService.getAnonymousList();

        IPage<DemocraticFillListVO> page = new Page<>();
        page.setRecords(anonymousList);
        return Result.OK(page);
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "民主测评填报-通过id查询")
    @ApiOperation(value = "民主测评填报-通过id查询", notes = "民主测评填报-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessDemocraticEvaluation> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessDemocraticEvaluation assessDemocraticEvaluation = evaluationService.getById(id);
        if (assessDemocraticEvaluation == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessDemocraticEvaluation);

    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "民主测评项通过主表ID查询")
    @ApiOperation(value = "民主测评项主表ID查询", notes = "民主测评项-通主表ID查询")
    @GetMapping(value = "/queryAssessDemocraticEvaluationItemByMainId")
    public Result<List<AssessDemocraticEvaluationItem>> queryAssessDemocraticEvaluationItemListByMainId(@RequestParam(name = "id", required = true) String id) {
        List<AssessDemocraticEvaluationItem> assessDemocraticEvaluationItemList = evaluationItemService.selectByMainId(id);
        return Result.OK(assessDemocraticEvaluationItemList);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessDemocraticEvaluation
     */
    //@RequiresPermissions("org.jeecg:assess_democratic_evaluation:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessDemocraticEvaluation assessDemocraticEvaluation) {
        // Step.1 组装查询条件查询数据
        QueryWrapper<AssessDemocraticEvaluation> queryWrapper = QueryGenerator.initQueryWrapper(assessDemocraticEvaluation, request.getParameterMap());
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        // 配置选中数据查询条件
        String selections = request.getParameter("selections");
        if (oConvertUtils.isNotEmpty(selections)) {
            List<String> selectionList = Arrays.asList(selections.split(","));
            queryWrapper.in("id", selectionList);
        }
        // Step.2 获取导出数据
        List<AssessDemocraticEvaluation> assessDemocraticEvaluationList = evaluationService.list(queryWrapper);

        // Step.3 组装pageList
        List<AssessDemocraticEvaluationPage> pageList = new ArrayList<AssessDemocraticEvaluationPage>();
        for (AssessDemocraticEvaluation main : assessDemocraticEvaluationList) {
            AssessDemocraticEvaluationPage vo = new AssessDemocraticEvaluationPage();
            BeanUtils.copyProperties(main, vo);
            List<AssessDemocraticEvaluationItem> assessDemocraticEvaluationItemList = evaluationItemService.selectByMainId(main.getId());
            vo.setAssessDemocraticEvaluationItemList(assessDemocraticEvaluationItemList);
            pageList.add(vo);
        }

        // Step.4 AutoPoi 导出Excel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        mv.addObject(NormalExcelConstants.FILE_NAME, "民主测评填报列表");
        mv.addObject(NormalExcelConstants.CLASS, AssessDemocraticEvaluationPage.class);
        mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("民主测评填报数据", "导出人:" + sysUser.getRealname(), "民主测评填报"));
        mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
        return mv;
    }

    @PostMapping("/submit")
    public Result<?> submit(@RequestBody List<DemocraticSubmitPage> democraticSubmitPage) {
        if (!this.checkDemocraticSubmit(democraticSubmitPage)) {
            return Result.error("系统异常，请稍后再试！");
        }

        evaluationService.submit(democraticSubmitPage);

        return Result.OK("提交成功！");
    }

    public boolean checkDemocraticSubmit(List<DemocraticSubmitPage> democraticSubmitPage) {
        for (DemocraticSubmitPage page : democraticSubmitPage) {
            // 校验分数
            for (AssessDemocraticEvaluationItem item : page.getItems()) {
                BigDecimal optionWeight = item.getOptionWeight();
                if (optionWeight == null || optionWeight.compareTo(BigDecimal.ZERO) == 0) {
                    throw new JeecgBootException("存在未填写的分数，请核对后再提交");
                }
            }

            String summaryId = page.getEvaluationSummaryId();
            AssessDemocraticEvaluationSummary summary = summaryService.getById(summaryId);
            if (summary == null) {
                throw new JeecgBootException("未找到对应的测评汇总");
            }

        }

        return true;
    }


}
