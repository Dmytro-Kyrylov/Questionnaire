package com.kyrylov.questionnaire.web.beans.session;

import com.kyrylov.questionnaire.persistence.domain.entities.User;
import com.kyrylov.questionnaire.util.TypeOfLocale;
import com.kyrylov.questionnaire.util.helpers.ResourceHelper;
import com.kyrylov.questionnaire.web.beans.BaseSessionBean;
import com.kyrylov.questionnaire.web.security.UserDetailsImpl;
import com.kyrylov.questionnaire.web.util.helpers.UserActivationHelper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * User`s session bean. Contains user locale
 *
 * @author Dmitrii
 */
@Slf4j
@Getter
@Setter
@Named
@SessionScoped
public class UserBean extends BaseSessionBean {

    @Getter(AccessLevel.PRIVATE)
    private enum SpringSecurityAction {
        LOG_IN("/j_spring_security_check"),
        LOG_OUT("/logout");

        private String servletURL;

        SpringSecurityAction(String servletURL) {
            this.servletURL = servletURL;
        }
    }

    private static final long serialVersionUID = 8904223199987995841L;

    private Locale userLocale;

    private User user;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private Date lastEmailSendingTime;

    @PostConstruct
    private void init() {
        this.userLocale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
    }

    private void performSpringSecurityAction(SpringSecurityAction action, HttpServletRequest request, HttpServletResponse response) {
        RequestDispatcher requestDispatcher = request.getRequestDispatcher(action.getServletURL());
        try {
            requestDispatcher.forward(request, response);
        } catch (ServletException | IOException e) {
            e.printStackTrace();
        }
        FacesContext.getCurrentInstance().responseComplete();
    }

    public void logout() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        performSpringSecurityAction(SpringSecurityAction.LOG_OUT,
                (HttpServletRequest) ctx.getExternalContext().getRequest(),
                (HttpServletResponse) ctx.getExternalContext().getResponse());
    }

    /**
     * Spring security authentication
     */
    public void login() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        performSpringSecurityAction(SpringSecurityAction.LOG_IN,
                (HttpServletRequest) ctx.getExternalContext().getRequest(),
                (HttpServletResponse) ctx.getExternalContext().getResponse());
    }

    /**
     * Update user rights if his roles have changed and he is logged in
     */
    public void updateUserAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        userDetails.initAuthorities();
        Authentication newAuth = new UsernamePasswordAuthenticationToken(authentication.getPrincipal(),
                authentication.getCredentials(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    /**
     * Change locale for current user
     *
     * @param typeOfLocale locale type from page
     */
    public void changeLocale(String typeOfLocale) {
        setUserLocale(TypeOfLocale.getByCode(typeOfLocale).createLocale());
        FacesContext context = FacesContext.getCurrentInstance();
        context.getViewRoot().setLocale(getUserLocale());
    }

    public boolean equalWithCurrentLocale(String typeOfLocale) {
        return getUserLocale().getLanguage().equals(TypeOfLocale.getByCode(typeOfLocale).getLanguage());
    }

    /**
     * Send activation link to user`s mail.
     * <p>
     * If the email was sent successfully, block this option for 24 hours.
     * But if the session will be invalidate, this block will not work.
     * I do not want to implement the correct logic for this, because it is not necessary.
     */
    public void sendNewActivationEmail() {
        if (getLastEmailSendingTime() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(getLastEmailSendingTime());
            calendar.add(Calendar.DAY_OF_WEEK, 1);
            if (new Date().compareTo(calendar.getTime()) < 0) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        ResourceHelper.getMessageResource("sendActivationEmailCooldown", getUserLocale())
                        , ""));
                return;
            }
        }
        try {
            UserActivationHelper.sendActivationEmail(getUser(), getUserLocale());
        } catch (IOException | MessagingException e) {
            log.error(e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ResourceHelper.getMessageResource("sendActivationEmailError", getUserLocale())
                            , ""));
            return;
        }
        setLastEmailSendingTime(new Date());
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        ResourceHelper.getMessageResource("sendActivationEmailSuccess", getUserLocale())
                        , ""));
    }

}
