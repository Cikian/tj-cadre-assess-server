package org.jeecg.modules.annual.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.sys.entity.annual.AssessAnnualDemocraticItem;
import org.jeecg.modules.sys.mapper.annual.AssessAnnualDemocraticItemMapper;
import org.jeecg.modules.annual.service.IAssessAnnualDemocraticItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: 民主测评指标项
 * @Author: jeecg-boot
 * @Date: 2024-09-11
 * @Version: V1.0
 */
@Service
public class AssessAnnualDemocraticItemServiceImpl extends ServiceImpl<AssessAnnualDemocraticItemMapper, AssessAnnualDemocraticItem> implements IAssessAnnualDemocraticItemService {

    @Autowired
    private AssessAnnualDemocraticItemMapper democraticItemMapper;

    @Override
    public List<AssessAnnualDemocraticItem> selectByMainId(String mainId) {
        return democraticItemMapper.selectByMainId(mainId);
    }

    @Override
    public List<AssessAnnualDemocraticItem> getItemByType(String type) {
        return democraticItemMapper.getLastVersionItemsByType(type);
    }
}
