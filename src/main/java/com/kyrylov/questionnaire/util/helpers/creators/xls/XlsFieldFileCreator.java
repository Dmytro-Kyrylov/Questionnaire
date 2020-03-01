package com.kyrylov.questionnaire.util.helpers.creators.xls;

import com.kyrylov.questionnaire.persistence.domain.entities.Field;
import com.kyrylov.questionnaire.persistence.domain.entities.Option;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.Collection;
import java.util.Locale;

@Getter(AccessLevel.PRIVATE)
public class XlsFieldFileCreator extends XlsFileCreator {

    private final String[] fieldColumns = new String[]{
            resource("fileXlsFieldLabel"),
            resource("fileXlsFieldType"),
            resource("fileXlsFieldIsActive"),
            resource("fileXlsFieldIsRequired"),
            resource("fileXlsFieldOptions")
    };

    private Collection<Field> fields;

    public XlsFieldFileCreator(Locale locale, Collection<Field> fields) {
        super(locale);
        this.fields = fields;
    }

    @Override
    byte[] createFile(Workbook workbook) throws Exception {
        Sheet sheet = workbook.createSheet(resource("fileXlsFieldHeader"));

        CellStyle headerCellStyle = createStandardHeaderCellStyle(workbook);

        Row headerRow = sheet.createRow(0);

        createStandardHeader(headerCellStyle, headerRow, getFieldColumns());

        int rowNum = 1;

        for (Field field : getFields()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(field.getLabel());
            row.createCell(1).setCellValue(field.getType().name());
            row.createCell(2).setCellValue(String.valueOf(field.getActive()));
            row.createCell(3).setCellValue(String.valueOf(field.getRequired()));
            if (field.getType().isOptionsType()) {
                boolean firstRow = true;
                for (Option option : field.getOptions()) {
                    if (!firstRow) {
                        row = sheet.createRow(rowNum++);
                    }
                    row.createCell(4).setCellValue(option.getText());
                    firstRow = false;
                }
            } else {
                row.createCell(4).setCellValue(resource("fileXlsFieldNoOptions"));
            }
            addSeparatorBorderToRow(workbook, sheet,
                    new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));
        }

        sheet.createFreezePane(0, 1);
        autosizeAllColumns(sheet, getFieldColumns().length);

        return getBytes(workbook);
    }

}
