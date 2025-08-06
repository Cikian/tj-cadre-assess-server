package org.jeecg.modules.sys.mapper.business;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.sys.dto.AssessBusinessDepartGradeDTO;
import org.jeecg.modules.sys.dto.AssessBusinessGradeDTO;
import org.jeecg.modules.sys.dto.GradeExportDTO;
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
public interface AssessBusinessGradeMapper extends BaseMapper<AssessBusinessGrade> {
    void updateRankingByYear(@Param("currentYear") String currentYear); // 更新年度排名

    void updateLevelByYear(String currentYear, BigDecimal excellent, BigDecimal fine);  // 更新等次

    int getExcellentCount(@Param("currentYear") String currentYear, @Param("type") List<String> type);  // 获取优秀人数

    int getGoodCount(@Param("currentYear") String currentYear, @Param("type") List<String> type);  // 获取良好人数

    int getGeneralCount(@Param("currentYear") String currentYear, @Param("type") List<String> type);  // 获取一般人数

    List<AssessBusinessGradeDTO> getReportQuery(@Param("currentYear") String currentYear, @Param("departType") List<String> departType);  // 获取报表查询

    AssessBusinessDepartGradeDTO getDepartReportQuery(@Param("currentYear") String currentYear, @Param("departId") String departId);  // 获取报表查询（单个单位）

    List<AssessBusinessGrade> queryBusinessGradeByDepartIds(@Param("departIds") List<String> departIds, @Param("year") String currentYear);  // 通过用户id查询业务考核成绩

    List<String> getDepartIdsByScope(@Param("scope") List<String> scope, @Param("currentYear") String currentYear);  // 通过范围获取单位id

    List<AssessBusinessGrade> selectByDepartTypeNotEqualTo(@Param("departType") String departType,@Param("year") String year);

    List<GradeExportDTO> getSummary(@Param("year") String year, @Param("departType") List<String> departType);

    @Select("SELECT \n" +
            "    a.DEPART_ID as departId,a.DEPART_TYPE as departType,a.RANKING AS rankingVirtual,a.SCORE AS score,a.LEVEL AS level,a.CURRENT_YEAR as currentYear,b.DEPART_NAME as departName,b.DEPART_ORDER as departOrder,\n" +
            "    ROW_NUMBER() OVER (PARTITION BY a.DEPART_TYPE ORDER BY a.RANKING,b.DEPART_ORDER ASC) AS row_num\n" +
            "FROM \n" +
            "    ASSESS_BUSINESS_GRADE a INNER JOIN SYS_DEPART b  ON a.DEPART_ID = b.id\n" +
            "WHERE \n" +
            "    CURRENT_YEAR=#{year}")
    List<AssessBusinessGradeDTO>  getGradeListByRank(@Param("year") String year);

}
