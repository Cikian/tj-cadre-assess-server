package org.jeecg.modules.annual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.sys.entity.annual.AssessAnnualDutyReport;
import org.jeecg.modules.sys.mapper.annual.AssessAnnualDutyReportMapper;
import org.jeecg.modules.annual.service.IAssessAnnualDutyReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: 述职报告
 * @Author: jeecg-boot
 * @Date: 2024-09-04
 * @Version: V1.0
 */
@Service
public class AssessAnnualDutyReportServiceImpl extends ServiceImpl<AssessAnnualDutyReportMapper, AssessAnnualDutyReport> implements IAssessAnnualDutyReportService {

    @Autowired
    private AssessAnnualDutyReportMapper dutyReportMapper;

    @Override
    public List<AssessAnnualDutyReport> selectByMainId(String mainId) {
        return dutyReportMapper.selectByMainId(mainId);
    }

    @Override
    public List<AssessAnnualDutyReport> queryByFillId(String fillId) {
        LambdaQueryWrapper<AssessAnnualDutyReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssessAnnualDutyReport::getAnnualFillId, fillId);
        return dutyReportMapper.selectList(queryWrapper);
    }
}
