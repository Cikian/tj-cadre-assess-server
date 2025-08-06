package org.jeecg.modules.annual.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.sys.entity.annual.AssessAnnualDemocraticItem;

import java.util.List;

/**
 * @Description: 民主测评指标项
 * @Author: jeecg-boot
 * @Date: 2024-09-11
 * @Version: V1.0
 */
public interface IAssessAnnualDemocraticItemService extends IService<AssessAnnualDemocraticItem> {

    /**
     * 通过主表id查询子表数据
     *
     * @param mainId 主表id
     * @return List<AssessAnnualDemocraticItem>
     */
    List<AssessAnnualDemocraticItem> selectByMainId(String mainId);

    List<AssessAnnualDemocraticItem> getItemByType(String type);

}
