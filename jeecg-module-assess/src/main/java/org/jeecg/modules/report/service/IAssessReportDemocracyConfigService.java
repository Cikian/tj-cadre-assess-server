package org.jeecg.modules.report.service;

import org.jeecg.modules.sys.entity.report.AssessReportDemocracyConfigItem;
import org.jeecg.modules.sys.entity.report.AssessReportDemocracyConfig;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.sys.entity.report.AssessReportEvaluationItem;
import org.jeecg.modules.sys.entity.report.AssessReportMultiItem;
import org.jeecg.modules.report.vo.AssessReportDemocracyConfigPage;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @Description: 一报告两评议民主测评配置
 * @Author: jeecg-boot
 * @Date: 2024-10-07
 * @Version: V1.0
 */
public interface IAssessReportDemocracyConfigService extends IService<AssessReportDemocracyConfig> {

    /**
     * 添加一对多
     *
     * @param assessReportDemocracyConfig
     * @param assessReportDemocracyConfigItemList
     */
    public void saveMain(AssessReportDemocracyConfig assessReportDemocracyConfig,
                         List<AssessReportDemocracyConfigItem> assessReportDemocracyConfigItemList,
                         List<AssessReportMultiItem> multiItems);

    /**
     * 修改一对多
     *
     * @param config
     * @param itemList
     */
    public void updateMain(AssessReportDemocracyConfig config,
                           List<AssessReportDemocracyConfigItem> itemList,
                           List<AssessReportMultiItem> multiItems);

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

    AssessReportDemocracyConfigPage selectById(String id);

    List<AssessReportEvaluationItem> getFillItems();

    boolean updateEnable(String id);
}
