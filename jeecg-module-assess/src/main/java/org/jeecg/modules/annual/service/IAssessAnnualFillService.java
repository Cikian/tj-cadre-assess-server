package org.jeecg.modules.annual.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.annual.entity.AnnualIndexEmployee;
import org.jeecg.modules.annual.entity.AnnualIndexLeader;
import org.jeecg.modules.annual.vo.AssessAnnualListVo;
import org.jeecg.modules.sys.dto.AnnualAssessDepartFillDTO;
import org.jeecg.modules.sys.dto.RegularGradeDTO;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @Description: 年度考核填报列表
 * @Author: jeecg-boot
 * @Date: 2024-09-04
 * @Version: V1.0
 */
public interface IAssessAnnualFillService extends IService<AssessAnnualFill> {

    /**
     * 添加一对多
     *
     * @param assessAnnualFill
     * @param assessAnnualArrangeList
     */
    public void saveMain(AssessAnnualFill assessAnnualFill, List<AssessAnnualArrange> assessAnnualArrangeList);

    /**
     * 修改一对多
     *
     * @param annualFill
     * @param arrangeList
     * @param summaryList
     * @param isSave
     */
    void updateMain(AssessAnnualFill annualFill,
                    List<AssessAnnualArrange> arrangeList,
                    List<AssessAnnualSummary> summaryList,
                    boolean isSave);

    /**
     * 删除一对多
     *
     * @param id
     */
    void delMain(String id);

    /**
     * 批量删除一对多
     *
     * @param idList
     */
    void delBatchMain(Collection<? extends Serializable> idList);

    void initAssess(AnnualAssessDepartFillDTO map);

    // TODO 此方法应该在平时考核模块中
    List<RegularGradeDTO> queryRegularGrade(List<String> userIds, String year);

    @Deprecated
    List<AssessAnnualListVo> getSummaryList(IPage<AssessAnnualFill> pageList);


    /**
     * 年度考核首页领导信息
     *
     */
    public AnnualIndexLeader getAllItems();

    /**
     * 年度考核首页员工信息
     *
     */
    public AnnualIndexEmployee[] getPartial(String departId, AssessCurrentAssess assessInfo);

    List<AssessAnnualSummary> getEvaluationSuggestion(List<AssessAnnualSummary> summaryList);

    AssessAnnualSummary getEvaluationSuggestion(AssessAnnualSummary summary);

    Result<String> passAudit(String assessId, String name);

    void stopAssess();
    void revocationAssess();
    void stopDem();

    Boolean checkDepartFillOver (String year);

    void getEvaluationSuggestionByBusiness(String departId, String year);

}
