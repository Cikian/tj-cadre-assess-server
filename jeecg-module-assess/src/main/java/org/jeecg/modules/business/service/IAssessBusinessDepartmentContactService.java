package org.jeecg.modules.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.sys.entity.business.AssessBusinessDepartmentContact;

/**
 * @Description: 业务考核单位互评配置
 * @Author: jeecg-boot
 * @Date: 2024-08-22
 * @Version: V1.0
 */
public interface IAssessBusinessDepartmentContactService extends IService<AssessBusinessDepartmentContact> {
    Result<String> saveNewContact(AssessBusinessDepartmentContact departContact);
}
