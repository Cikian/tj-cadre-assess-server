package org.jeecg.modules.sys.mapper.business;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.sys.entity.business.AssessBusinessFillItem;

import java.util.List;

/**
 * @Description: 业务考核填报项目
 * @Author: jeecg-boot
 * @Date: 2024-08-22
 * @Version: V1.0
 */
public interface AssessBusinessFillItemMapper extends BaseMapper<AssessBusinessFillItem> {

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
     * @return List<AssessBusinessFillItem>
     */
    public List<AssessBusinessFillItem> selectByMainId(@Param("mainId") String mainId);

    List<AssessBusinessFillItem> selectByGatherId(@Param("gatherId") String gatherId);

    @Update("update assess_business_fill_item set score = NULL, reason_of_deduction = '0' where id = #{itemId}")
    void setScoreNull(@Param("itemId") String itemId);


}
