package org.jeecg.modules.sys.mapper.annual;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.sys.entity.annual.AssessAnnualRecommendTemp;

import java.util.List;

/**
 * @Description: 局领导推优临时
 * @Author: jeecg-boot
 * @Date:   2024-11-05
 * @Version: V1.0
 */
public interface AssessAnnualRecommendTempMapper extends BaseMapper<AssessAnnualRecommendTemp> {

    List<AssessAnnualRecommendTemp> getRecommendTempExl();
}
