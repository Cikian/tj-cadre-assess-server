package org.jeecg.modules.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jeecg.modules.sys.entity.business.AssessBusinessDepartFill;
import org.jeecg.modules.sys.entity.business.AssessBusinessFillItem;
import org.jeecg.modules.sys.mapper.business.AssessBusinessDepartFillMapper;
import org.jeecg.modules.sys.mapper.business.AssessBusinessFillItemMapper;
import org.jeecg.modules.business.service.IAssessBusinessFillItemService;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: 业务考核填报项目
 * @Author: jeecg-boot
 * @Date: 2024-08-22
 * @Version: V1.0
 */
@Service
public class AssessBusinessFillItemServiceImpl extends ServiceImpl<AssessBusinessFillItemMapper, AssessBusinessFillItem> implements IAssessBusinessFillItemService {

    @Autowired
    private AssessBusinessFillItemMapper fillItemMapper;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private AssessBusinessDepartFillMapper departFillMapper;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public List<AssessBusinessFillItem> selectByMainId(String mainId) {
        return fillItemMapper.selectByMainId(mainId);
    }

    @Override
    public IPage<AssessBusinessFillItem> getFillItemByGradeId(String gradeId) {
        LambdaQueryWrapper<AssessBusinessFillItem> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessBusinessFillItem::getGatherId, gradeId);
        List<AssessBusinessFillItem> itemList = fillItemMapper.selectList(lqw);
        IPage<AssessBusinessFillItem> page = new Page<>();
        page.setRecords(itemList);
        return page;
    }

    @Override
    public void addMustAssessItem(List<Map<String, String>> array) {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("business");

        LambdaQueryWrapper<AssessBusinessDepartFill> lqw = new LambdaQueryWrapper<>();

        // 获取当前年度的所有部门填报信息
        lqw.eq(AssessBusinessDepartFill::getCurrentYear, currentAssessInfo.getCurrentYear());
        List<AssessBusinessDepartFill> departFillList = departFillMapper.selectList(lqw);

        List<AssessBusinessDepartFill> departFillUpdateList = new ArrayList<>();
        List<AssessBusinessFillItem> fillItemUpdateList = new ArrayList<>();
        List<AssessBusinessFillItem> fillItemInsertList = new ArrayList<>();

        for (Map<String, String> map : array) {
            // TODO: 2024/10/24 17:35
            String assessedDepart = map.get("assessedDepart");
            String assessDeparts = map.get("assessDeparts");

            String[] assessedList = assessDeparts.split(",");
            for (String assessId : assessedList) {
                // 从所有部门填报信息中找到assessId的填报信息
                AssessBusinessDepartFill departFill = departFillList.stream().filter(item -> item.getReportDepart().equals(assessId)).findFirst().get();
                if (departFill.getAssessDepart().contains(assessedDepart)) {
                    LambdaQueryWrapper<AssessBusinessFillItem> lqwItem = new LambdaQueryWrapper<>();
                    lqwItem.eq(AssessBusinessFillItem::getFillId, departFill.getId());
                    lqwItem.eq(AssessBusinessFillItem::getDepartCode, assessedDepart);
                    AssessBusinessFillItem fillItem = fillItemMapper.selectOne(lqwItem);

                    if (fillItem != null) {
                        fillItem.setMustFill(true);
                        fillItemUpdateList.add(fillItem);
                    } else {
                        AssessBusinessFillItem fillItemInsert = new AssessBusinessFillItem();
                        fillItemInsert.setFillId(departFill.getId());
                        fillItemInsert.setDepartCode(assessedDepart);
                        fillItemInsert.setReportDepart(assessId);
                        fillItemInsert.setMustFill(true);
                        fillItemInsert.setCanEdit(true);
                        fillItemInsertList.add(fillItemInsert);
                    }

                    departFill.setStatus(1);
                    departFillUpdateList.add(departFill);
                } else {
                    AssessBusinessFillItem fillItemInsert = new AssessBusinessFillItem();
                    fillItemInsert.setFillId(departFill.getId());
                    fillItemInsert.setDepartCode(assessedDepart);
                    fillItemInsert.setReportDepart(assessId);
                    fillItemInsert.setMustFill(true);
                    fillItemInsert.setCanEdit(true);
                    fillItemInsertList.add(fillItemInsert);
                }


                departFill.setStatus(1);
                if (departFill.getAssessDepart() != null && !departFill.getAssessDepart().isEmpty()) {
                    departFill.setAssessDepart(departFill.getAssessDepart() + "," + assessedDepart);
                } else {
                    departFill.setAssessDepart(assessedDepart);
                }
                departFillUpdateList.add(departFill);
            }
        }
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        AssessBusinessFillItemMapper fillItemMapperNew = CommonUtils.getBatchMapper(sqlSession, AssessBusinessFillItemMapper.class);
        AssessBusinessDepartFillMapper fillMapperNew = CommonUtils.getBatchMapper(sqlSession, AssessBusinessDepartFillMapper.class);

        fillItemInsertList.forEach(fillItemMapperNew::insert);
        fillItemUpdateList.forEach(fillItemMapperNew::updateById);
        departFillUpdateList.forEach(fillMapperNew::updateById);
        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();

    }

    @Override
    public List<AssessBusinessFillItem> getFillItemsByGatherId(String gatherId) {
        return fillItemMapper.selectByGatherId(gatherId);
    }
}
