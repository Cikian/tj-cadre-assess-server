package org.jeecg.modules.sys.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.enums.CommonEnum;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.CommissionItem;
import org.jeecg.modules.sys.entity.annual.*;
import org.jeecg.modules.sys.entity.business.AssessBusinessCommend;
import org.jeecg.modules.sys.entity.business.AssessBusinessDenounce;
import org.jeecg.modules.sys.entity.business.AssessBusinessDepartFill;
import org.jeecg.modules.sys.entity.business.AssessLeaderDepartConfig;
import org.jeecg.modules.sys.entity.report.AssessReportFill;
import org.jeecg.modules.sys.mapper.AssessCurrentAssessMapper;
import org.jeecg.modules.sys.mapper.SysCommonMapper;
import org.jeecg.modules.sys.mapper.annual.*;
import org.jeecg.modules.sys.mapper.business.AssessBusinessCommendMapper;
import org.jeecg.modules.sys.mapper.business.AssessBusinessDenounceMapper;
import org.jeecg.modules.sys.mapper.business.AssessBusinessDepartFillMapper;
import org.jeecg.modules.sys.mapper.business.AssessLeaderDepartConfigMapper;
import org.jeecg.modules.sys.mapper.report.AssessReportFillMapper;
import org.jeecg.modules.sys.vo.DemocraticFillListVO;
import org.jeecg.modules.system.entity.SysCategory;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysDictItem;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysDepartMapper;
import org.jeecg.modules.user.UserCommonApi;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;

@Service
public class AssessCommonApiImpl implements AssessCommonApi {
    @Autowired
    private SysCommonMapper sysCommonMapper;
    @Autowired
    private ISysBaseAPI sysBaseAPI;
    @Autowired
    private SysDepartMapper departMapper;
    @Autowired
    private AssessCurrentAssessMapper currentAssessMapper;
    @Autowired
    private AssessBusinessDepartFillMapper businessFillMapper;
    @Autowired
    private AssessReportFillMapper reportFillMapper;
    @Autowired
    private AssessAnnualFillMapper annualFillMapper;
    @Value("${jeecg.path.upload}")
    private String uploadPath;
    @Autowired
    private AssessBusinessCommendMapper businessCommendMapper;
    @Autowired
    private AssessBusinessDenounceMapper businessDenounceMapper;
    @Autowired
    private AssessAnnualAccountabilityMapper annualAccountabilityMapper;
    @Autowired
    private AssessAnnualVacationMapper annualVacationMapper;
    @Autowired
    private AssessAnnualExcellentNumMapper excellentNumMapper;
    @Autowired
    private AssessLeaderDepartConfigMapper leaderDepartConfigMapper;
    @Autowired
    private AssessLeaderConfigMapper leaderConfigMapper;
    @Autowired
    private AssessAssistConfigMapper assistConfigMapper;
    @Autowired
    private DepartCommonApi departCommonApi;

    // 定义评估类型枚举
    private enum AssessType {
        annual, business, regular, report
    }


    /**
     * @param code:需手动拼接模糊查询条件%、_，如：B03%
     * @return
     */
    @Override
    public List<SysCategory> getCategoryLeafByParentCode(String code) {
        return sysCommonMapper.getAllLeafByParentCode(code);
    }

    @Override
    public List<SysDictItem> getAnnualSummaryDictItemsByDepart(String departId, String type) {
        List<JSONObject> jsonObjects = sysBaseAPI.queryDepartsByIds(departId);
        String departType = (String) jsonObjects.get(0).get("departType");

        String dictCode;
        if ("3".equals(departType)) dictCode = "public_institution_annual_level";
        else dictCode = "civil_servant_annual_level";

        if (type != null && "group".equals(type)) {
            dictCode = "leading_group_annual_level";
        }

        return sysCommonMapper.getDictItemsByCode(dictCode);
    }

    @Override
    public void getExportExcel(String url, String filename, Map<String, Object> param, HttpServletResponse response) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json;charset=UTF-8");

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(param, headers);

        try {
            Resource resource = restTemplate.postForObject(url, entity, Resource.class);
            InputStream inputStream = resource.getInputStream();
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

            OutputStream os = response.getOutputStream();
            os.write(data);
            os.flush();
            os.close();
            inputStream.close();

        } catch (NullPointerException ne) {
            throw new JeecgBootException("当前无匿名账号！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getExportPDF(String url, String filename, Map<String, Object> param, HttpServletResponse response) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json;charset=UTF-8");

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(param, headers);

        try {
            Resource resource = restTemplate.postForObject(url, entity, Resource.class);
            InputStream inputStream = null;
            if (resource != null) {
                inputStream = resource.getInputStream();
            }
            byte[] data = null;
            if (inputStream != null) {
                data = new byte[inputStream.available()];
            }
            if (inputStream != null) {
                inputStream.read(data);
            }

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

            OutputStream os = response.getOutputStream();
            if (data != null) {
                os.write(data);
            }
            os.flush();
            os.close();
            if (inputStream != null) {
                inputStream.close();
            }

        } catch (RestClientException | IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void getExportZip(String url, String filename, String entryEndName, List<Map<String, Object>> paramList, Map<String, String> folders, HttpServletResponse response) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json;charset=UTF-8");

        RestTemplate restTemplate = new RestTemplate();
        Set<String> addedFolders = new HashSet<>();

        try {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

            OutputStream os = response.getOutputStream();
            ZipOutputStream zos = new ZipOutputStream(os);

            for (Map<String, Object> param : paramList) {
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(param, headers);

                try {
                    Resource resource = restTemplate.postForObject(url, entity, Resource.class);
                    if (resource != null) {
                        try (InputStream inputStream = resource.getInputStream()) {
                            byte[] data = new byte[inputStream.available()];
                            int len;
                            Object o = param.get("queryParam");
                            JSONObject queryParam = (JSONObject) o;
                            String departId = queryParam.getString("departId");
                            String currentYear = queryParam.getString("currentYear");

                            SysDepart depart = departMapper.selectById(departId);

                            String folderName = null;
                            if (folders != null) {
                                folderName = folders.get(depart.getDepartType());

                                if (folderName != null && !addedFolders.contains(folderName)) {
                                    // 创建文件夹条目
                                    ZipEntry folderEntry = new ZipEntry(folderName + "/");
                                    zos.putNextEntry(folderEntry);
                                    zos.closeEntry();
                                    addedFolders.add(folderName);
                                }
                            }


                            // 创建一个新的 ZIP 条目
                            String entryName = (folderName != null ? folderName + "/" : "") + depart.getDepartName() + currentYear + entryEndName;
                            ZipEntry zipEntry = new ZipEntry(entryName);
                            zos.putNextEntry(zipEntry);

                            while ((len = inputStream.read(data)) > 0) {
                                zos.write(data, 0, len);
                            }
                            zos.closeEntry();
                        }
                    }
                } catch (RestClientException | IOException e) {
                    e.printStackTrace();
                }
            }

            // 关闭 ZIP 输出流
            zos.flush();
            zos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void getExportZipWithExcel(String url, String filename, List<String> entryEndNames, List<Map<String, Object>> paramList, HttpServletResponse response) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json;charset=UTF-8");

        RestTemplate restTemplate = new RestTemplate();
        Set<String> addedFolders = new HashSet<>();

        try {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

            OutputStream os = response.getOutputStream();
            ZipOutputStream zos = new ZipOutputStream(os);

            for (int i = 0; i < paramList.size(); i++) {
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(paramList.get(i), headers);

                try {
                    Resource resource = restTemplate.postForObject(url, entity, Resource.class);
                    if (resource != null) {
                        try (InputStream inputStream = resource.getInputStream()) {
                            byte[] data = new byte[inputStream.available()];
                            int len;

                            // 创建一个新的 ZIP 条目
                            String entryName = entryEndNames.get(i);
                            ZipEntry zipEntry = new ZipEntry(entryName);
                            zos.putNextEntry(zipEntry);

                            while ((len = inputStream.read(data)) > 0) {
                                zos.write(data, 0, len);
                            }
                            zos.closeEntry();
                        }
                    }
                } catch (RestClientException | IOException e) {
                    e.printStackTrace();
                }
            }

            // 关闭 ZIP 输出流
            zos.flush();
            zos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void getExportDemZipWithExcel(String url, String filename, List<String> entryEndNames, List<Map<String, Object>> paramList, Map<String, String> leaderMap, HttpServletResponse response) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json;charset=UTF-8");

        RestTemplate restTemplate = new RestTemplate();
        Set<String> addedFolders = new HashSet<>();

        try {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

            OutputStream os = response.getOutputStream();
            ZipOutputStream zos = new ZipOutputStream(os);

            for (int i = 0; i < paramList.size(); i++) {
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(paramList.get(i), headers);

                try {
                    Resource resource = restTemplate.postForObject(url, entity, Resource.class);
                    if (resource != null) {
                        try (InputStream inputStream = resource.getInputStream()) {
                            byte[] data = new byte[inputStream.available()];
                            int len;

                            // 创建一个新的 ZIP 条目
                            String entryName = entryEndNames.get(i);
                            if (entryName.contains("分项")) {
                                Map<String, Object> stringObjectMap = paramList.get(i);
                                Map<String, Object> queryParam = (Map<String, Object>) stringObjectMap.get("queryParam");
                                String leader = queryParam.get("leader").toString();
                                if (leader.equals("0")) entryName = "不分考核单元/分项成绩/" + entryName;
                                else entryName = leaderMap.get(leader) + "/分项成绩/" + entryName;
                            } else {
                                Map<String, Object> stringObjectMap = paramList.get(i);
                                Map<String, Object> queryParam = (Map<String, Object>) stringObjectMap.get("queryParam");
                                String leader = queryParam.get("leader").toString();
                                if (leader.equals("0")) entryName = "不分考核单元/成绩/" + entryName;
                                else entryName = leaderMap.get(leader) + "/成绩/" + entryName;
                            }
                            ZipEntry zipEntry = new ZipEntry(entryName);
                            zos.putNextEntry(zipEntry);

                            while ((len = inputStream.read(data)) > 0) {
                                zos.write(data, 0, len);
                            }
                            zos.closeEntry();
                        }
                    }
                } catch (RestClientException | IOException e) {
                    e.printStackTrace();
                }
            }

            // 关闭 ZIP 输出流
            zos.flush();
            zos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void getExportRecExlZipWithExcel(String url, String filename, List<String> entryEndNames, List<Map<String, Object>> paramList, Map<String, String> leaderMap, HttpServletResponse response) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json;charset=UTF-8");

        RestTemplate restTemplate = new RestTemplate();
        Set<String> addedFolders = new HashSet<>();

        try {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

            OutputStream os = response.getOutputStream();
            ZipOutputStream zos = new ZipOutputStream(os);

            for (int i = 0; i < paramList.size(); i++) {
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(paramList.get(i), headers);

                try {
                    Resource resource = restTemplate.postForObject(url, entity, Resource.class);
                    if (resource != null) {
                        try (InputStream inputStream = resource.getInputStream()) {
                            byte[] data = new byte[inputStream.available()];
                            int len;

                            // 创建一个新的 ZIP 条目
                            String entryName = entryEndNames.get(i);
                            Map<String, Object> stringObjectMap = paramList.get(i);
                            Map<String, Object> queryParam = (Map<String, Object>) stringObjectMap.get("queryParam");
                            String leader = queryParam.get("leader").toString();
                            entryName = leaderMap.get(leader) + "/" + entryName;

                            ZipEntry zipEntry = new ZipEntry(entryName);
                            zos.putNextEntry(zipEntry);

                            while ((len = inputStream.read(data)) > 0) {
                                zos.write(data, 0, len);
                            }
                            zos.closeEntry();
                        }
                    }
                } catch (RestClientException | IOException e) {
                    e.printStackTrace();
                }
            }

            // 关闭 ZIP 输出流
            zos.flush();
            zos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void exportZipWithExcel(String url, String filename, List<Map<String, Object>> paramList, HttpServletResponse response) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json;charset=UTF-8");
        RestTemplate restTemplate = new RestTemplate();
        try {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            OutputStream os = response.getOutputStream();
            ZipOutputStream zos = new ZipOutputStream(os);
            for (int i = 0; i < paramList.size(); i++) {
                Map<String, Object> map = paramList.get(i);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
                try {
                    Resource resource = restTemplate.postForObject(url, entity, Resource.class);
                    if (resource != null) {
                        try (InputStream inputStream = resource.getInputStream()) {
                            byte[] data = new byte[inputStream.available()];
                            int len;
                            JSONObject jsonObject = (JSONObject) map.get("queryParam");
                            String departId = jsonObject.get("depart").toString();
                            String entryName = departCommonApi.queryDepartById(departId).getDepartName();
                            // 创建一个新的ZIP条目
                            ZipEntry zipEntry = new ZipEntry(entryName + ".xlsx");
                            zos.putNextEntry(zipEntry);
                            while ((len = inputStream.read(data)) > 0) {
                                zos.write(data, 0, len);
                            }
                            zos.closeEntry();
                        }
                    }
                } catch (RestClientException | IOException e) {
                    e.printStackTrace();
                }
            }
            // 关闭 ZIP 输出流
            zos.flush();
            zos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getMergePdf(String url, String filename, List<Map<String, Object>> paramList, HttpServletResponse response) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json;charset=UTF-8");
        RestTemplate restTemplate = new RestTemplate();
        List<byte[]> files = new ArrayList<>();

        try {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

            for (Map<String, Object> param : paramList) {
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(param, headers);
                try {
                    Resource resource = restTemplate.postForObject(url, entity, Resource.class);
                    if (resource != null) {
                        InputStream inputStream = resource.getInputStream();
                        byte[] data = new byte[inputStream.available()];
                        inputStream.read(data);

                        files.add(data);
                        inputStream.close();
                    }
                } catch (RestClientException | IOException e) {
                    e.printStackTrace();
                }
            }

            byte[] pdfs = mergePdfFiles(files);

            OutputStream os = response.getOutputStream();
            os.write(pdfs);
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isCadre() {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<String> rolesByUsername = sysBaseAPI.getRolesByUsername(sysUser.getUsername());
        return rolesByUsername.contains("department_cadre_admin") || rolesByUsername.contains("department_human_admin");
    }

    @Override
    public String getAssessLastYear() {
        return sysCommonMapper.getAssessLastYear();
    }

    @Override
    public void updateAssessCurrentYear(AssessCurrentAssess assessCurrentAssess) {
        LambdaQueryWrapper<AssessCurrentAssess> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessCurrentAssess::getAssess, assessCurrentAssess.getAssess());
        AssessCurrentAssess currentAssess = currentAssessMapper.selectOne(lqw);
        if (currentAssess == null) {
            currentAssessMapper.insert(assessCurrentAssess);
        } else {
            currentAssess.setCurrentYear(assessCurrentAssess.getCurrentYear());
            currentAssess.setAssessName(assessCurrentAssess.getAssessName());
            currentAssess.setStartDate(assessCurrentAssess.getStartDate());
            currentAssess.setDeadline(assessCurrentAssess.getDeadline());
            if (assessCurrentAssess.getAssess().equals(CommonEnum.REGULAR.getValue())) {
                currentAssess.setQuarter(assessCurrentAssess.getQuarter());
            }
            currentAssess.setAssessing(assessCurrentAssess.isAssessing());
            currentAssessMapper.updateById(currentAssess);
        }

    }

    @Override
    public void revocationCurrentYear(AssessCurrentAssess assessCurrentAssess) {

        String assess = assessCurrentAssess.getAssess();

        String currentYear = assessCurrentAssess.getCurrentYear();
        int year = Integer.parseInt(currentYear);
        int quarter = 0;
        if ("regular".equals(assess)) {
            String currentQuarter = assessCurrentAssess.getQuarter();
            quarter = Integer.parseInt(currentQuarter);
            // 减1
            quarter--;
            if (quarter == 0) {
                year--;
                quarter = 4;
            }
        } else {
            year--;
        }
        // 转为String
        AssessCurrentAssess currentAssess = new AssessCurrentAssess();
        String lastYear = String.valueOf(year);
        if ("regular".equals(assess)) {
            String lastQuarter = String.valueOf(quarter);
            currentAssess.setQuarter(lastQuarter);
        }
        currentAssess.setCurrentYear(lastYear);
        currentAssess.setAssessName(null);
        currentAssess.setStartDate(null);
        currentAssess.setDeadline(null);
        currentAssess.setAssessing(false);
        currentAssess.setAssess(assess);

        currentAssessMapper.deleteById(assessCurrentAssess.getId());
        currentAssessMapper.insert(currentAssess);
    }

    @Override
    public AssessCurrentAssess getCurrentAssessInfo(String assess) {
        CommonEnum commonEnum;
        try {
            commonEnum = CommonEnum.valueOf(assess.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
        LambdaQueryWrapper<AssessCurrentAssess> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessCurrentAssess::getAssess, commonEnum.getValue());
        return currentAssessMapper.selectOne(lqw);
    }

    private byte[] mergePdfFiles(List<byte[]> files) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Document document = new Document();// 创建一个新的PDF
        byte[] pdfs = new byte[0];
        try {
            PdfCopy copy = new PdfCopy(document, bos);
            document.open();
            for (byte[] file : files) {// 取出单个PDF的数据
                PdfReader reader = new PdfReader(new ByteArrayInputStream(file));
                int pageTotal = reader.getNumberOfPages();
//                logger.info("pdf的页码数是 ==> {}",pageTotal);
                for (int pageNo = 1; pageNo <= pageTotal; pageNo++) {
                    document.newPage();
                    PdfImportedPage page = copy.getImportedPage(reader, pageNo);
                    copy.addPage(page);
                }
                reader.close();
            }
            document.close();
            pdfs = bos.toByteArray();
            bos.close();
            copy.close();
        } catch (DocumentException | IOException e) {
            throw new JeecgBootException("合并PDF出错：" + e);
        }
        return pdfs;
    }

    @Override
    public Result<String> rejectedAudit(String assessId, String remark, String assess) {
        switch (assess) {
            case "regular":
                return null;
            case "business":
                AssessBusinessDepartFill var1 = businessFillMapper.selectById(assessId);
                if (var1.getStatus() != 2) {
                    return Result.error("当前考核状态异常，请稍后重试！");
                } else {
                    LambdaUpdateWrapper<AssessBusinessDepartFill> lqw = new LambdaUpdateWrapper<>();
                    lqw.eq(AssessBusinessDepartFill::getId, assessId);
                    lqw.set(AssessBusinessDepartFill::getStatus, 4);
                    lqw.set(AssessBusinessDepartFill::getRemark, remark);
                    int update = businessFillMapper.update(null, lqw);
                    if (update > 0) return Result.ok("退回成功");
                }
                break;
            case "report":
                AssessReportFill var2 = reportFillMapper.selectById(assessId);
                if (!"2".equals(var2.getStatus()) && !"3".equals(var2.getStatus())) {
                    return Result.error("当前考核状态异常，请稍后重试！");
                } else {
                    LambdaUpdateWrapper<AssessReportFill> lqw = new LambdaUpdateWrapper<>();
                    lqw.eq(AssessReportFill::getId, assessId);
                    lqw.set(AssessReportFill::getStatus, 4);
                    lqw.set(AssessReportFill::getRemark, remark);
                    int update = reportFillMapper.update(null, lqw);
                    if (update > 0) return Result.ok("退回成功");
                }
                break;
            case "annual":
                AssessAnnualFill var3 = annualFillMapper.selectById(assessId);
                if (var3.getStatus() != 2 && var3.getStatus() != 3) {
                    return Result.error("当前考核状态异常，请稍后重试！");
                } else {
                    LambdaUpdateWrapper<AssessAnnualFill> lqw = new LambdaUpdateWrapper<>();
                    lqw.eq(AssessAnnualFill::getId, assessId);
                    lqw.set(AssessAnnualFill::getStatus, 4);
                    lqw.set(AssessAnnualFill::getRemark, remark);
                    int update = annualFillMapper.update(null, lqw);
                    if (update > 0) return Result.ok("退回成功");
                }
                break;
        }

        return Result.error("系统异常，请稍后重试！");
    }

    @Override
    public <T> List<T> importExcel(HttpServletRequest request, Class<T> clazz, String fileName) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();

        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        List<T> itemList = new ArrayList<>(); // 用于存储解析后的数据
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(0); // 设置标题行数（如果有标题行）
            params.setHeadRows(1); // 设置表头行数

            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            // 获取文件后缀名
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 生成新的文件名
            String[] split = fileName.split("_");
            String type = split[0];
            String year = split[1];
            String quarter = "";
            String path = "";
            if ("regular".equals(type)) {
                quarter = split[2];
                path = uploadPath + "//" + type + "//person//" + year + "//" + quarter;
            } else {
                path = uploadPath + "//" + type + "//person//" + year;
            }


            // 如果上传目录不存在，则创建目录
            File uploadDir = new File(path);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            File saveFile = new File(path, fileName + suffix);

            try {
                inputStream = file.getInputStream();
                outputStream = new FileOutputStream(saveFile);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.flush();


                // 使用 AutoPoi 读取 Excel 文件，映射到 AssessRegularReportItem 类
                List<T> list = ExcelImportUtil.importExcel(file.getInputStream(), clazz, params);
                for (T item : list) {
                    itemList.add(item); // 将每个条目加入到列表中
                }
                return itemList; // 返回成功消息和数据
            } catch (Exception e) {
                throw new JeecgBootException("文件读取失败：" + e.getMessage());
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
        }
        return null;
    }

    @Override
    public List<CommissionItem> getCommissionItems() {

        List<AssessCurrentAssess> assessList = currentAssessMapper.selectList(null);
        List<CommissionItem> commissionItems = new ArrayList<>();
        Date now = new Date();

        for (AssessCurrentAssess assess : assessList) {
            if (assess.isAssessing()) {
                AssessType assessType = AssessType.valueOf(assess.getAssess());
                Date deadline = assess.getDeadline();

                handleAssessType(assessType, now, deadline, commissionItems, assess.getCurrentYear());
            }
        }

        List<CommissionItem> list = Arrays.asList(commissionItems.toArray(new CommissionItem[0]));

        // 通过list中对象的status降序排序
        list.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getStatus())));

        return list;
    }

    @Override
    public void stopAssess(String assess) {
        switch (assess) {
            case "regular":
                break;
            case "business":
                break;
            case "report":
                break;
            case "annual":
                break;
        }
    }

    @Override
    public AssessLeaderDepartConfig getAssessUnitByDepart(String departId) {
        List<AssessLeaderDepartConfig> configs = leaderDepartConfigMapper.selectList(null);
        for (AssessLeaderDepartConfig config : configs) {
            if (config.getDepartId().contains(departId)) {
                return config;
            }
        }

        return null;
    }

    @Override
    public AssessLeaderDepartConfig getAssessUnitByLeader(String leader) {
        LambdaQueryWrapper<AssessLeaderDepartConfig> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessLeaderDepartConfig::getLeaderId, leader);
        List<AssessLeaderDepartConfig> configs = leaderDepartConfigMapper.selectList(lqw);
        if (configs != null && !configs.isEmpty()) {
            return configs.get(0);
        }

        return null;
    }

    @Override
    public Map<String, List<?>> getSpecialPeople() {
        // 查询分管外考核配置
        List<AssessLeaderConfig> leaderConfigs = leaderConfigMapper.selectList(null);
        // 查询协管
        List<AssessAssistConfig> assistantConfigs = assistConfigMapper.selectList(null);

        // 将两个集合中的hashId取出，存入集合
        List<String> leaderHashIds = leaderConfigs.stream().map(AssessLeaderConfig::getHashId).collect(Collectors.toList());
        List<String> assistHashIds = assistantConfigs.stream().map(AssessAssistConfig::getHashId).collect(Collectors.toList());

        // 合并
        List<String> allHashIds = new ArrayList<>();
        allHashIds.addAll(leaderHashIds);
        allHashIds.addAll(assistHashIds);

        Map<String, List<?>> map = new HashMap<>();
        map.put("leader", leaderConfigs);
        map.put("assist", assistantConfigs);
        map.put("allHashIds", allHashIds);


        return map;
    }

    @Override
    public Map<String, String> getDictMapByCode(String code) {
        List<DictModel> dictItems = sysBaseAPI.getDictItems(code);
        if (dictItems != null && !dictItems.isEmpty()) {
            // 转为map，value为k，text为v
            return dictItems.stream().collect(Collectors.toMap(DictModel::getValue, DictModel::getText));
        }
        return Collections.emptyMap();
    }

    @Override
    public List<AssessCurrentAssess> getAssessing() {
        LambdaQueryWrapper<AssessCurrentAssess> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessCurrentAssess::isAssessing, true);
        return currentAssessMapper.selectList(lqw);
    }


    @Override
    public JSONObject getAssessingStatus(SysUser user, List<AssessCurrentAssess> assessingList, JSONObject res) {
        int count = res.get("count") == null ? 0 : (int) res.get("count");
        boolean assessing = res.getBoolean("status");
        String userId = user.getId();

        // 获取领导分管处室配置
        AssessLeaderDepartConfig config = getAssessUnitByLeader(userId);
        if (config != null) {
            List<String> departIds = Arrays.asList(config.getDepartId().split(","));

            for (AssessCurrentAssess assess : assessingList) {
                if ("annual".equals(assess.getAssess())) {
                    // 判断是否需要进行民主测评
                    List<DemocraticFillListVO> anonymousList = this.getAnonymousList(user);
                    if (anonymousList != null && !anonymousList.isEmpty()) {
                        assessing = true;
                        count++;
                    }

                    // 判断是否需要进行推优
                    List<AssessLeaderRecItem> leaderRecommendList = this.getLeaderRecommendList(user.getId(), assess.getCurrentYear());
                    if (leaderRecommendList != null && !leaderRecommendList.isEmpty()) {
                        assessing = true;
                        count++;
                    }
                }
            }
        }

        res.put("status", assessing);
        res.put("count", count);

        return res;
    }

    @Autowired
    @Lazy
    private UserCommonApi userCommonApi;
    @Autowired
    private AssessDemocraticEvaluationSummaryMapper summaryMapper;
    @Autowired
    private AssessDemocraticEvaluationMapper democraticEvaluationMapper;
    @Autowired
    private AssessAnnualSummaryMapper annualSummaryMapper;

    private List<DemocraticFillListVO> getAnonymousList(SysUser sysUser) {
        // 获取当前登录用户的部门id
        List<String> roles = userCommonApi.getUserRolesByUserName(sysUser.getUsername());

        String departIds = sysUser.getDepartIds();
        if (departIds == null) {
            return Collections.emptyList();
        }
        List<String> departIdList = Arrays.asList(departIds.split(","));

        AssessCurrentAssess annual = this.getCurrentAssessInfo("annual");
        if (annual == null || !annual.isAssessing()) {
            return Collections.emptyList();
        }

        // 查询当前用户所在部门的所有民主测评填报
        LambdaQueryWrapper<AssessDemocraticEvaluationSummary> qw = new LambdaQueryWrapper<>();

        if (roles.contains("director_leader")) {
            List<String> specialPeople = new ArrayList<>();

            // LambdaQueryWrapper<AssessAssistConfig> configQw1 = new LambdaQueryWrapper<>();
            // configQw1.like(AssessAssistConfig::getLeader, sysUser.getId());
            List<AssessAssistConfig> assistConfig = assistConfigMapper.selectList(null);
            for (AssessAssistConfig config : assistConfig) {
                if (!specialPeople.contains(config.getHashId())) {
                    specialPeople.add(config.getHashId());
                }
            }

            // LambdaQueryWrapper<AssessLeaderConfig> configQw2 = new LambdaQueryWrapper<>();
            // configQw2.eq(AssessLeaderConfig::getLeader, sysUser.getId());
            List<AssessLeaderConfig> leaderConfig = leaderConfigMapper.selectList(null);
            for (AssessLeaderConfig config : leaderConfig) {
                if (!specialPeople.contains(config.getHashId())) {
                    specialPeople.add(config.getHashId());
                }
            }


            if (!specialPeople.isEmpty()) {
                qw.notIn(AssessDemocraticEvaluationSummary::getAppraisee, specialPeople);
            }
        }

        qw.in(AssessDemocraticEvaluationSummary::getDepart, departIdList);
        qw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, annual.getCurrentYear());
        qw.orderByDesc(AssessDemocraticEvaluationSummary::getType);
        List<AssessDemocraticEvaluationSummary> summaries = summaryMapper.selectList(qw);

        qw.clear();

        if (departIdList.contains("JG")) {
            // 获取当前账号所在考核单元
            AssessLeaderDepartConfig unit = null;
            if (roles.contains("director_leader")) {
                unit = this.getAssessUnitByLeader(sysUser.getId());
            } else {
                unit = this.getAssessUnitByDepart(sysUser.getDepart());
            }
            String unitDeparts = unit.getDepartId();
            List<String> unitDepartList = Arrays.asList(unitDeparts.split(","));

            // departIdList中所有元素前面加上JG
            List<String> departIdListWithJG = unitDepartList.stream().map(departId -> "JG" + departId).collect(Collectors.toList());
            qw.in(AssessDemocraticEvaluationSummary::getDepart, departIdListWithJG);
            qw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, annual.getCurrentYear());
            qw.orderByDesc(AssessDemocraticEvaluationSummary::getType);
            List<AssessDemocraticEvaluationSummary> summariesWithJG = summaryMapper.selectList(qw);
            summaries.addAll(summariesWithJG);
            qw.clear();
        }

        if (!roles.contains("department_cadre_admin")) {
            // 局领导不测评基层副职
            if (roles.contains("director_leader")) {
                // 过滤：去除departType不为bureau的并且type为22的记录
                summaries = summaries.stream()
                        .filter(summary -> !summary.getType().equals("31"))
                        .collect(Collectors.toList());


                LambdaQueryWrapper<AssessLeaderConfig> lqw = new LambdaQueryWrapper<>();
                lqw.eq(AssessLeaderConfig::getLeader, sysUser.getId());
                List<AssessLeaderConfig> leaderConfigs = leaderConfigMapper.selectList(lqw);

                if (leaderConfigs != null && !leaderConfigs.isEmpty()) {
                    List<String> personHashId = leaderConfigs.stream().map(AssessLeaderConfig::getHashId).collect(Collectors.toList());

                    qw.in(AssessDemocraticEvaluationSummary::getAppraisee, personHashId);
                    qw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, annual.getCurrentYear());
                    qw.orderByDesc(AssessDemocraticEvaluationSummary::getType);
                    List<AssessDemocraticEvaluationSummary> s = summaryMapper.selectList(qw);
                    qw.clear();

                    if (s != null && !s.isEmpty()) {
                        summaries.addAll(s);
                    }
                }

                LambdaQueryWrapper<AssessAssistConfig> lqw2 = new LambdaQueryWrapper<>();
                lqw2.like(AssessAssistConfig::getLeader, sysUser.getId());
                List<AssessAssistConfig> assistConfigs = assistConfigMapper.selectList(lqw2);

                if (assistConfigs != null && !assistConfigs.isEmpty()) {
                    List<String> personHashId = assistConfigs.stream().map(AssessAssistConfig::getHashId).collect(Collectors.toList());
                    qw.in(AssessDemocraticEvaluationSummary::getAppraisee, personHashId);
                    qw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, annual.getCurrentYear());
                    qw.orderByDesc(AssessDemocraticEvaluationSummary::getType);
                    List<AssessDemocraticEvaluationSummary> s = summaryMapper.selectList(qw);
                    qw.clear();

                    if (s != null && !s.isEmpty()) {
                        summaries.addAll(s);
                    }
                }

                if (!summaries.isEmpty()) {
                    List<String> dSummaryIds = summaries.stream().map(AssessDemocraticEvaluationSummary::getId).collect(Collectors.toList());

                    LambdaQueryWrapper<AssessDemocraticEvaluation> lqw3 = new LambdaQueryWrapper<>();
                    lqw3.in(AssessDemocraticEvaluation::getEvaluationSummaryId, dSummaryIds);
                    lqw3.eq(AssessDemocraticEvaluation::getCreateBy, sysUser.getUsername());
                    List<AssessDemocraticEvaluation> assessDemocraticEvaluations = democraticEvaluationMapper.selectList(lqw3);

                    if (assessDemocraticEvaluations != null && !assessDemocraticEvaluations.isEmpty()) {
                        List<String> filledIds = assessDemocraticEvaluations.stream().map(AssessDemocraticEvaluation::getEvaluationSummaryId).collect(Collectors.toList());


                        if (!filledIds.isEmpty())
                            summaries = summaries.stream().filter(summary -> !filledIds.contains(summary.getId())).collect(Collectors.toList());
                    }
                }
            }


            // 基层A票只查询副职
//            if (!roles.contains("director_leader") && "basic_AB".equals(sysUser.getVoteType())) {
//                summaries = summaries.stream()
//                        .filter(summary -> summary.getType().equals("31"))
//                        .collect(Collectors.toList());
//            }

            // 机关C票不查询总师、二巡
            if (!roles.contains("director_leader") && "bureau_C".equals(sysUser.getVoteType())) {
                summaries = summaries.stream()
                        .filter(summary -> !summary.getType().equals("23"))
                        .collect(Collectors.toList());
            }
        }

        List<String> allPeople = new ArrayList<>();

        // 封装返回数据
        if (summaries != null && !summaries.isEmpty()) {
            List<DemocraticFillListVO> list = new ArrayList<>();
            for (AssessDemocraticEvaluationSummary summary : summaries) {
                DemocraticFillListVO vo = new DemocraticFillListVO();
                vo.setAppraisee(summary.getAppraisee());
                vo.setDepartId(summary.getDepart());
                vo.setAssessName(summary.getAssessName());
                vo.setType(summary.getType());
                vo.setSummaryId(summary.getId());
                vo.setEndDate(summary.getEndDate());
                vo.setCurrentYear(summary.getCurrentYear());
                list.add(vo);
                allPeople.add(summary.getAppraisee());
            }

            LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
            lqw.eq(AssessAnnualSummary::getCurrentYear, annual.getCurrentYear());
            lqw.in(AssessAnnualSummary::getHashId, allPeople);
            lqw.ne(AssessAnnualSummary::getHashId, "group");
            lqw.orderByAsc(AssessAnnualSummary::getPersonOrder);
            List<AssessAnnualSummary> annualSummaries = annualSummaryMapper.selectList(lqw);

            if (annualSummaries != null && !annualSummaries.isEmpty()) {
                List<DemocraticFillListVO> groupList = list.stream().filter(vo -> "group".equals(vo.getAppraisee())).collect(Collectors.toList());
                List<DemocraticFillListVO> peopleList = list.stream().filter(vo -> !"group".equals(vo.getAppraisee())).collect(Collectors.toList());


                // 将list根据annualSummaries的顺序排序，annualSummaries中的hashId属性对应list中的appraisee属性
                peopleList.sort(Comparator.comparing(o -> annualSummaries.stream().filter(a -> a.getHashId().equals(o.getAppraisee())).findFirst().get().getPersonOrder()));

                list = new ArrayList<>();
                list.addAll(groupList);
                list.addAll(peopleList);
            }

            return list;
        }

        return Collections.emptyList();
    }

    @Autowired
    private AssessLeaderRecItemMapper leaderRecItemMapper;
    @Autowired
    private AssessLeaderRecMapper leaderRecMapper;

    private List<AssessLeaderRecItem> getLeaderRecommendList(String leaderId, String year) {

        LambdaQueryWrapper<AssessLeaderRec> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessLeaderRec::getLeader, leaderId);
        lqw.eq(AssessLeaderRec::getCurrentYear, year);
        AssessLeaderRec leaderRecs = leaderRecMapper.selectOne(lqw);

        if (leaderRecs == null) {
            throw new JeecgBootException("当前年度考核未到推优节点，详情请联系管理员！");
        }

        if ("1".equals(leaderRecs.getStatus())) {
            throw new JeecgBootException("当前年度考核未到推优节点，详情请联系管理员！");
        }

        String mainId = leaderRecs.getId();

        LambdaQueryWrapper<AssessLeaderRecItem> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(AssessLeaderRecItem::getMainId, mainId);
        List<AssessLeaderRecItem> items = leaderRecItemMapper.selectList(lqw2);

        // 对item排序，通过personOrder，升序，如果personOrder为空，排在后面
        // items.sort(Comparator.comparing(AssessLeaderRecItem::getPersonOrder, Comparator.nullsLast(Comparator.naturalOrder())));
        items.sort(Comparator.comparing(AssessLeaderRecItem::getPersonOrder, Comparator.nullsLast(Comparator.comparingInt(Integer::parseInt))));

        // 将正职兼二巡、正职、副职、其他分开
        List<AssessLeaderRecItem> items1 = new ArrayList<>(); // 总师二巡
        List<AssessLeaderRecItem> items2 = new ArrayList<>(); // 正职
        List<AssessLeaderRecItem> items3 = new ArrayList<>(); // 副职
        List<AssessLeaderRecItem> items4 = new ArrayList<>(); // 其他
        for (AssessLeaderRecItem item : items) {
            if ("正职兼总师、二巡".equals(item.getType())) item.setType("正职");

            if ("局管总师二巡".equals(item.getDepartName())) {
                items1.add(item);
                continue;
            }

            switch (item.getType()) {
                case "正职":
                    items2.add(item);
                    break;
                case "副职":
                    items3.add(item);
                    break;
                default:
                    items4.add(item);
                    break;
            }
        }

        items.clear();
        // 将正职兼二巡、正职、副职、其他合并， 顺序为正职兼总师、二巡、正职、副职、其他
        items.addAll(items1);
        items.addAll(items2);
        items.addAll(items3);
        items.addAll(items4);
        return items;
    }

    private void handleAssessType(AssessType assessType, Date now, Date deadline, List<CommissionItem> commissionItems, String year) {
        switch (assessType) {
            case annual:
                handleAnnualType(now, deadline, commissionItems, year);
                break;
            case business:
                handleBusinessType(now, deadline, commissionItems, year);
                break;
        }
    }

    private void handleBusinessType(Date now, Date deadline, List<CommissionItem> commissionItems, String year) {
        // 处理 businessCommendMapper
        List<AssessBusinessCommend> businessCommends = businessCommendMapper.selectList(null);
        boolean hasStatusNotEqual2 = (businessCommends != null) && businessCommends.stream().anyMatch(b -> b.getCurrentYear().equals(year));

        if (!hasStatusNotEqual2) {
            int status = calculateStatus(now, deadline);
            commissionItems.add(new CommissionItem("表彰", String.valueOf(status), "/business/report/commend"));
        }

        // 处理 businessDenounceMapper
        List<AssessBusinessDenounce> businessDenounces = businessDenounceMapper.selectList(null);
        hasStatusNotEqual2 = (businessDenounces != null) && businessDenounces.stream().anyMatch(b -> b.getCurrentYear().equals(year));

        if (!hasStatusNotEqual2) {
            int status = calculateStatus(now, deadline);
            commissionItems.add(new CommissionItem("通报批评", String.valueOf(status), "/business/report/denounce"));
        }
    }

    private void handleAnnualType(Date now, Date deadline, List<CommissionItem> commissionItems, String year) {
        // 处理 annualAccountabilityMapper
        List<AssessAnnualAccountability> annualAccountability = annualAccountabilityMapper.selectList(null);
        boolean hasStatusNotEqual2 = (annualAccountability != null) && annualAccountability.stream().anyMatch(a -> a.getCurrentYear().equals(year));

        if (!hasStatusNotEqual2) {
            int status = calculateStatus(now, deadline);
            commissionItems.add(new CommissionItem("问责", String.valueOf(status), "/annual/accountability"));
        }

        // 处理 annualVacationMapper
        List<AssessAnnualVacation> annualVacations = annualVacationMapper.selectList(null);
        hasStatusNotEqual2 = (annualVacations != null) && annualVacations.stream().anyMatch(a -> a.getCurrentYear().equals(year));

        if (!hasStatusNotEqual2) {
            int status = calculateStatus(now, deadline);
            commissionItems.add(new CommissionItem("休假", String.valueOf(status), "/assess/annual/vacation"));
        }
    }

    private int calculateStatus(Date now, Date deadline) {
        long diffInMillis = deadline.getTime() - now.getTime();
        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

        if (diffInDays > 7) {
            return 3;
        } else if (diffInDays >= 3) {
            return 2;
        } else {
            return 1;
        }
    }

}
