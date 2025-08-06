package org.jeecg.modules.report.service.impl;

import org.jeecg.modules.sys.entity.report.AssessReportEvaluationItem;
import org.jeecg.modules.sys.mapper.report.AssessReportEvaluationItemMapper;
import org.jeecg.modules.report.service.IAssessReportEvaluationItemService;
import org.springframework.stereotype.Service;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Description: 一报告两评议民主测评项
 * @Author: jeecg-boot
 * @Date:   2024-10-08
 * @Version: V1.0
 */
@Service
public class AssessReportEvaluationItemServiceImpl extends ServiceImpl<AssessReportEvaluationItemMapper, AssessReportEvaluationItem> implements IAssessReportEvaluationItemService {
	
	@Autowired
	private AssessReportEvaluationItemMapper assessReportEvaluationItemMapper;
	
	@Override
	public List<AssessReportEvaluationItem> selectByMainId(String mainId) {
		return assessReportEvaluationItemMapper.selectByMainId(mainId);
	}
}
