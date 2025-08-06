package org.jeecg.modules.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.SysDepartModel;
import org.jeecg.modules.sys.dto.AssessBusinessDepartGradeDTO;
import org.jeecg.modules.sys.dto.AssessBusinessGradeDTO;
import org.jeecg.modules.sys.entity.business.AssessBusinessFillItem;
import org.jeecg.modules.sys.entity.business.AssessBusinessGrade;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Description: 业务考核成绩汇总
 * @Author: jeecg-boot
 * @Date: 2024-08-28
 * @Version: V1.0
 */
public interface IAssessBusinessGradeService extends IService<AssessBusinessGrade> {
    boolean summaryScore(String currentYear, List<AssessBusinessFillItem> itemList);

    void calculateGradesByGradeId(String gradeId);

    void calculateGrades(List<String> gradeIds);

    Result<String> edit(AssessBusinessGrade assessBusinessGrade);

    Map<String, BigDecimal> getProportionCount(String year, String type);

    List<AssessBusinessGradeDTO> getReportQuery(String currentYear, List<String> departType);

    AssessBusinessDepartGradeDTO getDepartReportQuery(String currentYear, String departId,String exportDepart);

    List<AssessBusinessGrade> getByDepartIdAndYear(String departId, String year);

    List<SysDepartModel> getNonAssessDepart(String year);

    List<AssessBusinessGrade> getSummaryByYear(String year);

    void updateBusinessRanking(String currentYear);
    void updateBusinessRanking();

    void updateBusinessLevel(String currentYear);
    void updateBusinessLevel();

    List<AssessBusinessGrade> getGradesBetweenYears(String startYear, String endYear);

    List<AssessBusinessGrade> getGradeListByType(String departType,String year);

    List<AssessBusinessGrade> getGradeListByTypeNotEqualTo(String departType,String year);

    List<String> getDepartAliases(String type);

    List<AssessBusinessGrade> listByCurrentYear(String year);

    List<AssessBusinessGrade> selectByDepartType(String departType,String year);

    /**
     * 更新汇总表加减分原因
     * @param departId 处室（单位）id
     * @param year 年度
     * @param isAdd true为加分，false为减分
     * @return
     */
    boolean updateAddOrSubtractReason(String departId,String year,boolean isAdd);

    Map<String,String>  getItemReason(String departId, String year);  // 获取考核项扣分原因

    List<AssessBusinessGrade>  sort(List<AssessBusinessGrade> list);  // 单位排序

    Map<String,List<AssessBusinessGrade>>  groupByDepartType(List<AssessBusinessGrade> list);  // 按单位类型分组

}
