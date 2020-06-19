package com.kyrylov.questionnaire.web.beans.page;

import com.kyrylov.questionnaire.persistence.dao.DaoManager;
import com.kyrylov.questionnaire.persistence.domain.entities.Field;
import com.kyrylov.questionnaire.persistence.domain.entities.Field_;
import com.kyrylov.questionnaire.persistence.domain.entities.Option;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import com.kyrylov.questionnaire.util.helpers.ResourceHelper;
import com.kyrylov.questionnaire.web.beans.BasePageBean;
import com.kyrylov.questionnaire.web.util.helpers.DialogHelper;
import com.kyrylov.questionnaire.web.util.helpers.RedirectHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
                Optional<Field> optionalField = DaoManager.select(Field.class)
                        .leftJoin(Field_.OPTIONS, "o", true)
                        .where().equal(Field_.ID, Long.parseLong(fieldId)).singleResult();

                if (optionalField.isPresent()) {
                    this.field = optionalField.get();
                } else {
                    displayErrorMessageWithUserLocale("fieldBeanErrorFieldWithCurrentIdIsNotExist");
                    ajaxUpdate("messages");
                    return;
                }
            } catch (DatabaseException e) {
                log.error("Error when trying to get field entity", e);
                displayErrorMessageWithUserLocale("fieldBeanErrorOnPageInit");
                ajaxUpdate("messages");
                return;
            }
            this.fieldStatus = ResourceHelper.getMessageResource("fieldBeanDialogHeaderEdit",
                    getUserBean().getUserLocale()) + "(" + fieldId + ")";
            this.displaySectionForOptions = getField().getType().isOptionsType();
        } else {
            this.field = new Field();
            this.fieldStatus = ResourceHelper.getMessageResource("fieldBeanDialogHeaderAdd",
                    getUserBean().getUserLocale());
        }
        this.fieldTypes = Arrays.asList(Field.FieldType.values());
    }

    public void addOptionToField() {
        if (getOptionText() != null && !getOptionText().isEmpty()) {
            Option option = new Option();
            option.setText(getOptionText());
            getField().getOptions().add(option);
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
