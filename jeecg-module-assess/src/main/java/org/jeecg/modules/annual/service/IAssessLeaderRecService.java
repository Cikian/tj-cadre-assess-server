package org.jeecg.modules.annual.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.sys.entity.annual.*;

/**
 * @Description: 业务考核等次默认比例
 * @Author: jeecg-boot
 * @Date: 2024-10-29
 * @Version: V1.0
 */
public interface IAssessLeaderRecService extends IService<AssessLeaderRec> {

    void initRec();

    void flush();

    void pushDown(String id);

    void reject(String leader);

    AssessLeaderRec getByLeaderId();
}
