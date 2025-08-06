package org.jeecg.modules.sys.dto;


import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;


/**
 * 干部选拔任用工作民主评议结果汇总表
 */
@Data
public class ReportLeaderWorkDetailsDTO {
    private String id;
    @Dict(dictTable = "sys_depart", dicText = "alias", dicCode = "id")
    private String depart;
    private String year;

    private int code;//序号

    private String voteType;//票类

    private int total;//参评总人数

    //第1题（对本地区（单位）干部选拔任用工作的总体评价）
    private int goodNum1;//选择好的人数
    private double goodRate1;//选择好的比例
    private int betterNum1;//选择较好的人数
    private double betterRate1;//选择较好的比例
    private int generalNum1;//选择一般的人数
    private double generalRate1;//选择一般的比例
    private int badNum1;//选择差的人数
    private double badRate1;//选择差的比例

    //第2题（对本地区（单位）加强干部全方位管理和经常性监督情况的评价）
    private int goodNum2;//选择好的人数
    private double goodRate2;//选择好的比例
    private int betterNum2;//选择较好的人数
    private double betterRate2;//选择较好的比例
    private int generalNum2;//选择一般的人数
    private double generalRate2;//选择一般的比例
    private int badNum2;//选择差的人数
    private double badRate2;//选择差的比例

    //第3题（对上一年度评议反映问题整改情况的评价）
    private int goodNum3;//选择好的人数
    private double goodRate3;//选择好的比例
    private int betterNum3;//选择较好的人数
    private double betterRate3;//选择较好的比例
    private int generalNum3;//选择一般的人数
    private double generalRate3;//选择一般的比例
    private int badNum3;//选择差的人数
    private double badRate3;//选择差的比例

    //第4题（聚焦落实党中央和市委要求，聚焦本地区（单位）职责使命，加强领导班子和干部队伍建设不够）
    private int num4;//选择此项的人数
    private double rate4;//选择此项的比例

    //第5题（干部队伍建设统筹谋划不够、干部队伍结构不优）
    private int num5;//选择此项的人数
    private double rate5;//选择此项的比例

    //第6题（干部队伍能力素质不适应工作要求）
    private int num6;//选择此项的人数
    private double rate6;//选择此项的比例

    //第7题（干部队伍精神状态不佳或斗争精神不足）
    private int num7;//选择此项的人数
    private double rate7;//选择此项的比例

    //第8题（选人用人突出政治标准不够，严把政治关、廉洁关不到位）
    private int num8;//选择此项的人数
    private double rate8;//选择此项的比例

    //第9题（选人用人注重工作实绩、群众公认不够）
    private int num9;//选择此项的人数
    private double rate9;//选择此项的比例

    //第10题（激励担当作为有差距，推进领导干部能上能下不力）
    private int num10;//选择此项的人数
    private double rate10;//选择此项的比例

    //第11题（选人用人落实民主集中制不到位，存在“个人说了算”）
    private int num11;//选择此项的人数
    private double rate11;//选择此项的比例

    //第12题（坚持五湖四海、任人唯贤不够，存在任人唯亲、搞小圈子等不正之风）
    private int num12;//选择此项的人数
    private double rate12;//选择此项的比例

    //第13题（执行干部选拔任用政策和程序不规范）
    private int num13;//选择此项的人数
    private double rate13;//选择此项的比例

    //第14题（对“一把手”、关键岗位等管理监督不够严格，存在薄弱环节）
    private int num14;//选择此项的人数
    private double rate14;//选择此项的比例

    //第15题（市委巡视选人用人专项检查反馈问题整改落实不到位）
    private int num15;//选择此项的人数
    private double rate15;//选择此项的比例

    //第16题（您对加强和改进本地区（单位）干部选拔任用工作有何意见建议？）
    private String suggest16;//建议

    //第17题（您对加强和改进 “一报告两评议”工作有何意见建议？）
    private String suggest17;//A票建议

}
