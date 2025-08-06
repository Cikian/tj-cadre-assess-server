package org.jeecg.modules.annual.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.sys.entity.annual.AssessDemocraticEvaluationItem;

import java.util.List;

/**
 * @Description: 民主测评项
 * @Author: jeecg-boot
 * @Date: 2024-09-11
 * @Version: V1.0
 */
public interface IAssessDemocraticEvaluationItemService extends IService<AssessDemocraticEvaluationItem> {

    /**
     * 通过主表id查询子表数据
     *
     * @param mainId 主表id
     * @return List<AssessDemocraticEvaluationItem>
     */
    public List<AssessDemocraticEvaluationItem> selectByMainId(String mainId);
}
