package org.jeecg.modules.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.sys.entity.annual.AssessLeaderConfig;
import org.jeecg.modules.sys.entity.business.AssessLeaderDepartConfig;
import org.jeecg.modules.sys.mapper.business.AssessLeaderDepartConfigMapper;
import org.jeecg.modules.business.service.IAssessLeaderDepartConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @Description: 领导分管处室（单位）配置
 * @Author: jeecg-boot
 * @Date: 2024-08-26
 * @Version: V1.0
 */
@Service
public class AssessLeaderDepartConfigServiceImpl extends ServiceImpl<AssessLeaderDepartConfigMapper, AssessLeaderDepartConfig> implements IAssessLeaderDepartConfigService {
    @Autowired
    private AssessLeaderDepartConfigMapper leaderDepartConfigMapper;

    @Override
    public Result<String> saveDistinct(AssessLeaderDepartConfig assessLeaderDepartConfig) {
        String leaderId = assessLeaderDepartConfig.getLeaderId();
        AssessLeaderDepartConfig one = this.getOne(new LambdaQueryWrapper<AssessLeaderDepartConfig>().eq(AssessLeaderDepartConfig::getLeaderId, leaderId));
        if (one != null) {
            return Result.error("不可重复添加！");
        }
        if (assessLeaderDepartConfig.getDepartId() == null || "".equals(assessLeaderDepartConfig.getDepartId())) {
            return Result.error("分管处室不可为空！");
        }
        boolean savedFlag = this.save(assessLeaderDepartConfig);

        if (savedFlag) return Result.ok("添加成功！");
        else return Result.error("添加失败！");
    }

    @Override
    public List<String> getDepartIdsByLeaderIds(List<String> leaderIds) {
        if (leaderIds == null || leaderIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<AssessLeaderDepartConfig> lqw = new LambdaQueryWrapper<>();
        lqw.in(AssessLeaderDepartConfig::getLeaderId, leaderIds);
        List<AssessLeaderDepartConfig> list = leaderDepartConfigMapper.selectList(lqw);

        List<String> res = new ArrayList<>();
        for (AssessLeaderDepartConfig config : list) {
            String departId = config.getDepartId();
            if (departId != null && !departId.isEmpty()) {
                List<String> l = Arrays.asList(departId.split(","));
                res.addAll(l);
            }
        }

        return res;
    }
}
