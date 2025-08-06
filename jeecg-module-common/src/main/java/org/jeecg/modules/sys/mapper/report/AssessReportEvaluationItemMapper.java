package org.jeecg.modules.sys.mapper.report;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.sys.dto.ReportDemocracyGoodNum;
import org.jeecg.modules.sys.entity.report.AssessReportEvaluationItem;

import java.util.List;

/**
 * @Description: 一报告两评议民主测评项
 * @Author: jeecg-boot
 * @Date: 2024-10-08
 * @Version: V1.0
 */
public interface AssessReportEvaluationItemMapper extends BaseMapper<AssessReportEvaluationItem> {

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
     * @return List<AssessReportEvaluationItem>
     */
    public List<AssessReportEvaluationItem> selectByMainId(@Param("mainId") String mainId);

    List<ReportDemocracyGoodNum> getRankingByUpdate(List<String> summaryIds);
}
