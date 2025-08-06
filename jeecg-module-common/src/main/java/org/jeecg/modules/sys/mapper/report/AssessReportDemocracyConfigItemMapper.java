package org.jeecg.modules.sys.mapper.report;

import java.util.List;
import org.jeecg.modules.sys.entity.report.AssessReportDemocracyConfigItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Description: 一报告两评议民主测评项
 * @Author: jeecg-boot
 * @Date:   2024-10-07
 * @Version: V1.0
 */
public interface AssessReportDemocracyConfigItemMapper extends BaseMapper<AssessReportDemocracyConfigItem> {

  /**
   * 通过主表id删除子表数据
   *
   * @param mainId 主表id
   * @return boolean
   */
	public boolean deleteByMainId(@Param("mainId") String mainId);

  /**
   * 通过主表id查询子表数据
   *
   * @param mainId 主表id
   * @return List<AssessReportDemocracyConfigItem>
   */
	public List<AssessReportDemocracyConfigItem> selectByMainId(@Param("mainId") String mainId);
}
