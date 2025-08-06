package org.jeecg.modules.sys.mapper.annual;

import org.jeecg.modules.sys.entity.annual.AssessAccountabilityPeriodConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 问责影响期配置
 * @Author: jeecg-boot
 * @Date:   2024-09-18
 * @Version: V1.0
 */
public interface AssessAccountabilityPeriodConfigMapper extends BaseMapper<AssessAccountabilityPeriodConfig> {
    void deleteByCode(String code);

    Integer getInfluenceTime(String id);
}
