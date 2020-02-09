package com.kyrylov.questionnaire.web.beans.page;

import com.kyrylov.questionnaire.persistence.dao.DaoManager;
import com.kyrylov.questionnaire.persistence.domain.entities.Field;
import com.kyrylov.questionnaire.persistence.domain.entities.Option;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import com.kyrylov.questionnaire.web.beans.BasePageBean;
import com.kyrylov.questionnaire.web.util.helpers.DialogHelper;
import com.kyrylov.questionnaire.web.util.helpers.RedirectHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.Arrays;
import java.util.List;

@Setter
@Getter
@Slf4j
@Named
@ViewScoped
public class FieldBean extends BasePageBean {

    private static final long serialVersionUID = 1123749836450827957L;

    private Field field;

    private String fieldStatus;
    private List<Field.FieldType> fieldTypes;
    private boolean displaySectionForOptions;
    private String optionText;

    @PostConstruct
    private void init() {
        String fieldId = getHttpServletRequest().getParameter(RedirectHelper.Parameter.ID_OF_ENTITY.getParameter());
        if (fieldId != null) {
            try {
                this.field = DaoManager.get(Field.class, Long.parseLong(fieldId));
            } catch (DatabaseException e) {
                //todo
                log.error("Error when trying to get field entity", e);
                displayErrorMessageWithUserLocale("fieldManageBeanErrorSaveField");
                ajaxUpdate("messages");
            }
            this.fieldStatus = "Edit";
        } else {
            this.field = new Field();
            this.fieldStatus = "Add";
        }
        this.fieldTypes = Arrays.asList(Field.FieldType.values());
    }

    public void addOptionToField() {
        if (getOptionText() != null && !getOptionText().isEmpty()) {
            Option option = new Option();
            option.setText(getOptionText());
            getField().getOptions().add(new Option());
            setOptionText(null);
        }
    }

    public void onTypeOfFieldChangeListener() {
        if (getField().getType() != null && getField().getType().isOptionsType()) {
            setDisplaySectionForOptions(true);
        } else {
            setDisplaySectionForOptions(false);
        }
    }

    public void saveFieldFromDialog() {
        try {
            getField().getOptions().forEach(o -> o.setField(getField()));
            DaoManager.save(getField(), true);
        } catch (DatabaseException e) {
            log.error("Error when saving field entity", e);
            closeDialog(DialogHelper.DialogResult.ERROR);
            return;
        }
        closeDialog(DialogHelper.DialogResult.SUCCESS);
    }

    public void closeDialog(DialogHelper.DialogResult dialogResult) {
        PrimeFaces.current().dialog().closeDynamic(dialogResult);
    }

}
