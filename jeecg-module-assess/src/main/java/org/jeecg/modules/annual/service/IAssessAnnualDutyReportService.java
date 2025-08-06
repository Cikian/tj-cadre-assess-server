package org.jeecg.modules.annual.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.sys.entity.annual.AssessAnnualDutyReport;

import java.util.List;

/**
 * @Description: 述职报告
 * @Author: jeecg-boot
 * @Date: 2024-09-04
 * @Version: V1.0
 */
public interface IAssessAnnualDutyReportService extends IService<AssessAnnualDutyReport> {

    /**
     * 通过主表id查询子表数据
     *
     * @param mainId 主表id
     * @return List<AssessAnnualDutyReport>
     */
    public List<AssessAnnualDutyReport> selectByMainId(String mainId);

    List<AssessAnnualDutyReport> queryByFillId(String fillId);
}
