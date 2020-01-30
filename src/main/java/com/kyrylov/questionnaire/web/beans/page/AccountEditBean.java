package com.kyrylov.questionnaire.web.beans.page;

import com.kyrylov.questionnaire.persistence.dao.DaoManager;
import com.kyrylov.questionnaire.persistence.domain.entities.User;
import com.kyrylov.questionnaire.persistence.domain.entities.UserRole;
import com.kyrylov.questionnaire.persistence.domain.entities.User_;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import com.kyrylov.questionnaire.util.helpers.ResourceHelper;
import com.kyrylov.questionnaire.web.beans.BasePageBean;
import com.kyrylov.questionnaire.web.util.helpers.EmailHelper;
import com.kyrylov.questionnaire.web.util.helpers.UserActivationHelper;
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

    private User pageUser;

    @PostConstruct
    private void init() {
        setPageUser(new User());
        initUser(getPageUser(), getUserBean().getUser());
    }

    private void initUser(User targetUser, User sourceUser) {
        targetUser.setEmail(sourceUser.getEmail());
        targetUser.setFirstName(sourceUser.getFirstName());
        targetUser.setLastName(sourceUser.getLastName());
        targetUser.setPhone(sourceUser.getPhone());
        targetUser.setRoles(sourceUser.getRoles());
        targetUser.setActive(sourceUser.getActive());
        targetUser.setActivationKey(sourceUser.getActivationKey());
    }

    /**
     * Check if new entered user`s email exist in database, save changed data and send to new and old emails appropriate messages
     */
    public void editAccount() {
        if (!getPageUser().getEmail().equals(getUserBean().getUser().getEmail())) {
            try {
                if (isUserEmailAlreadyExistInDB(getPageUser().getEmail())) {
                    displayErrorMessageWithUserLocale("accountEditBeanEmailIsAlreadyUsedError");
                    return;
                }
            } catch (DatabaseException e) {
                log.error("Error when trying to get users from DB with new user`s email", e);
                displayErrorMessageWithUserLocale("accountEditBeanEditUserDataSaveError");
                return;
            }
        }

        User userToSave = getUserBean().getUser();

        User oldUserdata = new User();
        initUser(oldUserdata, getUserBean().getUser());

        try {
            DaoManager.beginTransaction();

            if (!getPageUser().getEmail().equals(userToSave.getEmail())) {
                userToSave.setActivationKey(UserActivationHelper.generateActivationKey(userToSave));
                userToSave.setActive(false);
                userToSave.getRoles().removeIf(r -> r.getRole().equals(UserRole.RoleEnum.ROLE_ADMIN));
            }

            userToSave.setPhone(getPageUser().getPhone());
            userToSave.setEmail(getPageUser().getEmail());
            userToSave.setFirstName(getPageUser().getFirstName());
            userToSave.setLastName(getPageUser().getLastName());

            DaoManager.save(getUserBean().getUser());

            if (!oldUserdata.getEmail().equals(userToSave.getEmail())) {
                sendEmailToUsers(userToSave, oldUserdata.getEmail());
            }

            DaoManager.commitTransaction();
        } catch (DatabaseException e) {
            DaoManager.rollbackTransaction();
            initUser(userToSave, oldUserdata);
            log.error("Error on user save to DB", e);
            displayErrorMessageWithUserLocale("accountEditBeanEditUserDataSaveError");
            return;
        } catch (IOException | MessagingException e) {
            DaoManager.rollbackTransaction();
            initUser(userToSave, oldUserdata);
            log.error(e.getMessage(), e);
            displayErrorMessageWithUserLocale("accountEditBeanSendEmailError");
            return;
        }

        getUserBean().updateUserAuthorities();
        displaySuccessMessageWithUserLocale("accountEditBeanEditUserDataSaveSuccessfully");
    }

    private void sendEmailToUsers(User userWithNewEmail, String oldUserEmail) throws IOException, MessagingException {
        UserActivationHelper.sendActivationEmail(userWithNewEmail, getUserBean().getUserLocale());
        EmailHelper.sendEmail(oldUserEmail,
                ResourceHelper.getMessageResource("userEmailChangedEmailMessageSubject",
                        getUserBean().getUserLocale()),
                ResourceHelper.getMessageResource("userEmailChangedEmailMessage",
                        getUserBean().getUserLocale()));
    }

    private boolean isUserEmailAlreadyExistInDB(String newEmail) throws DatabaseException {
        long usersWithEnteredEmail = DaoManager.getCount(User.class)
                .where()
                .equal(User_.EMAIL, newEmail)
                .execute().get(0);
        return usersWithEnteredEmail != 0;
    }
}
