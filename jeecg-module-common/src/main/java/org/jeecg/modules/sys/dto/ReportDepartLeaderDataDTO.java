package org.jeecg.modules.sys.dto;


import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;


/**
 * 单位新提拔干部民主评议结果
 */
@Data
public class ReportDepartLeaderDataDTO {
    private String id;
    @Dict(dictTable = "sys_depart", dicText = "alias", dicCode = "id")
    private String depart;
    private String year;

    private int code;//序号

    private String person;//被评议干部

    private String voteType;//票类
    private int total;//参评总人数

    private int agreeNum;//认同人数
    private double agreeRate;//认同比例
    private int basicAgreeNum;//基本认同人数
    private double basicAgreeRate;//基本认同比例
    private int disagreeNum;//不认同人数
    private double disagreeRate;//不认同比例
    private int notUnderstandNum;//不了解人数
    private double notUnderstandRate;//不了解比例

    private String remarks;//备注

}
