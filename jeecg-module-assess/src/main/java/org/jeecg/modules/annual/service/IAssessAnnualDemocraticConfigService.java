package org.jeecg.modules.annual.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.sys.entity.annual.AssessAnnualDemocraticConfig;
import org.jeecg.modules.sys.entity.annual.AssessAnnualDemocraticItem;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Description: 民主测评指标
 * @Author: jeecg-boot
 * @Date: 2024-09-11
 * @Version: V1.0
 */
public interface IAssessAnnualDemocraticConfigService extends IService<AssessAnnualDemocraticConfig> {

    /**
     * 添加一对多
     *
     * @param assessAnnualDemocraticConfig
     * @param assessAnnualDemocraticItemList
     */
    public void saveMain(AssessAnnualDemocraticConfig assessAnnualDemocraticConfig,
                         List<AssessAnnualDemocraticItem> assessAnnualDemocraticItemList);

    /**
     * 修改一对多
     *
     * @param assessAnnualDemocraticConfig
     * @param assessAnnualDemocraticItemList
     */
    public void updateMain(AssessAnnualDemocraticConfig assessAnnualDemocraticConfig,
                           List<AssessAnnualDemocraticItem> assessAnnualDemocraticItemList);

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

    Map<String, BigDecimal> getOptionsByType(String type);

    List<AssessAnnualDemocraticConfig> getLastVersions();

}
