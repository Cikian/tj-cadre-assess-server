package org.jeecg.modules.sys.mapper.annual;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.sys.dto.ArrangeDTO;
import org.jeecg.modules.sys.dto.AssessAnnualArrangeDTO;
import org.jeecg.modules.sys.dto.ConfirmVoteNumDTO;
import org.jeecg.modules.sys.entity.annual.AssessAnnualArrange;
import org.jeecg.modules.sys.entity.annual.AssessExcellentNum;

import java.util.List;

/**
 * @Description: 年度考核工作安排
 * @Author: jeecg-boot
 * @Date: 2024-09-04
 * @Version: V1.0
 */
public interface AssessExcellentNumMapper extends BaseMapper<AssessExcellentNum> {

}
