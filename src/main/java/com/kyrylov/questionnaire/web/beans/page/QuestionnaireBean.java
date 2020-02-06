package com.kyrylov.questionnaire.web.beans.page;

import com.kyrylov.questionnaire.persistence.dao.DaoManager;
import com.kyrylov.questionnaire.persistence.domain.entities.*;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import com.kyrylov.questionnaire.util.helpers.FileHelper;
import com.kyrylov.questionnaire.web.beans.BasePageBean;
import com.kyrylov.questionnaire.web.beans.additional.SocketBean;
import com.kyrylov.questionnaire.web.util.Page;
import com.kyrylov.questionnaire.web.util.converters.IndexedEntitySelectConverter;
import com.kyrylov.questionnaire.web.util.helpers.RedirectHelper;
import com.kyrylov.questionnaire.web.util.models.IndexedEntitySelectItem;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.event.FileUploadEvent;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@Slf4j
@Named
@ViewScoped
public class QuestionnaireBean extends BasePageBean {

    private static final long serialVersionUID = -807505852671021087L;

    @Inject
    private SocketBean socketBean;

    private List<Field> fields;

    /**
     * Stores all entered on page values by field, except documents and options
     */
    private Map<Field, ResponseData> fieldValues;

    /**
     * Stores all selected options in appropriate fields that allow to choose multiple options
     */
    private Map<Field, IndexedEntitySelectItem[]> multipleOptionsForResponseData;

    /**
     * Stores all selected options in appropriate fields that allow to choose only one option
     */
    private Map<Field, IndexedEntitySelectItem> singleOptionsForResponseData;

    private IndexedEntitySelectConverter<Option> converter;

    /**
     * Stores all uploaded files
     */
    private Map<Field, Document> fieldUploadedFiles;

    @PostConstruct
    private void init() {
        this.fieldValues = new HashMap<>();
        this.multipleOptionsForResponseData = new HashMap<>();
        this.singleOptionsForResponseData = new HashMap<>();
        this.fieldUploadedFiles = new HashMap<>();
        try {
            this.fields = DaoManager.select(Field.class).where().equal(Field_.ACTIVE, Boolean.TRUE).execute();
        } catch (DatabaseException e) {
            log.error(e.getMessage(), e);
            displayErrorMessageWithUserLocale("questionnaireBeanErrorOnPageInit");
        }

        for (Field field : this.fields) {
            ResponseData responseData = new ResponseData();
            this.fieldValues.put(field, responseData);
            if (field.getType().isMultiOptionsType()) {
                this.multipleOptionsForResponseData.put(field, new IndexedEntitySelectItem[field.getOptions().size()]);
            } else if (field.getType().isSingleOptionType()) {
                this.singleOptionsForResponseData.put(field, null);
            }
        }
        List<Option> allOptionList = fields.stream().map(Field::getOptions).flatMap(List::stream)
                .distinct().collect(Collectors.toList());
        this.converter = new IndexedEntitySelectConverter<>(Option.class, allOptionList, Option::getText);
    }

    /**
     * Save response to database
     */
    public void confirmResponse() {
        Response response = new Response();
        response.setDate(new Date());

        try {
            User user = DaoManager.select(User.class).where()
                    .equal(User_.EMAIL, getUserBean().getUser().getEmail()).execute().get(0);
            response.setUser(user);

            DaoManager.beginTransaction();
            DaoManager.save(response);
            for (Map.Entry<Field, ResponseData> fieldResponseDataEntry : getFieldValues().entrySet()) {
                ResponseData responseData = fieldResponseDataEntry.getValue();
                Field field = fieldResponseDataEntry.getKey();

                responseData.initResponseData(field, response);

                initResponseDataMultipleSelectedOptionList(responseData);
                initResponseDataSingleSelectedOption(responseData);
                initResponseDataDocument(responseData);

                if (!responseData.isEmpty()) {
                    DaoManager.save(responseData);
                }
            }
            DaoManager.commitTransaction();
        } catch (IOException e) {
            DaoManager.rollbackTransaction();
            log.error("Cannot save file", e);
            displayErrorMessageWithUserLocale("questionnaireBeanErrorOnSavingFiles");
            return;
        } catch (Exception e) {
            DaoManager.rollbackTransaction();
            log.error("Error on saving response", e);
            displayErrorMessageWithUserLocale("questionnaireBeanErrorOnSavingResponse");
            return;
        }

        response.setResponseDataList(getFieldValues().values().stream().filter(rd -> !rd.isEmpty()).collect(Collectors.toList()));

        getSocketBean().updateResponseTableByPushMessageInApplicationScope();

        RedirectHelper.sendRedirect(Page.RESPONSE_SUCCESS);
    }

    private void initResponseDataSingleSelectedOption(final ResponseData responseData) {
        Object indexedEntitySelectItem = getSingleOptionsForResponseData()
                .getOrDefault(responseData.getField(), null);
        if (indexedEntitySelectItem != null) {
            responseData.setSelectedOptions(
                    Collections.singletonList((Option) ((IndexedEntitySelectItem) indexedEntitySelectItem).getEntity()));
        }
    }

    private void initResponseDataMultipleSelectedOptionList(final ResponseData responseData) {
        Object[] indexedEntitySelectItems = getMultipleOptionsForResponseData()
                .getOrDefault(responseData.getField(), null);
        if (indexedEntitySelectItems != null && indexedEntitySelectItems.length != 0) {
            responseData.setSelectedOptions(Arrays.stream((indexedEntitySelectItems))
                    .map(x -> (Option) ((IndexedEntitySelectItem) x).getEntity()).collect(Collectors.toList()));
        }
    }

    private void initResponseDataDocument(final ResponseData responseData) throws IOException {
        Document document = getFieldUploadedFiles().getOrDefault(responseData.getField(), null);
        if (document != null) {
            String path = FileHelper.saveFileOnServerAndGetPath(responseData.getField().getId() + "-" + document.getFileName(),
                    document.getContent(), String.valueOf(responseData.getResponse().getId()));
            document.setFilePath(path);
            responseData.setDocument(document);
        }
    }

    public List<IndexedEntitySelectItem<Option>> getOptionList(final Field field) {
        return getConverter().getEntities().stream().filter(ie -> field.getOptions().stream()
                .anyMatch(o -> o.equals(ie.getEntity()))).collect(Collectors.toList());
    }

    public void fileUploadListener(final FileUploadEvent event) {
        Field field = (Field) event.getComponent().getAttributes().get("field");
        Document document = new Document();
        document.setContent(event.getFile().getContents());
        document.setFileName(event.getFile().getFileName());
        getFieldUploadedFiles().put(field, document);
    }

}
