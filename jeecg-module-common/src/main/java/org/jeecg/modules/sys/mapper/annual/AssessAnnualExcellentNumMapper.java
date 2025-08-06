package org.jeecg.modules.sys.mapper.annual;

import org.jeecg.modules.sys.entity.annual.AssessAnnualExcellentNum;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @Description: 年度考核分管领导优秀名额
 * @Author: jeecg-boot
 * @Date:   2024-09-18
 * @Version: V1.0
 */
public interface AssessAnnualExcellentNumMapper extends BaseMapper<AssessAnnualExcellentNum> {
    List<AssessAnnualExcellentNum> getInitLeaderData();

}
