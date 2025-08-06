package org.jeecg.modules.sys.mapper.annual;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.sys.entity.annual.AssessAnnualDemocraticItem;

import java.util.List;

/**
 * @Description: 民主测评指标项
 * @Author: jeecg-boot
 * @Date: 2024-09-11
 * @Version: V1.0
 */
public interface AssessAnnualDemocraticItemMapper extends BaseMapper<AssessAnnualDemocraticItem> {

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
     * @return List<AssessAnnualDemocraticItem>
     */
    public List<AssessAnnualDemocraticItem> selectByMainId(@Param("mainId") String mainId);

    List<AssessAnnualDemocraticItem> getLastVersionItemsByType(@Param("type") String type);

}
