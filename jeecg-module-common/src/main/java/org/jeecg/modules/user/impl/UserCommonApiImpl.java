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
import org.jeecg.modules.sys.entity.annual.AssessAnnualArrange;
import org.jeecg.modules.sys.entity.annual.AssessAnnualFill;
import org.jeecg.modules.sys.entity.annual.AssessAnnualSummary;
import org.jeecg.modules.sys.entity.annual.AssessDemocraticEvaluationSummary;
import org.jeecg.modules.sys.entity.business.AssessLeaderDepartConfig;
import org.jeecg.modules.sys.entity.report.AssessReportEvaluationSummary;
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

    @Override
    public IPage<SysUser> queryDepartUserPageList(String departId, String username, String realname, int pageSize, int pageNo, String id, boolean getLeader) {
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
            // 查询部门下的领导
            List<SysUser> sysUsers = this.getAllLeader();
            pageList = new Page<>();
            pageList.setRecords(sysUsers);
        }
        List<SysUser> userList = pageList.getRecords();
        if (userList != null && userList.size() > 0) {
            List<String> userIds = userList.stream().map(SysUser::getId).collect(Collectors.toList());
            Map<String, SysUser> map = new HashMap(5);
            if (userIds != null && userIds.size() > 0) {
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
        return userCommonMapper.getUserByRoleCode(roleCode);
    }

    /**
     * 升级SpringBoot2.6.6,不允许循环依赖
     *
     * @param userIds
     * @return
     */
    private Map<String, String> getDepNamesByUserIds(List<String> userIds) {
        List<SysUserDepVo> list = sysUserMapper.getDepNamesByUserIds(userIds);

        Map<String, String> res = new HashMap(5);
        list.forEach(item -> {
                    if (res.get(item.getUserId()) == null) {
                        res.put(item.getUserId(), item.getDepartName());
                    } else {
                        res.put(item.getUserId(), res.get(item.getUserId()) + "," + item.getDepartName());
                    }
                }
        );
        return res;
    }

    private Map<String, String> getDepAliasByUserIds(List<String> userIds) {
        List<SysUserAliasVo> list = sysUserMapper.getDepAliasByUserIds(userIds);

        Map<String, String> res = new HashMap(5);
        list.forEach(item -> {
                    if (res.get(item.getUserId()) == null) {
                        res.put(item.getUserId(), item.getAlias());
                    } else {
                        res.put(item.getUserId(), res.get(item.getUserId()) + "," + item.getAlias());
                    }
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

    @Override
    public List<SysUser> addAnonymousAccount(String assess, String depart, String voteType, int voteNum) {
        if (assess == null || depart == null || voteType == null || voteNum == 0) {
            throw new JeecgBootException("参数错误！");
        }
        List<SysUser> insertUsers = new ArrayList<>();
        List<SysUserRole> insertUserRoles = new ArrayList<>();
        List<SysUserDepart> insertUserDeparts = new ArrayList<>();

        LambdaQueryWrapper<SysUser> lqw = new LambdaQueryWrapper<>();
        lqw.isNotNull(SysUser::getPwdPlainText);
        lqw.select(SysUser::getUsername);
        List<SysUser> users = sysUserService.list(lqw);
        Set<String> userNames = users.stream().map(SysUser::getUsername).collect(Collectors.toSet());

        String departType = departCommonApi.getDepartTypeById(depart);
        if ("annual".equals(assess)) {
            if ("1".equals(departType)) {
                switch (voteType) {
                    case "B":
                        for (int i = 0; i < voteNum; i++) {
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
                            List<AssessLeaderDepartConfig> leaderDepartConfigs = leaderDepartConfigMapper.selectList(null);

                            if (leaderDepartConfigs != null) {
                                for (AssessLeaderDepartConfig leaderDepartConfig : leaderDepartConfigs) {
                                    if (leaderDepartConfig.getDepartId().contains(depart)) {
                                        String departs = leaderDepartConfig.getDepartId();
                                        String[] departList = departs.split(",");

                                        Boolean b = this.hasChiefEngineer(leaderDepartConfig);

                                        List<SysDepartModel> allDepart = departCommonApi.queryDepartByType(new String[]{"1"});

                                        List<String> allDepartId = new ArrayList<>();
                                        for (SysDepartModel d : allDepart) {
                                            allDepartId.add(d.getId());
                                        }

                                        List<String> newList = new ArrayList<>();
                                        for (String departId : departList) {
                                            if (allDepartId.contains(departId)) {
                                                newList.add(departId);
                                            }
                                        }

                                        // departList转为字符串
                                        String departIds = String.join(",", newList);

                                        if (b) user.setDepartIds(departIds + ",JG");
                                        else user.setDepartIds(departIds);
                                        break;
                                    }
                                }
                            } else {
                                user.setDepartIds(depart);
                            }
                            user.setDepart(depart);
                            user.setVoteType("bureau_B");
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
                            SysUserDepart userDepart = new SysUserDepart(sysUser.getId(), depart);
                            insertUserDeparts.add(userDepart);
                        }
                        break;
                    case "C":
                        for (int i = 0; i < voteNum; i++) {
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
                            user.setDepart(depart);
                            user.setDepartIds(depart);
                            user.setVoteType("bureau_C");
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
                            SysUserDepart userDepart = new SysUserDepart(sysUser.getId(), depart);
                            insertUserDeparts.add(userDepart);
                        }
                        break;
                }
            } else {
                switch (voteType) {
                    case "A":
                        for (int i = 0; i < voteNum; i++) {
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

                            user.setDepartIds(depart);
                            user.setDepart(depart);
                            user.setVoteType("basic_AB");
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
                            SysUserDepart userDepart = new SysUserDepart(sysUser.getId(), depart);
                            insertUserDeparts.add(userDepart);
                        }
                        break;
                    case "B":
                        for (int i = 0; i < voteNum; i++) {
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

                            user.setDepartIds(depart);
                            user.setDepart(depart);
                            user.setVoteType("basic_B");
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
                            SysUserDepart userDepart = new SysUserDepart(sysUser.getId(), depart);
                            insertUserDeparts.add(userDepart);
                        }
                        break;
                    case "C":
                        for (int i = 0; i < voteNum; i++) {
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
                            user.setDepart(depart);
                            user.setDepartIds(depart);
                            user.setVoteType("basic_C");
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
                            SysUserDepart userDepart = new SysUserDepart(sysUser.getId(), depart);
                            insertUserDeparts.add(userDepart);
                        }
                        break;
                }
            }
        } else {
            switch (voteType) {
                case "A":
                    for (int i = 0; i < voteNum; i++) {
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

                        user.setDepartIds(depart);
                        user.setDepart(depart);
                        user.setVoteType("A");
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
                        SysUserDepart userDepart = new SysUserDepart(sysUser.getId(), depart);
                        insertUserDeparts.add(userDepart);
                    }
                    break;
                case "B":
                    for (int i = 0; i < voteNum; i++) {
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

                        user.setDepartIds(depart);
                        user.setDepart(depart);
                        user.setVoteType("B");
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
                        SysUserDepart userDepart = new SysUserDepart(sysUser.getId(), depart);
                        insertUserDeparts.add(userDepart);
                    }
                    break;
            }
        }

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        SysUserMapper userMapperNew = sqlSession.getMapper(SysUserMapper.class);
        SysUserRoleMapper userRoleMapperNew = sqlSession.getMapper(SysUserRoleMapper.class);
        SysUserDepartMapper userDepartMapperNew = sqlSession.getMapper(SysUserDepartMapper.class);

        insertUsers.forEach(userMapperNew::insert);
        insertUserRoles.forEach(userRoleMapperNew::insert);
        insertUserDeparts.forEach(userDepartMapperNew::insert);

        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();

        // 更新票数
        if ("annual".equals(assess)) {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("annual");
            LambdaQueryWrapper<AssessDemocraticEvaluationSummary> lqw2 = new LambdaQueryWrapper<>();
            lqw2.eq(AssessDemocraticEvaluationSummary::getDepart, depart);
            lqw2.eq(AssessDemocraticEvaluationSummary::getCurrentYear, currentAssessInfo.getCurrentYear());
            List<AssessDemocraticEvaluationSummary> summaries = democraticEvaluationSummaryMapper.selectList(lqw2);
            if (summaries != null && !summaries.isEmpty()) {
                for (AssessDemocraticEvaluationSummary summary : summaries) {
                    summary.setNum(summary.getNum() + voteNum);
                    democraticEvaluationSummaryMapper.updateById(summary);
                }
            }

            LambdaQueryWrapper<AssessAnnualFill> lqw3 = new LambdaQueryWrapper<>();
            lqw3.eq(AssessAnnualFill::getDepart, depart);
            lqw3.eq(AssessAnnualFill::getCurrentYear, currentAssessInfo.getCurrentYear());
            List<AssessAnnualFill> fills = annualFillMapper.selectList(lqw3);

            String fillId = fills.get(0).getId();

            LambdaQueryWrapper<AssessAnnualArrange> lqw4 = new LambdaQueryWrapper<>();
            lqw4.eq(AssessAnnualArrange::getAnnualFillId, fillId);
            AssessAnnualArrange arrange = annualArrangeMapper.selectList(lqw4).get(0);

            if (voteType.equals("A")) {
                arrange.setVoteA(arrange.getVoteA() + voteNum);
                arrange.setChiefNum(arrange.getChiefNum() + voteNum);
            }
            if (voteType.equals("B")) {
                arrange.setVoteB(arrange.getVoteB() + voteNum);
                arrange.setDeputyNum(arrange.getDeputyNum() + voteNum);
            }
            if (voteType.equals("C")) {
                arrange.setVoteC(arrange.getVoteC() + voteNum);
            }

            arrange.setVoteNum(arrange.getVoteNum() + voteNum);

        } else {
            AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("report");
            LambdaQueryWrapper<AssessReportEvaluationSummary> lqw2 = new LambdaQueryWrapper<>();
            lqw2.eq(AssessReportEvaluationSummary::getDepart, depart);
            lqw2.eq(AssessReportEvaluationSummary::getCurrentYear, currentAssessInfo.getCurrentYear());
            List<AssessReportEvaluationSummary> summaries = reportEvaluationSummaryMapper.selectList(lqw2);
            if (summaries != null && !summaries.isEmpty()) {
                for (AssessReportEvaluationSummary summary : summaries) {
                    summary.setNum(summary.getNum() + voteNum);
                    reportEvaluationSummaryMapper.updateById(summary);
                }
            }

        }

        return insertUsers;
    }

    @Override
    public List<SysUser> getAllLeader() {
        return userCommonMapper.getAllLeaders();
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
}
