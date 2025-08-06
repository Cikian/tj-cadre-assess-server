package org.jeecg.modules.annual.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.annual.dto.Dem4jjGradeDTO;
import org.jeecg.modules.annual.dto.DemGradeDTO;
import org.jeecg.modules.annual.dto.DemGradeItemsDTO;
import org.jeecg.modules.annual.vo.DepartProgressVO;
import org.jeecg.modules.annual.vo.LeaderProgressVO;
import org.jeecg.modules.sys.dto.DemocraticInitDTO;
import org.jeecg.modules.sys.dto.VmDemocraticAvgA;
import org.jeecg.modules.sys.entity.annual.AssessDemocraticEvaluation;
import org.jeecg.modules.sys.entity.annual.AssessDemocraticEvaluationSummary;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @Description: 民主测评汇总
 * @Author: jeecg-boot
 * @Date: 2024-09-12
 * @Version: V1.0
 */
public interface IVmDemocraticAvgAService extends IService<VmDemocraticAvgA> {
    List<VmDemocraticAvgA> getData(QueryWrapper<VmDemocraticAvgA> queryWrapper);
}
