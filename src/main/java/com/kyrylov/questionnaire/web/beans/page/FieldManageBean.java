package com.kyrylov.questionnaire.web.beans.page;

import com.kyrylov.questionnaire.persistence.dao.DaoManager;
import com.kyrylov.questionnaire.persistence.domain.entities.Field;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import com.kyrylov.questionnaire.util.helpers.ResourceHelper;
import com.kyrylov.questionnaire.util.helpers.creators.xls.XlsFieldFileCreator;
import com.kyrylov.questionnaire.util.helpers.creators.xml.XmlFieldFileCreator;
import com.kyrylov.questionnaire.web.beans.BaseLazyEntityModelBean;
import com.kyrylov.questionnaire.web.beans.additional.SocketBean;
import com.kyrylov.questionnaire.web.util.helpers.DialogHelper;
import com.kyrylov.questionnaire.web.util.helpers.RedirectHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Slf4j
@Named
@ViewScoped
public class FieldManageBean extends BaseLazyEntityModelBean<Field> {

    private static final long serialVersionUID = 1581953714944406695L;

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Inject
    private SocketBean socketBean;

    @PostConstruct
    private void init() {
        try {
            loadList(Field.class, x -> x, x -> x, x -> x);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            displayErrorMessageWithUserLocale("fieldManageBeanErrorOnPageInit");
        }
    }

    public void openFieldDialogIfIdParameterExist() {
        String fieldId = getHttpServletRequest().getParameter(RedirectHelper.Parameter.ID_OF_ENTITY.getParameter());
        if (fieldId != null && !fieldId.isEmpty()) {
            openFieldDialog(fieldId);
        }
    }

    public void openFieldDialog(String fieldId) {
        Map<String, List<String>> params = null;
        if (fieldId != null && !fieldId.isEmpty()) {
            params = new HashMap<>();
            params.put(RedirectHelper.Parameter.ID_OF_ENTITY.getParameter(),
                    Collections.singletonList(fieldId));
        }
        PrimeFaces.current().dialog().openDynamic("Dialog/field",
                DialogHelper.getDialogOptions(true, false, false, 700, 430),
                params);
    }

    public void handleFieldDialogReturn(SelectEvent event) {
        switch ((DialogHelper.DialogResult) event.getObject()) {
            case SUCCESS:
                getSocketBean().updateResponseTableByPushMessageInApplicationScope();
                displaySuccessMessageWithUserLocale("fieldManageBeanSaveFieldSuccess");
                break;
            case ERROR:
                displayErrorMessageWithUserLocale("fieldManageBeanErrorSaveField");
                break;
        }
        ajaxUpdate("messages", "fieldsTable");
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
            content = new XmlFieldFileCreator(getUserBean().getUserLocale(), DaoManager.select(Field.class).readonly().list())
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
            content = new XlsFieldFileCreator(getUserBean().getUserLocale(), DaoManager.select(Field.class).readonly().list())
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
