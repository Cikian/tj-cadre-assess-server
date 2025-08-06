package org.jeecg.modules.sys.mapper.annual;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.sys.dto.ArrangeDTO;
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
public interface AssessAnnualArrangeMapper extends BaseMapper<AssessAnnualArrange> {

    /**
     * 通过主表id删除子表数据
     *
     * @param mainId 主表id
     * @return boolean
     */
    public boolean deleteByMainId(@Param("mainId") String mainId);

    /**
     * 通过主表id查询子表数据
     *
     * @param mainId 主表id
     * @return List<AssessAnnualArrange>
     */
    public List<AssessAnnualArrange> selectByMainId(@Param("mainId") String mainId);

    Integer selectEvaluationNumByDepartId(String fillId);

    AssessAnnualArrangeDTO getAssessAnnualFillQuery(@Param("depart") String depart);

    List<ArrangeDTO> queryAllArrangeByYear(@Param("year") String year);

    @Select("SELECT arrange.id, depart.id as depart, depart.alias as depart_name, depart.depart_type, arrange.vote_a, arrange.vote_b, arrange.vote_c, arrange.vote_num as total FROM assess_annual_fill fill JOIN assess_annual_arrange arrange ON fill.id = arrange.annual_fill_id JOIN sys_depart depart ON fill.depart = depart.id WHERE fill.current_year = #{year} ORDER BY depart.depart_order")
    List<ConfirmVoteNumDTO> queryConfirmVoteNumDTO(@Param("year") String year);


}
