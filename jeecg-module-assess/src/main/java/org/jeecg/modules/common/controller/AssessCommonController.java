package org.jeecg.modules.common.controller;

import cn.hutool.core.net.URLDecoder;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.system.vo.SysDepartModel;
import org.jeecg.common.util.SqlInjectionUtil;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.annual.service.IAssessAnnualFillService;
import org.jeecg.modules.annual.service.IAssessAnnualSummaryService;
import org.jeecg.modules.business.service.IAssessBusinessDepartFillService;
import org.jeecg.modules.common.vo.WordTemResVo;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.depart.dto.DepartLeadersDTO;
import org.jeecg.modules.depart.dto.DepartLeadersRegularGradeDTO;
import org.jeecg.modules.regular.mapper.AssessRegularReportItemMapper;
import org.jeecg.modules.regular.service.IAssessRegularReportItemService;
import org.jeecg.modules.regular.service.IAssessRegularReportService;
import org.jeecg.modules.report.service.IAssessReportFillService;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.dto.UserToSelectDTO;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.AssessAnnualSummary;
import org.jeecg.modules.sys.entity.annual.AssessNegativeDepart;
import org.jeecg.modules.sys.mapper.annual.AssessNegativeDepartMapper;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysDictItem;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysDepartMapper;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.user.UserCommonApi;
import org.jeecg.modules.utils.CommonUtils;
import org.jeecg.modules.utils.FolderToZipUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.boot.actuate.health.Health.down;

@Api(tags = "通用api")
@RestController
@RequestMapping("/common")
@Slf4j
public class AssessCommonController {
    @Autowired
    private UserCommonApi userCommonApi;
    @Autowired
    private DepartCommonApi departCommonApi;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private ISysBaseAPI sysBaseAPI;
    @Autowired
    private AssessNegativeDepartMapper negativeDepartMapper;
    @Value("${jeecg.path.upload}")
    private String uploadPath;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private IAssessRegularReportService regularReportService;
    @Autowired
    private IAssessBusinessDepartFillService businessDepartFillService;
    @Autowired
    private IAssessReportFillService reportFillService;
    @Autowired
    private IAssessAnnualFillService annualFillService;
    @Autowired
    private IAssessRegularReportItemService regularReportItemService;
    @Autowired
    private IAssessAnnualSummaryService annualSummaryService;

    @RequestMapping(value = "/queryUserComponentData", method = RequestMethod.GET)
    public Result<IPage<SysUser>> queryUserComponentData(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                         @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                         @RequestParam(name = "departId", required = false) String departId,
                                                         @RequestParam(name = "realname", required = false) String realname,
                                                         @RequestParam(name = "username", required = false) String username,
                                                         @RequestParam(name = "getLeader", required = false, defaultValue = "false") boolean getLeader,
                                                         @RequestParam(name = "id", required = false) String id) {

        String[] arr = new String[]{departId, realname, username, id};
        SqlInjectionUtil.filterContent(arr, SymbolConstant.SINGLE_QUOTATION_MARK);

        IPage<SysUser> pageList = userCommonApi.queryDepartUserPageList(departId, username, realname, pageSize, pageNo, id, getLeader);
        return Result.OK(pageList);
    }

    @GetMapping("/getUserByIds")
    public Result<IPage<LoginUser>> getUserByIds(@RequestParam(name = "userIds") String userIdsStr) {
        String[] ids = userIdsStr.split(",");
        List<LoginUser> userByIds = userCommonApi.getUserByIds(ids);
        IPage<LoginUser> page = new Page<>();
        page.setRecords(userByIds);
        return Result.OK(page);
    }

    @GetMapping("/queryDepartLeaders")
    public Result<List<DepartLeadersDTO>> queryDepartLeaders(@RequestParam String departId) {
        List<DepartLeadersDTO> leaders = departCommonApi.queryDepartLeaders(departId);
        return Result.OK(leaders);
    }

    @GetMapping("/queryLeadersRegularGrade")
    public Result<IPage<DepartLeadersRegularGradeDTO>> queryDepartLeadersRegularGrade(@RequestParam String departId, @RequestParam String year) {
        List<DepartLeadersRegularGradeDTO> leaders = regularReportItemService.queryDepartLeadersRegularGrade(departId, year);
        IPage<DepartLeadersRegularGradeDTO> page = new Page<>();
        page.setRecords(leaders);
        return Result.OK(page);
    }

    @GetMapping("/queryLeaderRegularGrade")
    public Result<DepartLeadersRegularGradeDTO> queryDepartLeaderRegularGrade(@RequestParam String hashId, @RequestParam String year) {
        DepartLeadersRegularGradeDTO leaders = departCommonApi.queryDepartLeaderRegularGrade(hashId, year);
        return Result.OK(leaders);
    }

    @GetMapping("/getDictItemsByCode")
    public Result<List<DictModel>> getDictItemsByCode(@RequestParam String code) {
        List<DictModel> dictItems = sysBaseAPI.getDictItems(code);
        if (code.equals("assess_year")) {
            // 按value降序
            dictItems.sort(Comparator.comparing(DictModel::getValue).reversed());
        }
        return Result.OK(dictItems);
    }

    @GetMapping("/getAnnualDictItemsByDepart")
    public Result<List<SysDictItem>> getAnnualDictItemsByDepart(@RequestParam String departId,
                                                                @RequestParam(required = false, defaultValue = "") String type) {
        List<SysDictItem> items = assessCommonApi.getAnnualSummaryDictItemsByDepart(departId, type);
        return Result.OK(items);
    }

    @GetMapping("/getTotalPeopleNum")
    public Result<Integer> getTotalPeopleNum() {
        Integer totalPeopleNum = userCommonApi.getTotalPeopleNum();
        return Result.OK(totalPeopleNum);
    }

    @Deprecated
    @GetMapping("/lastYear")
    public Result<String> getLastAssessYear() {
        return Result.OK(assessCommonApi.getAssessLastYear());
    }

    @GetMapping("/getAssessingInfo")
    public Result<AssessCurrentAssess> getAssessingInfo(@RequestParam String assess) {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo(assess);
        if (currentAssessInfo == null) {
            return Result.error("当前不在考核期间");
        }
        return Result.OK(currentAssessInfo);
    }

    @GetMapping("/getAssessingInfo4Excel")
    public JSONObject getAssessingInfo4Excel(@RequestParam String assess) {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo(assess);
        if (currentAssessInfo == null) {
            return null;
        }
        List<AssessCurrentAssess> list = new ArrayList<>();
        list.add(currentAssessInfo);
        JSONObject json = new JSONObject();
        json.put("data", list);
        return json;
    }

    @RequestMapping(value = "/queryTreeList", method = RequestMethod.GET)
    public Result<List<SysDepartTreeModel>> queryTreeList(@RequestParam(name = "ids", required = false) String ids, @RequestParam(name = "types", required = false) String types) {
        Result<List<SysDepartTreeModel>> result = new Result<>();
        try {
            if (oConvertUtils.isNotEmpty(ids)) {
                List<SysDepartTreeModel> departList = departCommonApi.queryTreeList(ids);
                result.setResult(departList);
            } else if (oConvertUtils.isNotEmpty(types)) {
                List<String> typeList = Arrays.asList(types.split(","));
                List<SysDepartTreeModel> list = departCommonApi.queryTreeList(typeList);
                result.setResult(list);
            } else {
                List<SysDepartTreeModel> list = departCommonApi.queryTreeList();
                result.setResult(list);
            }
            result.setSuccess(true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    @PutMapping(value = "/rejectedAudit")
    public Result<String> rejectedTheAudit(@RequestBody Map<String, Object> params) {
        String id = (String) params.get("assessId");
        String remark = (String) params.get("rejectedRemark");
        String assess = (String) params.get("assess");
        return assessCommonApi.rejectedAudit(id, remark, assess);
    }

    @GetMapping("/getUserToSelected")
    public Result<?>
    getUserToSelected(@RequestParam String value, @RequestParam(required = false, defaultValue = "0") String depart) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if ("0".equals(depart)) {
            depart = "undefined";
        }
        List<UserToSelectDTO> selectedUser = userCommonApi.getSelectedUser(value, depart);
        return Result.OK(selectedUser);
    }

    @GetMapping("/getAnonymousAccountByAssess")
    public Result<IPage<SysUser>> getAnonymousAccountByAssess(@RequestParam String assess) {
        List<SysUser> users = userCommonApi.getAnonymousAccountByAssess(assess);
        if (users == null || users.isEmpty()) {
            return Result.error("未找到匿名账号");
        }
        IPage<SysUser> page = new Page<>();
        page.setRecords(users);
        return Result.OK(page);
    }

    /**
     * 文件上传统一方法
     *
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/uploadDuty")
    public Result<?> uploadDutyReport(@RequestParam(name = "type", required = false, defaultValue = "annual") String type, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Result<?> result = new Result<>();
        String savePath = "";

        String people = request.getHeader("people");

        // URI解码
        people = URLDecoder.decode(people, StandardCharsets.UTF_8);
        switch (type){
            case "annual":
                savePath = "//annual//duty//";
                break;
            case "report":
                savePath = "//report//duty//";
                break;

        }
        AssessCurrentAssess annual = assessCommonApi.getCurrentAssessInfo(type);
        String year = annual.getCurrentYear();

        Map<String, String> currentUserDepart = departCommonApi.getCurrentUserDepart();
        String departName = currentUserDepart.get("departName");
        String departType = currentUserDepart.get("departTypeText");


        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        // 获取上传文件对象
        MultipartFile file = multipartRequest.getFile("file");
        // 获取文件名
        String fileName = file.getOriginalFilename();
        // 获取文件后缀名
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        // 生成新的文件名
        String newFileName = people + suffixName;

        String turePath = uploadPath + savePath + year + "//" + departType + "//" + departName + "//" + newFileName;



        File saveFile = new File(turePath);
        if (!saveFile.getParentFile().exists()) {
            saveFile.getParentFile().mkdirs();
        }

        // 检查是否存在同名文件（忽略后缀名）
        String baseName = getBaseName(newFileName);
        File directory = saveFile.getParentFile();
        File[] files = directory.listFiles((dir, name) -> getBaseName(name).equals(baseName));
        if (files != null) {
            for (File existingFile : files) {
                existingFile.delete();
            }
        }

        // 如果文件存在，直接覆盖

        // if (saveFile.exists()) {
        //     int i = 1;
        //     while (saveFile.exists()) {
        //         newFileName = people + "(" + i + ")" + suffixName;
        //         turePath = uploadPath + "//annual_duty//" + year + "//" + departType + "//" + departName + "//" + newFileName;
        //         saveFile = new File(turePath);
        //         i++;
        //     }
        // }

        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            inputStream = file.getInputStream();
            outputStream = new FileOutputStream(saveFile);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();

        } catch (Exception e) {
            throw new JeecgBootException("文件上传失败：" + e.getMessage());
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (oConvertUtils.isNotEmpty(turePath)) {
            result.setMessage(savePath + year + "//" + departType + "//" + departName + "//" + newFileName);
            result.setSuccess(true);
        } else {
            result.setMessage("上传失败！");
            result.setSuccess(false);
        }
        return result;
    }

    @PostMapping(value = "/downloadDuty")
    public void downloadDuty(@RequestParam String year, @RequestParam String type, HttpServletResponse response) {
        String var = "";
        switch (type){
            case "annual":
                var = "//annual//duty//";
                break;
            case "report":
                var = "//report//duty//";
                break;
        }
        String zipPath = uploadPath + var + year;
        File file = new File(zipPath);// 创建指定目录和文件名称的文件对象
        try {
            FolderToZipUtil.zip(zipPath, year, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping(value = "/downloadReportFile")
    public void downloadZip(@RequestParam String year, @RequestParam String quarter, HttpServletResponse response) {
        String zipPath = uploadPath + "//regular//person//" + year + "//" + quarter;
        File file = new File(zipPath);// 创建指定目录和文件名称的文件对象
        try {
            FolderToZipUtil.zip(zipPath, year, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/currentUserRoles")
    public Result<List<String>> getCurrentUserRoles() {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<String> userRolesById = userCommonApi.getUserRolesByUserName(sysUser.getUsername());
        return Result.OK(userRolesById);
    }

    @GetMapping("/canFillNegative")
    public Result<Boolean> canFillNegative() {
        Map<String, String> currentUserDepart = departCommonApi.getCurrentUserDepart();
        String departId = currentUserDepart.get("departId");
        List<AssessNegativeDepart> assessNegativeDeparts = negativeDepartMapper.selectList(null);
        StringBuilder departIds = new StringBuilder();

        for (AssessNegativeDepart negativeDepart : assessNegativeDeparts) {
            departIds.append(negativeDepart.getDeparts()).append(",");
        }

        boolean canFillNegative = departIds.toString().contains(departId);
        return Result.OK(canFillNegative);
    }

    @PutMapping("/updateDepartSort")
    public Result<?> updateDepartSort(@RequestBody List<SysDepartModel> params) throws InvocationTargetException, IllegalAccessException {
        List<SysDepartModel> children1 = params.get(0).getChildren();
        List<SysDepartModel> children2 = params.get(1).getChildren();
        List<SysDepartModel> children3 = params.get(2).getChildren();
        List<SysDepartModel> children4 = params.get(3).getChildren();

        List<SysDepart> updateList = new ArrayList<>();

        for (int i = 0; i < children1.size(); i++) {
            SysDepart depart = new SysDepart();
            BeanUtils.copyProperties(depart, children1.get(i));
            depart.setDepartOrder(1000 + i);
            updateList.add(depart);
        }
        for (int i = 0; i < children2.size(); i++) {
            SysDepart depart = new SysDepart();
            BeanUtils.copyProperties(depart, children2.get(i));
            depart.setDepartOrder(2000 + i);
            updateList.add(depart);
        }
        for (int i = 0; i < children3.size(); i++) {
            SysDepart depart = new SysDepart();
            BeanUtils.copyProperties(depart, children3.get(i));
            depart.setDepartOrder(3000 + i);
            updateList.add(depart);
        }
        for (int i = 0; i < children4.size(); i++) {
            SysDepart depart = new SysDepart();
            BeanUtils.copyProperties(depart, children4.get(i));
            depart.setDepartOrder(4000 + i);
            updateList.add(depart);
        }

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        SysDepartMapper departMapperNew = sqlSession.getMapper(SysDepartMapper.class);

        updateList.forEach(departMapperNew::updateById);

        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();

        return Result.OK();
    }

    @GetMapping("/getDepartNameByIds")
    public Result<?> getDepartNameByIds(@RequestParam(name = "ids", required = true) String ids) {
        List<JSONObject> list=sysBaseAPI.queryDepartsByIds(ids);
        List<String> names=new LinkedList<>();
        for (JSONObject jsonObject:
        list) {
            String name=jsonObject.getString("departName");
            names.add(name);
        }
        return Result.OK(names);
    }

    @GetMapping("/getDepartType")
    public Result<?> getDepartType(@RequestParam(name = "departId" , defaultValue = "0") String departId){
        String departTypeById = departCommonApi.getDepartTypeById(departId);
        return Result.OK(departTypeById);
    }

    @PostMapping("/addAnonymousAccount")
    public Result<?> addAnonymousAccount(@RequestBody Map<String, String> params) {
        List<SysUser> sysUsers = userCommonApi.addAnonymousAccount(params.get("assess"), params.get("depart"), params.get("voteType"), Integer.parseInt(params.get("voteNum")));

        return Result.ok(sysUsers);
    }

    @DeleteMapping("/stop")
    public Result<?> stopAssess(@RequestParam(name = "assess") String assess) {
        assessCommonApi.stopAssess(assess);
        switch (assess) {
            case "regular":
                regularReportService.stopAssess();
                break;
            case "business":
                businessDepartFillService.stopAssess();
                break;
            case "report":
                reportFillService.stopAssess();
                break;
            case "annual":
                annualFillService.stopAssess();
                break;
        }

        return Result.ok();
    }

    @DeleteMapping("/revocation")
    public Result<?> revocationAssess(@RequestParam(name = "assess") String assess) {
//        assessCommonApi.stopAssess(assess);
        switch (assess) {
            case "regular":
                regularReportService.revocationAssess();
                break;
            case "business":
                businessDepartFillService.revocationAssess();
                break;
            case "report":
                reportFillService.revocationAssess();
                break;
            case "annual":
                annualFillService.revocationAssess();
                break;
        }

        return Result.ok();
    }

    @DeleteMapping("/stopDem")
    public Result<?> stopDem(@RequestParam(name = "assess") String assess) {

        switch (assess) {
            case "report":
                reportFillService.stopDem();
                break;
            case "annual":
                annualFillService.stopDem();
                break;
        }

        return Result.ok();
    }

    @DeleteMapping("/deleteCurrentUser")
    public Result<?> deleteCurrentUser() {
        userCommonApi.deleteCurrentUser();
        return Result.ok();
    }

    @GetMapping("/getFilterData")
    public Result<?> getFilterData(@RequestParam(name = "type") String type) {
        switch (type) {
            case "depart":
                return Result.ok(departCommonApi.queryAllDepart());
        }

        return Result.ok();
    }

    private String getBaseName(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }

    @GetMapping("/genera")
    public Result<?> genera(HttpServletResponse response) {
        //1.组装数据
        Map<String, Object> params = annualSummaryService.assertMap();
        //2.获取根目录，创建模板文件
        // String path = copyTempFile("tem.docx");
        String path = uploadPath + "/template/" + "tem.docx";
        String fileName = System.currentTimeMillis() + ".docx";
        String tmpPath = uploadPath + "/temp/" + fileName;
        try {
            //3.将模板文件写入到根目录
            //4.编译模板，渲染数据
            XWPFTemplate template = XWPFTemplate.compile(path).render(params);
            //5.写入到指定目录位置
            FileOutputStream fos = new FileOutputStream(tmpPath);
            template.write(fos);
            fos.flush();
            fos.close();
            template.close();
            //6.提供前端下载
            // CommonUtils.downTem(response, tmpPath, fileName);
            // CommonUtils.downTem(response, tmpPath, fileName);
            return Result.ok("/temp/" + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //7.删除临时文件
            // File file = new File(tmpPath);
            // file.delete();
            // File copyFile = new File(path);
            // copyFile.delete();
        }
        return Result.error("生成失败");
    }

    @GetMapping("/downRes")
    public Result<?> downRes(HttpServletResponse response) {
        //1.组装数据
        Map<String, Object> params = annualSummaryService.assertMap2Res();
        //2.获取根目录，创建模板文件
        // String path = copyTempFile("tem.docx");
        String path = uploadPath + "/template/" + "tem2.docx";
        String fileName = System.currentTimeMillis() + ".docx";
        String tmpPath = uploadPath + "/temp/" + fileName;
        try {
            //3.将模板文件写入到根目录
            //4.编译模板，渲染数据
            XWPFTemplate template = XWPFTemplate.compile(path).render(params);
            //5.写入到指定目录位置
            FileOutputStream fos = new FileOutputStream(tmpPath);
            template.write(fos);
            fos.flush();
            fos.close();
            template.close();
            //6.提供前端下载
            // CommonUtils.downTem(response, tmpPath, fileName);
            // CommonUtils.downTem(response, tmpPath, fileName);
            return Result.ok("/temp/" + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //7.删除临时文件
            // File file = new File(tmpPath);
            // file.delete();
            // File copyFile = new File(path);
            // copyFile.delete();
        }
        return Result.error("生成失败");
    }

    @GetMapping("/downloadList")
    public Result<?> downloadList(HttpServletResponse response) {
        //1.组装数据
        WordTemResVo table = annualSummaryService.assertMap2List();
        //2.获取根目录，创建模板文件
        // String path = copyTempFile("tem.docx");
        String path = uploadPath + "/template/" + "tem3.docx";
        String fileName = System.currentTimeMillis() + ".docx";
        String tmpPath = uploadPath + "/temp/" + fileName;
        try {
            //3.将模板文件写入到根目录
            //4.编译模板，渲染数据
            LoopRowTableRenderPolicy hackLoopTableRenderPolicy = new LoopRowTableRenderPolicy();
            Configure config =
                    Configure.builder().bind("negative", hackLoopTableRenderPolicy).bind("accountability", hackLoopTableRenderPolicy).build();
            XWPFTemplate template = XWPFTemplate.compile(path, config).render(table);
            //5.写入到指定目录位置
            FileOutputStream fos = new FileOutputStream(tmpPath);
            template.write(fos);
            fos.flush();
            fos.close();
            template.close();
            //6.提供前端下载
            // CommonUtils.downTem(response, tmpPath, fileName);
            // CommonUtils.downTem(response, tmpPath, fileName);
            return Result.ok("/temp/" + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //7.删除临时文件
            // File file = new File(tmpPath);
            // file.delete();
            // File copyFile = new File(path);
            // copyFile.delete();
        }
        return Result.error("生成失败");
    }

    private String copyTempFile(String templeFilePath) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(templeFilePath);
        String tempFileName = uploadPath + "//template//" + "1.docx";
        File tempFile = new File(tempFileName);
        try {
            FileUtils.copyInputStreamToFile(inputStream, tempFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tempFile.getPath();
    }

    @PostMapping("/downLoadDepartName")
    public void downLoadDepartName(@Value("${server.port}") String port, HttpServletRequest request, HttpServletResponse response) {
        String url = "http://localhost:" + port + "/jmreport/exportAllExcelStream" + "?token=" + TokenUtils.getTokenByRequest(request);

        JSONObject queryParam = new JSONObject();
        Map<String, Object> param = new JSONObject();
        param.put("excelConfigId", "1060463323813490688");
        param.put("queryParam", queryParam);

        assessCommonApi.getExportExcel(url, "标准简称.xlsx", param, response);
    }

    @GetMapping("/allLeaders")
    public Result<?> getAllLeaders() {
        List<SysUser> allLeader = userCommonApi.getAllLeader();
        // 转为map，id为k，name为v
        Map<String, String> map = allLeader.stream().collect(Collectors.toMap(SysUser::getId, SysUser::getRealname));
        return Result.ok(map);
    }

}
