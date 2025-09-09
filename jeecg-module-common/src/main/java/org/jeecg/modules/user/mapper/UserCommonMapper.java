package org.jeecg.modules.user.mapper;

import org.jeecg.modules.system.entity.SysUser;

import java.util.List;

public interface UserCommonMapper {
    List<SysUser> getUserByRoleCode(String roleCode);

    Integer getTotalPeopleNum();

    List<SysUser> getAllLeaders();

    List<SysUser> getHistoryAssessUnit(String year);
}
