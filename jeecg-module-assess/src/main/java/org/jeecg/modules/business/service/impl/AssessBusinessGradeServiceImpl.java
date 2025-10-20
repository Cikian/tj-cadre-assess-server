package org.jeecg.modules.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Synchronized;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.SysDepartModel;
import org.jeecg.modules.business.service.IAssessBusinessCommendService;
import org.jeecg.modules.business.service.IAssessBusinessDenounceService;
import org.jeecg.modules.business.service.IAssessBusinessGradeService;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.dto.AssessBusinessDepartGradeDTO;
import org.jeecg.modules.sys.dto.AssessBusinessGradeDTO;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.business.*;
import org.jeecg.modules.sys.mapper.business.AssessBusinessDepartFillMapper;
import org.jeecg.modules.sys.mapper.business.AssessBusinessFillItemMapper;
import org.jeecg.modules.sys.mapper.business.AssessBusinessGradeMapper;
import org.jeecg.modules.sys.mapper.business.AssessBusinessLevelRatioMapper;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.mapper.SysDepartMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 业务考核成绩汇总
 * @Author: jeecg-boot
 * @Date: 2024-08-28
 * @Version: V1.0
 */
@Service
public class AssessBusinessGradeServiceImpl extends ServiceImpl<AssessBusinessGradeMapper, AssessBusinessGrade> implements IAssessBusinessGradeService {
    @Autowired
    private AssessBusinessGradeMapper gradeMapper;
    @Autowired
    private AssessBusinessDepartFillMapper departFillMapper;
    @Autowired
    private AssessBusinessFillItemMapper itemMapper;
    @Autowired
    private SysDepartMapper departMapper;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private AssessCommonApi assessCommonApi;
    @Autowired
    private DepartCommonApi departCommonApi;
    @Autowired
    private AssessBusinessLevelRatioMapper levelRatioMapper;

    @Autowired
    private IAssessBusinessCommendService assessBusinessCommendService;

    @Autowired
    private IAssessBusinessDenounceService assessBusinessDenounceService;

    @Override
    @Synchronized
    public boolean summaryScore(String currentYear, List<AssessBusinessFillItem> itemList) {

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        AssessBusinessGradeMapper gradeMapperNew = sqlSession.getMapper(AssessBusinessGradeMapper.class);

        String gradeId = null;

        List<AssessBusinessGrade> insertList = new ArrayList<>();
        List<AssessBusinessFillItem> updataList = new ArrayList<>();
        List<String> updateGradeIdsList = new ArrayList<>();


        // 遍历填报项列表
        for (AssessBusinessFillItem item : itemList) {
            String assessedDepartId = item.getDepartCode(); // 被考核部门ID

            // 查询当前年份、被考核部门的考核成绩
            LambdaQueryWrapper<AssessBusinessGrade> lqw = new LambdaQueryWrapper<>();
            lqw.eq(AssessBusinessGrade::getDepartId, assessedDepartId);
            lqw.eq(AssessBusinessGrade::getCurrentYear, currentYear);
            AssessBusinessGrade savedGrade = gradeMapper.selectOne(lqw);

            // todo: 获取被考核部门的gradeId，作为填报项的gatherId
            if (savedGrade == null) {
                // 如果当前年份、被考核部门的考核成绩不存在，则新增
                // 设置被考核部门的类型
                SysDepart sysDepart = departMapper.selectById(assessedDepartId);
                // String departType = null;
                // // TODO： 硬编码
                // if ("1".equals(sysDepart.getDepartType())) {
                //     departType = "1";
                // } else if ("2".equals(sysDepart.getDepartType()) || "3".equals(sysDepart.getDepartType())) {
                //     departType = "2";
                // } else {
                //     departType = "3";
                // }

                // 新建考核成绩
                AssessBusinessGrade grade = new AssessBusinessGrade();
                gradeId = IdWorker.getIdStr();
                grade.setId(gradeId);
                grade.setCurrentYear(currentYear);
                grade.setDepartId(assessedDepartId);
                grade.setDepartType(sysDepart.getDepartType());
                insertList.add(grade);
            } else {
                gradeId = savedGrade.getId();
            }


//            LambdaUpdateWrapper<AssessBusinessFillItem> wrapper = new LambdaUpdateWrapper<>();
//            wrapper.eq(AssessBusinessFillItem::getId, item.getId());
//            wrapper.set(AssessBusinessFillItem::getGatherId, gradeId);

            // 更新填报项的gatherId
            item.setGatherId(gradeId);
            updataList.add(item);
            updateGradeIdsList.add(gradeId);
        }

//        LambdaQueryWrapper<AssessBusinessFillItem> lqw = new LambdaQueryWrapper<>();
//        lqw.eq(AssessBusinessFillItem::getGatherId, gradeId);
//        itemMapper.delete(lqw);

        insertList.forEach(gradeMapperNew::insert);
        updataList.forEach(itemMapper::updateById);

        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();

        // 计算受影响的考核的平均分
        this.calculateGrades(updateGradeIdsList);
        // 更新排名和等次
        this.updateBusinessRanking(currentYear);
        this.updateBusinessLevel(currentYear);

        return true;
    }

    // 计算某个考核的平均分
    @Override
    public void calculateGradesByGradeId(String gradeId) {
        // 查询某个考核的所有填报项
        LambdaQueryWrapper<AssessBusinessFillItem> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessBusinessFillItem::getGatherId, gradeId);
        List<AssessBusinessFillItem> itemList = itemMapper.selectList(lqw);

        BigDecimal totalScore = new BigDecimal(0);
        // 遍历填报项列表，计算总分
        for (AssessBusinessFillItem item : itemList) {
            totalScore = totalScore.add(item.getScore());
        }
        // 计算平均分
        BigDecimal averageScore = totalScore.divide(new BigDecimal(itemList.size()), 2, RoundingMode.HALF_UP);
        AssessBusinessGrade grade = gradeMapper.selectById(gradeId);
        grade.setScore(averageScore);
        grade.setAssessScore(averageScore);
        LambdaUpdateWrapper<AssessBusinessGrade> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AssessBusinessGrade::getId, gradeId);
        wrapper.set(AssessBusinessGrade::getScore, averageScore);
        wrapper.set(AssessBusinessGrade::getAssessScore, averageScore);
        gradeMapper.update(grade, wrapper);
    }

    @Override
    @Synchronized
    public void calculateGrades(List<String> gradeIds) {
        List<AssessBusinessGrade> updateList = gradeMapper.selectBatchIds(gradeIds);
        for (String gradeId : gradeIds) {
            // 查询某个考核的所有填报项
            LambdaQueryWrapper<AssessBusinessFillItem> lqw = new LambdaQueryWrapper<>();
            lqw.eq(AssessBusinessFillItem::getGatherId, gradeId);
            List<AssessBusinessFillItem> itemList = itemMapper.selectList(lqw);

            BigDecimal totalScore = new BigDecimal(0);
            // 遍历填报项列表，计算总分
            for (AssessBusinessFillItem item : itemList) {
                totalScore = totalScore.add(item.getScore());
            }
            // 计算平均分
            BigDecimal averageScore = totalScore.divide(new BigDecimal(itemList.size()), 2, RoundingMode.HALF_UP);
            AssessBusinessGrade grade = gradeMapper.selectById(gradeId);
            grade.setScore(averageScore);
            grade.setAssessScore(averageScore);
            updateList.add(grade);
        }

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        AssessBusinessGradeMapper gradeMapperNew = sqlSession.getMapper(AssessBusinessGradeMapper.class);

        updateList.forEach(gradeMapperNew::updateById);

        sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();
    }

    @Override
    public Result<String> edit(AssessBusinessGrade assessBusinessGrade) {
        BigDecimal addPoints = assessBusinessGrade.getAddPoints();
        BigDecimal subtractPoints = assessBusinessGrade.getSubtractPoints();
        if (addPoints != null) {
            // 判断addSubtractPoints是否为整数
            // if (addPoints.scale() > 0) {
            //     // return Result.error("加分值只能为整数！");
            // }

            if (addPoints.compareTo(new BigDecimal(0)) > 0) {
                String addReason = assessBusinessGrade.getAddReason();
                if (addReason == null || addReason.trim().isEmpty()) {
                    return Result.error("加分原因不能为空！");
                }
            }
        }

        if (subtractPoints != null) {
            // if (subtractPoints.scale() > 0) {
            //     return Result.error("减分值只能为整数！");
            // }

            if (subtractPoints.compareTo(new BigDecimal(0)) > 0) {
                String subtractReason = assessBusinessGrade.getSubtractReason();
                if (subtractReason == null || subtractReason.trim().isEmpty()) {
                    return Result.error("减分原因不能为空！");
                }
            }
        }
        BigDecimal assessScore = assessBusinessGrade.getAssessScore();
        if (addPoints != null) {
            assessScore = assessScore.add(addPoints);
        }
        if (subtractPoints != null) {
            assessScore = assessScore.subtract(subtractPoints);
        }
        assessBusinessGrade.setScore(assessScore);
        boolean levelIsChanged = this.checkLevelIsChanged(assessBusinessGrade);
        if (levelIsChanged) {
            assessBusinessGrade.setManEdit(1);
        }
        this.updateById(assessBusinessGrade);
        this.updateBusinessRanking();
        this.updateBusinessLevel();
        return Result.OK("修改成功!");
    }

    @Override
    public Map<String, BigDecimal> getProportionCount(String year, String type) {
        if (type.equals("undefined")) {
            type = "1";
        }
        List<String> typeList = new ArrayList<>();
        if (type.equals("1") || type.equals("2")) {
            typeList.add(type);
        } else {
            typeList.add("3");
            typeList.add("4");
        }

        int excellentCount = gradeMapper.getExcellentCount(year, typeList);
        int goodCount = gradeMapper.getGoodCount(year, typeList);
        int generalCount = gradeMapper.getGeneralCount(year, typeList);
        int total = excellentCount + goodCount + generalCount;

        if (total == 0) {
            return null;
        }
        // 转换成BigDecimal类型，保留两位小数
        BigDecimal totalBigDecimal = new BigDecimal(total);
        BigDecimal excellentCountBigDecimal = new BigDecimal(excellentCount);
        BigDecimal goodCountBigDecimal = new BigDecimal(goodCount);
        BigDecimal generalCountBigDecimal = new BigDecimal(generalCount);
        BigDecimal hundred = new BigDecimal(100);


        BigDecimal excellentProportionBigDecimal = excellentCountBigDecimal.multiply(hundred).divide(totalBigDecimal, 0, RoundingMode.HALF_UP);
        BigDecimal goodProportionBigDecimal = goodCountBigDecimal.multiply(hundred).divide(totalBigDecimal, 0, RoundingMode.HALF_UP);
        BigDecimal generalProportionBigDecimal = generalCountBigDecimal.multiply(hundred).divide(totalBigDecimal, 0, RoundingMode.HALF_UP);

        Map<String, BigDecimal> proportionMap = new HashMap<>();
        proportionMap.put("excellentProportion", excellentProportionBigDecimal);
        proportionMap.put("goodProportion", goodProportionBigDecimal);
        proportionMap.put("generalProportion", generalProportionBigDecimal);

        return proportionMap;
    }

    @Override
    public List<AssessBusinessGradeDTO> getReportQuery(String currentYear, List<String> departType) {
        if ("0".equals(currentYear)) {
            // 获取当前年份
            Calendar calendar = Calendar.getInstance();
            currentYear = String.valueOf(calendar.get(Calendar.YEAR));
        }

        LambdaQueryWrapper<AssessBusinessGrade> lqw = new LambdaQueryWrapper<>();
        lqw.in(AssessBusinessGrade::getDepartType, departType);
        lqw.eq(AssessBusinessGrade::getCurrentYear, currentYear);
        lqw.orderByDesc(AssessBusinessGrade::getScore);
        List<AssessBusinessGrade> grades = gradeMapper.selectList(lqw);
        List<AssessBusinessGradeDTO> dtoList = new ArrayList<>();

        int rank = 1;

        for (AssessBusinessGrade grade : grades) {
            AssessBusinessGradeDTO dto = new AssessBusinessGradeDTO();
            dto.setCurrentYear(grade.getCurrentYear());
            dto.setDepartId(grade.getDepartId());
            dto.setScore(String.valueOf(grade.getScore()));
            dto.setLevel(Integer.valueOf(grade.getLevel()));
            dto.setDepartType(Integer.valueOf(grade.getDepartType()));
            dto.setRankingVirtual(String.valueOf(rank));

            dtoList.add(dto);

            rank++;
        }

//        return gradeMapper.getReportQuery(currentYear, departType);
        return dtoList;
    }

    @Override
    public AssessBusinessDepartGradeDTO getDepartReportQuery(String currentYear, String departId, String exportDepart) {
        if ("0".equals(currentYear)) {
            // 获取当前年份
            Calendar calendar = Calendar.getInstance();
            currentYear = String.valueOf(calendar.get(Calendar.YEAR));
        }
        AssessBusinessDepartGradeDTO departReportQuery = gradeMapper.getDepartReportQuery(currentYear, departId);

        Map<String, String> itemReasonMap = this.getItemReason(departId, currentYear);
        // 合并考核项原因，加入换行符
        int index = 1;
        StringBuilder reason = new StringBuilder();
        for (String reportDepart : itemReasonMap.keySet()) {
            String data = itemReasonMap.get(reportDepart);
            if ("1".equals(exportDepart)) {
                String departName = departCommonApi.queryDepartById(reportDepart).getDepartName();
                reason.append(index).append("、").append(departName).append("：").append(data).append("\n");
            } else {
                reason.append(index).append("、").append(data).append("\n");
            }
            index++;
        }
        departReportQuery.setAssessSubReason(reason.toString());

        return departReportQuery;
    }

    @Override
    public List<AssessBusinessGrade> getByDepartIdAndYear(String departId, String year) {
        LambdaQueryWrapper<AssessBusinessGrade> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessBusinessGrade::getDepartId, departId);
        lqw.eq(AssessBusinessGrade::getCurrentYear, year);
        return gradeMapper.selectList(lqw);
    }

    @Override
    public List<SysDepartModel> getNonAssessDepart(String year) {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("business");
        if (currentAssessInfo == null) {
            throw new JeecgBootException("当前无业务工作测评！");
        }
        if (!currentAssessInfo.isAssessing()) {
            throw new JeecgBootException(currentAssessInfo.getAssessName() + "已结束！");
        }

        List<SysDepartModel> allSysDepart = departCommonApi.getAllSysDepart();
        LambdaQueryWrapper<AssessBusinessGrade> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessBusinessGrade::getCurrentYear, year);
        List<AssessBusinessGrade> assessBusinessGrades = gradeMapper.selectList(lqw);
        if (assessBusinessGrades != null && !assessBusinessGrades.isEmpty()) {
            List<String> assessDepartIds = new ArrayList<>();
            assessBusinessGrades.forEach(grade -> assessDepartIds.add(grade.getDepartId()));
            List<SysDepartModel> nonAssessDepart = new ArrayList<>();
            allSysDepart.forEach(depart -> {
                if (!assessDepartIds.contains(depart.getId())) {
                    nonAssessDepart.add(depart);
                }
            });
            return nonAssessDepart;
        }
        return allSysDepart;
    }

    @Override
    public List<AssessBusinessGrade> getSummaryByYear(String year) {
        LambdaQueryWrapper<AssessBusinessGrade> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessBusinessGrade::getCurrentYear, year);
        return gradeMapper.selectList(lqw);
    }

    @Override
    public void updateBusinessRanking() {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("business");
        List<String> departTypeList = new ArrayList<>(Arrays.asList("1", "2", "3,4"));

        if (currentAssessInfo != null) {
            for (String departType : departTypeList) {
                LambdaQueryWrapper<AssessBusinessGrade> lqw = new LambdaQueryWrapper<>();
                lqw.eq(AssessBusinessGrade::getCurrentYear, currentAssessInfo.getCurrentYear());
                if (departType.equals("3,4")) {
                    String[] split = departType.split(",");
                    lqw.in(AssessBusinessGrade::getDepartType, split);
                } else {
                    lqw.eq(AssessBusinessGrade::getDepartType, departType);
                }
                lqw.orderByDesc(AssessBusinessGrade::getScore);
                List<AssessBusinessGrade> gradeList = gradeMapper.selectList(lqw);

                int ranking = 1;
                int ranking1 = 1;
//                for (AssessBusinessGrade grade : gradeList) {
//                    grade.setRanking(ranking);
//                    ranking++;
//                }
                for (int i = 0; i < gradeList.size(); i++) {
                    AssessBusinessGrade assessBusinessGrade = gradeList.get(i);
                    if (i > 0) {
                        AssessBusinessGrade assessBusinessGrade1 = gradeList.get(i - 1);
                        if (assessBusinessGrade.getScore().compareTo(assessBusinessGrade1.getScore()) == 0) {
                            assessBusinessGrade.setRanking(ranking);
                        } else {
                            ranking = ranking1;
                            assessBusinessGrade.setRanking(ranking1);
                        }
                    } else {
                        assessBusinessGrade.setRanking(ranking);
                    }
                    ranking1++;
                }

                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
                AssessBusinessGradeMapper gradeMapperNew = sqlSession.getMapper(AssessBusinessGradeMapper.class);

                gradeList.forEach(gradeMapperNew::updateById);

                sqlSession.commit();
                sqlSession.clearCache();
                sqlSession.close();
            }
            // gradeMapper.updateRankingByYear(currentAssessInfo.getCurrentYear());
        }
    }

    @Override
    @Synchronized
    public void updateBusinessRanking(String currentYear) {
        List<String> departTypeList = new ArrayList<>(Arrays.asList("1", "2", "3,4"));

        for (String departType : departTypeList) {
            LambdaQueryWrapper<AssessBusinessGrade> lqw = new LambdaQueryWrapper<>();
            lqw.eq(AssessBusinessGrade::getCurrentYear, currentYear);
            if (departType.equals("3,4")) {
                String[] split = departType.split(",");
                lqw.in(AssessBusinessGrade::getDepartType, split);
            } else {
                lqw.eq(AssessBusinessGrade::getDepartType, departType);
            }
            lqw.orderByDesc(AssessBusinessGrade::getScore);
            List<AssessBusinessGrade> gradeList = gradeMapper.selectList(lqw);

            int ranking = 1;
            int ranking1 = 1;
//                for (AssessBusinessGrade grade : gradeList) {
//                    grade.setRanking(ranking);
//                    ranking++;
//                }
            for (int i = 0; i < gradeList.size(); i++) {
                AssessBusinessGrade assessBusinessGrade = gradeList.get(i);
                if (i > 0) {
                    AssessBusinessGrade assessBusinessGrade1 = gradeList.get(i - 1);
                    if (assessBusinessGrade.getAssessScore().compareTo(assessBusinessGrade1.getAssessScore()) == 0) {
                        assessBusinessGrade.setRanking(ranking);
                    } else {
                        ranking = ranking1;
                        assessBusinessGrade.setRanking(ranking1);
                    }
                } else {
                    assessBusinessGrade.setRanking(ranking);
                }
                ranking1++;
            }

            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
            AssessBusinessGradeMapper gradeMapperNew = sqlSession.getMapper(AssessBusinessGradeMapper.class);

            gradeList.forEach(gradeMapperNew::updateById);

            sqlSession.commit();
            sqlSession.clearCache();
            sqlSession.close();
        }
        // gradeMapper.updateRankingByYear(currentAssessInfo.getCurrentYear());
    }

    @Override
    public void updateBusinessLevel() {
        AssessCurrentAssess currentAssessInfo = assessCommonApi.getCurrentAssessInfo("business");
        List<String> departTypeList = new ArrayList<>(Arrays.asList("1", "2", "3,4"));

        if (currentAssessInfo != null) {
            AssessBusinessLevelRatio enabled = levelRatioMapper.getEnabled();
            if (enabled == null) {
                throw new JeecgBootException("未设置业务考核等次比例！");
            }
            for (String departType : departTypeList) {
                BigDecimal excellent = enabled.getExcellent();
                BigDecimal fine = enabled.getFine();
                BigDecimal excellentScore = excellent.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
                BigDecimal fineScore = fine.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
                LambdaQueryWrapper<AssessBusinessGrade> lqw = new LambdaQueryWrapper<>();
                lqw.eq(AssessBusinessGrade::getCurrentYear, currentAssessInfo.getCurrentYear());
                if (departType.equals("3,4")) {
                    String[] split = departType.split(",");
                    lqw.in(AssessBusinessGrade::getDepartType, split);
                } else {
                    lqw.eq(AssessBusinessGrade::getDepartType, departType);
                }
                lqw.orderByAsc(AssessBusinessGrade::getRanking);
                List<AssessBusinessGrade> gradeList = gradeMapper.selectList(lqw);

                int var1 = gradeList.size() * excellent.intValue() / 100;
                int var2 = gradeList.size() * (excellent.intValue() + fine.intValue()) / 100;

                for (AssessBusinessGrade grade : gradeList) {
                    if (grade.getManEdit() == 1) {
                        continue;
                    }
                    Integer ranking = grade.getRanking();
                    if (ranking <= var1) {
                        grade.setLevel("1");
                    } else if (ranking <= var2) {
                        grade.setLevel("2");
                    } else {
                        grade.setLevel("3");
                    }
                }

                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
                AssessBusinessGradeMapper gradeMapperNew = sqlSession.getMapper(AssessBusinessGradeMapper.class);

                gradeList.forEach(gradeMapperNew::updateById);

                sqlSession.commit();
                sqlSession.clearCache();
                sqlSession.close();
            }


            // gradeMapper.updateLevelByYear(currentAssessInfo.getCurrentYear(), excellentScore, fineScore);
        }
    }

    @Override
    @Synchronized
    public void updateBusinessLevel(String currentYear) {
        List<String> departTypeList = new ArrayList<>(Arrays.asList("1", "2", "3,4"));

        AssessBusinessLevelRatio enabled = levelRatioMapper.getEnabled();
        if (enabled == null) {
            throw new JeecgBootException("未设置业务考核等次比例！");
        }
        for (String departType : departTypeList) {
            BigDecimal excellent = enabled.getExcellent();
            BigDecimal fine = enabled.getFine();
            BigDecimal excellentScore = excellent.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            BigDecimal fineScore = fine.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            LambdaQueryWrapper<AssessBusinessGrade> lqw = new LambdaQueryWrapper<>();
            lqw.eq(AssessBusinessGrade::getCurrentYear, currentYear);
            if (departType.equals("3,4")) {
                String[] split = departType.split(",");
                lqw.in(AssessBusinessGrade::getDepartType, split);
            } else {
                lqw.eq(AssessBusinessGrade::getDepartType, departType);
            }
            lqw.orderByAsc(AssessBusinessGrade::getRanking);
            List<AssessBusinessGrade> gradeList = gradeMapper.selectList(lqw);

            int var1 = gradeList.size() * excellent.intValue() / 100;
            int var2 = gradeList.size() * (excellent.intValue() + fine.intValue()) / 100;

            for (AssessBusinessGrade grade : gradeList) {
                if (grade.getManEdit() == 1) {
                    continue;
                }
                Integer ranking = grade.getRanking();
                if (ranking <= var1) {
                    grade.setLevel("1");
                } else if (ranking <= var2) {
                    grade.setLevel("2");
                } else {
                    grade.setLevel("3");
                }
            }

            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
            AssessBusinessGradeMapper gradeMapperNew = sqlSession.getMapper(AssessBusinessGradeMapper.class);

            gradeList.forEach(gradeMapperNew::updateById);

            sqlSession.commit();
            sqlSession.clearCache();
            sqlSession.close();
        }


        // gradeMapper.updateLevelByYear(currentAssessInfo.getCurrentYear(), excellentScore, fineScore);
    }


    boolean checkLevelIsChanged(AssessBusinessGrade grade) {
        LambdaQueryWrapper<AssessBusinessGrade> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessBusinessGrade::getId, grade.getId());
        AssessBusinessGrade savedGrade = gradeMapper.selectOne(lqw);
        return !Objects.equals(savedGrade.getLevel(), grade.getLevel());
    }

    @Override
    public List<AssessBusinessGrade> getGradesBetweenYears(String startYear, String endYear) {
        QueryWrapper<AssessBusinessGrade> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("current_year", startYear)
                .le("current_year", endYear);
        return gradeMapper.selectList(queryWrapper);
    }

    @Override
    public List<AssessBusinessGrade> getGradeListByType(String departType, String year) {
        return this.selectByDepartType(departType, year);
    }

    @Override
    public List<AssessBusinessGrade> getGradeListByTypeNotEqualTo(String departType, String year) {
        return gradeMapper.selectByDepartTypeNotEqualTo(departType, year);
    }

    @Override
    public List<String> getDepartAliases(String type) {
        QueryWrapper<SysDepart> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("depart_order");
        if ("机关".equals(type)) {
            queryWrapper.ne("depart_type", "1").isNotNull("depart_type");
        } else {
            queryWrapper.eq("depart_type", "1").isNotNull("depart_type");
        }
        // 查询部门别名
        List<SysDepart> departList = departMapper.selectList(queryWrapper);
        return departList.stream().map(SysDepart::getAlias).collect(Collectors.toList());
    }

    @Override
    public List<AssessBusinessGrade> listByCurrentYear(String year) {
        QueryWrapper<AssessBusinessGrade> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("current_year", year);
        return gradeMapper.selectList(queryWrapper);
    }

    @Override
    public List<AssessBusinessGrade> selectByDepartType(String departType, String year) {
        LambdaQueryWrapper<AssessBusinessGrade> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.isNotNull(AssessBusinessGrade::getDepartType).eq(AssessBusinessGrade::getDepartType, departType).eq(AssessBusinessGrade::getCurrentYear, year);
        return this.list(lambdaQueryWrapper);
    }

    @Override
    public boolean updateAddOrSubtractReason(String departId, String year, boolean isAdd) {
        if (StringUtils.isBlank(departId)) {
            return false;
        }
        LambdaQueryWrapper<AssessBusinessGrade> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AssessBusinessGrade::getDepartId, departId).eq(AssessBusinessGrade::getCurrentYear, year);
        List<AssessBusinessGrade> list = this.list(lambdaQueryWrapper);
        if (list.isEmpty()) {
            return false;
        }
        AssessBusinessGrade grade = list.get(0);

        LambdaQueryWrapper<AssessBusinessCommend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssessBusinessCommend::getDepartmentCode, departId).eq(AssessBusinessCommend::getCurrentYear, year);
        wrapper.eq(AssessBusinessCommend::getStatus, 3);
        List<AssessBusinessCommend> commendList = assessBusinessCommendService.list(wrapper);

        String str = "";
        for (AssessBusinessCommend commend :
                commendList) {
            str += commend.getRemark();
        }
        grade.setAddReason(str);

        LambdaQueryWrapper<AssessBusinessDenounce> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(AssessBusinessDenounce::getDepartmentCode, departId).eq(AssessBusinessDenounce::getCurrentYear, year);
        wrapper2.eq(AssessBusinessDenounce::getStatus, 3);
        List<AssessBusinessDenounce> denounceList = assessBusinessDenounceService.list(wrapper2);

        String str2 = "";
        for (AssessBusinessDenounce denounce :
                denounceList) {
            str2 += denounce.getRemark();
        }
        grade.setSubtractReason(str2);

        this.updateById(grade);
        return true;
    }

    @Override
    public Map<String, String> getItemReason(String departId, String year) {
        Map<String, String> map = new LinkedHashMap<>();
        LambdaQueryWrapper<AssessBusinessGrade> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AssessBusinessGrade::getCurrentYear, year).eq(AssessBusinessGrade::getDepartId, departId);
        List<AssessBusinessGrade> list = this.list(lambdaQueryWrapper);
        if (list.size() == 0) {
            return map;
        }
        AssessBusinessGrade assessBusinessGrade = list.get(0);
        LambdaQueryWrapper<AssessBusinessFillItem> lambdaFillItemWrapper = new LambdaQueryWrapper<>();
        lambdaFillItemWrapper.eq(AssessBusinessFillItem::getGatherId, assessBusinessGrade.getId());
        List<AssessBusinessFillItem> fillList = itemMapper.selectList(lambdaFillItemWrapper);
        for (AssessBusinessFillItem assessBusinessFillItem :
                fillList) {
            String reportDepartId = assessBusinessFillItem.getReportDepart();
            String reason = assessBusinessFillItem.getReasonOfDeduction();
            if (StringUtils.isBlank(reason)) {
                continue;
            }
            map.put(reportDepartId, reason);
        }
        return map;
    }

    @Override
    public List<AssessBusinessGrade> sort(List<AssessBusinessGrade> list) {
        List<SysDepartModel> allDepart = departCommonApi.queryAllDepart();
        Map<String, Integer> departMap = allDepart.stream().collect(Collectors.toMap(SysDepartModel::getId, SysDepartModel::getDepartOrder));
        for (AssessBusinessGrade item : list) {
            item.setDepartOrder(departMap.get(item.getDepartId()));
        }
        List<AssessBusinessGrade> sortedList = list.stream().sorted(Comparator.comparing(AssessBusinessGrade::getDepartType).thenComparing(AssessBusinessGrade::getRanking).thenComparing(AssessBusinessGrade::getDepartOrder)).collect(Collectors.toList());//升序排序
        return sortedList;
    }

    @Override
    public Map<String, List<AssessBusinessGrade>> groupByDepartType(List<AssessBusinessGrade> list) {
        Map<String, List<AssessBusinessGrade>> departmentGradesMap = new LinkedHashMap<>();
        for (AssessBusinessGrade grade : list) {
            departmentGradesMap.computeIfAbsent(grade.getDepartType(), k -> new LinkedList<>()).add(grade);
        }
        return departmentGradesMap;
    }
}
