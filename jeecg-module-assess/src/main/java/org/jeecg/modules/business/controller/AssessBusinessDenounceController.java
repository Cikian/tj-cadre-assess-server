package org.jeecg.modules.business.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
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
import org.jeecg.common.util.TokenUtils;
import org.jeecg.modules.business.service.IAssessBusinessDenounceService;
import org.jeecg.modules.business.service.IAssessBusinessGradeService;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.business.AssessBusinessDenounce;
import org.jeecg.modules.user.UserCommonApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 通报批评
 * @Author: jeecg-boot
 * @Date: 2024-08-30
 * @Version: V1.0
 */
@Api(tags = "通报批评")
@RestController
@RequestMapping("/modules/assessBusinessDenounce")
@Slf4j
public class AssessBusinessDenounceController extends JeecgController<AssessBusinessDenounce, IAssessBusinessDenounceService> {
    @Autowired
    private IAssessBusinessDenounceService denounceService;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private UserCommonApi userCommonApi;
    @Autowired
    private DepartCommonApi departCommonApi;
    @Autowired
    private IAssessBusinessGradeService assessBusinessGradeService;

    /**
     * 分页列表查询
     *
     * @param assessBusinessDenounce
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "通报批评-分页列表查询")
    @ApiOperation(value = "通报批评-分页列表查询", notes = "通报批评-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessBusinessDenounce>> queryPageList(AssessBusinessDenounce assessBusinessDenounce,
                                                               @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                               @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                               HttpServletRequest req) {
        QueryWrapper<AssessBusinessDenounce> queryWrapper = QueryGenerator.initQueryWrapper(assessBusinessDenounce, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<String> roles = userCommonApi.getUserRolesByUserName(sysUser.getUsername());
        if (!roles.contains("department_cadre_admin") && !roles.contains("gbc_cz") && !roles.contains("gbc_fcz")){
            Map<String, String> currentUserDepart = departCommonApi.getCurrentUserDepart();
            String departId = currentUserDepart.get("departId");
            queryWrapper.eq("department_code", departId);

            pageNo = 1;
            pageSize = 1000;
        } else {
            queryWrapper.ne("status", 1);

        }
        Map<String, String[]> parameterMap = new HashMap<>(req.getParameterMap());
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("business");
        if (parameterMap.get("currentYear") == null) {
            if (currentAssessInfo != null && currentAssessInfo.isAssessing()) {
                queryWrapper.eq("current_year", currentAssessInfo.getCurrentYear());
            }
        }
        Page<AssessBusinessDenounce> page = new Page<AssessBusinessDenounce>(pageNo, pageSize);
        IPage<AssessBusinessDenounce> pageList = denounceService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param assessBusinessDenounce
     * @return
     */
    @AutoLog(value = "通报批评-添加")
    @ApiOperation(value = "通报批评-添加", notes = "通报批评-添加")
    //@RequiresPermissions("org.jeecg:assess_business_denounce:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessBusinessDenounce assessBusinessDenounce) {

        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<String> userRolesById = userCommonApi.getUserRolesByUserName(sysUser.getUsername());
        if (!userRolesById.contains("department_cadre_admin")){
            assessBusinessDenounce.setStatus(1);
        } else {
            assessBusinessDenounce.setStatus(3);
        }

        denounceService.save(assessBusinessDenounce);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param assessBusinessDenounce
     * @return
     */
    @AutoLog(value = "通报批评-编辑")
    @ApiOperation(value = "通报批评-编辑", notes = "通报批评-编辑")
    //@RequiresPermissions("org.jeecg:assess_business_denounce:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessBusinessDenounce assessBusinessDenounce) {
        denounceService.updateById(assessBusinessDenounce);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "通报批评-通过id删除")
    @ApiOperation(value = "通报批评-通过id删除", notes = "通报批评-通过id删除")
    //@RequiresPermissions("org.jeecg:assess_business_denounce:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        denounceService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "通报批评-批量删除")
    @ApiOperation(value = "通报批评-批量删除", notes = "通报批评-批量删除")
    //@RequiresPermissions("org.jeecg:assess_business_denounce:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.denounceService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "通报批评-通过id查询")
    @ApiOperation(value = "通报批评-通过id查询", notes = "通报批评-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessBusinessDenounce> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessBusinessDenounce assessBusinessDenounce = denounceService.getById(id);
        if (assessBusinessDenounce == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessBusinessDenounce);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessBusinessDenounce
     */
    //@RequiresPermissions("org.jeecg:assess_business_denounce:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessBusinessDenounce assessBusinessDenounce) {
        return super.exportXls(request, assessBusinessDenounce, AssessBusinessDenounce.class, "通报批评");
    }

    /**
     * 通报 Excel
     * @param currentYear 请求参数
     * @return
     */
    @GetMapping("/exportExcel")
    public void exportExcel(@RequestParam(name = "currentYear",defaultValue = "0") String currentYear, @Value("${server.port}") String port, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isBlank(currentYear)||"0".equals(currentYear)) {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("business");
            if (currentAssessInfo != null && currentAssessInfo.isAssessing()) {
                currentYear=currentAssessInfo.getCurrentYear();
            }
        }
        // 构建导出 Excel 的 URL，包括服务端口和 token 参数
        String url = "http://localhost:" + port + "/jmreport/exportAllExcelStream" + "?token=" + TokenUtils.getTokenByRequest(request);

        JSONObject queryParam = new JSONObject(); // 创建一个空的 JSON 对象用于查询参数
        queryParam.put("currentYear",currentYear);
        Map<String, Object> param = new JSONObject(); // 创建一个 Map 用于存放请求参数
        param.put("excelConfigId", "1021585940625137664"); // 添加 Excel 配置 ID 参数
        param.put("queryParam", queryParam); // 将查询参数放入请求参数中
        String filename = "通报信息汇总.xlsx"; // 指定导出的 Excel 文件名

        // 调用方法导出 Excel
        assessCommonApi.getExportExcel(url, filename, param, response);
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresPermissions("assess_business_denounce:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AssessBusinessDenounce.class);
    }


    @GetMapping("/getByDepartAndYear")
    public Result<IPage<AssessBusinessDenounce>> getByDepartId(@RequestParam(name = "departId", required = true) String departId, @RequestParam(name = "year", required = true) String year) {
        List<AssessBusinessDenounce> commendList = denounceService.queryByDepartIdAndYear(departId, year);
        IPage<AssessBusinessDenounce> page = new Page<>();
        page.setRecords(commendList);
        return Result.OK(page);
    }

    /**
     * 审核
     *
     * @param id
     * @param status
     * @return
     */
    @ApiOperation(value = "通报-审核", notes = "通报-审核")
    @GetMapping(value = "/check")
    public Result<String> check(@RequestParam(name = "id", required = true) String id,@RequestParam(name = "status", required = true) Integer status) {
        AssessBusinessDenounce assessBusinessDenounce = denounceService.getById(id);
        if (assessBusinessDenounce == null) {
            return Result.error("未找到对应数据");
        }
        assessBusinessDenounce.setStatus(status);
        denounceService.updateById(assessBusinessDenounce);
        assessBusinessGradeService.updateAddOrSubtractReason(assessBusinessDenounce.getDepartmentCode(),assessBusinessDenounce.getCurrentYear(),false);
        return Result.OK("更新成功");
    }

    @GetMapping("/getInfo")
    public Result<String> getInfo(@RequestParam(name = "year", required = true) String year, @RequestParam(name = "depart", required = true) String depart) {
        String info = denounceService.getInfo(year,depart);
        return Result.OK(info);
    }

}
