package com.kyrylov.questionnaire.web.beans.page;

import com.kyrylov.questionnaire.persistence.dao.DaoManager;
import com.kyrylov.questionnaire.persistence.domain.entities.Document;
import com.kyrylov.questionnaire.persistence.domain.entities.Field;
import com.kyrylov.questionnaire.persistence.domain.entities.Response;
import com.kyrylov.questionnaire.persistence.domain.entities.ResponseData;
import com.kyrylov.questionnaire.persistence.domain.entities.Response_;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import com.kyrylov.questionnaire.util.helpers.FileHelper;
import com.kyrylov.questionnaire.util.helpers.ResourceHelper;
import com.kyrylov.questionnaire.util.helpers.creators.xls.XlsResponseFileCreator;
import com.kyrylov.questionnaire.util.helpers.creators.xml.XmlResponseFileCreator;
import com.kyrylov.questionnaire.web.beans.BaseLazyEntityModelBean;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Getter
@Setter
@Slf4j
@Named
@ViewScoped
public class ResponseManageBean extends BaseLazyEntityModelBean<Response> {

    private static final long serialVersionUID = -9037950686810979854L;

    private List<Field> fields;

    @PostConstruct
    private void init() {
        updateResponseTable();
    }

    public void updateResponseTable() {
        try {
            loadList(Response.class, x -> x.leftJoin(Response_.USER, "u"), x -> x, x -> x);
            this.fields = DaoManager.select(Field.class).readonly().list();
        } catch (DatabaseException e) {
            log.error(e.getMessage(), e);
            displayErrorMessageWithUserLocale("responseManageBeanErrorOnPageInit");
        }
    }

    public String getResponseValueForField(Response response, Field field) {
        return response.getResponseDataList().stream().filter(rd -> rd.getField().equals(field)).findFirst()
                .flatMap(ResponseData::getDataAccordingTypeAsString)
                .orElse(getMessageResourceWithUserLocale("responseManageBeanEmptyDataForField"));
    }

    /**
     * Allow to download user`s file that his uploaded at questionnaire page
     *
     * @param response current response
     * @param field    FILE type field
     * @return file that contained in response by current field
     */
    public StreamedContent downloadDocument(Response response, Field field) {
        ResponseData responseData = response.getResponseDataList().stream()
                .filter(rd -> rd.getField().equals(field)).findFirst().orElse(null);
        if (responseData != null && responseData.getDocument() != null) {
            Document document = responseData.getDocument();
            try {
                InputStream stream = new FileInputStream(document.getFilePath());
                return new DefaultStreamedContent(stream, FileHelper.getFileExtension(document.getFilePath()),
                        document.getFileName());
            } catch (IOException e) {
                log.error("Error when trying to download document", e);
                log.debug("File path - {} and document id - {}", document.getFilePath(), document.getId());
            }
        }
        return null;
    }

    /**
     * Checks if this field is of type FILE and if a file exists for this field in this response
     *
     * @param response current response
     * @param field    field to check
     * @return flag that indicate if this field data contains file
     */
    public boolean isItFileFieldAndFileExist(Response response, Field field) {
        if (!field.getType().equals(Field.FieldType.FILE)) {
            return false;
        }
        ResponseData responseData = response.getResponseDataList().stream()
                .filter(rd -> rd.getField().equals(field)).findFirst().orElse(null);
        return responseData != null && responseData.getDocument() != null
                && responseData.getDocument().getFilePath() != null && !responseData.getDocument().getFilePath().isEmpty();
    }

    public void deleteResponse(Response response) {
        if (response != null) {
            try {
                DaoManager.delete(response, true);
            } catch (DatabaseException e) {
                log.error(e.getMessage(), e);
                displayErrorMessageWithUserLocale("responseManageBeanDeleteResponseError");
            }
            displaySuccessMessageWithUserLocale("responseManageBeanDeleteResponseSuccess");
        }
    }

    public StreamedContent downloadXlsRepresentationOfTable() {
        byte[] content;
        try {
            content = new XlsResponseFileCreator(getUserBean().getUserLocale(),
                    DaoManager.select(Response.class).readonly().list(), getFields()).createXls();
        } catch (Exception e) {
            log.error("Error on generating response xls - " + e.getMessage(), e);
            displayErrorMessageWithUserLocale("responseManageBeanErrorGeneratingXLS");
            return null;
        }
        return new DefaultStreamedContent(new ByteArrayInputStream(content), "xls",
                ResourceHelper.getFileCreatingResource("fileXlsResponseFileName",
                        getUserBean().getUserLocale()) + ".xls");
    }

    public StreamedContent downloadXmlRepresentationOfTable() {
        byte[] content;
        try {
            content = new XmlResponseFileCreator(getUserBean().getUserLocale(),
                    DaoManager.select(Response.class).readonly().list()).createFile();
        } catch (Exception e) {
            log.error("Error on generating response xml - " + e.getMessage(), e);
            displayErrorMessageWithUserLocale("responseManageBeanErrorGeneratingXML");
            return null;
        }
        return new DefaultStreamedContent(new ByteArrayInputStream(content), "xml",
                ResourceHelper.getFileCreatingResource("fileXmlResponseFileName",
                        getUserBean().getUserLocale()) + ".xml");
    }

}
