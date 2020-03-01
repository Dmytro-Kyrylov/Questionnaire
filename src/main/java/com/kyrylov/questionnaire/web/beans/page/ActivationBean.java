package com.kyrylov.questionnaire.web.beans.page;

import com.kyrylov.questionnaire.persistence.domain.entities.User;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import com.kyrylov.questionnaire.util.helpers.UserActivationHelper;
import com.kyrylov.questionnaire.web.beans.BasePageBean;
import com.kyrylov.questionnaire.web.security.SecurityHelper;
import com.kyrylov.questionnaire.web.util.helpers.RedirectHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.mail.MessagingException;
import java.io.IOException;

@Getter
@Setter
@Named
@Slf4j
@RequestScoped
public class ActivationBean extends BasePageBean {

    private static final long serialVersionUID = 3866658217772782351L;

    public String activateAccount() {
        String key = getRequestParameter(RedirectHelper.Parameter.ACTIVATION_KEY_PARAMETER);
        if (key != null && !key.isEmpty()) {
            try {
                User activatedAccountUser = UserActivationHelper.activateAccount(key);
                if (activatedAccountUser != null) {
                    if (getUserBean().getUser() != null
                            && getUserBean().getUser().getActivationKey().equals(key)) {
                        getUserBean().setUser(SecurityHelper.updateUserDetailsAndGetDtoOfUser(activatedAccountUser));
                    }
                    return getMessageResourceWithUserLocale("activationBeanActivationSuccess");
                }
            } catch (DatabaseException e) {
                log.error("Error when activating", e);
                return getMessageResourceWithUserLocale("activationBeanActivationError");
            }
        }
        return getMessageResourceWithUserLocale("activationBeanIncorrectActivationKey");
    }

    /**
     * Send activation link to user`s mail.
     */
    public void sendNewActivationEmail() {
        try {
            String activationUrl = UserActivationHelper.createActivationUrl(getUserBean().getUser().getActivationKey());
            UserActivationHelper.sendActivationEmail(activationUrl,
                    getUserBean().getUser().getEmail(), getUserBean().getUserLocale());
        } catch (IOException | MessagingException e) {
            log.error(e.getMessage(), e);
            displayErrorMessageWithUserLocale("sendActivationEmailError");
            return;
        }
        displaySuccessMessageWithUserLocale("sendActivationEmailSuccess");
    }

}
