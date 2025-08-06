package org.jeecg.modules.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.sys.entity.business.YearCode;

public interface IYearCodeService extends IService<YearCode> {
    void saveNewYearCode(String yearCode);
}
