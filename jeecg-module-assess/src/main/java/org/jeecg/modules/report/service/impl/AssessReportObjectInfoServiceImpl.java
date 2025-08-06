package org.jeecg.modules.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.sys.entity.report.AssessReportObjectInfo;
import org.jeecg.modules.sys.mapper.report.AssessReportObjectInfoMapper;
import org.jeecg.modules.report.service.IAssessReportObjectInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: 评议对象有关信息汇总表
 * @Author: jeecg-boot
 * @Date: 2024-09-24
 * @Version: V1.0
 */
@Service
public class AssessReportObjectInfoServiceImpl extends ServiceImpl<AssessReportObjectInfoMapper, AssessReportObjectInfo> implements IAssessReportObjectInfoService {
@Autowired
private AssessReportObjectInfoMapper objectInfoMapper;

    @Override
    public List<AssessReportObjectInfo> getByFillId(String fillId) {
        LambdaQueryWrapper<AssessReportObjectInfo> qw = new LambdaQueryWrapper<>();
        qw.eq(AssessReportObjectInfo::getFillId, fillId);
        return objectInfoMapper.selectList(qw);
    }
}
