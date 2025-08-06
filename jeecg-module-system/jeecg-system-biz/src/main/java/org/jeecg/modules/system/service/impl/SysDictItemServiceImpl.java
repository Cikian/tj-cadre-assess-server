package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.system.entity.SysDictItem;
import org.jeecg.modules.system.mapper.SysDictItemMapper;
import org.jeecg.modules.system.service.ISysDictItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
@Service
public class SysDictItemServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictItem> implements ISysDictItemService {

    @Autowired
    private SysDictItemMapper sysDictItemMapper;

    @Override
    public List<SysDictItem> selectItemsByMainId(String mainId) {
        return sysDictItemMapper.selectItemsByMainId(mainId);
    }

    @Override
    public void saveNewDictItem(SysDictItem sysDictItem) {
        LambdaQueryWrapper<SysDictItem> query = new LambdaQueryWrapper<>();
        query.eq(SysDictItem::getDictId, sysDictItem.getDictId());
        query.eq(SysDictItem::getItemValue, sysDictItem.getItemValue());
        SysDictItem item = sysDictItemMapper.selectOne(query);
        if (item == null) {
            sysDictItemMapper.insert(sysDictItem);
        }
    }
}
