package org.jeecg.modules.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jeecg.modules.business.service.IAssessBusinessCommendService;
import org.jeecg.modules.depart.DepartCommonApi;
import org.jeecg.modules.sys.dto.CommendExportDTO;
import org.jeecg.modules.sys.entity.business.AssessBusinessCommend;
import org.jeecg.modules.sys.mapper.business.AssessBusinessCommendMapper;
import org.jeecg.modules.sys.mapper.business.AssessBusinessGradeMapper;
import org.jeecg.modules.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 表彰
 * @Author: jeecg-boot
 * @Date: 2024-08-30
 * @Version: V1.0
 */
@Service
public class AssessBusinessCommendServiceImpl extends ServiceImpl<AssessBusinessCommendMapper, AssessBusinessCommend> implements IAssessBusinessCommendService {

    @Autowired
    private AssessBusinessCommendMapper commendMapper;
    @Autowired
    private AssessBusinessGradeMapper gradeMapper;
    @Autowired
    private DepartCommonApi departCommonApi;


    @Override
    public List<AssessBusinessCommend> queryByDepartIdAndYear(String departId, String year) {
        LambdaQueryWrapper<AssessBusinessCommend> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AssessBusinessCommend::getDepartmentCode, departId);
        lqw.eq(AssessBusinessCommend::getCurrentYear, year);
        return this.list(lqw);
    }

    @Override
    public List<AssessBusinessCommend> listByDepartmentCodeAndYear(String departmentCode, String year) {
        QueryWrapper<AssessBusinessCommend> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("department_code", departmentCode).eq("current_year", year);
        return commendMapper.selectList(queryWrapper);
    }

    @Override
    public boolean save(AssessBusinessCommend commend) {
        this.getRemark(commend);
        return super.save(commend);
    }

    @Override
    public boolean updateById(AssessBusinessCommend commend) {
        this.getRemark(commend);
        return super.updateById(commend);
    }

    private void getRemark(AssessBusinessCommend commend) {
        // 某时某单位受某单位某表彰
        String time = CommonUtils.getFormatDateStr(commend.getCommendTime(), "yyyy年MM月dd日");
        String depart = departCommonApi.queryDepartById(commend.getDepartmentCode()).getDepartName();
        String commendUnit = commend.getCommendUnit();
        String commendProject = commend.getCommendProject();

        commendUnit = commendUnit == null ? "" : commendUnit;
        String remark = time + depart +"因【"+commendProject+"】"+ "受到" + commendUnit + "表彰；";
        commend.setRemark(remark);
    }

    @Override
    public byte[] exportExcel(List<String> ids) {
        List<CommendExportDTO> commendExportDTOList = new ArrayList<>();

        if (ids == null || ids.isEmpty()) {
            commendExportDTOList = commendMapper.getAllCommend();
        } else {
            for (String id : ids) {
                List<CommendExportDTO> departCommends = commendMapper.getDepartCommend(id);
                if (departCommends != null && !departCommends.isEmpty()) {
                    commendExportDTOList.addAll(departCommends);
                }
            }
        }

        // 创建工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Commend Data");

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
        headerRow.createCell(1).setCellValue("表彰信息");

        // 设置表头样式
        for (int i = 0; i < 2; i++) {
            Cell headerCell = headerRow.getCell(i);
            headerCell.setCellStyle(headerCellStyle);
        }

        // 设置表头行高
        headerRow.setHeightInPoints(40);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");

        // 填充数据
        for (int i = 0; i < commendExportDTOList.size(); i++) {
            CommendExportDTO dto = commendExportDTOList.get(i);
            Row row = sheet.createRow(i + 1);
            // 序号
            row.createCell(0).setCellValue(i + 1);

            String commendInfo = sdf.format(dto.getCommendTime()) + dto.getAlias() + "受" + dto.getCommendUnit() + dto.getCommendProject() + "的表彰";
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
    public List<AssessBusinessCommend> getCurrentCommend(String year, String depart) {
        LambdaQueryWrapper<AssessBusinessCommend> qw = new LambdaQueryWrapper<>();
        qw.eq(AssessBusinessCommend::getCurrentYear, year);
        qw.eq(AssessBusinessCommend::getDepartmentCode, depart);
        return this.list(qw);
    }

    @Override
    public String getInfo(String year, String depart) {
        LambdaQueryWrapper<AssessBusinessCommend> qw = new LambdaQueryWrapper<>();
        qw.eq(AssessBusinessCommend::getCurrentYear, year);
        qw.eq(AssessBusinessCommend::getDepartmentCode, depart);
        qw.eq(AssessBusinessCommend::getStatus, "0");
        List<AssessBusinessCommend> commend = commendMapper.selectList(qw);
        if (!commend.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (AssessBusinessCommend businessCommend : commend) {
                if (businessCommend.getRemark() != null && !businessCommend.getRemark().isEmpty()) {
                    sb.append(businessCommend.getRemark()).append("\n");
                }
            }
            return sb.toString();
        }
        return "";
    }
}
