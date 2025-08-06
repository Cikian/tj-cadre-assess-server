package org.jeecg.modules.annual.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.annual.service.IAssessLeaderRecItemService;
import org.jeecg.modules.annual.service.IAssessLeaderRecService;
import org.jeecg.modules.sys.AssessCommonApi;
import org.jeecg.modules.sys.entity.annual.AssessLeaderRec;
import org.jeecg.modules.sys.entity.annual.AssessLeaderRecItem;
import org.jeecg.modules.sys.mapper.annual.AssessLeaderRecItemMapper;
import org.jeecg.modules.sys.mapper.annual.AssessLeaderRecMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description: 业务考核等次默认比例
 * @Author: jeecg-boot
 * @Date: 2024-10-29
 * @Version: V1.0
 */
@Service
public class AssessLeaderRecItemServiceImpl extends ServiceImpl<AssessLeaderRecItemMapper, AssessLeaderRecItem> implements IAssessLeaderRecItemService {
@Autowired
private AssessLeaderRecMapper recMapper;
@Autowired
private AssessLeaderRecItemMapper recItemMapper;
@Autowired
private AssessCommonApi assessCommonApi;

}
