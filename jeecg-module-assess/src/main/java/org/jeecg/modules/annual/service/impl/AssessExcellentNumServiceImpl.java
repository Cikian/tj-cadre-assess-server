package org.jeecg.modules.annual.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.annual.service.IAssessExcellentNumService;
import org.jeecg.modules.sys.entity.annual.AssessExcellentNum;
import org.jeecg.modules.sys.mapper.annual.AssessExcellentNumMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description: 年度考核优秀比例设置
 * @Author: jeecg-boot
 * @Date: 2024-09-16
 * @Version: V1.0
 */
@Service
public class AssessExcellentNumServiceImpl extends ServiceImpl<AssessExcellentNumMapper, AssessExcellentNum> implements IAssessExcellentNumService {
    @Autowired
    private AssessExcellentNumMapper numMapper;

}
