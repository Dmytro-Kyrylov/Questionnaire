package com.kyrylov.questionnaire.web.beans;

import com.kyrylov.questionnaire.persistence.util.SessionHolder;
import com.kyrylov.questionnaire.persistence.util.SessionManager;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.io.Serializable;

public abstract class BaseSessionBean implements Serializable {

    private static final long serialVersionUID = -1282033772653103009L;

    public BaseSessionBean() {
        openSession();
    }

    /**
     * @param timeInSeconds session time
     * @since implementing Spring Security
     * @deprecated
     */
    public void setSessionMaxInactiveInterval(int timeInSeconds) {
        HttpSession httpSession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        httpSession.setMaxInactiveInterval(timeInSeconds);
    }

    /**
     * Create new hibernate session holder for current session
     */
    private void openSession() {
        HttpSession httpSession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        httpSession.setAttribute(SessionManager.getHIBERNATE_SESSION_ATTRIBUTE(), new SessionHolder());
    }

    @Override
    protected void finalize() throws Throwable {
        HttpSession httpSession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        SessionHolder sessionHolder = (SessionHolder) httpSession.getAttribute(SessionManager.getHIBERNATE_SESSION_ATTRIBUTE());
        sessionHolder.closeSession();
    }
}
