package com.kyrylov.questionnaire.web.beans.page;

import com.kyrylov.questionnaire.persistence.dao.DaoManager;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import com.kyrylov.questionnaire.web.beans.BasePageBean;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Getter
@Setter
@Slf4j
@Named
@RequestScoped
public class ChangePasswordBean extends BasePageBean {

    private static final long serialVersionUID = -6199378736319529747L;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private PasswordEncoder passwordEncoder;

    private String newPassword;

    private String currentPasswordConfirm;

    @PostConstruct
    private void init() {
        WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
        if (context != null) {
            setPasswordEncoder(context.getBean(PasswordEncoder.class));
        }
    }

    public void changePassword() {
        String newPasswordEncoded = getNewPassword();
        if (getPasswordEncoder() != null) {
            newPasswordEncoded = getPasswordEncoder().encode(getNewPassword());
        }
        String oldPassword = getUserBean().getUser().getPassword();

        if (!getPasswordEncoder().matches(getCurrentPasswordConfirm(), oldPassword)) {
            displayErrorMessageWithUserLocale("changePasswordBeanIncorrectCurrentPassword");
            return;
        }

        if (newPasswordEncoded.equals(oldPassword)) {
            displayErrorMessageWithUserLocale("changePasswordBeanPasswordsMatchWithPreviousError");
            return;
        }

        getUserBean().getUser().setPassword(newPasswordEncoded);
        try {
            DaoManager.save(getUserBean().getUser(), true);
        } catch (DatabaseException e) {
            log.error("Error when trying to edit user password", e);

            getUserBean().getUser().setPassword(oldPassword);

            displayErrorMessageWithUserLocale("changePasswordBeanPasswordSaveError");
            return;
        }

        setNewPassword(null);
        getUserBean().updateUserAuthorities();
        displaySuccessMessageWithUserLocale("changePasswordBeanSaveSuccessfully");
    }

}
