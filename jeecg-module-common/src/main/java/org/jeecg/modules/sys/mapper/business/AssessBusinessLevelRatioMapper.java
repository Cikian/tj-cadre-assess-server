package org.jeecg.modules.sys.mapper.business;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.sys.entity.business.AssessBusinessLevelRatio;

/**
 * @Description: 业务考核等次默认比例
 * @Author: jeecg-boot
 * @Date: 2024-10-29
 * @Version: V1.0
 */
public interface AssessBusinessLevelRatioMapper extends BaseMapper<AssessBusinessLevelRatio> {

    AssessBusinessLevelRatio getEnabled();

}
