package org.jeecg.modules.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.sys.dto.*;
import org.jeecg.modules.sys.entity.report.AssessReportEvaluationItem;
import org.jeecg.modules.sys.entity.report.AssessReportEvaluationSummary;
import org.jeecg.modules.report.vo.AssessReportDemocracyFillVO;
import org.jeecg.modules.sys.entity.report.AssessReportNewLeader;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Description: 一报告两评议民主测评汇总
 * @Author: jeecg-boot
 * @Date: 2024-10-08
 * @Version: V1.0
 */
public interface IAssessReportEvaluationSummaryService extends IService<AssessReportEvaluationSummary> {

    /**
     * 添加一对多
     *
     * @param assessReportEvaluationSummary
     * @param assessReportEvaluationItemList
     */
    public void saveMain(AssessReportEvaluationSummary assessReportEvaluationSummary, List<AssessReportEvaluationItem> assessReportEvaluationItemList);

    /**
     * 修改一对多
     *
     * @param assessReportEvaluationSummary
     * @param assessReportEvaluationItemList
     */
    public void updateMain(AssessReportEvaluationSummary assessReportEvaluationSummary, List<AssessReportEvaluationItem> assessReportEvaluationItemList);

    /**
     * 删除一对多
     *
     * @param id
     */
    public void delMain(String id);

    /**
     * 批量删除一对多
     *
     * @param idList
     */
    public void delBatchMain(Collection<? extends Serializable> idList);

    boolean initDemocratic(DemocraticInitDTO map);

    void saveDemocracy(AssessReportDemocracyFillVO map);

    Map<String, List<?>> getFillData(String summaryId);

    List<ReportNewLeaderDTO> getNewLeaderSummaryByYear(String year, String column, String order);

    List<ReportEnterpriseAgreeRankDTO> getEnterpriseAgreeRankByYear(String year);

    List<ReportLeaderWorkRankDTO> getLeaderWorkRankByQuestion1(String year);

    List<ReportLeaderWorkRankDTO> getLeaderWorkRankByQuestion2(String year);

    List<ReportLeaderWorkRankDTO> getLeaderWorkRankByQuestion3(String year);

    List<ReportLeaderWorkDetailsDTO> getLeaderWorkDetailsData(String year);

    List<ReportDepartLeaderDataDTO> getLeaderDataByDepart(String year,String depart);

    void reRanking();

}
