package org.jeecg.modules.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.sys.entity.report.AssessReportArrange;

/**
 * @Description: 一报告两评议工作安排
 * @Author: jeecg-boot
 * @Date:   2024-09-24
 * @Version: V1.0
 */
public interface IAssessReportArrangeService extends IService<AssessReportArrange> {
    AssessReportArrange getByFillId(String fillId);

}
