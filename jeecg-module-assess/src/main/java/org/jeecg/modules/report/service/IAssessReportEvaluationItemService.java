package org.jeecg.modules.report.service;

import org.jeecg.modules.sys.entity.report.AssessReportEvaluationItem;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * @Description: 一报告两评议民主测评项
 * @Author: jeecg-boot
 * @Date:   2024-10-08
 * @Version: V1.0
 */
public interface IAssessReportEvaluationItemService extends IService<AssessReportEvaluationItem> {

  /**
   * 通过主表id查询子表数据
   *
   * @param mainId 主表id
   * @return List<AssessReportEvaluationItem>
   */
	public List<AssessReportEvaluationItem> selectByMainId(String mainId);
}
