package org.jeecg.modules.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.sys.entity.business.AssessBusinessDenounce;

import java.util.List;

/**
 * @Description: 通报批评
 * @Author: jeecg-boot
 * @Date: 2024-08-30
 * @Version: V1.0
 */
public interface IAssessBusinessDenounceService extends IService<AssessBusinessDenounce> {
    List<AssessBusinessDenounce> queryByDepartIdAndYear(String departId, String year);

    public List<AssessBusinessDenounce> listByDepartmentCodeAndYear(String departmentCode, String year);


    public byte[] exportExcel(List<String> ids);
    boolean save(AssessBusinessDenounce denounce);

    boolean updateById(AssessBusinessDenounce denounce);

    List<AssessBusinessDenounce> getCurrentDenounce(String year, String depart);

    String getInfo(String year, String depart);
}
