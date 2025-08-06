package org.jeecg.modules.sys.mapper.business;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.sys.dto.DenounceExportDTO;
import org.jeecg.modules.sys.entity.business.AssessBusinessDenounce;

import java.util.List;

/**
 * @Description: 通报批评
 * @Author: jeecg-boot
 * @Date: 2024-08-30
 * @Version: V1.0
 */
public interface AssessBusinessDenounceMapper extends BaseMapper<AssessBusinessDenounce> {

    public List<DenounceExportDTO> getAllDenounce();

    public List<DenounceExportDTO> getDepartDenounce(@Param("departId") String departId);

}
