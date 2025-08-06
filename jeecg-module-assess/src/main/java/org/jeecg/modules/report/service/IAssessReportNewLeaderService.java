package org.jeecg.modules.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.sys.entity.report.AssessReportNewLeader;
import org.jeecg.modules.report.vo.AssessReportDemocracyNewLeaderVO;

import java.util.List;

/**
 * @Description: 新提拔干部名册
 * @Author: jeecg-boot
 * @Date: 2024-09-24
 * @Version: V1.0
 */
public interface IAssessReportNewLeaderService extends IService<AssessReportNewLeader> {

    List<AssessReportNewLeader> getByFillId(String fillId);

    List<AssessReportDemocracyNewLeaderVO> getDemocracyItemByFillId(String fillId);

}
