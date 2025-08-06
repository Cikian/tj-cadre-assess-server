package org.jeecg.modules.sys.mapper.annual;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.sys.entity.annual.AssessDemocraticEvaluationItem;

import java.util.List;

/**
 * @Description: 民主测评项
 * @Author: jeecg-boot
 * @Date: 2024-09-11
 * @Version: V1.0
 */
public interface AssessDemocraticEvaluationItemMapper extends BaseMapper<AssessDemocraticEvaluationItem> {

    /**
     * 通过主表id删除子表数据
     *
     * @param mainId 主表id
     * @return boolean
     */
    public boolean deleteByMainId(@Param("mainId") String mainId);

    /**
     * 通过主表id查询子表数据
     *
     * @param mainId 主表id
     * @return List<AssessDemocraticEvaluationItem>
     */
    public List<AssessDemocraticEvaluationItem> selectByMainId(@Param("mainId") String mainId);
}
