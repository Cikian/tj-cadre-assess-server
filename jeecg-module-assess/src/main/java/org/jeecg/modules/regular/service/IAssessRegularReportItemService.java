package org.jeecg.modules.regular.service;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.modules.depart.dto.DepartLeadersRegularGradeDTO;
import org.jeecg.modules.regular.entity.AssessRegularReportItem;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.system.entity.SysUser;

import java.util.List;

/**
 * @Description: 平时考核填报明细表
 * @Author: admin
 * @Date:   2024-08-30
 * @Version: V1.0
 */
public interface IAssessRegularReportItemService extends IService<AssessRegularReportItem> {

	/**
	 * 通过主表id查询子表数据
	 *
	 * @param mainId 主表id
	 * @return List<AssessRegularReportItem>
	 */
	public List<AssessRegularReportItem> selectByMainId(String mainId);

	/**
	 * 新增单条平时考核填报明细
	 *
	 * @param item 平时考核填报明细信息
	 */
	void addAssessRegularReportItem(AssessRegularReportItem item);

	/**
	 * 新增多条平时考核填报明细
	 *
	 * @param item 平时考核填报明细信息
	 */
	void addAssessRegularReportItem(List<AssessRegularReportItem> item);

	/**
	 * 根据主表更新子表考核成绩
	 *
	 * @param item 平时考核填报明细信息
	 */
	void upDataAssessRegularReportItem(List<AssessRegularReportItem> item);

	/**
	 * 根据单位id和年度获取单位领导平时考核成绩
	 * @param departId
	 * @param year
	 */
	List<DepartLeadersRegularGradeDTO> queryDepartLeadersRegularGrade(String departId, String year);

	void resetStatus(String year);

	JSONObject getAssessingStatus(SysUser user, List<AssessCurrentAssess> assessingList, JSONObject res);

	void deleteRetiree(String currentYear, List<AssessRegularReportItem> reportItems);

    void dontAssess(String id);
    void goon(String id);
}
