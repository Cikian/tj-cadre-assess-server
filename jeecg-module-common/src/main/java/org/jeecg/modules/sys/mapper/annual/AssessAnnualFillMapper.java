package org.jeecg.modules.sys.mapper.annual;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.sys.dto.AnnualFieldDTO;
import org.jeecg.modules.sys.dto.RegularGradeDTO;
import org.jeecg.modules.sys.entity.annual.AssessAnnualFill;

import java.util.List;

/**
 * @Description: 年度考核填报列表
 * @Author: jeecg-boot
 * @Date: 2024-09-04
 * @Version: V1.0
 */
public interface AssessAnnualFillMapper extends BaseMapper<AssessAnnualFill> {
    List<RegularGradeDTO> queryRegularGradeByUserIds(List<String> userIds, String year);

    List<AnnualFieldDTO> getAllItems(@Param("currentYear")String currentYear);
}
