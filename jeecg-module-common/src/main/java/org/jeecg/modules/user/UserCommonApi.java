package org.jeecg.modules.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.sys.dto.UserToSelectDTO;
import org.jeecg.modules.sys.entity.LeaderDepartHistory;
import org.jeecg.modules.sys.entity.business.AssessLeaderDepartConfig;
import org.jeecg.modules.system.entity.SysUser;

import java.util.List;
import java.util.Map;

public interface UserCommonApi {
    IPage<SysUser> queryDepartUserPageList(String departId,
                                           String username,
                                           String realname,
                                           int pageSize,
                                           int pageNo,
                                           String id,
                                           boolean getLeader,
                                           String year);

    /**
     * 通过用户id（数组）获取用户
     *
     * @param userIds
     * @return List<LoginUser>
     */
    List<LoginUser> getUserByIds(String[] userIds);

    /**
     * 通过角色编码获取用户
     *
     * @param roleCode
     * @return LoginUser
     */
    List<SysUser> getUserByRoleCode(String roleCode);


    /**
     * 通过部门id获取用户
     *
     * @param orgId
     * @return SysUser
     */
    List<SysUser> getUserByOrgId(String orgId);

    /**
     * 判断某个用户是否包含角色
     *
     * @param username
     * @param roles
     * @return
     */
    boolean hasRole(String username, List<String> roles);

    /**
     * 获取所有人数
     *
     * @return Integer
     */
    Integer getTotalPeopleNum();

    /**
     * 搜索用户（前端select使用）
     *
     * @param value
     */
    List<UserToSelectDTO> getSelectedUser(String value, String departId);

    /**
     * 生成匿名账号
     *
     * @param assess：测评类型 ？ "annual" or "report"
     * @return
     */
    List<SysUser> generateAnonymousAccount(String assess, String currentYear);

    /**
     * 通过测评类型获取匿名账号
     *
     * @param assess：测评类型 ？ "annual" or "report"
     * @return
     */
    List<SysUser> getAnonymousAccountByAssess(String assess);

    /**
     * 获取所有匿名账号
     *
     * @return
     */
    List<SysUser> getAllAnonymousAccount();

    /**
     * 获取所有匿名账号-分页
     *
     * @return
     */
    IPage<SysUser> getAllAnonymousAccount(Page<SysUser> page);

    /**
     * 获取用户角色
     *
     */
    List<String> getUserRolesByUserName(String userName);

    /**
     * 根据id删除用户
     * */
    void deleteById(String id);

    /**
     * 新增匿名账号
     */
    List<SysUser> addAnonymousAccount(String assess, String depart, String voteType, int voteNum);

    /**
     * 获取所有局领导
     */
    List<SysUser> getAllLeader();
    List<SysUser> getCurrentLeader();
    List<SysUser> getHistoryAssessUnit(String year);
    List<String> getHistoryUnitDeptIds(List<String> leaders, String year);
    List<String> getAssessUnitDepart(String year, String leader);

    /**
     * 删除当前登录用户，慎用！！！
     *
     * @return
     */
    boolean deleteCurrentUser();

    void deleteAllAnonymousAccount(String type);

    /**
     * 判断考核单元内是否有总师、二巡
     */
    Boolean hasChiefEngineer(AssessLeaderDepartConfig config);

    Map<String, String> getPeopleInfo(String hashId, String year);

}
