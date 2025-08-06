package org.jeecg.modules.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.sys.entity.business.AssessBusinessLevelRatio;

/**
 * @Description: 业务考核等次默认比例
 * @Author: jeecg-boot
 * @Date: 2024-10-29
 * @Version: V1.0
 */
public interface IAssessBusinessLevelRatioService extends IService<AssessBusinessLevelRatio> {

    void changeEnable(String cid);

}
