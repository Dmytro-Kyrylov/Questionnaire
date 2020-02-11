package com.kyrylov.questionnaire.web.beans.session;

import com.kyrylov.questionnaire.util.TypeOfLocale;
import com.kyrylov.questionnaire.util.dto.UserDto;
import com.kyrylov.questionnaire.util.helpers.ResourceHelper;
import com.kyrylov.questionnaire.util.helpers.UserActivationHelper;
import com.kyrylov.questionnaire.web.beans.BaseSessionBean;
import com.kyrylov.questionnaire.web.security.SecurityHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    private static final long serialVersionUID = 8904223199987995841L;

    private Locale userLocale;

    private Long userId;

    private UserDto user;

    @PostConstruct
    private void init() {
        this.userLocale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
    }

    /**
     * Spring security logout
     */
    public void logout() throws ServletException, IOException {
        FacesContext ctx = FacesContext.getCurrentInstance();
        SecurityHelper.performSpringSecurityAction(SecurityHelper.SpringSecurityAction.LOG_OUT,
                (HttpServletRequest) ctx.getExternalContext().getRequest(),
                (HttpServletResponse) ctx.getExternalContext().getResponse());
    }

    /**
     * Spring security authentication
     */
    public void login() throws ServletException, IOException {
        FacesContext ctx = FacesContext.getCurrentInstance();
        SecurityHelper.performSpringSecurityAction(SecurityHelper.SpringSecurityAction.LOG_IN,
                (HttpServletRequest) ctx.getExternalContext().getRequest(),
                (HttpServletResponse) ctx.getExternalContext().getResponse());
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
     * I do not want to implement the correct logic for this, because it is not necessary.
     */
    public void sendNewActivationEmail() {
        try {
            String activationUrl = UserActivationHelper.createActivationUrl(getUser().getActivationKey());
            UserActivationHelper.sendActivationEmail(activationUrl, getUser().getEmail(), getUserLocale());
        } catch (IOException | MessagingException e) {
            log.error(e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ResourceHelper.getMessageResource("sendActivationEmailError", getUserLocale())
                            , ""));
            return;
        }
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        ResourceHelper.getMessageResource("sendActivationEmailSuccess", getUserLocale())
                        , ""));
    }

}
