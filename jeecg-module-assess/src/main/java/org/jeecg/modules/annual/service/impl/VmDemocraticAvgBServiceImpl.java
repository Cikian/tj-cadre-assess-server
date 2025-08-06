package org.jeecg.modules.annual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.annual.service.IVmDemocraticAvgAService;
import org.jeecg.modules.annual.service.IVmDemocraticAvgBService;
import org.jeecg.modules.sys.dto.VmDemocraticAvgA;
import org.jeecg.modules.sys.dto.VmDemocraticAvgB;
import org.jeecg.modules.sys.mapper.annual.VwDemocraticAvgAMapper;
import org.jeecg.modules.sys.mapper.annual.VwDemocraticAvgBMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: 民主测评汇总
 * @Author: jeecg-boot
 * @Date: 2024-09-12
 * @Version: V1.0
 */
@Service
public class VmDemocraticAvgBServiceImpl extends ServiceImpl<VwDemocraticAvgBMapper, VmDemocraticAvgB> implements IVmDemocraticAvgBService {

    @Autowired
    private VwDemocraticAvgBMapper avgMapper;

    @Override
    public List<VmDemocraticAvgB> getData(QueryWrapper<VmDemocraticAvgB> queryWrapper) {
        return avgMapper.selectList(queryWrapper);
    }

}
