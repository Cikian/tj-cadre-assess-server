package org.jeecg.modules.depart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.depart.dto.DepartLeadersDTO;
import org.jeecg.modules.depart.dto.DepartLeadersRegularGradeDTO;
import org.jeecg.modules.system.entity.SysUser;

import java.util.List;
import java.util.Map;

@Mapper
public interface DepartCommonMapper {
    List<DepartLeadersDTO> queryDepartLeaders(String departId);

    List<SysUser> queryDepartLeadersByPage(String departId, int offset, int depth);

    List<DepartLeadersRegularGradeDTO> queryDepartLeadersAndRegularGrade(String departId, String year);

    DepartLeadersRegularGradeDTO queryDepartLeaderAndRegularGrade(String hashId, String year);

    int getDepartLeadersCount(String departId);
}
