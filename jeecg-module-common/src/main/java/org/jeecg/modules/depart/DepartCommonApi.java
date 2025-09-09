package org.jeecg.modules.depart;

import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.SysDepartModel;
import org.jeecg.modules.depart.dto.DepartLeadersDTO;
import org.jeecg.modules.depart.dto.DepartLeadersRegularGradeDTO;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.SysDepartTreeModel;

import java.util.List;
import java.util.Map;

public interface DepartCommonApi extends ISysBaseAPI {
    /**
     * 将当前考核年度插入考核年度字典
     * @param year
     */
    void saveAssessYear(String year);

    /**
     * 获取当前登录用户的部门信息
     */
    Map<String, String> getCurrentUserDepart();

    /**
     * 获取所有部门信息
     */
    List<SysDepartModel> queryAllDepart();

    /**
     * 根据类型获取处室
     * @param types
     */
    List<SysDepartModel> queryDepartByType(List<String> types);

    List<SysDepartModel> queryDepartByType(String[] types);

    /**
     * 根据id获取单位领导
     * @param departId
     */
    List<DepartLeadersDTO> queryDepartLeaders(String departId);

    /**
     * 根据id获取单位领导，分页
     * @param departId
     * @param pageSize
     * @param pageNo
     */
    List<SysUser> queryDepartLeadersByPage(String departId, int pageSize, int pageNo);

    /**
     * 根据单位id和年度获取单位领导平时考核成绩
     * @param departId
     * @param year
     */
//    List<DepartLeadersRegularGradeDTO> queryDepartLeadersRegularGrade(String departId, String year);

    /**
     * 根据hashId和年度获取单位领导平时考核成绩
     *
     * @param hashId
     * @param year
     */
    DepartLeadersRegularGradeDTO queryDepartLeaderRegularGrade(String hashId, String year);

    /**
     * 根据单位id查询单位
     *
     * @param departId
     */
    SysDepart queryDepartById(String departId);



    /**
     * 查询所有部门信息,并分节点进行显示
     * @return
     */
    List<SysDepartTreeModel> queryTreeList();


    /**
     * 查询所有部门信息,并分节点进行显示
     * @param ids 多个部门id
     * @return
     */
    List<SysDepartTreeModel> queryTreeList(String ids);

    /**
     * 查询所有部门信息,并分节点进行显示
     * @param types 部门类型
     * @return
     */
    List<SysDepartTreeModel> queryTreeList(List<String> types);

    /**
     * 查询当前用户部门类型
     * @param id 部门名称
     * @return 部门类型
     */
    String getDepartTypeById(String id);

    /**
     * 查询部门类型
     * @param departName
     * @return
     */
    String getDepartTypeByName(String departName);

    Map<String, String> getId2NameMap();

}
