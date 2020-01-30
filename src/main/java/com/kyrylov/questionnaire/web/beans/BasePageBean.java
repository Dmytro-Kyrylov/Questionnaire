package com.kyrylov.questionnaire.web.beans;

import com.kyrylov.questionnaire.util.helpers.ResourceHelper;
import com.kyrylov.questionnaire.web.beans.session.UserBean;
import com.kyrylov.questionnaire.web.util.helpers.RedirectHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

@Slf4j
@Getter
@Setter
public abstract class BasePageBean implements Serializable {

    private static final long serialVersionUID = 8297883342845600733L;

    @Inject
    private UserBean userBean;

    protected void displayErrorMessageWithUserLocale(String resourceMessage) {
        displayErrorMessage(getMessageResourceWithUserLocale(resourceMessage));
    }

    protected void displaySuccessMessageWithUserLocale(String resourceMessage) {
        displaySuccessMessage(getMessageResourceWithUserLocale(resourceMessage));
    }

    protected void displayErrorMessage(String message) {
        displayMessage(FacesMessage.SEVERITY_ERROR, message);
    }

    protected void displaySuccessMessage(String message) {
        displayMessage(FacesMessage.SEVERITY_INFO, message);
    }

    private void displayMessage(FacesMessage.Severity severity, String message) {
        FacesMessage facesMessage = new FacesMessage(severity, message, "");
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
    }

    protected String getMessageResourceWithUserLocale(String resourceMessage) {
        return ResourceHelper.getMessageResource(resourceMessage, getUserBean().getUserLocale());
    }

    protected String getRequestParameter(RedirectHelper.Parameter parameter) {
        return getHttpServletRequest().getParameter(parameter.getParameter());
    }

    protected HttpServletRequest getHttpServletRequest() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        return (HttpServletRequest) ctx.getExternalContext().getRequest();
    }

    protected HttpServletResponse getHttpServletResponse() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        return (HttpServletResponse) ctx.getExternalContext().getResponse();
    }

    protected void ajaxUpdate(String... components) {
        PrimeFaces.current().ajax().update(components);
    }

}
