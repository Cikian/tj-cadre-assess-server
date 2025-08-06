package org.jeecg.modules.annual.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.sys.entity.annual.AssessDemocraticEvaluationItem;
import org.jeecg.modules.sys.mapper.annual.AssessDemocraticEvaluationItemMapper;
import org.jeecg.modules.annual.service.IAssessDemocraticEvaluationItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: 民主测评项
 * @Author: jeecg-boot
 * @Date: 2024-09-11
 * @Version: V1.0
 */
@Service
public class AssessDemocraticEvaluationItemServiceImpl extends ServiceImpl<AssessDemocraticEvaluationItemMapper, AssessDemocraticEvaluationItem> implements IAssessDemocraticEvaluationItemService {

    @Autowired
    private AssessDemocraticEvaluationItemMapper assessDemocraticEvaluationItemMapper;

    @Override
    public List<AssessDemocraticEvaluationItem> selectByMainId(String mainId) {
        return assessDemocraticEvaluationItemMapper.selectByMainId(mainId);
    }
}
