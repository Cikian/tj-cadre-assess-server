package org.jeecg.modules.sys.mapper.annual;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.sys.dto.AssessAnnualVacationDTO;
import org.jeecg.modules.sys.entity.annual.AssessAnnualVacation;

import java.util.List;

/**
 * @Description: 年度考核休假情况
 * @Author: jeecg-boot
 * @Date: 2024-09-03
 * @Version: V1.0
 */
public interface AssessAnnualVacationMapper extends BaseMapper<AssessAnnualVacation> {

    List<AssessAnnualVacation> getAssessVacation(List<String> userIds, String year);

    AssessAnnualVacationDTO getAssessAnnualVacation(@Param("currentYear") String currentYear, @Param("depart") String depart);

}
