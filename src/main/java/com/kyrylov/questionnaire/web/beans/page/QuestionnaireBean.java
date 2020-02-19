package com.kyrylov.questionnaire.web.beans.page;

import com.kyrylov.questionnaire.persistence.dao.DaoManager;
import com.kyrylov.questionnaire.persistence.domain.entities.Document;
import com.kyrylov.questionnaire.persistence.domain.entities.Field;
import com.kyrylov.questionnaire.persistence.domain.entities.Field_;
import com.kyrylov.questionnaire.persistence.domain.entities.Option;
import com.kyrylov.questionnaire.persistence.domain.entities.Response;
import com.kyrylov.questionnaire.persistence.domain.entities.ResponseData;
import com.kyrylov.questionnaire.persistence.domain.entities.User;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Slf4j
@Named
@ViewScoped
public class QuestionnaireBean extends BasePageBean {

    private static final long serialVersionUID = -807505852671021087L;

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Inject
    private SocketBean socketBean;

    private List<Field> fields;

    private Map<Field, Date> dateForCorrespondingFields;

    private Map<Field, String> textForCorrespondingFields;

    private Map<Field, IndexedEntitySelectItem[]> multipleOptionsForCorrespondingFields;

    private Map<Field, IndexedEntitySelectItem> singleOptionsForCorrespondingFields;

    private Map<Field, Document> uploadedFilesForCorrespondingFields;

    private IndexedEntitySelectConverter<Option> converter;

    @PostConstruct
    private void init() {
        this.dateForCorrespondingFields = new HashMap<>();
        this.textForCorrespondingFields = new HashMap<>();
        this.multipleOptionsForCorrespondingFields = new HashMap<>();
        this.singleOptionsForCorrespondingFields = new HashMap<>();
        this.uploadedFilesForCorrespondingFields = new HashMap<>();

        try {
            this.fields = DaoManager.select(Field.class).where().equal(Field_.ACTIVE, Boolean.TRUE).readonly().list();
        } catch (DatabaseException e) {
            log.error(e.getMessage(), e);
            displayErrorMessageWithUserLocale("questionnaireBeanErrorOnPageInit");
        }

        this.fields.stream().filter(f -> f.getType().isMultiOptionsType())
                .forEach(f -> this.multipleOptionsForCorrespondingFields
                        .put(f, new IndexedEntitySelectItem[f.getOptions().size()]));

        Set<Option> allOptionList = fields.stream().map(Field::getOptions).flatMap(Set::stream).collect(Collectors.toSet());
        this.converter = new IndexedEntitySelectConverter<>(Option.class, allOptionList, Option::getText);
    }

    /**
     * Save response to database
     */
    public void confirmResponse() {
        Response response = new Response();
        response.setDate(new Date());

        try {
            if (getUserBean().getUserId() != null) {
                response.setUser(DaoManager.get(User.class, getUserBean().getUserId()));
            }

            DaoManager.beginTransaction();
            DaoManager.save(response);
            for (Field field : getFields()) {
                ResponseData responseData = new ResponseData(field, response);

                switch (field.getType()) {
                    case SINGLE_LINE_TEXT:
                        responseData.setText(getTextForCorrespondingFields().getOrDefault(field, null));
                        break;
                    case MULTILINE_TEXT:
                        responseData.setBigText(getTextForCorrespondingFields().getOrDefault(field, null));
                        break;
                    case CHECKBOX:
                        initResponseDataMultipleSelectedOptionList(responseData);
                        break;
                    case COMBOBOX:
                    case RADIO_BUTTON:
                        initResponseDataSingleSelectedOption(responseData);
                        break;
                    case DATE:
                        responseData.setDate(getDateForCorrespondingFields().getOrDefault(field, null));
                        break;
                    case FILE:
                        initResponseDataDocumentAndSaveFileToServer(responseData);
                        break;
                }
                if (!responseData.isEmpty()) {
                    DaoManager.save(responseData);
                    response.getResponseDataList().add(responseData);
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

        getSocketBean().updateResponseTableByPushMessageInApplicationScope();

        RedirectHelper.sendRedirect(Page.RESPONSE_SUCCESS);
    }

    private void initResponseDataSingleSelectedOption(final ResponseData responseData) {
        Object indexedEntitySelectItem = getSingleOptionsForCorrespondingFields()
                .getOrDefault(responseData.getField(), null);
        if (indexedEntitySelectItem != null) {
            responseData.setSelectedOptions(
                    Collections.singleton((Option) ((IndexedEntitySelectItem) indexedEntitySelectItem).getEntity()));
        }
    }

    private void initResponseDataMultipleSelectedOptionList(final ResponseData responseData) {
        Object[] indexedEntitySelectItems = getMultipleOptionsForCorrespondingFields()
                .getOrDefault(responseData.getField(), null);
        if (indexedEntitySelectItems != null && indexedEntitySelectItems.length != 0) {
            responseData.setSelectedOptions(Arrays.stream((indexedEntitySelectItems))
                    .map(x -> (Option) ((IndexedEntitySelectItem) x).getEntity()).collect(Collectors.toSet()));
        }
    }

    private void initResponseDataDocumentAndSaveFileToServer(final ResponseData responseData) throws IOException {
        Document document = getUploadedFilesForCorrespondingFields().getOrDefault(responseData.getField(), null);
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
        getUploadedFilesForCorrespondingFields().put(field, document);
    }

}
