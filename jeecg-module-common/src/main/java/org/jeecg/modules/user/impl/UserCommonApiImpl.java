package org.jeecg.modules.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.system.vo.SysDepartModel;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.depart.mapper.DepartCommonMapper;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.dto.ArrangeDTO;
import org.jeecg.modules.sys.dto.UserToSelectDTO;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.LeaderDepartHistory;
import org.jeecg.modules.sys.entity.annual.AssessAnnualArrange;
import org.jeecg.modules.sys.entity.annual.AssessAnnualFill;
import org.jeecg.modules.sys.entity.annual.AssessAnnualSummary;
import org.jeecg.modules.sys.entity.annual.AssessDemocraticEvaluationSummary;
import org.jeecg.modules.sys.entity.business.AssessLeaderDepartConfig;
import org.jeecg.modules.sys.entity.report.AssessReportEvaluationSummary;
import org.jeecg.modules.sys.mapper.LeaderDepartHistoryMapper;
import org.jeecg.modules.sys.mapper.annual.AssessAnnualArrangeMapper;
import org.jeecg.modules.sys.mapper.annual.AssessAnnualFillMapper;
import org.jeecg.modules.sys.mapper.annual.AssessAnnualSummaryMapper;
import org.jeecg.modules.sys.mapper.annual.AssessDemocraticEvaluationSummaryMapper;
import org.jeecg.modules.sys.mapper.business.AssessLeaderDepartConfigMapper;
import org.jeecg.modules.sys.mapper.report.AssessReportArrangeMapper;
import org.jeecg.modules.sys.mapper.report.AssessReportEvaluationSummaryMapper;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.mapper.SysThirdAccountMapper;
import org.jeecg.modules.system.mapper.SysUserDepartMapper;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.mapper.SysUserRoleMapper;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysRoleService;
import org.jeecg.modules.system.service.ISysUserDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.service.impl.ThirdAppDingtalkServiceImpl;
import org.jeecg.modules.system.service.impl.ThirdAppWechatEnterpriseServiceImpl;
import org.jeecg.modules.system.vo.SysUserAliasVo;
import org.jeecg.modules.system.vo.SysUserDepVo;
import org.jeecg.modules.user.UserCommonApi;
import org.jeecg.modules.user.mapper.UserCommonMapper;
import org.jeecg.modules.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserCommonApiImpl extends ServiceImpl<SysUserDepartMapper, SysUserDepart> implements UserCommonApi {
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private ISysDepartService sysDepartService;
    @Autowired
    private ISysUserDepartService sysUserDepartService;
    @Autowired
    private DepartCommonMapper departCommonMapper;
    @Autowired
    private ISysBaseAPI sysBaseAPI;
    @Autowired
    private UserCommonMapper userCommonMapper;
    @Autowired
    private AssessAnnualSummaryMapper annualSummaryMapper;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private AssessAnnualArrangeMapper annualArrangeMapper;
    @Autowired
    private AssessReportArrangeMapper reportArrangeMapper;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private AssessLeaderDepartConfigMapper leaderDepartConfigMapper;
    @Autowired
    private DepartCommonApi departCommonApi;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private AssessDemocraticEvaluationSummaryMapper democraticEvaluationSummaryMapper;
    @Autowired
    private AssessReportEvaluationSummaryMapper reportEvaluationSummaryMapper;
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;
    @Autowired
    private SysUserDepartMapper sysUserDepartMapper;
    @Autowired
    ThirdAppWechatEnterpriseServiceImpl wechatEnterpriseService;
    @Autowired
    ThirdAppDingtalkServiceImpl dingtalkService;
    @Autowired
    private SysThirdAccountMapper sysThirdAccountMapper;
    @Autowired
    private AssessAnnualFillMapper annualFillMapper;
    @Autowired
    private LeaderDepartHistoryMapper ldHistoryMapper;

    @Override
    public IPage<SysUser> queryDepartUserPageList(String departId, String username, String realname, int pageSize, int pageNo, String id, boolean getLeader, String year) {
        IPage<SysUser> pageList = null;
        if (!getLeader) {
            // 部门ID不存在 直接查询用户表即可
            Page<SysUser> page = new Page<SysUser>(pageNo, pageSize);
            if (oConvertUtils.isEmpty(departId)) {
                LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
                // update-begin---author:wangshuai ---date:20220104  for：[JTC-297]已冻结用户仍可设置为代理人------------
                query.eq(SysUser::getStatus, Integer.parseInt(CommonConstant.STATUS_1));
                // update-end---author:wangshuai ---date:20220104  for：[JTC-297]已冻结用户仍可设置为代理人------------
                if (oConvertUtils.isNotEmpty(username)) {
                    query.like(SysUser::getUsername, username);
                }
                // update-begin---author:wangshuai ---date:20220608  for：[VUEN-1238]邮箱回复时，发送到显示的为用户id------------
                if (oConvertUtils.isNotEmpty(id)) {
                    query.eq(SysUser::getId, id);
                }
                // update-end---author:wangshuai ---date:20220608  for：[VUEN-1238]邮箱回复时，发送到显示的为用户id------------
                // update-begin---author:wangshuai ---date:20220902  for：[VUEN-2121]临时用户不能直接显示------------
                query.ne(SysUser::getUsername, "_reserve_user_external");
                // update-end---author:wangshuai ---date:20220902  for：[VUEN-2121]临时用户不能直接显示------------
                pageList = sysUserMapper.selectPage(page, query);
            } else {
                // 有部门ID 需要走自定义sql
                SysDepart sysDepart = sysDepartService.getById(departId);
                pageList = this.baseMapper.queryDepartUserPageList(page, sysDepart.getOrgCode(), username, realname);
            }
        } else {
            List<SysUser> sysUsers;
            if (!"0".equals(year)) {
                sysUsers = this.getHistoryAssessUnit(year);
            } else {
                sysUsers = this.getCurrentLeader();
            }
            pageList = new Page<>();
            pageList.setRecords(sysUsers);
        }
        List<SysUser> userList = pageList.getRecords();
        if (userList != null && !userList.isEmpty()) {
            List<String> userIds = userList.stream().map(SysUser::getId).collect(Collectors.toList());
            Map<String, SysUser> map = new HashMap<>(5);
            if (!userIds.isEmpty()) {
                // 查部门名称
                // Map<String, String> useDepNames = this.getDepNamesByUserIds(userIds);
                Map<String, String> useDepNames = this.getDepAliasByUserIds(userIds);
                userList.forEach(item -> {
                    // TODO 临时借用这个字段用于页面展示
                    item.setOrgCodeTxt(useDepNames.get(item.getId()));
                    item.setSalt("");
                    item.setPassword("");
                    // 去重
                    map.put(item.getId(), item);
                });
            }
            pageList.setRecords(new ArrayList<>(map.values()));
        }
        return pageList;
    }

    @Override
    public List<LoginUser> getUserByIds(String[] userIds) {
        return sysBaseAPI.queryAllUserByIds(userIds);

    }

    @Override
    public List<SysUser> getUserByRoleCode(String roleCode) {
        List<SysUser> userByRoleCode = userCommonMapper.getUserByRoleCode(roleCode);
        // 移除username中包含“admin”的用户
        userByRoleCode.removeIf(item -> item.getUsername().contains("admin"));
        return  userByRoleCode;
    }

    /**
     * 升级SpringBoot2.6.6,不允许循环依赖
     *
     * @param userIds
     * @return
     */
    private Map<String, String> getDepNamesByUserIds(List<String> userIds) {
        List<SysUserDepVo> list = sysUserMapper.getDepNamesByUserIds(userIds);

        Map<String, String> res = new HashMap<>(5);
        list.forEach(item -> {
                    res.merge(item.getUserId(), item.getDepartName(), (a, b) -> a + "," + b);
                }
        );
        return res;
    }

    private Map<String, String> getDepAliasByUserIds(List<String> userIds) {
        List<SysUserAliasVo> list = sysUserMapper.getDepAliasByUserIds(userIds);

        Map<String, String> res = new HashMap<>(5);
        list.forEach(item -> {
                    res.merge(item.getUserId(), item.getAlias(), (a, b) -> a + "," + b);
                }
        );
        return res;
    }

    /**
     * 通过部门id获取用户
     *
     * @param orgId
     * @return
     */
    @Override
    public List<SysUser> getUserByOrgId(String orgId) {
        if (StringUtils.isNotBlank(orgId)) {
            return sysUserDepartService.queryUserByDepId(orgId);
        }
        return null;
    }

    /**
     * 判断某个用户是否包含角色
     *
     * @param username
     * @param roles
     * @return
     */
    @Override
    public boolean hasRole(String username, List<String> roles) {
        Set<String> userRoles = sysBaseAPI.queryUserRoles(username);
        for (String role : roles) {
            if (userRoles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Integer getTotalPeopleNum() {
        return userCommonMapper.getTotalPeopleNum();
    }

    @Override
    public List<UserToSelectDTO> getSelectedUser(String value, String departId) {
        AssessCurrentAssess annual = assessCommonApi.getCurrentAssessInfo("annual");
        if (annual == null) {
            // 返回空List
            return Collections.emptyList();
        }

        LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
        lqw.ne(AssessAnnualSummary::getRecommendType, "group");
        if ("undefined".equals(value)) value = "";
        lqw.like(!"@".equals(value), AssessAnnualSummary::getHashId, value);


        if (!"undefined".equals(departId)) {
            lqw.eq(AssessAnnualSummary::getDepart, departId);
        }

        lqw.eq(AssessAnnualSummary::getCurrentYear, annual.getCurrentYear());
        lqw.orderByAsc(AssessAnnualSummary::getPersonOrder);

        List<AssessAnnualSummary> summaries = annualSummaryMapper.selectList(lqw);

        // 将元素的hashId属性重复的元素去重

        List<String> hashIds = new ArrayList<>();

        List<UserToSelectDTO> list = new ArrayList<>();
        if (!summaries.isEmpty()) {
            for (AssessAnnualSummary summary : summaries) {
                if (hashIds.contains(summary.getHashId())) {
                    continue;
                }
                hashIds.add(summary.getHashId());

                UserToSelectDTO dto = new UserToSelectDTO();
                String hashId = summary.getHashId();
                String[] split = hashId.split("@");
//                String s = split[2];
//                StringBuilder sb = new StringBuilder(s);
//                sb.insert(8, "日").insert(6, "月").insert(4, "年");
                String name = split[0];
                if (name.length() == 2) {
                    name = name.charAt(0) + "  " + name.charAt(1);
                }
//                String text = name + " " + split[1] + " " + sb;
                String text = CommonUtils.getNameByHashId(hashId);
                dto.setText(text);
                dto.setValue(summary.getHashId());
                dto.setLabel(name);
                list.add(dto);
            }
        }
        return list;
    }

    // todo：方法待验证
    @Override
    public List<SysUser> generateAnonymousAccount(String assess, String currentYear) {
        List<ArrangeDTO> arranges = new ArrayList<>();
        List<SysUser> insertUsers = new ArrayList<>();
        List<SysUser> updateUsers = new ArrayList<>();

        List<SysUserRole> insertUserRoles = new ArrayList<>();
        List<SysUserDepart> insertUserDeparts = new ArrayList<>();
        Set<String> userNames = new HashSet<>();

        switch (assess) {
            case "annual":
                arranges = annualArrangeMapper.queryAllArrangeByYear(currentYear);
                break;
            case "report":
                // todo：生成一报告两评议的匿名账号
                arranges = reportArrangeMapper.queryAllArrangeByYear(currentYear);
                break;
            default:
                break;
        }

        List<AssessLeaderDepartConfig> leaderDepartConfigs = leaderDepartConfigMapper.selectList(null);

        Map<String, Boolean> jgMap = new HashMap<>();

        if (leaderDepartConfigs != null && !leaderDepartConfigs.isEmpty()) {
            for (AssessLeaderDepartConfig leaderDepartConfig : leaderDepartConfigs) {
                Boolean b = this.hasChiefEngineer(leaderDepartConfig);
                jgMap.put(leaderDepartConfig.getId(), b);
            }
        }


        // 局领导A票
        if ("annual".equals(assess)) {
            // 生成A票
            if (leaderDepartConfigs != null && !leaderDepartConfigs.isEmpty()) {
                for (AssessLeaderDepartConfig leaderDepartConfig : leaderDepartConfigs) {
                    SysUser leader = sysUserMapper.selectById(leaderDepartConfig.getLeaderId());
                    Boolean b = jgMap.get(leaderDepartConfig.getId());
                    if (b) leader.setDepartIds(leaderDepartConfig.getDepartId() + ",JG");
                    else leader.setDepartIds(leaderDepartConfig.getDepartId());
                    leader.setVoteType("A");
                    updateUsers.add(leader);
                }
            }
        }

        if ("annual".equals(assess)) {
            for (ArrangeDTO arrange : arranges) {
//                String departType = departCommonApi.getDepartTypeById(arrange.getDepart());
                String departType = arrange.getDepartType();
                String voteTypePrefix = "institution".equals(departType) ? "basic" : departType;
                voteTypePrefix = voteTypePrefix + "_";
                String roleId = "";
                if ("bureau".equals(departType)) roleId = "1834392963461025793";
                else roleId = "1834392963461025795";


                int voteA = arrange.getVoteA();

                if (voteA != 0) {
                    // 如果是基层单位
                    if (!"bureau".equals(departType)) {
                        // 判断这单位本年度有多少正职
                        for (int i = 0; i < voteA; i++) {
                            SysUser user = new SysUser();

                            // 随机生成用户名
                            String username = CommonUtils.randomString(6);
                            while (userNames.contains(username)) {
                                username = CommonUtils.randomString(6);
                            }
                            userNames.add(username);
                            String id = IdWorker.getIdStr();
                            user.setId(id);
                            user.setUsername(username);

//                        if (leaderDepartConfigs != null) {
//                            for (AssessLeaderDepartConfig leaderDepartConfig : leaderDepartConfigs) {
//                                if (leaderDepartConfig.getDepartId().contains(arrange.getDepart())) {
//                                    user.setDepartIds(leaderDepartConfig.getDepartId());
//                                    break;
//                                }
//                            }
//                        }

                            user.setDepartIds(arrange.getDepart());
                            user.setDepart(arrange.getDepart());
                            user.setVoteType(voteTypePrefix + "AB");
                            user.setAssess(assess);
                            // 生成匿名账号，将匿名账号存入待插入列表
                            SysUser sysUser = addAnonymousUser(user);
                            insertUsers.add(sysUser);

                            // 将匿名账号与临时角色绑定，将绑定信息存入待插入列表
                            SysUserRole userRole = new SysUserRole();
                            userRole.setUserId(sysUser.getId());
                            userRole.setRoleId("1834392963461025793");// 基层临时角色Id
                            insertUserRoles.add(userRole);

                            // 将匿名账号与部门绑定，将绑定信息存入待插入列表
                            SysUserDepart userDepart = new SysUserDepart(sysUser.getId(), arrange.getDepart());
                            insertUserDeparts.add(userDepart);
                        }
                    }
                }


                int voteB = arrange.getVoteB();
                int voteC = arrange.getVoteC();

                if (voteB != 0) {
                    for (int i = 0; i < voteB; i++) {
                        SysUser user = new SysUser();

                        // 随机生成用户名
                        String username = CommonUtils.randomString(6);
                        // String username = RandomUtil.randomStringWithoutStr(6, "0Oo1lIiZz2");
                        while (userNames.contains(username)) {
                            username = CommonUtils.randomString(6);
                        }
                        userNames.add(username);
                        String id = IdWorker.getIdStr();
                        user.setId(id);
                        user.setUsername(username);

                        if (leaderDepartConfigs != null && departType.equals("bureau")) {
                            for (AssessLeaderDepartConfig leaderDepartConfig : leaderDepartConfigs) {
                                if (leaderDepartConfig.getDepartId().contains(arrange.getDepart())) {
                                    String departs = leaderDepartConfig.getDepartId();
                                    String[] departList = departs.split(",");

                                    // 获取所有处室
                                    List<SysDepartModel> allDepart = departCommonApi.queryDepartByType(new String[]{"1"});
                                    // 获取所有处室id
                                    List<String> bureauId = allDepart.stream().map(SysDepartModel::getId).collect(Collectors.toList());


                                    List<String> newList = new ArrayList<>();
                                    for (String departId : departList) {
                                        if (bureauId.contains(departId)) {
                                            newList.add(departId);
                                        }
                                    }

                                    // departList转为字符串
                                    String departIds = String.join(",", newList);

                                    Boolean b = jgMap.get(leaderDepartConfig.getId());
                                    if (b) user.setDepartIds(departIds + ",JG");
                                    else user.setDepartIds(departIds);
                                    break;
                                }
                            }
                        } else {
                            user.setDepartIds(arrange.getDepart());
                        }
                        user.setDepart(arrange.getDepart());
                        user.setVoteType(voteTypePrefix + "B");
                        user.setAssess(assess);
                        // 生成匿名账号，将匿名账号存入待插入列表
                        SysUser sysUser = addAnonymousUser(user);
                        insertUsers.add(sysUser);

                        // 将匿名账号与临时角色绑定，将绑定信息存入待插入列表
                        SysUserRole userRole = new SysUserRole();
                        userRole.setUserId(sysUser.getId());
                        userRole.setRoleId("1834392963461025793");// 临时角色Id
                        insertUserRoles.add(userRole);

                        // 将匿名账号与部门绑定，将绑定信息存入待插入列表
                        SysUserDepart userDepart = new SysUserDepart(sysUser.getId(), arrange.getDepart());
                        insertUserDeparts.add(userDepart);
                    }
                }
                if (voteC != 0) {
                    for (int i = 0; i < voteC; i++) {
                        SysUser user = new SysUser();

                        // 随机生成用户名
                        String username = CommonUtils.randomString(6);
                        while (userNames.contains(username)) {
                            username = CommonUtils.randomString(6);
                        }
                        userNames.add(username);
                        String id = IdWorker.getIdStr();
                        user.setId(id);
                        user.setUsername(username);
                        user.setDepart(arrange.getDepart());
                        user.setDepartIds(arrange.getDepart());
                        user.setVoteType(voteTypePrefix + "C");
                        user.setAssess(assess);
                        // 生成匿名账号，将匿名账号存入待插入列表
                        SysUser sysUser = addAnonymousUser(user);
                        insertUsers.add(sysUser);

                        // 将匿名账号与临时角色绑定，将绑定信息存入待插入列表
                        SysUserRole userRole = new SysUserRole();
                        userRole.setUserId(sysUser.getId());
                        userRole.setRoleId("1834392963461025793");// 临时角色Id
                        insertUserRoles.add(userRole);

                        // 将匿名账号与部门绑定，将绑定信息存入待插入列表
                        SysUserDepart userDepart = new SysUserDepart(sysUser.getId(), arrange.getDepart());
                        insertUserDeparts.add(userDepart);
                    }
                }
            }
        } else if ("report".equals(assess)) {
            for (ArrangeDTO arrange : arranges) {
                int voteA = arrange.getVoteA();
                int voteB = arrange.getVoteB();

                if (voteA != 0) {
                    for (int i = 0; i < voteA; i++) {
                        SysUser user = new SysUser();

                        // 随机生成用户名
                        String username = CommonUtils.randomString(6);
                        while (userNames.contains(username)) {
                            username = CommonUtils.randomString(6);
                        }
                        userNames.add(username);
                        String id = IdWorker.getIdStr();
                        user.setId(id);
                        user.setUsername(username);

                        user.setDepartIds(arrange.getDepart());
                        user.setDepart(arrange.getDepart());
                        user.setVoteType("A");
                        user.setAssess(assess);
                        // 生成匿名账号，将匿名账号存入待插入列表
                        SysUser sysUser = addAnonymousUser(user);
                        insertUsers.add(sysUser);

                        // 将匿名账号与临时角色绑定，将绑定信息存入待插入列表
                        SysUserRole userRole = new SysUserRole();
                        userRole.setUserId(sysUser.getId());
                        userRole.setRoleId("1834392963461025795");// 临时角色Id
                        insertUserRoles.add(userRole);

                        // 将匿名账号与部门绑定，将绑定信息存入待插入列表
                        SysUserDepart userDepart = new SysUserDepart(sysUser.getId(), arrange.getDepart());
                        insertUserDeparts.add(userDepart);
                    }
                }
                if (voteB != 0) {
                    for (int i = 0; i < voteB; i++) {
                        SysUser user = new SysUser();

                        // 随机生成用户名
                        String username = CommonUtils.randomString(6);
                        while (userNames.contains(username)) {
                            username = CommonUtils.randomString(6);
                        }
                        userNames.add(username);
                        String id = IdWorker.getIdStr();
                        user.setId(id);
                        user.setUsername(username);

                        user.setDepartIds(arrange.getDepart());
                        user.setDepart(arrange.getDepart());
                        user.setVoteType("B");
                        user.setAssess(assess);
                        // 生成匿名账号，将匿名账号存入待插入列表
                        SysUser sysUser = addAnonymousUser(user);
                        insertUsers.add(sysUser);

                        // 将匿名账号与临时角色绑定，将绑定信息存入待插入列表
                        SysUserRole userRole = new SysUserRole();
                        userRole.setUserId(sysUser.getId());
                        userRole.setRoleId("1834392963461025795");// 临时角色Id
                        insertUserRoles.add(userRole);

                        // 将匿名账号与部门绑定，将绑定信息存入待插入列表
                        SysUserDepart userDepart = new SysUserDepart(sysUser.getId(), arrange.getDepart());
                        insertUserDeparts.add(userDepart);
                    }
                }
            }
        }


        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        SysUserMapper userMapperNew = sqlSession.getMapper(SysUserMapper.class);
        SysUserRoleMapper userRoleMapperNew = sqlSession.getMapper(SysUserRoleMapper.class);
        SysUserDepartMapper userDepartMapperNew = sqlSession.getMapper(SysUserDepartMapper.class);

        insertUsers.forEach(userMapperNew::insert);
        updateUsers.forEach(userMapperNew::updateById);
        insertUserRoles.forEach(userRoleMapperNew::insert);
        insertUserDeparts.forEach(userDepartMapperNew::insert);

        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();

        return insertUsers;
    }

    @Override
    public List<SysUser> getAnonymousAccountByAssess(String assess) {
        LambdaQueryWrapper<SysUser> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SysUser::getAssess, assess);
        lqw.isNotNull(SysUser::getPwdPlainText);
        lqw.orderByDesc(SysUser::getDepart);
        lqw.orderByAsc(SysUser::getVoteType);
        List<SysUser> users = sysUserMapper.selectList(lqw);
        if (users != null && !users.isEmpty()) {
            return users;
        }
        return null;
    }

    @Override
    public List<SysUser> getAllAnonymousAccount() {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        Map<String, String> currentUserDepart = departCommonApi.getCurrentUserDepart();


        List<String> roles = this.getUserRolesByUserName(loginUser.getUsername());
        LambdaQueryWrapper<SysUser> lqw = new QueryWrapper<SysUser>().lambda();
        if (!roles.contains("department_cadre_admin")) {
            // 不是管理员
            lqw.eq(SysUser::getDepart, currentUserDepart.get("departId"));
        }
        lqw.isNotNull(SysUser::getPwdPlainText);
        lqw.orderByDesc(SysUser::getDepart).orderByAsc(SysUser::getVoteType);
        List<SysUser> users = sysUserService.list(lqw);

        if (users != null && !users.isEmpty()) {
            List<SysDepartModel> allDepart = departCommonApi.queryAllDepart();
            // 转换为Map，以id为key，departType为value
            Map<String, String> departMap = allDepart.stream().collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getDepartType));
            for (SysUser user : users) {
                user.setWorkNo(departMap.get(user.getDepart()));
            }

            return users;
        }

        return null;
    }

    @Override
    public IPage<SysUser> getAllAnonymousAccount(Page<SysUser> page) {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        Map<String, String> currentUserDepart = departCommonApi.getCurrentUserDepart();


        List<String> roles = this.getUserRolesByUserName(loginUser.getUsername());
        LambdaQueryWrapper<SysUser> lqw = new QueryWrapper<SysUser>().lambda();
        if (!roles.contains("department_cadre_admin")) {
            // 不是管理员
            lqw.eq(SysUser::getDepart, currentUserDepart.get("departId"));
        }
        lqw.isNotNull(SysUser::getPwdPlainText);
        lqw.orderByDesc(SysUser::getDepart).orderByAsc(SysUser::getVoteType);

        Page<SysUser> pageList = sysUserService.page(page, lqw);

        List<SysUser> users = pageList.getRecords();

        if (users != null && !users.isEmpty()) {
            List<SysDepartModel> allDepart = departCommonApi.queryAllDepart();
            // 转换为Map，以id为key，departType为value
            Map<String, String> departMap = allDepart.stream().collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getDepartType));
            for (SysUser user : users) {
                user.setWorkNo(departMap.get(user.getDepart()));
            }

            pageList.setRecords(users);

            return pageList;
        }

        return null;
    }

    @Override
    public List<String> getUserRolesByUserName(String userName) {
        return sysBaseAPI.getRolesByUsername(userName);
    }

    @Override
    public void deleteById(String id) {
        SysUser sysUser = sysUserMapper.selectById(id);
        if (sysUser == null) {
            throw new JeecgBootException("用户不存在，请刷新后重试！");
        }
        String assess = sysUser.getAssess();
        String depart = sysUser.getDepart();

        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo(sysUser.getAssess());

        if ("annual".equals(assess)) {
            LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw = new LambdaQueryWrapper<>();
            lqw.eq(AssessDemocraticEvaluationSummary::getDepart, depart);
            lqw.eq(AssessDemocraticEvaluationSummary::getCurrentYear, currentAssessInfo.getCurrentYear());
            List<AssessDemocraticEvaluationSummary> summaries = democraticEvaluationSummaryMapper.selectList(lqw);
            if (summaries != null && !summaries.isEmpty()) {
                for (AssessDemocraticEvaluationSummary summary : summaries) {
                    summary.setNum(summary.getNum() - 1);
                    democraticEvaluationSummaryMapper.updateById(summary);
                }
            }
        } else {
            LambdaQueryWrapper<AssessReportEvaluationSummary> lqw = new LambdaQueryWrapper<>();
            lqw.eq(AssessReportEvaluationSummary::getDepart, depart);
            lqw.eq(AssessReportEvaluationSummary::getCurrentYear, currentAssessInfo.getCurrentYear());
            List<AssessReportEvaluationSummary> summaries = reportEvaluationSummaryMapper.selectList(lqw);
            if (summaries != null && !summaries.isEmpty()) {
                for (AssessReportEvaluationSummary summary : summaries) {
                    summary.setNum(summary.getNum() - 1);
                    reportEvaluationSummaryMapper.updateById(summary);
                }
            }
        }


        sysUserMapper.deleteById(id);
    }

    // 常量定义
    private static final String TEMP_ROLE_ID = "1834392963461025793";

    @Override
    public List<SysUser> addAnonymousAccount(String assess, String depart, String voteType, int voteNum) {
        // 参数校验
        if (assess == null || depart == null || voteType == null || voteNum <= 0) {
            throw new JeecgBootException("参数错误！");
        }

        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo(assess);
        if (currentAssessInfo == null || !currentAssessInfo.isAssessing()) {
            throw new JeecgBootException("当前无正在进行中的年度考核民主测评！");
        }

        List<SysUser> insertUsers = new ArrayList<>();
        List<SysUserRole> insertUserRoles = new ArrayList<>();
        List<SysUserDepart> insertUserDeparts = new ArrayList<>();

        // 获取已存在的用户名集合
        Set<String> userNames = getExistingUsernames();

        // 根据考核类型和部门类型处理不同的投票类型
        if ("annual".equals(assess)) {
            String departType = departCommonApi.getDepartTypeById(depart);
            if ("1".equals(departType)) {
                handleBureauVoteType(assess, depart, voteType, voteNum, userNames, insertUsers, insertUserRoles, insertUserDeparts);
            } else {
                handleBasicVoteType(assess, depart, voteType, voteNum, userNames, insertUsers, insertUserRoles, insertUserDeparts);
            }
        } else {
            handleOtherVoteType(assess, depart, voteType, voteNum, userNames, insertUsers, insertUserRoles, insertUserDeparts);
        }

        // 批量插入数据
        batchInsertData(insertUsers, insertUserRoles, insertUserDeparts);

        // 更新票数统计
        updateVoteCounts(assess, depart, voteType, voteNum, currentAssessInfo);

        return insertUsers;
    }

    // 辅助方法1：获取已存在的用户名
    private Set<String> getExistingUsernames() {
        LambdaQueryWrapper<SysUser> lqw = new LambdaQueryWrapper<>();
        lqw.isNotNull(SysUser::getPwdPlainText)
                .select(SysUser::getUsername);
        return sysUserService.list(lqw).stream()
                .map(SysUser::getUsername)
                .collect(Collectors.toSet());
    }

    // 辅助方法2：处理局机关投票类型
    private void handleBureauVoteType(String assess, String depart, String voteType, int voteNum,
                                      Set<String> userNames, List<SysUser> insertUsers,
                                      List<SysUserRole> insertUserRoles, List<SysUserDepart> insertUserDeparts) {
        switch (voteType) {
            case "B":
                createAnonymousUsers(assess, depart, voteNum, userNames, insertUsers, insertUserRoles, insertUserDeparts,
                        "bureau_B", this::getBureauDepartIds);
                break;
            case "C":
                createAnonymousUsers(assess, depart, voteNum, userNames, insertUsers, insertUserRoles, insertUserDeparts,
                        "bureau_C", user -> depart);
                break;
        }
    }

    // 辅助方法3：处理基层投票类型
    private void handleBasicVoteType(String assess, String depart, String voteType, int voteNum,
                                     Set<String> userNames, List<SysUser> insertUsers,
                                     List<SysUserRole> insertUserRoles, List<SysUserDepart> insertUserDeparts) {
        switch (voteType) {
            case "A":
                createAnonymousUsers(assess, depart, voteNum, userNames, insertUsers, insertUserRoles, insertUserDeparts,
                        "basic_AB", user -> depart);
                break;
            case "B":
                createAnonymousUsers(assess, depart, voteNum, userNames, insertUsers, insertUserRoles, insertUserDeparts,
                        "basic_B", user -> depart);
                break;
            case "C":
                createAnonymousUsers(assess, depart, voteNum, userNames, insertUsers, insertUserRoles, insertUserDeparts,
                        "basic_C", user -> depart);
                break;
        }
    }

    // 辅助方法4：处理其他投票类型
    private void handleOtherVoteType(String assess, String depart, String voteType, int voteNum,
                                     Set<String> userNames, List<SysUser> insertUsers,
                                     List<SysUserRole> insertUserRoles, List<SysUserDepart> insertUserDeparts) {
        switch (voteType) {
            case "A":
                createAnonymousUsers(assess, depart, voteNum, userNames, insertUsers, insertUserRoles, insertUserDeparts,
                        "A", user -> depart);
                break;
            case "B":
                createAnonymousUsers(assess, depart, voteNum, userNames, insertUsers, insertUserRoles, insertUserDeparts,
                        "B", user -> depart);
                break;
        }
    }

    // 辅助方法5：创建匿名用户
    private void createAnonymousUsers(String assess, String depart, int voteNum,
                                      Set<String> userNames, List<SysUser> insertUsers,
                                      List<SysUserRole> insertUserRoles, List<SysUserDepart> insertUserDeparts,
                                      String voteType, Function<SysUser, String> departIdsProvider) {

        for (int i = 0; i < voteNum; i++) {
            SysUser user = createBaseUser(userNames);
            user.setDepart(depart);
            user.setDepartIds(departIdsProvider.apply(user));
            user.setVoteType(voteType);
            user.setAssess(assess);

            SysUser sysUser = addAnonymousUser(user);
            insertUsers.add(sysUser);

            // 添加用户角色关联
            insertUserRoles.add(createUserRole(sysUser.getId()));

            // 添加用户部门关联
            insertUserDeparts.add(createUserDepart(sysUser.getId(), depart));
        }
    }

    // 辅助方法6：获取局机关部门ID
    private String getBureauDepartIds(SysUser user) {
        List<AssessLeaderDepartConfig> leaderDepartConfigs = leaderDepartConfigMapper.selectList(null);
        if (leaderDepartConfigs == null) {
            return user.getDepart();
        }

        for (AssessLeaderDepartConfig config : leaderDepartConfigs) {
            if (config.getDepartId().contains(user.getDepart())) {
                List<String> departList = Arrays.asList(config.getDepartId().split(","));
                List<SysDepartModel> allDepart = departCommonApi.queryDepartByType(new String[]{"1"});

                List<String> validDepartIds = departList.stream()
                        .filter(departId -> allDepart.stream().anyMatch(d -> d.getId().equals(departId)))
                        .collect(Collectors.toList());

                String departIds = String.join(",", validDepartIds);
                return this.hasChiefEngineer(config) ? departIds + ",JG" : departIds;
            }
        }
        return user.getDepart();
    }

    // 辅助方法7：创建基础用户
    private SysUser createBaseUser(Set<String> userNames) {
        SysUser user = new SysUser();
        user.setId(IdWorker.getIdStr());

        // 生成唯一用户名
        String username;
        do {
            username = CommonUtils.randomString(6);
        } while (userNames.contains(username));

        userNames.add(username);
        user.setUsername(username);
        return user;
    }

    // 辅助方法8：创建用户角色关联
    private SysUserRole createUserRole(String userId) {
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(TEMP_ROLE_ID);
        return userRole;
    }

    // 辅助方法9：创建用户部门关联
    private SysUserDepart createUserDepart(String userId, String departId) {
        return new SysUserDepart(userId, departId);
    }

    // 辅助方法10：批量插入数据
    private void batchInsertData(List<SysUser> users, List<SysUserRole> userRoles, List<SysUserDepart> userDeparts) {
        if (users.isEmpty()) return;

        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
            SysUserMapper userMapper = sqlSession.getMapper(SysUserMapper.class);
            SysUserRoleMapper roleMapper = sqlSession.getMapper(SysUserRoleMapper.class);
            SysUserDepartMapper departMapper = sqlSession.getMapper(SysUserDepartMapper.class);

            users.forEach(userMapper::insert);
            userRoles.forEach(roleMapper::insert);
            userDeparts.forEach(departMapper::insert);

            sqlSession.commit();
        }
    }

    // 辅助方法11：更新票数统计
    private void updateVoteCounts(String assess, String depart, String voteType, int voteNum,
                                  AssessCurrentAssess currentAssessInfo) {
        if ("annual".equals(assess)) {
            updateAnnualVoteCounts(depart, voteType, voteNum, currentAssessInfo);
        } else {
            updateOtherVoteCounts(depart, voteType, voteNum, currentAssessInfo);
        }
    }

    // 辅助方法12：更新年度考核票数
    private void updateAnnualVoteCounts(String depart, String voteType, int voteNum,
                                        AssessCurrentAssess currentAssessInfo) {
        // 更新民主评价汇总
        LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessDemocraticEvaluationSummary::getDepart, depart)
                .eq(AssessDemocraticEvaluationSummary::getCurrentYear, currentAssessInfo.getCurrentYear());

        List<AssessDemocraticEvaluationSummary> summaries = democraticEvaluationSummaryMapper.selectList(lqw);
        if (summaries != null && !summaries.isEmpty()) {
            summaries.forEach(summary -> {
                summary.setNum(summary.getNum() + voteNum);
                democraticEvaluationSummaryMapper.updateById(summary);
            });
        }

        // 更新年度安排
        LambdaQueryWrapper<AssessAnnualFill> fillQuery = new LambdaQueryWrapper<>();
        fillQuery.eq(AssessAnnualFill::getDepart, depart)
                .eq(AssessAnnualFill::getCurrentYear, currentAssessInfo.getCurrentYear());

        AssessAnnualFill fill = annualFillMapper.selectOne(fillQuery);
        if (fill == null) return;

        LambdaQueryWrapper<AssessAnnualArrange> arrangeQuery = new LambdaQueryWrapper<>();
        arrangeQuery.eq(AssessAnnualArrange::getAnnualFillId, fill.getId());
        AssessAnnualArrange arrange = annualArrangeMapper.selectOne(arrangeQuery);
        if (arrange == null) return;

        switch (voteType) {
            case "A":
                arrange.setVoteA(arrange.getVoteA() + voteNum);
                arrange.setChiefNum(arrange.getChiefNum() + voteNum);
                break;
            case "B":
                arrange.setVoteB(arrange.getVoteB() + voteNum);
                arrange.setDeputyNum(arrange.getDeputyNum() + voteNum);
                break;
            case "C":
                arrange.setVoteC(arrange.getVoteC() + voteNum);
                break;
        }

        arrange.setVoteNum(arrange.getVoteNum() + voteNum);
        annualArrangeMapper.updateById(arrange);
    }

    // 辅助方法13：更新其他考核票数
    private void updateOtherVoteCounts(String depart, String voteType, int voteNum,
                                       AssessCurrentAssess currentAssessInfo) {
        LambdaQueryWrapper<AssessReportEvaluationSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessReportEvaluationSummary::getDepart, depart)
                .eq(AssessReportEvaluationSummary::getCurrentYear, currentAssessInfo.getCurrentYear());

        List<AssessReportEvaluationSummary> summaries = reportEvaluationSummaryMapper.selectList(lqw);
        if (summaries != null && !summaries.isEmpty()) {
            summaries.forEach(summary -> {
                summary.setNum(summary.getNum() + voteNum);
                reportEvaluationSummaryMapper.updateById(summary);
            });
        }
    }

    @Override
    public List<SysUser> getAllLeader() {
        return this.getUserByRoleCode("director_leader");
    }

    @Override
    public List<SysUser> getCurrentLeader() {
        return userCommonMapper.getAllLeaders();
    }

    @Override
    public List<SysUser> getHistoryAssessUnit(String year) {
        LambdaQueryWrapper<LeaderDepartHistory> lqw = new LambdaQueryWrapper<>();
        lqw.eq(LeaderDepartHistory::getAssessYear, year);
        List<LeaderDepartHistory> all = ldHistoryMapper.selectList(lqw);
        List<SysUser> users = new ArrayList<>();
        if (all != null && !all.isEmpty() && year != null) {
            for (LeaderDepartHistory h : all) {
                SysUser user = new SysUser();
                user.setId(h.getLeaderId());
                user.setRealname(h.getRealname());
                user.setUsername(h.getUsername());
                users.add(user);
            }
        } else {
            return getAllLeader();
        }

        return users;
    }

    @Override
    public List<String> getHistoryUnitDeptIds(List<String> leaders, String year) {
        LambdaQueryWrapper<LeaderDepartHistory> lqw = new LambdaQueryWrapper<>();
        lqw.eq(LeaderDepartHistory::getAssessYear, year);
        lqw.in(LeaderDepartHistory::getLeaderId, leaders);
        List<LeaderDepartHistory> all = ldHistoryMapper.selectList(lqw);
        List<String> res = new ArrayList<>();
        if (all != null && !all.isEmpty() && year != null) {
            for (LeaderDepartHistory h : all) {
                res.addAll(Arrays.asList(h.getDepartId().split(",")));
            }
        } else {
            LambdaQueryWrapper<AssessLeaderDepartConfig> lqw2 = new LambdaQueryWrapper<>();
            lqw2.in(AssessLeaderDepartConfig::getLeaderId, leaders);
            List<AssessLeaderDepartConfig> configs = leaderDepartConfigMapper.selectList(lqw2);
            for (AssessLeaderDepartConfig c : configs) {
                res.addAll(Arrays.asList(c.getDepartId().split(",")));
            }
        }

        return res;
    }

    @Override
    public List<String> getAssessUnitDepart(String year, String leader) {
        AssessCurrentAssess assess = assessCommonApi.getCurrentAssessInfo("annual");
        if (assess == null) {
            return new ArrayList<>();
        }

        List<String> res = new ArrayList<>();

        if (year.equals(assess.getCurrentYear())) {
            LambdaQueryWrapper<AssessLeaderDepartConfig> lqw2 = new LambdaQueryWrapper<>();
            lqw2.eq(AssessLeaderDepartConfig::getLeaderId, leader);
            List<AssessLeaderDepartConfig> configs = leaderDepartConfigMapper.selectList(lqw2);
            for (AssessLeaderDepartConfig c : configs) {
                res.addAll(Arrays.asList(c.getDepartId().split(",")));
            }
        } else {
            List<String> historyUnitDeptIds = this.getHistoryUnitDeptIds(Collections.singletonList(leader), year);
            res.addAll(historyUnitDeptIds);
        }

        return res;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCurrentUser() {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        String id = loginUser.getId();
        List<String> userIds = new ArrayList<>();
        userIds.add(id);

        LambdaUpdateWrapper<SysUser> luw = new LambdaUpdateWrapper<>();
        luw.eq(SysUser::getId, id);
        luw.set(SysUser::getDelFlag, CommonConstant.DEL_FLAG_1);
        sysUserMapper.update(null, luw);

        return sysUserService.removeLogicDeleted(userIds);
    }

    /**
     * 生成匿名账号
     *
     * @param user
     * @Description: 参数中需要包含userName，voteType，assess
     */
    SysUser addAnonymousUser(SysUser user) {
        try {
            user.setCreateTime(new Date());// 设置创建时间
            String password = CommonUtils.randomString(6);
            user.setPwdPlainText(password);
            String salt = oConvertUtils.randomGen(8);
            String passwordEncode = PasswordUtil.encrypt(user.getUsername(), password, salt);
            user.setSalt(salt);
            user.setRealname("匿名用户");
            user.setPassword(passwordEncode);
            user.setStatus(CommonConstant.USER_UNFREEZE);
            user.setDelFlag(CommonConstant.DEL_FLAG_0);
            user.setActivitiSync(CommonConstant.ACT_SYNC_0);
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void deleteAllAnonymousAccount(String type) {
        LambdaQueryWrapper<SysUser> lqw = new LambdaQueryWrapper<>();
        lqw.isNotNull(SysUser::getPwdPlainText);
        lqw.eq(SysUser::getAssess, type);
        List<SysUser> users = sysUserService.list(lqw);
        if (users == null || users.isEmpty()) {
            return;
        }
        List<String> ids = users.stream().map(SysUser::getId).collect(Collectors.toList());

        LambdaUpdateWrapper<SysUser> luw = new LambdaUpdateWrapper<>();
        luw.in(SysUser::getId, ids);
        luw.set(SysUser::getDelFlag, CommonConstant.DEL_FLAG_1);
        sysUserMapper.update(null, luw);

        sysUserService.removeLogicDeleted(ids);
    }

    @Override
    public Boolean hasChiefEngineer(AssessLeaderDepartConfig config) {
        if (config == null) {
            return false;
        }

        String departIds = config.getDepartId();
        if (departIds == null) {
            return false;
        }
        List<String> departList = Arrays.asList(departIds.split(","));
        LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
        lqw.in(AssessAnnualSummary::getDepart, departList);
        lqw.in(AssessAnnualSummary::getPosition, Arrays.asList("局管总师", "二级巡视员"));
        List<AssessAnnualSummary> list = annualSummaryMapper.selectList(lqw);

        return list != null && !list.isEmpty();
    }

    @Override
    public Map<String, String> getPeopleInfo(String hashId, String year) {
        LambdaQueryWrapper<AssessAnnualSummary> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessAnnualSummary::getHashId, hashId);
        lqw.eq(AssessAnnualSummary::getCurrentYear, year);
        AssessAnnualSummary summary = annualSummaryMapper.selectOne(lqw);
        if (summary == null) {
            return null;
        }

        Map<String, String> res = new HashMap<>();
        res.put("name", summary.getPerson());
        res.put("depart", departCommonApi.getDepartNameById(summary.getDepart()));
        res.put("departType", "bureau".equals(summary.getDepartType()) ? "处室" : "单位");
        String type = summary.getType();
        res.put("type", "chief".equals(type) ? "正职" : "deputy".equals(type) ? "副职" : "chiefPro".equals(type) ? "总师、二巡" : "其他");

        LambdaQueryWrapper<LeaderDepartHistory> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(LeaderDepartHistory::getAssessYear, year);
        List<LeaderDepartHistory> lds = ldHistoryMapper.selectList(lqw2);
        for (LeaderDepartHistory ld : lds) {
            if (ld.getDepartId().contains(summary.getDepart())) {
                res.put("unit", ld.getRealname());
            }
        }
        return res;
    }
}
