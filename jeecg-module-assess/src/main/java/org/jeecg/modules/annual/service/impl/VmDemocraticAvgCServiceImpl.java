package org.jeecg.modules.annual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.annual.service.IVmDemocraticAvgAService;
import org.jeecg.modules.annual.service.IVmDemocraticAvgCService;
import org.jeecg.modules.sys.dto.VmDemocraticAvgA;
import org.jeecg.modules.sys.dto.VmDemocraticAvgC;
import org.jeecg.modules.sys.mapper.annual.VwDemocraticAvgAMapper;
import org.jeecg.modules.sys.mapper.annual.VwDemocraticAvgCMapper;
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
public class VmDemocraticAvgCServiceImpl extends ServiceImpl<VwDemocraticAvgCMapper, VmDemocraticAvgC> implements IVmDemocraticAvgCService {

    @Autowired
    private VwDemocraticAvgCMapper avgMapper;

    @Override
    public List<VmDemocraticAvgC> getData(QueryWrapper<VmDemocraticAvgC> queryWrapper) {
        return avgMapper.selectList(queryWrapper);
    }

}
