package org.jeecg.modules.annual.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.annual.service.IAssessAnnualExcellentRatioService;
import org.jeecg.modules.annual.vo.AssessAnnualExcellentConfigVO;
import org.jeecg.modules.sys.entity.annual.AssessAnnualExcellentNum;
import org.jeecg.modules.sys.entity.annual.AssessAnnualExcellentRatio;
import org.jeecg.modules.user.UserCommonApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Description: 年度考核优秀比例设置
 * @Author: jeecg-boot
 * @Date: 2024-09-16
 * @Version: V1.0
 */
@Api(tags = "年度考核优秀比例设置")
@RestController
@RequestMapping("/modules/annual/assessAnnualExcellentRatio")
@Slf4j
public class AssessAnnualExcellentRatioController {
    @Autowired
    private IAssessAnnualExcellentRatioService ratioService;
    @Autowired
    private UserCommonApi userCommonApi;

    /**
     * 分页列表查询
     *
     * @param assessAnnualExcellentRatio
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "年度考核优秀比例设置-分页列表查询")
    @ApiOperation(value = "年度考核优秀比例设置-分页列表查询", notes = "年度考核优秀比例设置-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<AssessAnnualExcellentRatio>> queryPageList(AssessAnnualExcellentRatio assessAnnualExcellentRatio,
                                                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                   HttpServletRequest req) {
        QueryWrapper<AssessAnnualExcellentRatio> queryWrapper = QueryGenerator.initQueryWrapper(assessAnnualExcellentRatio, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<AssessAnnualExcellentRatio> page = new Page<AssessAnnualExcellentRatio>(pageNo, pageSize);
        IPage<AssessAnnualExcellentRatio> pageList = ratioService.page(page, queryWrapper);
        pageList.getRecords().forEach(item -> {
            item.setGroupRatio(item.getGroupRatio().multiply(new java.math.BigDecimal(100)));
            item.setBureauRatio(item.getBureauRatio().multiply(new java.math.BigDecimal(100)));
            item.setBasicRatio(item.getBasicRatio().multiply(new java.math.BigDecimal(100)));
            item.setInstitutionRatio(item.getInstitutionRatio().multiply(new java.math.BigDecimal(100)));
        });
        return Result.OK(pageList);
    }

    /**
     * 添加
     * @return
     */
    @AutoLog(value = "年度考核优秀比例设置-添加")
    @ApiOperation(value = "年度考核优秀比例设置-添加", notes = "年度考核优秀比例设置-添加")
    //@RequiresPermissions("org.jeecg:assess_annual_excellent_ratio:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessAnnualExcellentRatio ratio) {
        ratioService.saveMain(ratio);
        if (ratio.getEnable() == 1){
            ratioService.updateEnable(ratio.getId());
        }
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     * @return
     */
    @AutoLog(value = "年度考核优秀比例设置-编辑")
    @ApiOperation(value = "年度考核优秀比例设置-编辑", notes = "年度考核优秀比例设置-编辑")
    //@RequiresPermissions("org.jeecg:assess_annual_excellent_ratio:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessAnnualExcellentRatio assessAnnualExcellentRatio) {
        AssessAnnualExcellentRatio assessAnnualExcellentRatioEntity = ratioService.getById(assessAnnualExcellentRatio.getId());
        if (assessAnnualExcellentRatioEntity == null) {
            return Result.error("未找到对应数据");
        }
        ratioService.updateMain(assessAnnualExcellentRatio);
        return Result.OK("编辑成功!");
    }


    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "年度考核优秀比例设置-通过id查询")
    @ApiOperation(value = "年度考核优秀比例设置-通过id查询", notes = "年度考核优秀比例设置-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<AssessAnnualExcellentRatio> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessAnnualExcellentRatio assessAnnualExcellentRatio = ratioService.getById(id);
        if (assessAnnualExcellentRatio == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(assessAnnualExcellentRatio);

    }

    @GetMapping("/getInitLeaderData")
    public Result<IPage<AssessAnnualExcellentNum>> getInitLeaderData() {
        List<AssessAnnualExcellentNum> initLeaderData = ratioService.getInitLeaderData();


        IPage<AssessAnnualExcellentNum> page = new Page<>();
        page.setRecords(initLeaderData);
        Integer totalPeopleNum = userCommonApi.getTotalPeopleNum();
        page.setTotal(totalPeopleNum);

        return Result.OK(page);
    }

    @PutMapping("/updateEnable")
    public Result<String> updateEnable(@RequestParam(name = "id", required = true) String id) {
        boolean b = ratioService.updateEnable(id);
        if (b) {
            return Result.OK("启用成功！");
        } else {
            return Result.error("启用失败！");
        }
    }

    @GetMapping("/getConfig")
    public Result<AssessAnnualExcellentConfigVO> getConfig() {
        AssessAnnualExcellentConfigVO enableConfig = ratioService.getEnableConfig();
        return Result.OK(enableConfig);
    }

}
