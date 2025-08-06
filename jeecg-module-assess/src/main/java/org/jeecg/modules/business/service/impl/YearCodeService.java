package org.jeecg.modules.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.sys.entity.business.YearCode;
import org.jeecg.modules.sys.mapper.business.YearCodeMapper;
import org.jeecg.modules.business.service.IYearCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class YearCodeService extends ServiceImpl<YearCodeMapper, YearCode> implements IYearCodeService {
    @Autowired
    private YearCodeMapper yearCodeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveNewYearCode(String year) {
        LambdaQueryWrapper<YearCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(YearCode::getYearKey, year);
        List<YearCode> currentYearCodes = yearCodeMapper.selectList(wrapper);
        for (YearCode yearCode : currentYearCodes) {
            if (yearCode.getYearKey().equals(year) || yearCode.getYearValue().equals(year)) {
                return;
            }
        }

        YearCode yearCode = new YearCode(year, year);

        List<YearCode> yearCodes = yearCodeMapper.selectList(null);
        yearCodeMapper.delete(null);

        yearCodeMapper.insert(yearCode);
        for (YearCode kv : yearCodes) {
            yearCodeMapper.insert(kv);
        }
    }
}
