package org.jeecg.modules.regular.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.regular.entity.AssessRegularReportItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 平时考核结果
 * @Author: admin
 * @Date:   2024-10-09
 * @Version: V1.0
 */
public interface AssessRegularResultMapper extends BaseMapper<AssessRegularReportItem> {

    void deleteByYear(String year);

}
