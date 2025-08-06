package org.jeecg.modules.annual.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.sys.dto.AssessAnnualArrangeDTO;
import org.jeecg.modules.sys.dto.ConfirmVoteNumDTO;
import org.jeecg.modules.sys.entity.annual.AssessAnnualArrange;

import java.util.List;

/**
 * @Description: 年度考核工作安排
 * @Author: jeecg-boot
 * @Date: 2024-09-04
 * @Version: V1.0
 */
public interface IAssessAnnualArrangeService extends IService<AssessAnnualArrange> {

    /**
     * 通过主表id查询子表数据
     *
     * @param mainId 主表id
     * @return List<AssessAnnualArrange>
     */
    public List<AssessAnnualArrange> selectByMainId(String mainId);

    AssessAnnualArrangeDTO getAssessAnnualFillQuery(String depart);

    AssessAnnualArrange getArrangeByFillId(String fillId);

    List<?> getVoteNum(String year);

    Integer getExcellentDeputyNum(String year, String depart);

    List<ConfirmVoteNumDTO> getVoteNumList(String year);

    void changeVoteNum(String id, Integer voteA, Integer voteB, Integer voteC);
}
