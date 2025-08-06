package org.jeecg.modules.report.service.impl;

import org.jeecg.modules.sys.entity.report.AssessReportDemocracyConfigItem;
import org.jeecg.modules.sys.mapper.report.AssessReportDemocracyConfigItemMapper;
import org.jeecg.modules.report.service.IAssessReportDemocracyConfigItemService;
import org.springframework.stereotype.Service;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Description: 一报告两评议民主测评项
 * @Author: jeecg-boot
 * @Date:   2024-10-07
 * @Version: V1.0
 */
@Service
public class AssessReportDemocracyConfigItemServiceImpl extends ServiceImpl<AssessReportDemocracyConfigItemMapper, AssessReportDemocracyConfigItem> implements IAssessReportDemocracyConfigItemService {
	
	@Autowired
	private AssessReportDemocracyConfigItemMapper assessReportDemocracyConfigItemMapper;
	
	@Override
	public List<AssessReportDemocracyConfigItem> selectByMainId(String mainId) {
		return assessReportDemocracyConfigItemMapper.selectByMainId(mainId);
	}
}
