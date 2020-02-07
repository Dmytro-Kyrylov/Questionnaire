package com.kyrylov.questionnaire.web.beans.page;

import com.kyrylov.questionnaire.persistence.dao.DaoManager;
import com.kyrylov.questionnaire.persistence.domain.entities.User;
import com.kyrylov.questionnaire.persistence.domain.entities.UserRole;
import com.kyrylov.questionnaire.persistence.domain.entities.User_;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import com.kyrylov.questionnaire.util.dto.UserDto;
import com.kyrylov.questionnaire.util.helpers.UserActivationHelper;
import com.kyrylov.questionnaire.web.beans.BasePageBean;
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
import javax.servlet.ServletException;
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

    private UserDto pageUser;

    private boolean showLogInSection;

    @PostConstruct
    private void init() {
        setPageUser(new UserDto());
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
        try {
            getUserBean().login();
        } catch (ServletException | IOException e) {
            log.error(e.getMessage(), e);
            displayErrorMessageWithUserLocale("authorizationBeanLoginError");
        }
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
            String activationUrl = UserActivationHelper.createActivationUrl(user.getActivationKey());
            UserActivationHelper.sendActivationEmail(activationUrl, user.getEmail(), getUserBean().getUserLocale());
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
        setPageUser(new UserDto());
    }

    private long usersRegisteredOnThisEmail() throws DatabaseException {
        return DaoManager.getCount(User.class)
                .where()
                .equal(User_.EMAIL, getPageUser().getEmail())
                .execute().get(0);
    }

    private User saveNewUser() throws DatabaseException {
        User user = new User();

        user.setFirstName(getPageUser().getFirstName());
        user.setLastName(getPageUser().getLastName());
        user.setEmail(getPageUser().getEmail());
        user.setPhone(getPageUser().getPhone());
        if (getPasswordEncoder() != null) {
            user.setPassword(getPasswordEncoder().encode(getPageUser().getPassword()));
        } else {
            user.setPassword(getPageUser().getPassword());
        }
        user.setActivationKey(UserActivationHelper.generateActivationKey(user.getEmail()));
        user.setActive(Boolean.FALSE);
        user.getRoles().add(UserRole.getRoleByEnum(UserRole.RoleEnum.ROLE_USER));

        DaoManager.save(user, true);

        return user;
    }

}

