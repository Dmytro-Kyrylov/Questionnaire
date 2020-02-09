package com.kyrylov.questionnaire.web.beans.page;

import com.kyrylov.questionnaire.persistence.dao.DaoManager;
import com.kyrylov.questionnaire.persistence.domain.entities.User;
import com.kyrylov.questionnaire.persistence.domain.entities.UserRole;
import com.kyrylov.questionnaire.persistence.domain.entities.User_;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import com.kyrylov.questionnaire.util.dto.UserDto;
import com.kyrylov.questionnaire.util.helpers.EmailHelper;
import com.kyrylov.questionnaire.util.helpers.ResourceHelper;
import com.kyrylov.questionnaire.util.helpers.UserActivationHelper;
import com.kyrylov.questionnaire.util.helpers.entities.UserHelper;
import com.kyrylov.questionnaire.web.beans.BasePageBean;
import com.kyrylov.questionnaire.web.security.SecurityHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.mail.MessagingException;
import java.io.IOException;

@Getter
@Setter
@Slf4j
@Named
@RequestScoped
public class AccountEditBean extends BasePageBean {

    private static final long serialVersionUID = -791468635376297771L;

    private UserDto pageUser;

    @PostConstruct
    private void init() {
        setPageUser(getUserBean().getUser().clone());
    }

    /**
     * Check if new entered user`s email exist in database, save changed data and send to new and old emails appropriate messages
     */
    public void editAccount() {
        if (!getPageUser().getEmail().equals(getUserBean().getUser().getEmail())) {
            try {
                if (UserHelper.isUserEmailAlreadyExistInDB(getPageUser().getEmail())) {
                    displayErrorMessageWithUserLocale("accountEditBeanEmailIsAlreadyUsedError");
                    return;
                }
            } catch (DatabaseException e) {
                log.error("Error when trying to get users from DB with new user`s email", e);
                displayErrorMessageWithUserLocale("accountEditBeanEditUserDataSaveError");
                return;
            }
        }

        try {
            User user = DaoManager.get(User.class, getUserBean().getUserId());
            DaoManager.beginTransaction();

            if (!getPageUser().getEmail().equals(user.getEmail())) {
                user.setActivationKey(UserActivationHelper.generateActivationKey(user.getEmail()));
                user.setActive(false);
                user.getRoles().removeIf(r -> r.getRole().equals(UserRole.RoleEnum.ROLE_ADMIN));
            }

            user.setPhone(getPageUser().getPhone());
            user.setEmail(getPageUser().getEmail());
            user.setFirstName(getPageUser().getFirstName());
            user.setLastName(getPageUser().getLastName());

            DaoManager.save(user);

            if (!getUserBean().getUser().getEmail().equals(user.getEmail())) {
                sendEmailToUsers(user, getUserBean().getUser().getEmail());
            }

            DaoManager.commitTransaction();

            if (!getUserBean().getUser().getEmail().equals(user.getEmail())) {
                getUserBean().setUser(SecurityHelper.updateUserDetailsAndGetDtoOfUser(user));
            } else {
                getUserBean().setUser(getPageUser());
            }
        } catch (DatabaseException e) {
            DaoManager.rollbackTransaction();
            log.error("Error on user save to DB", e);
            displayErrorMessageWithUserLocale("accountEditBeanEditUserDataSaveError");
            return;
        } catch (IOException | MessagingException e) {
            DaoManager.rollbackTransaction();
            log.error(e.getMessage(), e);
            displayErrorMessageWithUserLocale("accountEditBeanSendEmailError");
            return;
        }

        displaySuccessMessageWithUserLocale("accountEditBeanEditUserDataSaveSuccessfully");
    }

    /**
     * Sends an activation link to the user's new email address if it has been changed.
     * And send an email to the old email address of the user that will notify the user of a change in his email address
     *
     * @param userWithNewEmail user entity
     * @param oldUserEmail     user`s old email address
     * @throws IOException        if something go wrong
     * @throws MessagingException if something go wrong
     */
    private void sendEmailToUsers(User userWithNewEmail, String oldUserEmail) throws IOException, MessagingException {
        String activationUrl = UserActivationHelper.createActivationUrl(userWithNewEmail.getActivationKey());
        UserActivationHelper.sendActivationEmail(activationUrl, userWithNewEmail.getEmail(), getUserBean().getUserLocale());
        EmailHelper.sendEmail(oldUserEmail,
                ResourceHelper.getMessageResource("userEmailChangedEmailMessageSubject",
                        getUserBean().getUserLocale()),
                ResourceHelper.getMessageResource("userEmailChangedEmailMessage",
                        getUserBean().getUserLocale()));
    }
}
