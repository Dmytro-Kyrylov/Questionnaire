package com.kyrylov.questionnaire.web.beans.page;

import com.kyrylov.questionnaire.persistence.dao.DaoManager;
import com.kyrylov.questionnaire.persistence.domain.entities.User;
import com.kyrylov.questionnaire.persistence.domain.entities.UserRole;
import com.kyrylov.questionnaire.persistence.domain.entities.User_;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import com.kyrylov.questionnaire.web.beans.BasePageBean;
import com.kyrylov.questionnaire.web.util.helpers.UserActivationHelper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.mail.MessagingException;
import java.io.IOException;

@Getter
@Setter
@Slf4j
@Named
@ViewScoped
public class AuthorizationBean extends BasePageBean {

    private static final long serialVersionUID = 6809037600867563012L;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private PasswordEncoder passwordEncoder;

    private String email;

    private String password;

    private String lastName;

    private String firstName;

    private String phone;

    private boolean showLogInSection;

    @PostConstruct
    private void init() {
        setShowLogInSection(true);
        WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
        if (context != null) {
            setPasswordEncoder(context.getBean(PasswordEncoder.class));
        }
    }

    public void switchLogInAndSignUpSectionsToShow() {
        setShowLogInSection(!isShowLogInSection());
    }

    public void logIn() {
        getUserBean().login();
    }

    public void singUp() {
        try {
            if (usersRegisteredOnThisEmail() != 0) {
                displayErrorMessageWithUserLocale("authorizationBeanEmailAlreadyExist");
                return;
            }
        } catch (DatabaseException e) {
            log.error("Exception when trying to get users by email", e);
            displayErrorMessageWithUserLocale("authorizationBeanErrorWhenTryingToLoadUsersWithCurrentEmail");
            return;
        }

        try {
            User user = saveNewUser();
            UserActivationHelper.sendActivationEmail(user, getUserBean().getUserLocale());
        } catch (DatabaseException e) {
            log.error("Error when creating user", e);
            displayErrorMessageWithUserLocale("authorizationBeanErrorSaveUser");
            return;
        } catch (MessagingException | IOException e) {
            log.error("Error when sending activation email", e);
            displayErrorMessageWithUserLocale("authorizationBeanErrorSendActivationEmail");
        }

        resetPageData();
        displaySuccessMessageWithUserLocale("authorizationBeanSuccessRegistered");
    }

    private void resetPageData() {
        setShowLogInSection(true);
        setFirstName(null);
        setLastName(null);
        setEmail(null);
        setPassword(null);
        setPhone(null);
    }

    private long usersRegisteredOnThisEmail() throws DatabaseException {
        return DaoManager.getCount(User.class)
                .where()
                .equal(User_.EMAIL, getEmail())
                .execute().get(0);
    }

    private User saveNewUser() throws DatabaseException {
        User user = new User();

        user.setFirstName(getFirstName());
        user.setLastName(getLastName());
        user.setEmail(getEmail());
        user.setPhone(getPhone());
        if (getPasswordEncoder() != null) {
            user.setPassword(getPasswordEncoder().encode(getPassword()));
        } else {
            user.setPassword(getPassword());
        }
        user.setActivationKey(UserActivationHelper.generateActivationKey(user));
        user.setActive(Boolean.FALSE);
        user.getRoles().add(UserRole.getRoleByEnum(UserRole.RoleEnum.ROLE_USER));

        DaoManager.save(user, true);

        return user;
    }

}

