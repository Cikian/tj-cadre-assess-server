package org.jeecg.modules.annual.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.annual.vo.AssessAnnualExcellentConfigVO;
import org.jeecg.modules.sys.entity.annual.AssessAnnualExcellentNum;
import org.jeecg.modules.sys.entity.annual.AssessAnnualExcellentRatio;

import java.util.List;

/**
 * @Description: 年度考核优秀比例设置
 * @Author: jeecg-boot
 * @Date: 2024-09-16
 * @Version: V1.0
 */
public interface IAssessAnnualExcellentRatioService extends IService<AssessAnnualExcellentRatio> {

    /**
     * 添加一对多
     *
     * @param assessAnnualExcellentRatio
     */
    public void saveMain(AssessAnnualExcellentRatio assessAnnualExcellentRatio);

    /**
     * 修改一对多
     *
     * @param assessAnnualExcellentRatio
     */
    public void updateMain(AssessAnnualExcellentRatio assessAnnualExcellentRatio);

    List<AssessAnnualExcellentNum> getInitLeaderData();

    boolean updateEnable(String id);

    AssessAnnualExcellentConfigVO getEnableConfig();


}
