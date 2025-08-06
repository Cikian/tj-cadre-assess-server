package org.jeecg.modules.regular.service;

import org.jeecg.modules.regular.entity.AssessRegularReportItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @Description: 平时考核结果
 * @Author: admin
 * @Date:   2024-10-09
 * @Version: V1.0
 */
public interface IAssessRegularResultService extends IService<AssessRegularReportItem> {

    void deleteByCurrentYear(String year);

    Map<String, Object> getCurrentDetail();

    Map<String, Boolean> getLeaderPro(String year, String q);
}