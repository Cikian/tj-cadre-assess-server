package org.jeecg.modules.sys.dto;


import lombok.Data;

/**
 * @className: ArrangeDTO
 * @author: Cikian
 * @date: 2024/11/13 17:01
 * @Version: 1.0
 * @description: 生成匿名账号用
 */

@Data
public class ArrangeDTO {
    private String depart;
    private String departType;
    private int voteA;
    private int voteB;
    private int voteC;
}
