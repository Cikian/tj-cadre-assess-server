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
import org.jeecg.modules.sys.entity.business.AssessBusinessDepartmentContact;
import org.jeecg.modules.business.service.IAssessBusinessDepartmentContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @Description: 业务考核单位互评配置
 * @Author: jeecg-boot
 * @Date: 2024-08-22
 * @Version: V1.0
 */
@Api(tags = "业务考核单位互评配置")
@RestController
@RequestMapping("/modules/assessBusinessDepartmentContact")
@Slf4j
public class AssessBusinessDepartmentContactController extends JeecgController<AssessBusinessDepartmentContact, IAssessBusinessDepartmentContactService> {
    @Autowired
    private IAssessBusinessDepartmentContactService contactService;

    /**
     * 分页列表查询
     *
     * @param assessBusinessDepartmentContact
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "业务考核单位互评配置-分页列表查询")
    @ApiOperation(value = "业务考核单位互评配置-分页列表查询", notes = "业务考核单位互评配置-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessBusinessDepartmentContact>> queryPageList(AssessBusinessDepartmentContact assessBusinessDepartmentContact,
                                                                        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                        HttpServletRequest req) {
        QueryWrapper<AssessBusinessDepartmentContact> queryWrapper = QueryGenerator.initQueryWrapper(assessBusinessDepartmentContact, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<AssessBusinessDepartmentContact> page = new Page<AssessBusinessDepartmentContact>(pageNo, pageSize);
        IPage<AssessBusinessDepartmentContact> pageList = contactService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param assessBusinessDepartmentContact
     * @return
     */
    @AutoLog(value = "业务考核单位互评配置-添加")
    @ApiOperation(value = "业务考核单位互评配置-添加", notes = "业务考核单位互评配置-添加")
    //@RequiresPermissions("org.jeecg:assess_business_department_contact:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessBusinessDepartmentContact assessBusinessDepartmentContact) {
        return contactService.saveNewContact(assessBusinessDepartmentContact);
    }

    /**
     * 编辑
     *
     * @param assessBusinessDepartmentContact
     * @return
     */
    @AutoLog(value = "业务考核单位互评配置-编辑")
    @ApiOperation(value = "业务考核单位互评配置-编辑", notes = "业务考核单位互评配置-编辑")
    //@RequiresPermissions("org.jeecg:assess_business_department_contact:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessBusinessDepartmentContact assessBusinessDepartmentContact) {
        contactService.updateById(assessBusinessDepartmentContact);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "业务考核单位互评配置-通过id删除")
    @ApiOperation(value = "业务考核单位互评配置-通过id删除", notes = "业务考核单位互评配置-通过id删除")
    //@RequiresPermissions("org.jeecg:assess_business_department_contact:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        contactService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "业务考核单位互评配置-批量删除")
    @ApiOperation(value = "业务考核单位互评配置-批量删除", notes = "业务考核单位互评配置-批量删除")
    //@RequiresPermissions("org.jeecg:assess_business_department_contact:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.contactService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "业务考核单位互评配置-通过id查询")
    @ApiOperation(value = "业务考核单位互评配置-通过id查询", notes = "业务考核单位互评配置-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessBusinessDepartmentContact> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessBusinessDepartmentContact assessBusinessDepartmentContact = contactService.getById(id);
        if (assessBusinessDepartmentContact == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessBusinessDepartmentContact);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param assessBusinessDepartmentContact
     */
    //@RequiresPermissions("org.jeecg:assess_business_department_contact:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AssessBusinessDepartmentContact assessBusinessDepartmentContact) {
        return super.exportXls(request, assessBusinessDepartmentContact, AssessBusinessDepartmentContact.class, "业务考核单位互评配置");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresPermissions("assess_business_department_contact:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AssessBusinessDepartmentContact.class);
    }

}
