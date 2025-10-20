package org.jeecg.modules.sys;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.CommissionItem;
import org.jeecg.modules.sys.entity.business.AssessLeaderDepartConfig;
import org.jeecg.modules.system.entity.SysCategory;
import org.jeecg.modules.system.entity.SysDictItem;
import org.jeecg.modules.system.entity.SysUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface AssessCommonApi {
    List<SysCategory> getCategoryLeafByParentCode(String code);

    public List<SysDictItem> getAnnualSummaryDictItemsByDepart(String departId, String type);

    void getExportExcel(String url, String filename, Map<String, Object> param, HttpServletResponse response);

    void getExportPDF(String url, String filename, Map<String, Object> param, HttpServletResponse response);

    void getExportZip(String url, String filename, String entryEndName, List<Map<String, Object>> paramList, Map<String, String> folders, HttpServletResponse response);

    void getExportZipWithExcel(String url, String filename, List<String> entryEndNames, List<Map<String, Object>> paramList, HttpServletResponse response);

    void getExportDemZipWithExcel(String url, String filename, List<String> entryEndNames, List<Map<String, Object>> paramList, Map<String, String> leaderMap, HttpServletResponse response);

    void getExportRecExlZipWithExcel(String url, String filename, List<String> entryEndNames, List<Map<String, Object>> paramList, Map<String, String> leaderMap, HttpServletResponse response);

    void exportZipWithExcel(String url, String filename, List<Map<String, Object>> paramList, HttpServletResponse response);

    void getMergePdf(String url, String filename, List<Map<String, Object>> paramList, HttpServletResponse response);

    boolean isCadre();

    @Deprecated
    String getAssessLastYear();

    /**
     * 更新当前考核年度
     *
     * @param assessCurrentAssess
     * @Description: 对象中assess属性为考核名称：regular，business，report，annual
     */
    void updateAssessCurrentYear(AssessCurrentAssess assessCurrentAssess);
    void revocationCurrentYear(AssessCurrentAssess assessCurrentAssess);

    /**
     * 获取当前考核信息
     *
     * @param assess
     * @return
     */
    AssessCurrentAssess getCurrentAssessInfo(String assess);

    Result<String> rejectedAudit(String assessId, String remark, String assess);

    <T> List<T> importExcel(HttpServletRequest request, Class<T> clazz, String fileName);

    List<CommissionItem> getCommissionItems();

    /**
     * 结束考核
     */
    void stopAssess(String assess);

    AssessLeaderDepartConfig getAssessUnitByDepart(String departId);
    AssessLeaderDepartConfig getAssessUnitByLeader(String leaderId);

    Map<String, List<?>> getSpecialPeople();

    Map<String, String> getDictMapByCode(String code);

    List<AssessCurrentAssess> getAssessing();

    JSONObject getAssessingStatus(SysUser user, List<AssessCurrentAssess> assessingList, JSONObject res);

    void updateLdh(String year);

}
