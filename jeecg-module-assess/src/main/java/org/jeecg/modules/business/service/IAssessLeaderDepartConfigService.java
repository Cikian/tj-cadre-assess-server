package org.jeecg.modules.business.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.sys.entity.annual.AssessLeaderConfig;
import org.jeecg.modules.sys.entity.business.AssessLeaderDepartConfig;

import java.util.Collections;
import java.util.List;

/**
 * @Description: 领导分管处室（单位）配置
 * @Author: jeecg-boot
 * @Date: 2024-08-26
 * @Version: V1.0
 */
public interface IAssessLeaderDepartConfigService extends IService<AssessLeaderDepartConfig> {
    Result<String> saveDistinct(AssessLeaderDepartConfig assessLeaderDepartConfig);

    public List<String> getDepartIdsByLeaderIds(List<String> leaderIds);
}
