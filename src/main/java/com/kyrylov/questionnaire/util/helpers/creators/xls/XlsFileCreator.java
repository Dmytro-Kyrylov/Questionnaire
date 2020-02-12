package com.kyrylov.questionnaire.util.helpers.creators.xls;

import com.kyrylov.questionnaire.util.helpers.creators.BaseFileCreator;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

@Getter(AccessLevel.PRIVATE)
abstract class XlsFileCreator extends BaseFileCreator {

    protected XlsFileCreator(Locale locale) {
        super(locale);
    }

    public byte[] createXlsx() throws Exception {
        return createFile(new XSSFWorkbook());
    }

    public byte[] createXls() throws Exception {
        return createFile(new HSSFWorkbook());
    }

    abstract byte[] createFile(Workbook workbook) throws Exception;

    protected void addSeparatorBorderToRow(Workbook workbook, Sheet sheet, CellRangeAddress region) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);

        RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
    }

    protected void autosizeAllColumns(Sheet sheet, int countOfColumns) {
        for (int i = 0; i < countOfColumns; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    protected void createStandardHeader(CellStyle headerCellStyle, Row headerRow, String[] columns) {
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }
    }

    protected CellStyle createStandardHeaderCellStyle(Workbook workbook) {
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 18);
        headerFont.setColor(IndexedColors.GREEN.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setBorderBottom(BorderStyle.MEDIUM);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

        return headerCellStyle;
    }

    protected CellStyle createStandardCellStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 14);

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(font);

        return cellStyle;
    }

    protected void applyStyleToAllCellsInRange(CellStyle cellStyle, Sheet sheet, CellRangeAddress region) {
        for (int row = region.getFirstRow(); row < region.getLastRow(); row++) {
            Row sheetRow = sheet.getRow(row);
            for (int col = region.getFirstColumn(); col < region.getLastColumn(); col++) {
                Cell cell = sheetRow.getCell(col);
                if (cell != null) {
                    cell.setCellStyle(cellStyle);
                }
            }
        }
    }

    protected byte[] getBytes(Workbook workbook) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        workbook.write(bos);
        workbook.close();

        byte[] bytes = bos.toByteArray();
        bos.close();
        return bytes;
    }
}
