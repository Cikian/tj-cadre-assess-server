package org.jeecg.modules.sys.entity.business;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@TableName("year_code")
public class YearCode {
    @TableId(type = IdType.ASSIGN_ID)
    private String orderCodeId;
    private String yearKey;
    private String yearValue;

    public YearCode(String yearKey, String yearValue) {
        this.yearKey = yearKey;
        this.yearValue = yearValue;
    }
}
