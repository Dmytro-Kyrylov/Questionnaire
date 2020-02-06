package com.kyrylov.questionnaire.web.beans.page;

import com.kyrylov.questionnaire.persistence.domain.entities.User;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import com.kyrylov.questionnaire.web.beans.BasePageBean;
import com.kyrylov.questionnaire.web.security.SecurityHelper;
import com.kyrylov.questionnaire.web.util.helpers.RedirectHelper;
import com.kyrylov.questionnaire.web.util.helpers.UserActivationHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Getter
@Setter
@Named
@Slf4j
@RequestScoped
public class ActivationBean extends BasePageBean {

    private static final long serialVersionUID = 3866658217772782351L;

    private String activationMessage;

    @PostConstruct
    private void init() {
        this.activationMessage = activateAccount();
    }

    private String activateAccount() {
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

}
