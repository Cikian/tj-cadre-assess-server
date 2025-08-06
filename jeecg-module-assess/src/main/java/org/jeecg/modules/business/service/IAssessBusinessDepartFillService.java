package org.jeecg.modules.business.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.business.entity.BusinessIndexEmployee;
import org.jeecg.modules.business.entity.BusinessIndexLeader;
import org.jeecg.modules.sys.dto.AssessBusinessDepartFillDTO;
import org.jeecg.modules.sys.entity.AssessCurrentAssess;
import org.jeecg.modules.sys.entity.business.AssessBusinessFillItem;
import org.jeecg.modules.sys.entity.business.AssessBusinessDepartFill;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Description: 业务考核填报列表
 * @Author: jeecg-boot
 * @Date: 2024-08-22
 * @Version: V1.0
 */
public interface IAssessBusinessDepartFillService extends IService<AssessBusinessDepartFill> {

    /**
     * 添加一对多
     *
     * @param assessBusinessDepartFill
     * @param assessBusinessFillItemList
     */
    public void saveMain(AssessBusinessDepartFill assessBusinessDepartFill,
                         List<AssessBusinessFillItem> assessBusinessFillItemList);

    /**
     * 修改一对多
     *
     * @param assessBusinessDepartFill
     * @param assessBusinessFillItemList
     * @return
     */
    public Result<String> updateMain(AssessBusinessDepartFill assessBusinessDepartFill,
                                     List<AssessBusinessFillItem> assessBusinessFillItemList);

    public void saveTemp(AssessBusinessDepartFill assessBusinessDepartFill,
                         List<AssessBusinessFillItem> assessBusinessFillItemList);

    /**
     * 删除一对多
     *
     * @param id
     */
    public void delMain(String id);

    /**
     * 批量删除一对多
     *
     * @param idList
     */
    public void delBatchMain(Collection<? extends Serializable> idList);


    /**
     * 业务考核首页领导信息
     *
     */
    public BusinessIndexLeader getAllItems();

    /**
     * 业务考核首页员工信息
     *
     */
    public BusinessIndexEmployee[] getPartial(String departId, AssessCurrentAssess assessInfo);


    void initAssess(AssessBusinessDepartFillDTO map);

    Result<String> passAudit(String assessId,String name, List<AssessBusinessFillItem> itemList);

    IPage<AssessBusinessDepartFill> listByPageToReporter(Page page, QueryWrapper queryWrapper);

    IPage<AssessBusinessDepartFill> listByPageToHeader(Page page, QueryWrapper queryWrapper);

    IPage<AssessBusinessDepartFill> listByPageDistRole(Page page, QueryWrapper<AssessBusinessDepartFill> queryWrapper);

    void stopAssess();
    void revocationAssess();

    boolean assessNone(List<AssessBusinessFillItem> list);

}
