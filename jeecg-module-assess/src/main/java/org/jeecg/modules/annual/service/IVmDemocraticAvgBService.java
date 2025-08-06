package org.jeecg.modules.annual.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.sys.dto.VmDemocraticAvgA;
import org.jeecg.modules.sys.dto.VmDemocraticAvgB;

import java.util.List;

/**
 * @Description: 民主测评汇总
 * @Author: jeecg-boot
 * @Date: 2024-09-12
 * @Version: V1.0
 */
public interface IVmDemocraticAvgBService extends IService<VmDemocraticAvgB> {
    List<VmDemocraticAvgB> getData(QueryWrapper<VmDemocraticAvgB> queryWrapper);

}
