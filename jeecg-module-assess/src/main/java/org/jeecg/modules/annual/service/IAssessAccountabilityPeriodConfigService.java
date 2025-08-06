package org.jeecg.modules.annual.service;

import org.jeecg.modules.sys.entity.annual.AssessAccountabilityPeriodConfig;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 问责影响期配置
 * @Author: jeecg-boot
 * @Date:   2024-09-18
 * @Version: V1.0
 */
public interface IAssessAccountabilityPeriodConfigService extends IService<AssessAccountabilityPeriodConfig> {

    void checkAll();

    String getEndDateById(String id, String startDate);

}
