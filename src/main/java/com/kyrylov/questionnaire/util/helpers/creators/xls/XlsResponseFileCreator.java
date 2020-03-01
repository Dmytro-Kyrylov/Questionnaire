package com.kyrylov.questionnaire.util.helpers.creators.xls;

import com.kyrylov.questionnaire.persistence.domain.entities.Field;
import com.kyrylov.questionnaire.persistence.domain.entities.Option;
import com.kyrylov.questionnaire.persistence.domain.entities.Response;
import com.kyrylov.questionnaire.persistence.domain.entities.ResponseData;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter(AccessLevel.PRIVATE)
public class XlsResponseFileCreator extends XlsFileCreator {

    private final String[] responsesColumns = new String[]{
            resource("fileXlsResponseUser"),
            resource("fileXlsResponseCreateDate")
    };

    private Collection<Response> responses;
    private Collection<Field> fields;

    public XlsResponseFileCreator(Locale locale, Collection<Response> responses, Collection<Field> fields) {
        super(locale);
        this.responses = responses;
        this.fields = fields;
    }

    @Override
    byte[] createFile(Workbook workbook) throws Exception {
        Sheet sheet = workbook.createSheet(resource("fileXlsResponseHeader"));

        CellStyle headerCellStyle = createStandardHeaderCellStyle(workbook);

        Row headerRow = sheet.createRow(0);

        List<String> columns = new ArrayList<>(Arrays.asList(getResponsesColumns()));
        columns.addAll(getFields().stream().map(Field::getLabel).collect(Collectors.toList()));

        createStandardHeader(headerCellStyle, headerRow, columns.toArray(new String[]{}));

        int rowNum = 1;

        for (Response response : getResponses()) {
            int cellNum = 0;

            Row row = sheet.createRow(rowNum++);
            int nextEmptyRow = rowNum;

            row.createCell(cellNum++).setCellValue(response.getUser() != null ? response.getUser().getEmail() : "");
            row.createCell(cellNum++).setCellValue(response.getDate().toString());

            List<ResponseData> responseDataList = response.getResponseDataList();
            for (Field field : getFields()) {
                Optional<ResponseData> responseData = responseDataList.stream()
                        .filter(rd -> rd.getField().equals(field)).findAny();
                if (field.getType().isMultiOptionsType()
                        && responseData.filter(x->!x.getSelectedOptions().isEmpty()).isPresent()) {
                    int lastOptionRow = addOptionColumnAndGetLastRow(sheet, rowNum, cellNum, row,
                            responseData.map(ResponseData::getSelectedOptions).get());

                    if (nextEmptyRow < lastOptionRow + 1) {
                        nextEmptyRow = lastOptionRow + 1;
                    }
                    cellNum++;
                }else{
                    row.createCell(cellNum++).setCellValue(responseData
                            .flatMap(ResponseData::getDataAccordingTypeAsString)
                            .orElse(resource("fileXlsResponseNoAnswer")));
                }
            }
            rowNum = nextEmptyRow;
            addSeparatorBorderToRow(workbook, sheet, new CellRangeAddress(
                    rowNum - 1, rowNum - 1, 0, cellNum - 1));
        }

        sheet.createFreezePane(0, 1);
        autosizeAllColumns(sheet, columns.size());

        return getBytes(workbook);
    }

    private int addOptionColumnAndGetLastRow(Sheet sheet, int rowNum, int cellNum, Row row, Set<Option> selectedOptions) {
        Row rowForOption = row;
        int rowNumForOption = rowNum;
        boolean firstRow = true;
        for (Option option : selectedOptions) {
            if (!firstRow) {
                rowForOption = sheet.getRow(rowNumForOption);
                if (rowForOption == null) {
                    rowForOption = sheet.createRow(rowNumForOption++);
                } else {
                    rowNumForOption++;
                }
            }
            rowForOption.createCell(cellNum).setCellValue(option.getText());
            firstRow = false;
        }
        return rowNumForOption - 1;
    }
}
