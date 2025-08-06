package org.jeecg.modules.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.sys.entity.report.AssessReportArrange;
import org.jeecg.modules.sys.mapper.report.AssessReportArrangeMapper;
import org.jeecg.modules.report.service.IAssessReportArrangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description: 一报告两评议工作安排
 * @Author: jeecg-boot
 * @Date: 2024-09-24
 * @Version: V1.0
 */
@Service
public class AssessReportArrangeServiceImpl extends ServiceImpl<AssessReportArrangeMapper, AssessReportArrange> implements IAssessReportArrangeService {
@Autowired
private AssessReportArrangeMapper reportArrangeMapper;

    @Override
    public AssessReportArrange getByFillId(String fillId) {
        LambdaQueryWrapper<AssessReportArrange> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessReportArrange::getFillId, fillId);

        return reportArrangeMapper.selectOne(lqw);
    }
}
