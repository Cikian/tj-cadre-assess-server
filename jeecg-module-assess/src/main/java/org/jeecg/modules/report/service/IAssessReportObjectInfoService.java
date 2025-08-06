package org.jeecg.modules.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.sys.entity.report.AssessReportObjectInfo;

import java.util.List;

/**
 * @Description: 评议对象有关信息汇总表
 * @Author: jeecg-boot
 * @Date: 2024-09-24
 * @Version: V1.0
 */
public interface IAssessReportObjectInfoService extends IService<AssessReportObjectInfo> {
    List<AssessReportObjectInfo> getByFillId(String fillId);
}
