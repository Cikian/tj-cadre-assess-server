package org.jeecg.modules.sys.mapper.annual;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.sys.dto.AssessAnnualAccountabilityDTO;
import org.jeecg.modules.sys.entity.annual.AssessAnnualAccountability;

import java.util.List;

/**
 * @Description: 年度考核受问责情况
 * @Author: jeecg-boot
 * @Date: 2024-09-02
 * @Version: V1.0
 */
public interface AssessAnnualAccountabilityMapper extends BaseMapper<AssessAnnualAccountability> {

    List<AssessAnnualAccountability> getAssessAccountability(List<String> userIds);

    AssessAnnualAccountabilityDTO getAccountabilityInformationQuery(@Param("currentYear") String currentYear, @Param("depart") String depart);

}
