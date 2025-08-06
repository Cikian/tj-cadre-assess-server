package org.jeecg.modules.annual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.SysDepartModel;
import org.jeecg.modules.annual.service.IAssessAnnualArrangeService;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.sys.dto.AssessAnnualArrangeDTO;
import org.jeecg.modules.sys.dto.ConfirmVoteNumDTO;
import org.jeecg.modules.sys.entity.annual.AssessAnnualArrange;
import org.jeecg.modules.sys.entity.annual.AssessAnnualFill;
import org.jeecg.modules.sys.mapper.annual.AssessAnnualArrangeMapper;
import org.jeecg.modules.sys.mapper.annual.AssessAnnualFillMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 年度考核工作安排
 * @Author: jeecg-boot
 * @Date: 2024-09-04
 * @Version: V1.0
 */
@Service
public class AssessAnnualArrangeServiceImpl extends ServiceImpl<AssessAnnualArrangeMapper, AssessAnnualArrange> implements IAssessAnnualArrangeService {

    @Autowired
    private AssessAnnualArrangeMapper assessAnnualArrangeMapper;
    @Autowired
    private AssessAnnualFillMapper fillMapper;
    @Autowired
    private DepartCommonApi departCommonApi;


    @Override
    public List<AssessAnnualArrange> selectByMainId(String mainId) {
        return assessAnnualArrangeMapper.selectByMainId(mainId);
    }

    @Override
    public AssessAnnualArrangeDTO getAssessAnnualFillQuery(String depart) {

        return assessAnnualArrangeMapper.getAssessAnnualFillQuery(depart);
    }

    @Override
    public AssessAnnualArrange getArrangeByFillId(String fillId) {
        LambdaQueryWrapper<AssessAnnualArrange> qw = new LambdaQueryWrapper<>();
        qw.eq(AssessAnnualArrange::getAnnualFillId, fillId);
        return assessAnnualArrangeMapper.selectOne(qw);
    }

    @Override
    public List<?> getVoteNum(String year) {
        LambdaQueryWrapper<AssessAnnualFill> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualFill::getCurrentYear, year);
        List<AssessAnnualFill> fills = fillMapper.selectList(lqw);
        // 将fills各项的id转为集合
        List<String> fillIds = fills.stream().map(AssessAnnualFill::getId).collect(Collectors.toList());

        // 转换为Map，K：id，V：depart
        Map<String, String> fillIdDepartMap = fills.stream().collect(Collectors.toMap(AssessAnnualFill::getId, AssessAnnualFill::getDepart));

        List<SysDepartModel> sysDepartModels = departCommonApi.queryAllDepart();
        Map<String, String> departNameMap = sysDepartModels.stream().collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getAlias));

        if (fillIds.isEmpty()) {
            throw new JeecgBootException("该年份没有考核安排！");
        }

        LambdaQueryWrapper<AssessAnnualArrange> qw = new LambdaQueryWrapper<>();
        qw.isNotNull(AssessAnnualArrange::getVoteNum);
        qw.in(AssessAnnualArrange::getAnnualFillId, fillIds);

        List<AssessAnnualArrange> arranges = assessAnnualArrangeMapper.selectList(qw);

        List<Map<String, Object>> result = new ArrayList<>();

        for (AssessAnnualArrange arrange : arranges) {
            Integer voteA = arrange.getVoteNum();
            Integer voteB = arrange.getVoteB();
            Integer voteC = arrange.getVoteC();

            Map<String, Integer> voteNum = new HashMap<>();
            voteNum.put("voteA", voteA);
            voteNum.put("voteB", voteB);
            voteNum.put("voteC", voteC);

            Map<String, Object> item = new HashMap<>();
            item.put("depart", departNameMap.get(fillIdDepartMap.get(arrange.getAnnualFillId())));
            item.put("voteNum", voteNum);

            result.add(item);
        }

        return result;
    }

    @Override
    public Integer getExcellentDeputyNum(String year, String depart) {
        LambdaQueryWrapper<AssessAnnualFill> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualFill::getCurrentYear, year);
        lqw.eq(AssessAnnualFill::getDepart, depart);
        List<AssessAnnualFill> fill = fillMapper.selectList(lqw);
        if (fill.isEmpty()) {
            throw new JeecgBootException("该单位该考核年度无考核安排！");
        }

        return fill.get(0).getExcellentDeputyNum();
    }

    @Override
    public List<ConfirmVoteNumDTO> getVoteNumList(String year) {
        return assessAnnualArrangeMapper.queryConfirmVoteNumDTO(year);
    }

    @Override
    public void changeVoteNum(String id, Integer voteA, Integer voteB, Integer voteC) {
        LambdaQueryWrapper<AssessAnnualArrange> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualArrange::getId, id);
        AssessAnnualArrange arrange = assessAnnualArrangeMapper.selectOne(lqw);
        if (arrange == null) {
            throw new JeecgBootException("该考核安排不存在！");
        }

        LambdaUpdateWrapper<AssessAnnualArrange> luw = new LambdaUpdateWrapper<>();
        luw.eq(AssessAnnualArrange::getId, id);
        luw.set(AssessAnnualArrange::getVoteA, voteA);
        luw.set(AssessAnnualArrange::getVoteB, voteB);
        luw.set(AssessAnnualArrange::getVoteC, voteC);
        luw.set(AssessAnnualArrange::getVoteNum, voteA + voteB + voteC);
        luw.set(AssessAnnualArrange::getChiefNum, voteA);
        assessAnnualArrangeMapper.update(null, luw);
    }
}
