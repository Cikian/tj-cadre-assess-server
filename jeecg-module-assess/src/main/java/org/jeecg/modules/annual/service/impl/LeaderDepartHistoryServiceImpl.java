package org.jeecg.modules.annual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.annual.service.IAssessDemocraticEvaluationItemService;
import org.jeecg.modules.annual.service.ILeaderDepartHistoryService;
import org.jeecg.modules.sys.entity.LeaderDepartHistory;
import org.jeecg.modules.sys.entity.annual.AssessDemocraticEvaluationItem;
import org.jeecg.modules.sys.entity.business.AssessLeaderDepartConfig;
import org.jeecg.modules.sys.mapper.LeaderDepartHistoryMapper;
import org.jeecg.modules.sys.mapper.annual.AssessDemocraticEvaluationItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @Description: 民主测评项
 * @Author: jeecg-boot
 * @Date: 2024-09-11
 * @Version: V1.0
 */
@Service
public class LeaderDepartHistoryServiceImpl extends ServiceImpl<LeaderDepartHistoryMapper, LeaderDepartHistory> implements ILeaderDepartHistoryService {


    @Override
    public List<String> getDepartIdsByLeaderIds(List<String> leaderIds, String assessYear) {
        if (leaderIds == null || leaderIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<LeaderDepartHistory> lqw = new LambdaQueryWrapper<>();
        lqw.in(LeaderDepartHistory::getLeaderId, leaderIds);
        List<LeaderDepartHistory> list = this.list(lqw);

        List<String> res = new ArrayList<>();
        for (LeaderDepartHistory config : list) {
            String departId = config.getDepartId();
            if (departId != null && !departId.isEmpty()) {
                List<String> l = Arrays.asList(departId.split(","));
                res.addAll(l);
            }
        }

        return res;
    }
}
