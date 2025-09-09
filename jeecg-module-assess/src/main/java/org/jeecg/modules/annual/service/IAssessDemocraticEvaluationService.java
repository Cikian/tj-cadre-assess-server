package org.jeecg.modules.annual.service;

import org.jeecg.modules.sys.entity.annual.AssessDemocraticEvaluation;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.annual.vo.DemocraticFillListVO;
import org.jeecg.modules.annual.vo.DemocraticSubmitPage;
import org.jeecg.modules.system.entity.SysUser;

import java.util.List;

/**
 * @Description: 民主测评填报
 * @Author: jeecg-boot
 * @Date:   2024-09-12
 * @Version: V1.0
 */
public interface IAssessDemocraticEvaluationService extends IService<AssessDemocraticEvaluation> {

  /**
   * 通过主表id查询子表数据
   *
   * @param mainId 主表id
   * @return List<AssessDemocraticEvaluation>
   */
	public List<AssessDemocraticEvaluation> selectByMainId(String mainId);

    List<DemocraticFillListVO> getAnonymousList();
    List<DemocraticFillListVO> getAnonymousList(SysUser user);

    void submit(List<DemocraticSubmitPage> democraticSubmitPage);
}
