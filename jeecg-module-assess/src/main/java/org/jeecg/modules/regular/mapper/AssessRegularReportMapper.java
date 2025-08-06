package org.jeecg.modules.regular.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.regular.dto.RegularFieldDTO;
import org.jeecg.modules.regular.entity.AssessRegularReport;

import java.util.List;

/**
 * @Description: 平时考核填报
 * @Author: admin
 * @Date:   2024-08-30
 * @Version: V1.0
 */
public interface AssessRegularReportMapper extends BaseMapper<AssessRegularReport> {

    List<RegularFieldDTO> getAllItems(@Param("currentYear") String currentYear);

}
