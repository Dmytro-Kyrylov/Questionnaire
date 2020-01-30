package com.kyrylov.questionnaire.web.beans.page;

import com.kyrylov.questionnaire.persistence.dao.DaoManager;
import com.kyrylov.questionnaire.persistence.domain.entities.Field;
import com.kyrylov.questionnaire.persistence.domain.entities.Option;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import com.kyrylov.questionnaire.util.helpers.ResourceHelper;
import com.kyrylov.questionnaire.util.helpers.creators.xls.XlsFieldFileCreator;
import com.kyrylov.questionnaire.util.helpers.creators.xml.XmlFieldFileCreator;
import com.kyrylov.questionnaire.web.beans.BaseLazyEntityModelBean;
import com.kyrylov.questionnaire.web.beans.additional.SocketBean;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@Slf4j
@Named
@ViewScoped
public class FieldManageBean extends BaseLazyEntityModelBean<Field> {

    private static final long serialVersionUID = 1581953714944406695L;

    @Inject
    private SocketBean socketBean;

    private Field tempField;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private Field fieldForSave;

    private List<Field.FieldType> fieldTypes;

    private boolean displaySectionForOptions;

    private Option optionForFieldManageDialog;

    @PostConstruct
    private void init() {
        try {
            loadList(Field.class, x -> x, x -> x, x -> x);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            displayErrorMessageWithUserLocale("fieldManageBeanErrorOnPageInit");
        }
        this.fieldTypes = Arrays.asList(Field.FieldType.values());
        this.optionForFieldManageDialog = new Option();
    }

    public void prepareFieldManageDialog(Field field) {
        Field fieldForDialog = new Field();
        if (field != null) {
            fieldForDialog.setActive(field.getActive());
            fieldForDialog.setType(field.getType());
            fieldForDialog.setLabel(field.getLabel());
            fieldForDialog.setRequired(field.getRequired());
            fieldForDialog.getOptions().addAll(field.getOptions());

            setFieldForSave(field);
        } else {
            setFieldForSave(fieldForDialog);
        }
        setTempField(fieldForDialog);
        onTypeOfFieldChangeListener();
    }

    public void addOptionToField() {
        if (getTempField() != null && getOptionForFieldManageDialog() != null
                && getOptionForFieldManageDialog().getText() != null
                && !getOptionForFieldManageDialog().getText().isEmpty()) {
            getTempField().getOptions().add(getOptionForFieldManageDialog());
            setOptionForFieldManageDialog(new Option());
        }
    }

    public void onTypeOfFieldChangeListener() {
        if (getTempField() != null && getTempField().getType() != null && getTempField().getType().isOptionsType()) {
            setDisplaySectionForOptions(true);
        } else {
            setDisplaySectionForOptions(false);
        }
    }

    public void saveFieldFromDialog() {
        getFieldForSave().setActive(getTempField().getActive());
        getFieldForSave().setType(getTempField().getType());
        getFieldForSave().setLabel(getTempField().getLabel());
        getFieldForSave().setRequired(getTempField().getRequired());
        getFieldForSave().setOptions(getTempField().getOptions());

        getFieldForSave().getOptions().forEach(o -> o.setField(getFieldForSave()));
        try {
            DaoManager.save(getFieldForSave(), true);
        } catch (DatabaseException e) {
            log.error("Error when saving field entity", e);
            displayErrorMessageWithUserLocale("fieldManageBeanErrorSaveField");
        }

        displaySuccessMessageWithUserLocale("fieldManageBeanSaveFieldSuccess");

        getSocketBean().updateResponseTableByPushMessageInApplicationScope();
    }

    public void deleteField(Field field) {
        if (field != null) {
            try {
                DaoManager.delete(field, true);
            } catch (DatabaseException e) {
                log.error(e.getMessage(), e);
                displayErrorMessageWithUserLocale("fieldManageBeanErrorDeleteField");
                return;
            }
            displaySuccessMessageWithUserLocale("fieldManageBeanDeleteFieldSuccess");

            getSocketBean().updateResponseTableByPushMessageInApplicationScope();
        }
    }

    public StreamedContent downloadXmlRepresentationOfTable() {
        byte[] content;
        try {
            content = new XmlFieldFileCreator(getUserBean().getUserLocale(), DaoManager.select(Field.class).execute())
                    .createFile();
        } catch (Exception e) {
            log.error("Error on generating field xml - " + e.getMessage(), e);
            displayErrorMessageWithUserLocale("fieldManageBeanErrorGeneratingXML");
            return null;
        }
        return new DefaultStreamedContent(new ByteArrayInputStream(content), "xml",
                ResourceHelper.getFileCreatingResource("fileXmlFieldFileName",
                        getUserBean().getUserLocale()) + ".xml");
    }

    public StreamedContent downloadXlsRepresentationOfTable() {
        byte[] content;
        try {
            content = new XlsFieldFileCreator(getUserBean().getUserLocale(), DaoManager.select(Field.class).execute())
                    .createXls();
        } catch (Exception e) {
            log.error("Error on generating field xls - " + e.getMessage(), e);
            displayErrorMessageWithUserLocale("fieldManageBeanErrorGeneratingXLS");
            return null;
        }
        return new DefaultStreamedContent(new ByteArrayInputStream(content), "xls",
                ResourceHelper.getFileCreatingResource("fileXlsFieldFileName",
                        getUserBean().getUserLocale()) + ".xls");
    }

}
