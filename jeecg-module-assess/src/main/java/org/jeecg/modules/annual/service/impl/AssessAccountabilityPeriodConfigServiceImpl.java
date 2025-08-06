package org.jeecg.modules.annual.service.impl;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jeecg.modules.sys.entity.annual.AssessAccountabilityPeriodConfig;
import org.jeecg.modules.sys.mapper.annual.AssessAccountabilityPeriodConfigMapper;
import org.jeecg.modules.annual.service.IAssessAccountabilityPeriodConfigService;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.system.entity.SysCategory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description: 问责影响期配置
 * @Author: jeecg-boot
 * @Date: 2024-09-18
 * @Version: V1.0
 */
@Service
public class AssessAccountabilityPeriodConfigServiceImpl extends ServiceImpl<AssessAccountabilityPeriodConfigMapper, AssessAccountabilityPeriodConfig> implements IAssessAccountabilityPeriodConfigService {
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private AssessAccountabilityPeriodConfigMapper periodConfigMapper;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public void checkAll() {
        long start = System.currentTimeMillis();
        List<SysCategory> b03 = assessCommonApi.getCategoryLeafByParentCode("B03%");
        List<AssessAccountabilityPeriodConfig> periodConfigs = periodConfigMapper.selectList(null);
        List<String> notInB03Codes = new ArrayList<>();
        List<String> notInPeriodConfigCodes = new ArrayList<>();

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        AssessAccountabilityPeriodConfigMapper periodConfigMapperNew = sqlSession.getMapper(AssessAccountabilityPeriodConfigMapper.class);

        // 判断b03中的code是否在periodConfigs中
        for (SysCategory sysCategory : b03) {
            boolean flag = false;
            for (AssessAccountabilityPeriodConfig periodConfig : periodConfigs) {
                if (sysCategory.getCode().equals(periodConfig.getType())) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                notInPeriodConfigCodes.add(sysCategory.getCode());
            }
        }

        // 判断periodConfigs中的type是否在b03中
        for (AssessAccountabilityPeriodConfig periodConfig : periodConfigs) {
            boolean flag = false;
            for (SysCategory sysCategory : b03) {
                if (sysCategory.getCode().equals(periodConfig.getType())) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                notInB03Codes.add(periodConfig.getType());
            }
        }

        if (!notInB03Codes.isEmpty()) {
            notInB03Codes.forEach(periodConfigMapperNew::deleteByCode);
        }

        List<AssessAccountabilityPeriodConfig> insertList = new ArrayList<>();

        if (!notInPeriodConfigCodes.isEmpty()) {
            for (String code : notInPeriodConfigCodes) {
                AssessAccountabilityPeriodConfig periodConfig = new AssessAccountabilityPeriodConfig();

                for (SysCategory sysCategory : b03) {
                    if (sysCategory.getCode().equals(code)) {
                        periodConfig.setTypeText(sysCategory.getName());
                        periodConfig.setType(sysCategory.getCode());
                        break;
                    }
                }

                insertList.add(periodConfig);
            }

            insertList.forEach(periodConfigMapperNew::insert);
        }

        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();

        long end = System.currentTimeMillis();
    }

    @Override
    public String getEndDateById(String id, String startDate) {
        // String转Date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDateDate = null;
        try {
            startDateDate = sdf.parse(startDate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Integer influenceTime = 0;

        String[] split = id.split(",");
        for (String s : split) {
            Integer a = periodConfigMapper.getInfluenceTime(s);
            if (a != null && a > influenceTime) {
                influenceTime = a;
            }
        }

        // 获取影响时间（月）
        if (startDateDate != null) {
            return getString(startDateDate, influenceTime);
        }
        return null;
    }

    @NotNull
    private static String getString(Date startDateDate, Integer influenceTime) {
        int startYear = startDateDate.getYear() + 1900;
        int startMonth = startDateDate.getMonth() + 1;
        int startDay = startDateDate.getDate();

        int endMonth = startMonth + influenceTime;
        int endYear = startYear + (endMonth - 1) / 12;
        endMonth = endMonth % 12;
        if (endMonth == 0) {
            endMonth = 12;
        }
        int endDate = startDay - 1;

        String endDateStr = endYear + "-" + endMonth + "-" + endDate;
        return endDateStr;
    }
}
