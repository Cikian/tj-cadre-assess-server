package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.system.entity.SysUserOa;
import org.jeecg.modules.system.mapper.SysUserOaMapper;
import org.jeecg.modules.system.service.ISysUserOaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SysUserOaServiceImpl extends ServiceImpl<SysUserOaMapper, SysUserOa> implements ISysUserOaService {
    @Autowired
    private SysUserOaMapper sysUserOaMapper;
}
