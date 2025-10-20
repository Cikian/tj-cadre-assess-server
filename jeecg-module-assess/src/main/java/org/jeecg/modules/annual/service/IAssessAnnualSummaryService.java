package org.jeecg.modules.annual.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.annual.vo.*;
import org.jeecg.modules.common.vo.WordTemResVo;
import org.jeecg.modules.sys.entity.annual.AssessAnnualRecommendTemp;
import org.jeecg.modules.sys.entity.annual.AssessAnnualSummary;
import org.jeecg.modules.sys.entity.annual.AssessLeaderRecItem;

import java.util.List;
import java.util.Map;

/**
 * @Description: 年度考核成绩汇总
 * @Author: jeecg-boot
 * @Date: 2024-09-10
 * @Version: V1.0
 */
public interface IAssessAnnualSummaryService extends IService<AssessAnnualSummary> {

    String getAssessLastYear();

    void leaderRecommend(String summaryId);

    List<AssessAnnualSummary> recommendList(String year);

    List<AssessAnnualSummary> queryPersonByFillId(String fillId);

    void recommend(RecommendVO recommendVO);

    void submitMerit(String year, List<String> idList);

    void submitPartyAssess(String year, List<String> idList);

    void submitInspection(String year, List<String> idList);

    AssessAnnualSummary getGroupSummary(String fillId);

    List<AssessAnnualSummary> getMerit(String depart);

    List<AssessAnnualSummary> getPartyOrganization();

    List<AssessLeaderRecItem> getLeaderRecommendList(String year);

    List<AssessLeaderRecItem> getLeaderRecommendListByMainId(String id);

    List<AssessLeaderRecItem> getLeaderRecommendList(String year, String leader);

    List<RecommendTempVO> getRecommendTemp(String year);

    void returnLeaderRecommend(String recommendTempId);

    List<AssessAnnualRecommendTemp> getRecommendTempExl(String year);

    List<InspectionVO> getInspection(String year);

    List<AnnualGroupSummaryVO> getGroupSummaryInfo(List<AssessAnnualSummary> page, String year);

    List<AssessAnnualSummary> queryByIds(List<String> ids, String year);

    List<AssessAnnualSummary> getNotExcellent(LambdaQueryWrapper<AssessAnnualSummary> lqw);

    List<AssessAnnualSummary> getAssessResByType(String year, String type);

    List<String> getDistancePartyAssessIdList(String year, List<String> idList, String type);

    List<String> notExcellent(String year);

    Map<String, Object> assertMap();

    Map<String, Object> assertMap2Res();

    WordTemResVo assertMap2List();

    List<Map<String, String>> getDetailInfo(String year, String departId);
    List<Map<String, String>> getDetailFill(String year, String departId);
    List<Map<String, String>> getDetailPerson(String year, String departId);
    List<Map<String, String>> getDetailAccount(String year, String departId);
    List<Map<String, String>> getDetailVacation(String year, String departId);

    void editPerson(AssessAnnualSummary summary);

    void editDepart(String hashId, String targetDepart);

    List<DepartResVO> getDepartRes(String year);


}
