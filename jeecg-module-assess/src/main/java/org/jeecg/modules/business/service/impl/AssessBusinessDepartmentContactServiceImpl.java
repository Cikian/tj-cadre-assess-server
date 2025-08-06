package org.jeecg.modules.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.sys.entity.business.AssessBusinessDepartmentContact;
import org.jeecg.modules.sys.mapper.business.AssessBusinessDepartmentContactMapper;
import org.jeecg.modules.business.service.IAssessBusinessDepartmentContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 业务考核单位互评配置
 * @Author: jeecg-boot
 * @Date: 2024-08-22
 * @Version: V1.0
 */
@Service
public class AssessBusinessDepartmentContactServiceImpl extends ServiceImpl<AssessBusinessDepartmentContactMapper, AssessBusinessDepartmentContact> implements IAssessBusinessDepartmentContactService {
    @Autowired
    private AssessBusinessDepartmentContactMapper contactMapper;

    @Override
    public Result<String> saveNewContact(AssessBusinessDepartmentContact departContact) {
        // 获取考核处室ID
        String departmentCode = departContact.getDepartmentCode();

        // 查询是否存在与当前添加考核处室相同的记录
        LambdaQueryWrapper<AssessBusinessDepartmentContact> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessBusinessDepartmentContact::getDepartmentCode, departmentCode);
        AssessBusinessDepartmentContact savedDepartContact = this.getOne(lqw);
        if (savedDepartContact != null) {
            return Result.error("不可重复添加！");
        }

        // 判断被考核处室中是否存在当前添加考核处室
        String assessedDepartCode = departContact.getAssessedDepartmentCode();
        String[] assessedDepartCodeArr = assessedDepartCode.split(",");
        List<String> assessedDepartIdsList = Arrays.asList(assessedDepartCodeArr);
        if (assessedDepartIdsList.contains(departmentCode)) {
            return Result.error("不可与本部门互评！");
        }

        // 将互评单位一同插入互评关系
        LambdaQueryWrapper<AssessBusinessDepartmentContact> lqw2 = new LambdaQueryWrapper<>();
        for (int i = 0; i < assessedDepartIdsList.size(); i++) {
            // 清空wrapper查询条件
            lqw2.clear();
            lqw2.eq(AssessBusinessDepartmentContact::getDepartmentCode, assessedDepartIdsList.get(i));
            lqw2.select(AssessBusinessDepartmentContact::getDepartmentCode, AssessBusinessDepartmentContact::getAssessedDepartmentCode);
            AssessBusinessDepartmentContact contact = contactMapper.selectOne(lqw2);
            // 如果存在则更新，否则插入
            if (contact != null) {
                String assessedIds = contact.getAssessedDepartmentCode();
                String[] assessedIdsArr = assessedIds.split(",");
                List<String> assessedIdsList = Arrays.asList(assessedIdsArr);
                if (!assessedIdsList.contains(departmentCode)) {
                    LambdaUpdateWrapper<AssessBusinessDepartmentContact> luw = new LambdaUpdateWrapper<>();
                    luw.eq(AssessBusinessDepartmentContact::getDepartmentCode, assessedDepartIdsList.get(i));
                    luw.set(AssessBusinessDepartmentContact::getAssessedDepartmentCode, assessedIds + "," + departmentCode);
                    contactMapper.update(null, luw);
                }
            } else {
                AssessBusinessDepartmentContact newContact = new AssessBusinessDepartmentContact();
                newContact.setDepartmentCode(assessedDepartIdsList.get(i));
                newContact.setAssessedDepartmentCode(departmentCode);
                this.save(newContact);
            }
        }


        this.save(departContact);
        return Result.OK("添加成功！");
    }

    @Override
    public boolean updateById(AssessBusinessDepartmentContact entity) {

        // 获取考核处室ID
        String departmentCode = entity.getDepartmentCode();


        // 判断被考核处室中是否存在当前添加考核处室
        String assessedDepartCode = entity.getAssessedDepartmentCode();
        String[] assessedDepartCodeArr = assessedDepartCode.split(",");
        List<String> assessedDepartIdsList = Arrays.asList(assessedDepartCodeArr);
        if (assessedDepartIdsList.contains(departmentCode)) {
            throw new JeecgBootException("不可与本部门互评！");
        }

        LambdaQueryWrapper<AssessBusinessDepartmentContact> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessBusinessDepartmentContact::getDepartmentCode, departmentCode);
        AssessBusinessDepartmentContact savedDepartContact = this.getOne(lqw);
        if (savedDepartContact == null) {
            throw new JeecgBootException("未找到对应数据");
        }

        String savedAssessedIds = savedDepartContact.getAssessedDepartmentCode();
        String[] savedAssessedIdsArr = savedAssessedIds.split(",");
        List<String> savedAssessedIdsList = Arrays.asList(savedAssessedIdsArr);

        List<String> deleteAssessedIdsList = new ArrayList<>();
        for (String s : savedAssessedIdsList) {
            if (!assessedDepartIdsList.contains(s)) {
                deleteAssessedIdsList.add(s);
            }
        }

        // 删除不再存在的互评单位
        for (String deleted : deleteAssessedIdsList) {
            lqw.clear();
            lqw.eq(AssessBusinessDepartmentContact::getDepartmentCode, deleted);
            AssessBusinessDepartmentContact contact = contactMapper.selectOne(lqw);
            String assessedDepartmentCode = contact.getAssessedDepartmentCode();
            String[] assessedDepartmentCodeArr = assessedDepartmentCode.split(",");
            List<String> assessedDepartmentCodeList = new ArrayList<>(Arrays.asList(assessedDepartmentCodeArr));
            assessedDepartmentCodeList.remove(departmentCode);
            if (assessedDepartmentCodeList.isEmpty()) {
                contactMapper.deleteById(contact.getId());
            } else {
                LambdaUpdateWrapper<AssessBusinessDepartmentContact> luw = new LambdaUpdateWrapper<>();
                luw.eq(AssessBusinessDepartmentContact::getDepartmentCode, deleted);
                luw.set(AssessBusinessDepartmentContact::getAssessedDepartmentCode, String.join(",", assessedDepartmentCodeList));
                contactMapper.update(null, luw);
            }
        }

        // 将互评单位一同插入互评关系
        LambdaQueryWrapper<AssessBusinessDepartmentContact> lqw2 = new LambdaQueryWrapper<>();
        for (String s : assessedDepartIdsList) {
            // 清空wrapper查询条件
            lqw2.clear();
            lqw2.eq(AssessBusinessDepartmentContact::getDepartmentCode, s);
            lqw2.select(AssessBusinessDepartmentContact::getDepartmentCode, AssessBusinessDepartmentContact::getAssessedDepartmentCode);
            AssessBusinessDepartmentContact contact = contactMapper.selectOne(lqw2);
            // 如果存在则更新，否则插入
            if (contact != null) {
                String assessedIds = contact.getAssessedDepartmentCode();
                String[] assessedIdsArr = assessedIds.split(",");
                List<String> assessedIdsList = Arrays.asList(assessedIdsArr);
                if (!assessedIdsList.contains(departmentCode)) {
                    LambdaUpdateWrapper<AssessBusinessDepartmentContact> luw = new LambdaUpdateWrapper<>();
                    luw.eq(AssessBusinessDepartmentContact::getDepartmentCode, s);
                    luw.set(AssessBusinessDepartmentContact::getAssessedDepartmentCode, assessedIds + "," + departmentCode);
                    contactMapper.update(null, luw);
                }
            } else {
                AssessBusinessDepartmentContact newContact = new AssessBusinessDepartmentContact();
                newContact.setDepartmentCode(s);
                newContact.setAssessedDepartmentCode(departmentCode);
                this.save(newContact);
            }
        }


        contactMapper.updateById(entity);
        return true;

    }
}
