package org.jeecg.modules.annual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Synchronized;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.SysDepartModel;
import org.jeecg.modules.annual.dto.Dem4jjGradeDTO;
import org.jeecg.modules.annual.dto.DemGradeDTO;
import org.jeecg.modules.annual.dto.DemGradeItemsDTO;
import org.jeecg.modules.annual.service.IAssessAnnualDemocraticConfigService;
import org.jeecg.modules.annual.service.IAssessDemocraticEvaluationSummaryService;
import org.jeecg.modules.annual.service.IVmDemocraticAvgAService;
import org.jeecg.modules.annual.vo.DepartProgressVO;
import org.jeecg.modules.annual.vo.LeaderProgressVO;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.dto.DemocraticInitDTO;
import org.jeecg.modules.sys.dto.VmDemocraticAvgA;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.annual.*;
import org.jeecg.modules.sys.entity.business.AssessLeaderDepartConfig;
import org.jeecg.modules.sys.mapper.annual.*;
import org.jeecg.modules.sys.mapper.business.AssessLeaderDepartConfigMapper;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.user.UserCommonApi;
import org.jeecg.modules.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: 民主测评汇总
 * @Author: jeecg-boot
 * @Date: 2024-09-12
 * @Version: V1.0
 */
@Service
public class VmDemocraticAvgAServiceImpl extends ServiceImpl<VwDemocraticAvgAMapper, VmDemocraticAvgA> implements IVmDemocraticAvgAService {

    @Autowired
    private VwDemocraticAvgAMapper avgMapper;

    @Override
    public List<VmDemocraticAvgA> getData(QueryWrapper<VmDemocraticAvgA> queryWrapper) {
        return avgMapper.selectList(queryWrapper);
    }

}
