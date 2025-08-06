package org.jeecg.modules.annual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.sys.entity.annual.AssessAnnualExcellentNum;
import org.jeecg.modules.sys.entity.annual.AssessAnnualExcellentRatio;
import org.jeecg.modules.sys.mapper.annual.AssessAnnualExcellentNumMapper;
import org.jeecg.modules.sys.mapper.annual.AssessAnnualExcellentRatioMapper;
import org.jeecg.modules.annual.service.IAssessAnnualExcellentRatioService;
import org.jeecg.modules.annual.vo.AssessAnnualExcellentConfigVO;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.user.UserCommonApi;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 年度考核优秀比例设置
 * @Author: jeecg-boot
 * @Date: 2024-09-16
 * @Version: V1.0
 */
@Service
public class AssessAnnualExcellentRatioServiceImpl extends ServiceImpl<AssessAnnualExcellentRatioMapper, AssessAnnualExcellentRatio> implements IAssessAnnualExcellentRatioService {
    @Autowired
    private AssessAnnualExcellentRatioMapper ratioMapper;
    @Autowired
    private AssessAnnualExcellentNumMapper numMapper;
    @Autowired
    private UserCommonApi userCommonApi;
    @Autowired
    private AssessAnnualExcellentRatioMapper excellentRatioMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMain(AssessAnnualExcellentRatio assessAnnualExcellentRatio) {
        BigDecimal groupRatio = assessAnnualExcellentRatio.getGroupRatio();
        BigDecimal bureauRatio = assessAnnualExcellentRatio.getBureauRatio();
        BigDecimal basicRatio = assessAnnualExcellentRatio.getBasicRatio();
        BigDecimal institutionRatio = assessAnnualExcellentRatio.getInstitutionRatio();
        if (groupRatio.compareTo(new BigDecimal(1)) > 0) {
            groupRatio = groupRatio.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            assessAnnualExcellentRatio.setGroupRatio(groupRatio);
        }
        if (bureauRatio.compareTo(new BigDecimal(1)) > 0) {
            bureauRatio = bureauRatio.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            assessAnnualExcellentRatio.setBureauRatio(bureauRatio);
        }
        if (basicRatio.compareTo(new BigDecimal(1)) > 0) {
            basicRatio = basicRatio.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            assessAnnualExcellentRatio.setBasicRatio(basicRatio);
        }
        if (institutionRatio.compareTo(new BigDecimal(1)) > 0) {
            institutionRatio = institutionRatio.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            assessAnnualExcellentRatio.setInstitutionRatio(institutionRatio);
        }

        ratioMapper.insert(assessAnnualExcellentRatio);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMain(AssessAnnualExcellentRatio assessAnnualExcellentRatio) {
        BigDecimal groupRatio = assessAnnualExcellentRatio.getGroupRatio();
        BigDecimal bureauRatio = assessAnnualExcellentRatio.getBureauRatio();
        BigDecimal basicRatio = assessAnnualExcellentRatio.getBasicRatio();
        BigDecimal institutionRatio = assessAnnualExcellentRatio.getInstitutionRatio();
        // 如果ratio比1大，除以100
        if (groupRatio.compareTo(new BigDecimal(1)) > 0) {
            groupRatio = groupRatio.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            assessAnnualExcellentRatio.setGroupRatio(groupRatio);
        }
        if (bureauRatio.compareTo(new BigDecimal(1)) > 0) {
            bureauRatio = bureauRatio.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            assessAnnualExcellentRatio.setBureauRatio(bureauRatio);
        }
        if (basicRatio.compareTo(new BigDecimal(1)) > 0) {
            basicRatio = basicRatio.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            assessAnnualExcellentRatio.setBasicRatio(basicRatio);
        }
        if (institutionRatio.compareTo(new BigDecimal(1)) > 0) {
            institutionRatio = institutionRatio.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            assessAnnualExcellentRatio.setInstitutionRatio(institutionRatio);
        }

        ratioMapper.updateById(assessAnnualExcellentRatio);

        }


    @Override
    public List<AssessAnnualExcellentNum> getInitLeaderData() {
        List<SysUser> allLeader = userCommonApi.getAllLeader();

        LambdaQueryWrapper<AssessAnnualExcellentRatio> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualExcellentRatio::getEnable, 1);
        AssessAnnualExcellentRatio ratio = excellentRatioMapper.selectOne(lqw);

        List<AssessAnnualExcellentNum> excellentNums = new ArrayList<>();


        if (ratio != null) {
            for (SysUser leader : allLeader) {
                AssessAnnualExcellentNum num = new AssessAnnualExcellentNum();
                num.setLeader(leader.getId());
                num.setGroupRatio(ratio.getGroupRatio());
                num.setBureauRatio(ratio.getBureauRatio());
                num.setBasicRatio(ratio.getBasicRatio());
                num.setInstitutionRatio(ratio.getInstitutionRatio());
                excellentNums.add(num);
            }
        }
        return excellentNums;
    }

    @Override
    public boolean updateEnable(String id) {
        LambdaUpdateWrapper<AssessAnnualExcellentRatio> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(AssessAnnualExcellentRatio::getEnable, 0);
        int update1 = ratioMapper.update(null, updateWrapper);

        LambdaUpdateWrapper<AssessAnnualExcellentRatio> updateWrapper2 = new LambdaUpdateWrapper<>();
        updateWrapper2.set(AssessAnnualExcellentRatio::getEnable, 1);
        updateWrapper2.eq(AssessAnnualExcellentRatio::getId, id);

        int update = ratioMapper.update(null, updateWrapper2);

        return update1 > 0 && update > 0;
    }

    @Override
    public AssessAnnualExcellentConfigVO getEnableConfig() {
        LambdaQueryWrapper<AssessAnnualExcellentRatio> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssessAnnualExcellentRatio::getEnable, 1);
        AssessAnnualExcellentRatio excellentRatio = ratioMapper.selectOne(queryWrapper);
        AssessAnnualExcellentConfigVO excellentConfigVO = new AssessAnnualExcellentConfigVO();
        BeanUtils.copyProperties(excellentRatio, excellentConfigVO);
        return excellentConfigVO;
    }

}
