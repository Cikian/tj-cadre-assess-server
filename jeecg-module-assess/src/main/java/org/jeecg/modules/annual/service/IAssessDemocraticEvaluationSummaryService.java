package org.jeecg.modules.annual.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.jeecg.modules.annual.dto.Dem4jjGradeDTO;
import org.jeecg.modules.annual.dto.DemGradeDTO;
import org.jeecg.modules.annual.dto.DemGradeItemsDTO;
import org.jeecg.modules.annual.vo.DepartProgressVO;
import org.jeecg.modules.annual.vo.LeaderProgressVO;
import org.jeecg.modules.sys.dto.DemocraticInitDTO;
import org.jeecg.modules.sys.entity.annual.AssessDemocraticEvaluation;
import org.jeecg.modules.sys.entity.annual.AssessDemocraticEvaluationSummary;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @Description: 民主测评汇总
 * @Author: jeecg-boot
 * @Date: 2024-09-12
 * @Version: V1.0
 */
public interface IAssessDemocraticEvaluationSummaryService extends IService<AssessDemocraticEvaluationSummary> {

    /**
     * 添加一对多
     *
     * @param assessDemocraticEvaluationSummary
     * @param assessDemocraticEvaluationList
     */
    public void saveMain(AssessDemocraticEvaluationSummary assessDemocraticEvaluationSummary, List<AssessDemocraticEvaluation> assessDemocraticEvaluationList);

    List<AssessDemocraticEvaluationSummary> getData(QueryWrapper<AssessDemocraticEvaluationSummary> queryWrapper);

    /**
     * 修改一对多
     *
     * @param assessDemocraticEvaluationSummary
     * @param assessDemocraticEvaluationList
     */
    public void updateMain(AssessDemocraticEvaluationSummary assessDemocraticEvaluationSummary,
                           List<AssessDemocraticEvaluation> assessDemocraticEvaluationList);

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

    void statisticalScore(List<String> ids);
    void updateRanking(String year);
    void updateScopeRanking(String year);

    List<AssessDemocraticEvaluationSummary> getByAnnualId(String annualId);

    List<LeaderProgressVO> getLeaderProgress();

    List<DepartProgressVO> getDepartProgressByYear(String year);

    List<DemGradeDTO> getSummary4Excel(String year, String departScope, String personType, String leader);

    List<DemGradeItemsDTO> getSummaryItems4Excel(String year, String departScope, String personType, String leader);

    List<Dem4jjGradeDTO> getSummaryItems4jjExcel(String year);

    void reRanking();

}
