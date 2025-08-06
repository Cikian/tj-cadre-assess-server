package org.jeecg.modules.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.sys.entity.business.AssessBusinessCommend;

import java.util.List;

/**
 * @Description: 表彰
 * @Author: jeecg-boot
 * @Date: 2024-08-30
 * @Version: V1.0
 */
public interface IAssessBusinessCommendService extends IService<AssessBusinessCommend> {
    List<AssessBusinessCommend> queryByDepartIdAndYear(String departId, String year);

    public List<AssessBusinessCommend> listByDepartmentCodeAndYear(String departmentCode, String year);

    boolean save(AssessBusinessCommend commend);

    boolean updateById(AssessBusinessCommend commend);

    public byte[] exportExcel(List<String> ids);

    List<AssessBusinessCommend> getCurrentCommend(String year, String depart);

    String getInfo(String year, String depart);

}
