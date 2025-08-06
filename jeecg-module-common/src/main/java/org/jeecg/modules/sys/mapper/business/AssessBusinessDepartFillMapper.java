package org.jeecg.modules.sys.mapper.business;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.sys.dto.BusinessFieldDTO;
import org.jeecg.modules.sys.entity.business.AssessBusinessDepartFill;

import java.util.List;

/**
 * @Description: 业务考核填报列表
 * @Author: jeecg-boot
 * @Date: 2024-08-22
 * @Version: V1.0
 */
public interface AssessBusinessDepartFillMapper extends BaseMapper<AssessBusinessDepartFill> {

    List<BusinessFieldDTO> getAllItems(@Param("currentYear") String currentYear);
}
