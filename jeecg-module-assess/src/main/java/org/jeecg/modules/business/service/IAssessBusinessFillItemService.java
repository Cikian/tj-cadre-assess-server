package org.jeecg.modules.business.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.sys.entity.business.AssessBusinessFillItem;

import java.util.List;
import java.util.Map;

/**
 * @Description: 业务考核填报项目
 * @Author: jeecg-boot
 * @Date: 2024-08-22
 * @Version: V1.0
 */
public interface IAssessBusinessFillItemService extends IService<AssessBusinessFillItem> {

    /**
     * 通过主表id查询子表数据
     *
     * @param mainId 主表id
     * @return List<AssessBusinessFillItem>
     */
    public List<AssessBusinessFillItem> selectByMainId(String mainId);

    IPage<AssessBusinessFillItem> getFillItemByGradeId(String gradeId);

    void addMustAssessItem(List<Map<String, String>> array);

    List<AssessBusinessFillItem> getFillItemsByGatherId(String gatherId);
}
