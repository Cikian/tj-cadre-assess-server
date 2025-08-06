package org.jeecg.modules.anonymity.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.user.UserCommonApi;
import org.jeecg.modules.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 平时考核结果
 * @Author: admin
 * @Date: 2024-10-09
 * @Version: V1.0
 */
@Api(tags = "平时考核结果")
@RestController
@RequestMapping("/anonymity")
@Slf4j
public class AnonymityController extends JeecgController<SysUser, ISysUserService> {
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private UserCommonApi userCommonApi;
    @Autowired
    private DepartCommonApi departCommonApi;
    @Autowired
    private AssessCommonApi assessCommonApi;

    /**
     * 分页列表查询
     *
     * @return
     */
    //@AutoLog(value = "平时考核结果-分页列表查询")
    @ApiOperation(value = "匿名账号-分页列表查询", notes = "平时考核结果-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<SysUser>> queryPageList() {
        List<SysUser> allAnonymousAccount = userCommonApi.getAllAnonymousAccount();

        IPage<SysUser> pageList = new Page<>();
        pageList.setRecords(allAnonymousAccount);

        return Result.OK(pageList);

        // Page<SysUser> page = new Page<>(pageNo, pageSize);
        //
        // IPage<SysUser> allAnonymousAccount = userCommonApi.getAllAnonymousAccount(page);
        //
        // return Result.OK(allAnonymousAccount);
    }

    @GetMapping(value = "/list4Excel")
    public JSONObject getAnonymousAccount4Excel(@RequestParam(name = "type", required = false) String type) {
        List<SysUser> allAnonymousAccount = userCommonApi.getAllAnonymousAccount();

        if (allAnonymousAccount != null && !allAnonymousAccount.isEmpty()) {
            if ("annual".equals(type)) {
                allAnonymousAccount = allAnonymousAccount.stream().filter(user -> "annual".equals(user.getAssess())).collect(Collectors.toList());
                for (SysUser user : allAnonymousAccount) {
                    String departType = "";
                    Map<String, String> currentUserDepart = departCommonApi.getCurrentUserDepart();
                    if (currentUserDepart != null && !currentUserDepart.isEmpty()) {
                        departType = currentUserDepart.get("departType");
                    }

                    switch (user.getVoteType()) {
                        case "A":
                            user.setVoteType("分管局领导");
                            break;
                        case "bureau_B":
                            user.setVoteType("总师、二巡、正副处长");
                            break;
                        case "bureau_C":
                            user.setVoteType("其他人员(代表)");
                            break;
                        case "basic_AB":
                            user.setVoteType("党政主要领导");
                            break;
                        case "basic_B":
                            user.setVoteType("领导班子成员");
                            break;
                        case "basic_C":
                            user.setVoteType("干部职工（代表）");
                            break;
                        default:
                            break;
                    }

                }
            }
            if ("report".equals(type)) {
                allAnonymousAccount = allAnonymousAccount.stream().filter(user -> "report".equals(user.getAssess())).collect(Collectors.toList());
                for (SysUser user : allAnonymousAccount) {
                    switch (user.getVoteType()) {
                        case "A":
                            user.setVoteType("领导班子成员");
                            break;
                        case "B":
                            user.setVoteType("干部职工代表");
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        JSONObject json = new JSONObject();
        json.put("data", allAnonymousAccount);

        return json;
    }

    @ApiOperation(value = "匿名账号-删除", notes = "平时考核结果-分页列表查询")
    @DeleteMapping(value = "/delete")
    public Result<?> deleteById(@RequestParam String id) {
        userCommonApi.deleteById(id);
        return Result.OK("删除成功!");
    }

    @PostMapping("/exportPDF")
    public void calculateLeadershipStatisticsExl(@RequestParam String type, @Value("${server.port}") String port, HttpServletRequest request, HttpServletResponse response) {
        String url = "http://localhost:" + port + "/jmreport/exportPdfStream" + "?token=" + TokenUtils.getTokenByRequest(request);
        JSONObject queryParam = new JSONObject();
        queryParam.put("type", type);
        Map<String, Object> param = new JSONObject();
        param.put("excelConfigId", "1025684626334887936");
        param.put("queryParam", queryParam);

        String fileName = "匿名账号.pdf";
        if ("annual".equals(type)) {
            fileName = "年度考核民主测评匿名账号.pdf";
        }
        if ("report".equals(type)) {
            fileName = "一报告两评议民主测评匿名账号.pdf";
        }
        assessCommonApi.getExportPDF(url, fileName, param, response);
    }


}
