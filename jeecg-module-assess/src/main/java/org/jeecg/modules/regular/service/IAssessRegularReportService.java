package org.jeecg.modules.regular.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.regular.entity.AssessRegularReport;
import org.jeecg.modules.regular.entity.AssessRegularReportItem;
import org.jeecg.modules.regular.entity.RegularIndexEmployee;
import org.jeecg.modules.regular.entity.RegularIndexLeader;
import org.jeecg.modules.regular.vo.ConflictInfoVO;
import org.jeecg.modules.regular.vo.RegularStartVO;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;

import java.io.Serializable;
import java.util.*;

/**
 * @Description: 平时考核填报
 * @Author: admin
 * @Date: 2024-08-30
 * @Version: V1.0
 */
public interface IAssessRegularReportService extends IService<AssessRegularReport> {

    /**
     * 添加一对多
     *
     * @param assessRegularReport
     * @param assessRegularReportItemList
     */
    public void saveMain(AssessRegularReport assessRegularReport, List<AssessRegularReportItem> assessRegularReportItemList);

    /**
     * 修改一对多
     *
     * @param assessRegularReport
     * @param assessRegularReportItemList
     */
    public void updateMain(AssessRegularReport assessRegularReport, List<AssessRegularReportItem> assessRegularReportItemList);

    /**
     * 删除一对多
     *
     * @param id
     */
    public void delMain(String id);

    /**
     * 批量删除一对多
     *
     * @param idList
     */
    public void delBatchMain(Collection<? extends Serializable> idList);

    /**
     * 平时考核首页领导信息
     *
     */
    public RegularIndexLeader getAllItems();

    /**
     * 启动平时考核流程
     *
     * @param regularStartVO 传入的值
     */
    public void startProcess(RegularStartVO regularStartVO, Set<String> uniqueDepartmentCodes);

    /**
     * 验证报表项 Excel
     *
     * @param reportItems 报表项
     */
    public void validateReportItems(List<AssessRegularReportItem> reportItems);

    /**
     * 获取唯一部门代码
     *
     * @param regularStartVO 子表项
     */
    public Set<String> getUniqueDepartmentCodes(RegularStartVO regularStartVO);

    /**
     * 获取部门名称
     *
     * @param uniqueDepartmentCodes 部门名称集合
     */
    public Set<String> getDepartmentNames(Set<String> uniqueDepartmentCodes);

    /**
     * 检查冲突
     *
     * @param regularStartVO 部门名称集合
     */
    public ConflictInfoVO checkForConflicts(RegularStartVO regularStartVO);

    /**
     * 合并冲突
     *
     * @param conflictItems 冲突部分
     */
    public void mergeConflicts(List<String> conflictItems);


    /**
     * 删除当前平时考核流程数据
     *
     * @param regularStartVO 传入的值
     */
    public void deleteCurrentProcess(RegularStartVO regularStartVO);

    /**
     * 平时考核首页员工信息
     *
     */
    public RegularIndexEmployee[] getPartial(String departName, AssessCurrentAssess assessInfo);

    /**
     * 结束考核
     */
    public void stopAssess();
    public void revocationAssess();

    List<Map<String, Object>> getDetailItem(String type);

}
