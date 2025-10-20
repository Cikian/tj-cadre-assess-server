package org.jeecg.modules.annual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.sys.entity.annual.*;
import org.jeecg.modules.sys.mapper.annual.*;
import org.jeecg.modules.annual.service.IAssessAnnualExcellentNumService;
import org.jeecg.modules.system.entity.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 年度考核分管领导优秀名额
 * @Author: jeecg-boot
 * @Date: 2024-09-18
 * @Version: V1.0
 */
@Service
public class AssessAnnualExcellentNumServiceImpl extends ServiceImpl<AssessAnnualExcellentNumMapper, AssessAnnualExcellentNum> implements IAssessAnnualExcellentNumService {
    @Autowired
    private AssessAnnualExcellentNumMapper excellentNumMapper;
    @Autowired
    private AssessAnnualExcellentRatioMapper excellentRatioMapper;
    @Autowired
    private AssessLeaderRecMapper leaderRecMapper;
    @Autowired
    private AssessLeaderRecItemMapper recItemMapper;
    @Autowired
    private AssessAnnualSummaryMapper annualSummaryMapper;

    @Override
    public AssessAnnualExcellentNum getByLeaderId(String year) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (sysUser != null) {
            LambdaQueryWrapper<AssessAnnualExcellentNum> lqw = new LambdaQueryWrapper<>();
            lqw.eq(AssessAnnualExcellentNum::getLeader, sysUser.getId());
            lqw.eq(AssessAnnualExcellentNum::getCurrentYear, year);
            return excellentNumMapper.selectOne(lqw);
        }
        return null;
    }

    @Override
    public List<AssessAnnualExcellentNum> getByCurrentYear(String year) {
        LambdaQueryWrapper<AssessAnnualExcellentNum> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualExcellentNum::getCurrentYear, year);
        return excellentNumMapper.selectList(lqw);
    }

    @Override
    public boolean checkNo(String year) {
        LambdaQueryWrapper<AssessAnnualExcellentRatio> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualExcellentRatio::getEnable, 1);
        AssessAnnualExcellentRatio ratio = excellentRatioMapper.selectOne(lqw);

        LambdaQueryWrapper<AssessLeaderRec> lqw3 = new LambdaQueryWrapper<>();
        lqw3.eq(AssessLeaderRec::getCurrentYear, year);
        List<AssessLeaderRec> leaderRecs = leaderRecMapper.selectList(lqw3);
        //将id转为list
        List<String> recIds = leaderRecs.stream().map(AssessLeaderRec::getId).collect(Collectors.toList());

        Map<String, String> leaderRecMap = leaderRecs.stream().collect(Collectors.toMap(
                AssessLeaderRec::getLeader,
                AssessLeaderRec::getId,
                (existing, replacement) -> {
                    System.out.println("Duplicate key found: " + existing);
                    return existing;
                }
        ));

        // 以id为k，id2为v转换为map
//        Map<String, String> leaderRecMap = leaderRecs.stream().collect(Collectors.toMap(AssessLeaderRec::getLeader, AssessLeaderRec::getId));

        LambdaQueryWrapper<AssessLeaderRecItem> lqw4 = new LambdaQueryWrapper<>();
        lqw4.in(AssessLeaderRecItem::getMainId, recIds);
        List<AssessLeaderRecItem> recItems = recItemMapper.selectList(lqw4);

        List<AssessAnnualExcellentNum> updateList = new ArrayList<>();

        if (ratio != null) {
            LambdaQueryWrapper<AssessAnnualExcellentNum> lqw2 = new LambdaQueryWrapper<>();
            lqw2.eq(AssessAnnualExcellentNum::getCurrentYear, year);
            List<AssessAnnualExcellentNum> eNums = excellentNumMapper.selectList(lqw2);

            // 分管纪检人员
            LambdaQueryWrapper<AssessAnnualSummary> lqw5 = new LambdaQueryWrapper<>();
            lqw5.eq(AssessAnnualSummary::getCurrentYear, year);
            lqw5.eq(AssessAnnualSummary::getInspectionWork, 1);
            List<AssessAnnualSummary> inspectionWork = annualSummaryMapper.selectList(lqw5);
            // 将hashid转为list
            List<String> inspectionWorkIds = inspectionWork.stream().map(AssessAnnualSummary::getHashId).collect(Collectors.toList());

            // 从eNums中过滤得到hashid不在inspectionWorkIds中的人
            recItems = recItems.stream().filter(item -> !inspectionWorkIds.contains(item.getHashId())).collect(Collectors.toList());

            if (!eNums.isEmpty()) {
                for (AssessAnnualExcellentNum eNum : eNums) {
                    String recId = leaderRecMap.get(eNum.getLeader());
                    // 从recItems中找到mainId为recId的记录
                    List<AssessLeaderRecItem> items = recItems.stream().filter(item -> item.getMainId().equals(recId)).collect(Collectors.toList());
                    // 从items中找到type为bureau的记录
                    List<AssessLeaderRecItem> bureauItems = items.stream().filter(item -> item.getRecommendType().equals("bureau")).collect(Collectors.toList());
                    List<AssessLeaderRecItem> basicItems = items.stream().filter(item -> item.getRecommendType().equals("basic")).collect(Collectors.toList());
                    List<AssessLeaderRecItem> institutionItems = items.stream().filter(item -> item.getRecommendType().equals("institution")).collect(Collectors.toList());
                    List<AssessLeaderRecItem> groupItems = items.stream().filter(item -> item.getRecommendType().equals("group")).collect(Collectors.toList());

                    // 分别统计数量
                    int bureauNum = bureauItems.size();
                    int basicNum = basicItems.size();
                    int institutionNum = institutionItems.size();
                    int groupNum = groupItems.size();

                    // 计算数量，保留两位小数
                    BigDecimal bureauRNum = new BigDecimal(bureauNum).multiply(ratio.getBureauRatio());
                    BigDecimal basicRNum = new BigDecimal(basicNum).multiply(ratio.getBasicRatio());
                    BigDecimal institutionRNum = new BigDecimal(institutionNum).multiply(ratio.getInstitutionRatio());
                    BigDecimal groupRNum = new BigDecimal(groupNum).multiply(ratio.getGroupRatio());

                    if (bureauRNum.compareTo(eNum.getBureauTheoryNum()) != 0 ) {
                        eNum.setBureauNum(BigDecimal.valueOf(bureauNum));
                        eNum.setBureauTheoryNum(bureauRNum.setScale(2, RoundingMode.HALF_UP));
                    }

                    if (basicRNum.compareTo(eNum.getBasicTheoryNum()) != 0 ) {
                        eNum.setBasicNum(BigDecimal.valueOf(basicNum));
                        eNum.setBasicTheoryNum(basicRNum.setScale(2, RoundingMode.HALF_UP));
                    }

                    if (institutionRNum.compareTo(eNum.getInstitutionTheoryNum()) != 0 ) {
                        eNum.setInstitutionNum(BigDecimal.valueOf(institutionNum));
                        eNum.setInstitutionTheoryNum(institutionRNum.setScale(2, RoundingMode.HALF_UP));
                    }

                    if (groupRNum.compareTo(eNum.getGroupTheoryNum()) != 0 ) {
                        eNum.setGroupNum(BigDecimal.valueOf(groupNum));
                        eNum.setGroupTheoryNum(groupRNum.setScale(2, RoundingMode.HALF_UP));
                    }
                    updateList.add(eNum);
                }

                return this.updateBatchById(updateList);
            }
        }

        return true;
    }
}
