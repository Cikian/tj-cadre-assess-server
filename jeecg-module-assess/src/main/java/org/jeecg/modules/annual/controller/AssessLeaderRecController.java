package org.jeecg.modules.annual.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.annual.service.IAssessLeaderRecItemService;
import org.jeecg.modules.annual.service.IAssessLeaderRecService;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.AssessLeaderRec;
import org.jeecg.modules.sys.entity.annual.AssessLeaderRecItem;
import org.jeecg.modules.user.UserCommonApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @Description: 业务考核等次默认比例
 * @Author: jeecg-boot
 * @Date: 2024-10-29
 * @Version: V1.0
 */
@Api(tags = "领导推优")
@RestController
@RequestMapping("/assess/leader/rec")
@Slf4j
public class AssessLeaderRecController extends JeecgController<AssessLeaderRec, IAssessLeaderRecService> {
    @Autowired
    private IAssessLeaderRecService recService;
    @Autowired
    private IAssessLeaderRecItemService recItemService;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private UserCommonApi userCommonApi;

    @GetMapping(value = "/list")
    public Result<IPage<AssessLeaderRec>> queryPageList(AssessLeaderRec AssessLeaderRec,
                                                        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                        HttpServletRequest req) {
        Map<String, String[]> parameterMap = new HashMap<>(req.getParameterMap());

        QueryWrapper<AssessLeaderRec> queryWrapper = QueryGenerator.initQueryWrapper(AssessLeaderRec, parameterMap);
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
        if (parameterMap.get("currentYear") == null) {
            if (currentAssessInfo != null) {
                queryWrapper.eq("current_year", currentAssessInfo.getCurrentYear());
            }
        }

        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();


        List<String> roles = Arrays.asList("gbc_cz", "gbc_fcz");
        boolean b2 = userCommonApi.hasRole(sysUser.getUsername(), roles);
        if (b2) {
            queryWrapper.ne("status", "1");
        }

        Page<AssessLeaderRec> page = new Page<>(pageNo, pageSize);
        IPage<AssessLeaderRec> pageList = recService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @GetMapping(value = "/init")
    public Result<?> init() {
        recService.initRec();
        return Result.OK("初始化成功！");
    }

    @GetMapping("/flush")
    public Result<?> flush() {
        recService.flush();
        return Result.OK("刷新成功！");
    }

    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AssessLeaderRec AssessLeaderRec) {
        recService.save(AssessLeaderRec);
        return Result.OK("添加成功！");
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AssessLeaderRecItem item) {
        recItemService.updateById(item);
        return Result.OK("编辑成功!");
    }

    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        recService.removeById(id);
        return Result.OK("删除成功!");
    }

    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.recService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    @GetMapping(value = "/queryById")
    public Result<AssessLeaderRec> queryById(@RequestParam(name = "id", required = true) String id) {
        AssessLeaderRec AssessLeaderRec = recService.getById(id);
        if (AssessLeaderRec == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(AssessLeaderRec);
    }

    @PutMapping(value = "/pushDown")
    public Result<?> pushDown(@RequestParam(name = "id", required = true) String id) {
        recService.pushDown(id);
        return Result.OK("推送成功!");
    }

    @GetMapping(value = "/getByLeader")
    public Result<?> pushDown() {
        AssessLeaderRec byLeaderId = recService.getByLeaderId();

        if (byLeaderId == null) {
            return Result.error("未找到对应数据");
        }

        return Result.OK(byLeaderId);
    }

    @GetMapping("/reject")
    public Result<?> reject(@RequestParam(name = "leader", required = true) String leader) {
        recService.reject(leader);
        return Result.OK("退回成功!");
    }


}
