package org.jeecg.modules.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.report.entity.ReportIndexEmployee;
import org.jeecg.modules.report.entity.ReportIndexLeader;
import org.jeecg.modules.sys.dto.ReportAssessInitDTO;
import org.jeecg.modules.sys.dto.ReportFillDTO;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.report.AssessReportFill;

/**
 * @Description: 一报告两评议填报
 * @Author: jeecg-boot
 * @Date: 2024-09-24
 * @Version: V1.0
 */
public interface IAssessReportFillService extends IService<AssessReportFill> {
    boolean initAssess(ReportAssessInitDTO reportAssessInitDTO);

    void submitFill(ReportFillDTO reportFillDTO, boolean check, boolean pass);


    /**
     * 一报告两评议首页信息
     *
     */
    public ReportIndexLeader getAllItems();

    /**
     * 年度考核首页员工信息
     *
     */
    public ReportIndexEmployee[] getPartial(String departId, AssessCurrentAssess assessInfo);

    void stopAssess();
    void revocationAssess();
    void stopDem();
}
