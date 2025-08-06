package org.jeecg.modules.sys.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.system.entity.SysCategory;
import org.jeecg.modules.system.entity.SysDictItem;

import java.util.List;

@Mapper
public interface SysCommonMapper {
    List<SysCategory> getAllLeafByParentCode(String code);

    List<SysDictItem> getDictItemsByCode(String dictCode);

    Integer getRemainingRecommendedNum(String leaderId, String year);

    String getLeaderDeparts(String leaderId);

    Integer getRecommendedCounts(String year, List<String> departsList);

    String getAssessLastYear();
}
