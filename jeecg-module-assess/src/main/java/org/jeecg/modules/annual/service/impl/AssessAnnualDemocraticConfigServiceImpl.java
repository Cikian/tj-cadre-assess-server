package org.jeecg.modules.annual.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.sys.entity.annual.AssessAnnualDemocraticConfig;
import org.jeecg.modules.sys.entity.annual.AssessAnnualDemocraticItem;
import org.jeecg.modules.sys.mapper.annual.AssessAnnualDemocraticConfigMapper;
import org.jeecg.modules.sys.mapper.annual.AssessAnnualDemocraticItemMapper;
import org.jeecg.modules.annual.service.IAssessAnnualDemocraticConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @Description: 民主测评指标
 * @Author: jeecg-boot
 * @Date: 2024-09-11
 * @Version: V1.0
 */
@Service
public class AssessAnnualDemocraticConfigServiceImpl extends ServiceImpl<AssessAnnualDemocraticConfigMapper, AssessAnnualDemocraticConfig> implements IAssessAnnualDemocraticConfigService {

    @Autowired
    private AssessAnnualDemocraticConfigMapper democraticConfigMapper;
    @Autowired
    private AssessAnnualDemocraticItemMapper democraticItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMain(AssessAnnualDemocraticConfig democraticConfig, List<AssessAnnualDemocraticItem> assessAnnualDemocraticItemList) {
        BigDecimal weightA = democraticConfig.getWeightA();
        BigDecimal weightB = democraticConfig.getWeightB();
        BigDecimal weightC = democraticConfig.getWeightC();
//        BigDecimal weightD = democraticConfig.getWeightD();
        // 除以100，保留两位小数
        democraticConfig.setWeightA(weightA.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
        democraticConfig.setWeightB(weightB.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
        democraticConfig.setWeightC(weightC.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
//        democraticConfig.setWeightD(weightD.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));

        democraticConfigMapper.insert(democraticConfig);

        if (assessAnnualDemocraticItemList != null && assessAnnualDemocraticItemList.size() > 0) {
            for (AssessAnnualDemocraticItem entity : assessAnnualDemocraticItemList) {
                //外键设置
                entity.setConfigId(democraticConfig.getId());
                democraticItemMapper.insert(entity);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMain(AssessAnnualDemocraticConfig democraticConfig, List<AssessAnnualDemocraticItem> assessAnnualDemocraticItemList) {
        BigDecimal weightA = democraticConfig.getWeightA();
        BigDecimal weightB = democraticConfig.getWeightB();
        BigDecimal weightC = democraticConfig.getWeightC();
//        BigDecimal weightD = democraticConfig.getWeightD();
        // 除以100，保留两位小数
        democraticConfig.setWeightA(weightA.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
        democraticConfig.setWeightB(weightB.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
        democraticConfig.setWeightC(weightC.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
//        democraticConfig.setWeightD(weightD.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));

        democraticConfigMapper.updateById(democraticConfig);

        //1.先删除子表数据
        democraticItemMapper.deleteByMainId(democraticConfig.getId());

        //2.子表数据重新插入
        if (assessAnnualDemocraticItemList != null && assessAnnualDemocraticItemList.size() > 0) {
            for (AssessAnnualDemocraticItem entity : assessAnnualDemocraticItemList) {
                //外键设置
                entity.setConfigId(democraticConfig.getId());
                democraticItemMapper.insert(entity);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delMain(String id) {
        democraticItemMapper.deleteByMainId(id);
        democraticConfigMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delBatchMain(Collection<? extends Serializable> idList) {
        for (Serializable id : idList) {
            democraticItemMapper.deleteByMainId(id.toString());
            democraticConfigMapper.deleteById(id);
        }
    }

    @Override
    public Map<String, BigDecimal> getOptionsByType(String type) {
        AssessAnnualDemocraticConfig lastVersionOption = democraticConfigMapper.getLastVersionOptionsByType(type);
        if (lastVersionOption != null) {
            BigDecimal weightA = lastVersionOption.getWeightA();
            BigDecimal weightB = lastVersionOption.getWeightB();
            BigDecimal weightC = lastVersionOption.getWeightC();
//            BigDecimal weightD = lastVersionOption.getWeightD();
            return new HashMap<String, BigDecimal>() {{
                put("A", weightA);
                put("B", weightB);
                put("C", weightC);
//                put("D", weightD);
            }};
        }

        return Collections.emptyMap();
    }

    public List<AssessAnnualDemocraticConfig> getLastVersions() {
        return democraticConfigMapper.getLastVersions();
    }

}
