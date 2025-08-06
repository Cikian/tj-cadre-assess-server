package org.jeecg.modules.sys.dto;

import lombok.Data;

/**
 * @Title: UserToSelectDTO
 * @Author Cikian
 * @Package org.jeecg.modules.sys.dto
 * @Date 2024/11/11 23:40
 * @description: jeecg-boot-parent: 前端搜索框使用
 */

@Data
public class UserToSelectDTO {
    private String text;
    private String value;
    private String label;
}
