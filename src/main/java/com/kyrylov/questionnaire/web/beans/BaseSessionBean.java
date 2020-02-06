package com.kyrylov.questionnaire.web.beans;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.io.Serializable;

public abstract class BaseSessionBean implements Serializable {

    private static final long serialVersionUID = -1282033772653103009L;

    /**
     * @param timeInSeconds session time
     * @since implementing Spring Security
     * @deprecated
     */
    public void setSessionMaxInactiveInterval(int timeInSeconds) {
        HttpSession httpSession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        httpSession.setMaxInactiveInterval(timeInSeconds);
    }

}
