package org.jeecg.modules.sys.mapper.annual;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.sys.entity.annual.AssessAnnualDutyReport;

import java.util.List;

/**
 * @Description: 述职报告
 * @Author: jeecg-boot
 * @Date: 2024-09-04
 * @Version: V1.0
 */
public interface AssessAnnualDutyReportMapper extends BaseMapper<AssessAnnualDutyReport> {

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
     * @return List<AssessAnnualDutyReport>
     */
    public List<AssessAnnualDutyReport> selectByMainId(@Param("mainId") String mainId);
}
