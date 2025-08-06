package org.jeecg.modules.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.sys.entity.report.AssessReportNewLeader;
import org.jeecg.modules.sys.mapper.report.AssessReportNewLeaderMapper;
import org.jeecg.modules.report.service.IAssessReportNewLeaderService;
import org.jeecg.modules.report.vo.AssessReportDemocracyNewLeaderVO;
import org.jeecg.modules.user.UserCommonApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 新提拔干部名册
 * @Author: jeecg-boot
 * @Date: 2024-09-24
 * @Version: V1.0
 */
@Service
public class AssessReportNewLeaderServiceImpl extends ServiceImpl<AssessReportNewLeaderMapper, AssessReportNewLeader> implements IAssessReportNewLeaderService {
    @Autowired
    private AssessReportNewLeaderMapper newLeaderMapper;
    @Autowired
    private UserCommonApi userCommonApi;

    @Override
    public List<AssessReportNewLeader> getByFillId(String fillId) {
        LambdaQueryWrapper<AssessReportNewLeader> qw = new LambdaQueryWrapper<>();
        qw.eq(AssessReportNewLeader::getFillId, fillId);
        return newLeaderMapper.selectList(qw);
    }

    @Override
    public List<AssessReportDemocracyNewLeaderVO> getDemocracyItemByFillId(String fillId) {
        LambdaQueryWrapper<AssessReportNewLeader> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessReportNewLeader::getFillId, fillId);
        List<AssessReportNewLeader> newLeadersList = newLeaderMapper.selectList(lqw);

        List<AssessReportDemocracyNewLeaderVO> voList = new ArrayList<>();
        for (AssessReportNewLeader newLeader : newLeadersList) {
            AssessReportDemocracyNewLeaderVO vo = new AssessReportDemocracyNewLeaderVO();
            vo.setNewLeaderItem(newLeader.getId());

            vo.setName(newLeader.getName());
            vo.setBirthday(newLeader.getBirthday());
            vo.setOriginalPosition(newLeader.getOriginalPosition());
            vo.setCurrentPosition(newLeader.getCurrentPosition());

            voList.add(vo);
        }
        return voList;
    }
}
