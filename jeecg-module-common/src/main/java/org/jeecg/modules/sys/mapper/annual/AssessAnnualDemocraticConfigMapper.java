package org.jeecg.modules.sys.mapper.annual;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.sys.entity.annual.AssessAnnualDemocraticConfig;

import java.util.List;

/**
 * @Description: 民主测评指标
 * @Author: jeecg-boot
 * @Date: 2024-09-11
 * @Version: V1.0
 */
public interface AssessAnnualDemocraticConfigMapper extends BaseMapper<AssessAnnualDemocraticConfig> {

    AssessAnnualDemocraticConfig getLastVersionOptionsByType(@Param("type") String type);

    List<AssessAnnualDemocraticConfig> getLastVersions();

}
