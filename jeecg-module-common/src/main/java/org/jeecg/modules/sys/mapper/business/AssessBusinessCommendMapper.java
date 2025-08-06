package org.jeecg.modules.sys.mapper.business;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.sys.dto.CommendExportDTO;
import org.jeecg.modules.sys.entity.business.AssessBusinessCommend;

import java.util.List;

/**
 * @Description: 表彰
 * @Author: jeecg-boot
 * @Date: 2024-08-30
 * @Version: V1.0
 */
public interface AssessBusinessCommendMapper extends BaseMapper<AssessBusinessCommend> {

    public List<CommendExportDTO> getAllCommend();

    public List<CommendExportDTO> getDepartCommend(@Param("departId") String departId);

}
