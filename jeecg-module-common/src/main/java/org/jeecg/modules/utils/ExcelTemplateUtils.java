package org.jeecg.modules.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ExcelTemplateUtils {

    // 读取模板文件
    public static Workbook readTemplate(String templatePath) throws IOException {
        FileSystemResource resource = new FileSystemResource(templatePath);
        try (InputStream inputStream = resource.getInputStream()) {
            return new XSSFWorkbook(inputStream);
        }
    }

    // 替换文本占位符
    public static void replaceText(Sheet sheet, Map<String, String> textMap) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING) {
                    String cellValue = cell.getStringCellValue();
                    for (Map.Entry<String, String> entry : textMap.entrySet()) {
                        if (cellValue.contains(entry.getKey())) {
                            cell.setCellValue(cellValue.replace(entry.getKey(), entry.getValue()));
                        }
                    }
                }
            }
        }
    }

    // 插入图片到占位符位置
    public static void insertImage(Workbook workbook, Sheet sheet, Map<String, byte[]> imageMap) throws IOException {
        for (Map.Entry<String, byte[]> entry : imageMap.entrySet()) {
            String placeholder = entry.getKey();
            byte[] imageBytes = entry.getValue();

            // 查找占位符所在单元格
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (cell.getCellType() == CellType.STRING &&
                            cell.getStringCellValue().equals(placeholder)) {
                        int rowIdx = cell.getRowIndex();
                        int colIdx = cell.getColumnIndex();

                        // 插入图片
                        int pictureIdx = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);
                        Drawing<?> drawing = sheet.createDrawingPatriarch();
                        ClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
                        anchor.setCol1(colIdx);
                        anchor.setRow1(rowIdx);
                        Picture pict = drawing.createPicture(anchor, pictureIdx);
                        pict.resize();

                        // 删除占位符文本
                        cell.setCellValue("");
                    }
                }
            }
        }
    }
}