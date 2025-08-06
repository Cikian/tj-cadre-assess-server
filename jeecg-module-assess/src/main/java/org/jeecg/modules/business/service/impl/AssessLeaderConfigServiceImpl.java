package org.jeecg.modules.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.business.service.IAssessLeaderConfigService;
import org.jeecg.modules.sys.entity.annual.AssessLeaderConfig;
import org.jeecg.modules.sys.mapper.annual.AssessLeaderConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.Collections;
import java.util.List;

/**
 * @Description: 考核领导配置
 * @Author: jeecg-boot
 * @Date: 2024-12-09
 * @Version: V1.0
 */
@Service
public class AssessLeaderConfigServiceImpl extends ServiceImpl<AssessLeaderConfigMapper, AssessLeaderConfig> implements IAssessLeaderConfigService {

}
