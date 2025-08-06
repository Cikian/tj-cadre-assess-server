package org.jeecg.modules.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jeecg.modules.sys.mapper.report.AssessReportDemocracyConfigItemMapper;
import org.jeecg.modules.sys.mapper.report.AssessReportDemocracyConfigMapper;
import org.jeecg.modules.sys.mapper.report.AssessReportMultiItemMapper;
import org.jeecg.modules.report.service.IAssessReportDemocracyConfigService;
import org.jeecg.modules.report.vo.AssessReportDemocracyConfigPage;
import org.jeecg.modules.sys.entity.report.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @Description: 一报告两评议民主测评配置
 * @Author: jeecg-boot
 * @Date: 2024-10-07
 * @Version: V1.0
 */
@Service
public class AssessReportDemocracyConfigServiceImpl extends ServiceImpl<AssessReportDemocracyConfigMapper, AssessReportDemocracyConfig> implements IAssessReportDemocracyConfigService {

    @Autowired
    private AssessReportDemocracyConfigMapper democracyConfigMapper;
    @Autowired
    private AssessReportDemocracyConfigItemMapper configItemMapper;
    @Autowired
    private AssessReportMultiItemMapper multiItemMapper;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMain(AssessReportDemocracyConfig democracyConfig,
                         List<AssessReportDemocracyConfigItem> configItemList,
                         List<AssessReportMultiItem> multiItems) {

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        AssessReportDemocracyConfigItemMapper itemMapperNew = sqlSession.getMapper(AssessReportDemocracyConfigItemMapper.class);
        AssessReportMultiItemMapper multiItemMapper = sqlSession.getMapper(AssessReportMultiItemMapper.class);
        String configId = IdWorker.getIdStr();
        democracyConfig.setId(configId);
        democracyConfigMapper.insert(democracyConfig);

        for (AssessReportDemocracyConfigItem item : configItemList) {
            item.setConfigId(configId);
            if ("2".equals(item.getType())) {
                String itemId = IdWorker.getIdStr();
                for (AssessReportMultiItem multiItem : multiItems) {
                    if (multiItem.getItemId().equals(item.getId())) {
                        multiItem.setItemId(itemId);
                        multiItem.setId(null);
                    }
                }
                item.setId(itemId);
            } else {
                item.setId(IdWorker.getIdStr());
            }
        }

        // 过滤出multiItems中id为null的元素
        List<AssessReportMultiItem> multiItemsInsert = multiItems.stream().filter(item -> item.getId() == null).collect(Collectors.toList());

        configItemList.forEach(itemMapperNew::insert);
        multiItemsInsert.forEach(multiItemMapper::insert);

        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMain(AssessReportDemocracyConfig config,
                           List<AssessReportDemocracyConfigItem> itemList,
                           List<AssessReportMultiItem> multiItems) {
//        democracyConfigMapper.updateById(config);
        democracyConfigMapper.deleteById(config.getId());

        // 1.先删除子表数据
        configItemMapper.deleteByMainId(config.getId());

        multiItemMapper.delete(new QueryWrapper<AssessReportMultiItem>().eq("item_id", multiItems.get(0).getItemId()));

        if (config.getEnable()){
            config.setEnable(true);
        }

        this.saveMain(config,itemList,multiItems);

//        // 2.子表数据重新插入
//        if (itemList != null && !itemList.isEmpty()) {
//            for (AssessReportDemocracyConfigItem entity : itemList) {
//                // 外键设置
//                entity.setConfigId(config.getId());
//                configItemMapper.insert(entity);
//            }
//        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delMain(String id) {
        configItemMapper.deleteByMainId(id);
        democracyConfigMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delBatchMain(Collection<? extends Serializable> idList) {
        for (Serializable id : idList) {
            configItemMapper.deleteByMainId(id.toString());
            democracyConfigMapper.deleteById(id);
        }
    }

    @Override
    public AssessReportDemocracyConfigPage selectById(String id) {
        System.out.println("-------------------");
        AssessReportDemocracyConfig byId = this.getById(id);
        List<AssessReportDemocracyConfigItem> items = configItemMapper.selectByMainId(id);

        List<String> itemIds = items.stream().map(AssessReportDemocracyConfigItem::getId).collect(Collectors.toList());

        LambdaQueryWrapper<AssessReportMultiItem> qw = new LambdaQueryWrapper<>();
        qw.in(AssessReportMultiItem::getItemId, itemIds);
        List<AssessReportMultiItem> multiItems = multiItemMapper.selectList(qw);

        AssessReportDemocracyConfigPage page = new AssessReportDemocracyConfigPage();
        BeanUtils.copyProperties(byId, page);
        page.setConfigItems(items);
        page.setMultiItems(multiItems);

        System.out.println("=========================");
        System.out.println(byId);
        System.out.println(items);
        System.out.println(multiItems);
        System.out.println(page);

        return page;
    }

    @Override
    public List<AssessReportEvaluationItem> getFillItems() {
        LambdaQueryWrapper<AssessReportDemocracyConfig> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessReportDemocracyConfig::getEnable, 1);
        AssessReportDemocracyConfig config = democracyConfigMapper.selectOne(lqw);

        String enabledConfigId = config.getId();

        LambdaQueryWrapper<AssessReportDemocracyConfigItem> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(AssessReportDemocracyConfigItem::getConfigId, enabledConfigId);
        lqw2.orderByAsc(AssessReportDemocracyConfigItem::getItemOrder);
        List<AssessReportDemocracyConfigItem> items = configItemMapper.selectList(lqw2);

        List<AssessReportEvaluationItem> evaluationItems = new ArrayList<>();
        for (AssessReportDemocracyConfigItem item : items) {
            AssessReportEvaluationItem evaluationItem = new AssessReportEvaluationItem();
            String itemId = IdWorker.getIdStr();
            evaluationItem.setId(itemId);
            if ("2".equals(item.getType())) {
                LambdaQueryWrapper<AssessReportMultiItem> lqw3 = new LambdaQueryWrapper<>();
                lqw3.eq(AssessReportMultiItem::getItemId, item.getId());
                List<AssessReportMultiItem> multiItems = multiItemMapper.selectList(lqw3);

                List<AssessReportDemocracyMulti> multiList = new ArrayList<>();
                for (AssessReportMultiItem multiItem : multiItems) {
                    AssessReportDemocracyMulti multi = new AssessReportDemocracyMulti();
                    multi.setId(IdWorker.getIdStr());
                    multi.setItemId(itemId);
                    multi.setTopic(multiItem.getTopic());
                    multiList.add(multi);
                }

                evaluationItem.setMultiList(multiList);

            }
            evaluationItem.setTopic(item.getTitle());
            evaluationItem.setInputType(item.getType());
            evaluationItem.setItemOrder(item.getItemOrder());
            System.out.println(item.getItemOrder());
            evaluationItems.add(evaluationItem);
        }

        return evaluationItems;
    }

    @Override
    public boolean updateEnable(String id) {
        LambdaUpdateWrapper<AssessReportDemocracyConfig> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(AssessReportDemocracyConfig::getEnable, 0);
        int update1 = democracyConfigMapper.update(null, updateWrapper);

        LambdaUpdateWrapper<AssessReportDemocracyConfig> updateWrapper2 = new LambdaUpdateWrapper<>();
        updateWrapper2.set(AssessReportDemocracyConfig::getEnable, 1);
        updateWrapper2.eq(AssessReportDemocracyConfig::getId, id);

        int update = democracyConfigMapper.update(null, updateWrapper2);

        return update1 > 0 && update > 0;
    }

}
