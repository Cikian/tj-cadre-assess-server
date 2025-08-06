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
import org.jeecg.modules.annual.service.IAssessExcellentNumService;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.annual.AssessExcellentNum;
import org.jeecg.modules.user.UserCommonApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description: 年度考核分管领导优秀名额
 * @Author: jeecg-boot
 * @Date: 2024-09-18
 * @Version: V1.0
 */
@Api(tags = "优秀副职数量")
@RestController
@RequestMapping("/modules/annual/assessExcellentNum")
@Slf4j
public class AssessExcellentNumController extends JeecgController<AssessExcellentNum, IAssessExcellentNumService> {
    @Autowired
    private IAssessExcellentNumService numService;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private DepartCommonApi departCommonApi;
    @Autowired
    private UserCommonApi userCommonApi;

    /**
     * 分页列表查询
     *
     * @param assessExcellentNum
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "年度考核分管领导优秀名额-分页列表查询")
    @ApiOperation(value = "年度考核分管领导优秀名额-分页列表查询", notes = "年度考核分管领导优秀名额-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessExcellentNum>> queryPageList(AssessExcellentNum assessExcellentNum,
                                                                 @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                 HttpServletRequest req) {
        QueryWrapper<AssessExcellentNum> queryWrapper = QueryGenerator.initQueryWrapper(assessExcellentNum, req.getParameterMap());
        Page<AssessExcellentNum> page = new Page<AssessExcellentNum>(pageNo, pageSize);
        IPage<AssessExcellentNum> pageList = numService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param assessExcellentNum
     * @return
     */
    @AutoLog(value = "年度考核分管领导优秀名额-添加")
    @ApiOperation(value = "年度考核分管领导优秀名额-添加", notes = "年度考核分管领导优秀名额-添加")
    //@RequiresPermissions("org.jeecg:assess_annual_excellent_num:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessExcellentNum assessExcellentNum) {
        numService.save(assessExcellentNum);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param assessExcellentNum
     * @return
     */
    @AutoLog(value = "年度考核分管领导优秀名额-编辑")
    @ApiOperation(value = "年度考核分管领导优秀名额-编辑", notes = "年度考核分管领导优秀名额-编辑")
    //@RequiresPermissions("org.jeecg:assess_annual_excellent_num:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessExcellentNum assessExcellentNum) {
        numService.updateById(assessExcellentNum);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "年度考核分管领导优秀名额-通过id删除")
    @ApiOperation(value = "年度考核分管领导优秀名额-通过id删除", notes = "年度考核分管领导优秀名额-通过id删除")
    //@RequiresPermissions("org.jeecg:assess_annual_excellent_num:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        numService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "年度考核分管领导优秀名额-通过id查询")
    @ApiOperation(value = "年度考核分管领导优秀名额-通过id查询", notes = "年度考核分管领导优秀名额-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessExcellentNum> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessExcellentNum assessAnnualExcellentNum = numService.getById(id);
        if (assessAnnualExcellentNum == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessAnnualExcellentNum);
    }
}
