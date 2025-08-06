package org.jeecg.modules.regular.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.regular.dto.RegularExportDTO;
import org.jeecg.modules.regular.entity.AssessRegularReportItem;

import java.util.List;

/**
 * @Description: 平时考核填报明细表
 * @Author: admin
 * @Date: 2024-08-30
 * @Version: V1.0
 */
public interface AssessRegularReportItemMapper extends BaseMapper<AssessRegularReportItem> {

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
     * @return List<AssessRegularReportItem>
     */
    public List<AssessRegularReportItem> selectByMainId(@Param("mainId") String mainId);

    /**
     * 通过主表id查询子表数据
     *
     * @param currentYear 副表年度
     * @return List<AssessRegularReportItem>
     */
    public List<AssessRegularReportItem> itemList(@Param("currentYear") String currentYear);

    /**
     * 查询冲突项信息
     *
     * @param name 名称
     * @param sex  性别
     * @return List<AssessRegularReportItem>
     */
    public List<AssessRegularReportItem> conflictList(@Param("name") String name, @Param("sex") String sex);

    /**
     * 查询干部信息
     */
    public List<RegularExportDTO> getAllCadres(String year);

    /**
     * 查询员工信息
     *
     */
    public List<RegularExportDTO> getAllEmployee(String year);

    /**
     * 查询全体信息
     *
     */
    public List<RegularExportDTO> getAllClerk(String year);

}
