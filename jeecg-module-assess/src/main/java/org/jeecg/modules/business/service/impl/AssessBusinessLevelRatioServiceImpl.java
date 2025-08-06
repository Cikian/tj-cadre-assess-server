package org.jeecg.modules.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.sys.entity.business.AssessBusinessLevelRatio;
import org.jeecg.modules.sys.mapper.business.AssessBusinessGradeMapper;
import org.jeecg.modules.sys.mapper.business.AssessBusinessLevelRatioMapper;
import org.jeecg.modules.business.service.IAssessBusinessGradeService;
import org.jeecg.modules.business.service.IAssessBusinessLevelRatioService;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description: 业务考核等次默认比例
 * @Author: jeecg-boot
 * @Date: 2024-10-29
 * @Version: V1.0
 */
@Service
public class AssessBusinessLevelRatioServiceImpl extends ServiceImpl<AssessBusinessLevelRatioMapper, AssessBusinessLevelRatio> implements IAssessBusinessLevelRatioService {
@Autowired
private AssessBusinessLevelRatioMapper ratioMapper;
@Autowired
private AssessBusinessGradeMapper gradeMapper;
@Autowired
private IAssessBusinessGradeService gradeService;
@Autowired
private AssessCommonApi assessCommonApi;

    @Override
    public void changeEnable(String cid) {
        LambdaQueryWrapper<AssessBusinessLevelRatio> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessBusinessLevelRatio::getId, cid);
        AssessBusinessLevelRatio ratio = ratioMapper.selectOne(lqw);
        if (ratio != null && !ratio.isEnable()) {
            LambdaUpdateWrapper<AssessBusinessLevelRatio> luw = new LambdaUpdateWrapper<>();
            luw.set(AssessBusinessLevelRatio::isEnable, false);
            ratioMapper.update(null, luw);

            ratio.setEnable(true);
            ratioMapper.updateById(ratio);

            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("business");

            if (currentAssessInfo != null && currentAssessInfo.isAssessing()){
                gradeService.updateBusinessRanking();
                gradeService.updateBusinessLevel();
            }

        }
    }

}
