package org.jeecg.modules.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jeecg.modules.business.service.IAssessBusinessDenounceService;
import org.jeecg.modules.sys.dto.DenounceExportDTO;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.sys.entity.business.AssessBusinessDenounce;
import org.jeecg.modules.sys.entity.business.AssessBusinessGrade;
import org.jeecg.modules.sys.mapper.business.AssessBusinessDenounceMapper;
import org.jeecg.modules.sys.mapper.business.AssessBusinessGradeMapper;
import org.jeecg.modules.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: 通报批评
 * @Author: jeecg-boot
 * @Date: 2024-08-30
 * @Version: V1.0
 */
@Service
public class AssessBusinessDenounceServiceImpl extends ServiceImpl<AssessBusinessDenounceMapper, AssessBusinessDenounce> implements IAssessBusinessDenounceService {

    @Autowired
    private AssessBusinessDenounceMapper denounceMapper;
    @Autowired
    private AssessBusinessGradeMapper gradeMapper;
    @Autowired
    private DepartCommonApi departCommonApi;
    @Autowired
    private AssessBusinessDenounceMapper assessBusinessDenounceMapper;

    @Override
    public List<AssessBusinessDenounce> queryByDepartIdAndYear(String departId, String year) {
        LambdaQueryWrapper<AssessBusinessDenounce> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessBusinessDenounce::getDepartmentCode, departId);
        lqw.eq(AssessBusinessDenounce::getCurrentYear, year);
        return this.list(lqw);
    }

    @Override
    public List<AssessBusinessDenounce> listByDepartmentCodeAndYear(String departmentCode, String year) {
        QueryWrapper<AssessBusinessDenounce> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("department_code", departmentCode).eq("current_year", year);
        return denounceMapper.selectList(queryWrapper);
    }

    @Override
    public boolean save(AssessBusinessDenounce denounce) {
        this.getRemark(denounce);
        return super.save(denounce);
    }

    @Override
    public boolean updateById(AssessBusinessDenounce denounce) {
        this.getRemark(denounce);

        return super.updateById(denounce);
    }

    private void getRemark(AssessBusinessDenounce denounce) {
        // 某时某单位受某单位某表彰
        String time = CommonUtils.getFormatDateStr(denounce.getDenounceTime(), "yyyy年MM月dd日");
        String depart = departCommonApi.queryDepartById(denounce.getDepartmentCode()).getDepartName();
        String denounceUnit = denounce.getDenounceUnit();
        String denounceProject = denounce.getDenounceProject();

        denounceUnit = denounceUnit == null ? "" : denounceUnit;
        String remark = time + depart + "因【"+denounceProject+"】"+"受到" + denounceUnit + "通报；";
        denounce.setRemark(remark);
    }

    @Override
    public byte[] exportExcel(List<String> ids) {
        List<DenounceExportDTO> DenounceExportDTOList = new ArrayList<>();
        ;

        if (ids == null || ids.isEmpty()) {
            DenounceExportDTOList = assessBusinessDenounceMapper.getAllDenounce();
        } else {
            for (String id : ids) {
                List<DenounceExportDTO> departCommends = assessBusinessDenounceMapper.getDepartDenounce(id);
                if (departCommends != null && !departCommends.isEmpty()) {
                    DenounceExportDTOList.addAll(departCommends);
                }
            }
        }

        // 创建工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Denounce Data");

        // 创建字体
        Font headerFont = workbook.createFont();
        headerFont.setFontName("宋体");
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 16);

        Font dataFont = workbook.createFont();
        dataFont.setFontName("宋体");
        dataFont.setFontHeightInPoints((short) 14);

        // 创建单元格样式
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        // 居中对齐
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 垂直居中
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setFont(dataFont);
        dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // 创建表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("序号");
        headerRow.createCell(1).setCellValue("通报信息");

        // 设置表头样式
        for (int i = 0; i < 2; i++) {
            Cell headerCell = headerRow.getCell(i);
            headerCell.setCellStyle(headerCellStyle);
        }

        // 设置表头行高
        headerRow.setHeightInPoints(40);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");

        // 填充数据
        for (int i = 0; i < DenounceExportDTOList.size(); i++) {
            DenounceExportDTO dto = DenounceExportDTOList.get(i);
            Row row = sheet.createRow(i + 1);
            // 序号
            row.createCell(0).setCellValue(i + 1);

            String commendInfo = sdf.format(dto.getDenounceTime()) + dto.getAlias() + "受" + dto.getDenounceUnit() + dto.getDenounceProject() + "的通报";
            // 表彰信息
            row.createCell(1).setCellValue(commendInfo);

            // 设置数据单元格样式
            for (int j = 0; j < 2; j++) {
                Cell dataCell = row.getCell(j);
                dataCell.setCellStyle(dataCellStyle);
            }

            // 设置数据行高
            row.setHeightInPoints(35);
        }

        // 设置号列宽
        sheet.setColumnWidth(0, 10 * 256);
        // 表彰信息列宽
        sheet.setColumnWidth(1, 90 * 256);

        // 将 Excel 写入输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return outputStream.toByteArray();
    }

    @Override
    public List<AssessBusinessDenounce> getCurrentDenounce(String year, String depart) {
        LambdaQueryWrapper<AssessBusinessDenounce> qw = new LambdaQueryWrapper<>();
        qw.eq(AssessBusinessDenounce::getCurrentYear, year);
        qw.eq(AssessBusinessDenounce::getDepartmentCode, depart);
        return this.list(qw);
    }

    @Override
    public String getInfo(String year, String depart) {
        LambdaQueryWrapper<AssessBusinessDenounce> qw = new LambdaQueryWrapper<>();
        qw.eq(AssessBusinessDenounce::getCurrentYear, year);
        qw.eq(AssessBusinessDenounce::getDepartmentCode, depart);
        qw.eq(AssessBusinessDenounce::getStatus, "0");
        List<AssessBusinessDenounce> denounce = denounceMapper.selectList(qw);
        if (!denounce.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (AssessBusinessDenounce businessDenounce : denounce) {
                if (businessDenounce.getRemark() != null && !businessDenounce.getRemark().isEmpty()) {
                    sb.append(businessDenounce.getRemark()).append("\n");
                }
            }
            return sb.toString();
        }
        return "";
    }
}
