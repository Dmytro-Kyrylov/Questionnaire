package com.kyrylov.questionnaire.persistence.util;

import lombok.extern.slf4j.Slf4j;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

@Slf4j
class SessionManagerImpl implements SessionManager {

    SessionManagerImpl() {

    }

    /**
     * Search sessionHolder in httpSession of current user and return it if exist, if not - create it
     * If there is no session, then return new instance of holder
     *
     * @return session holder if exist or new
     */
    @Override
    public SessionHolder getSessionHolder() {
        if (FacesContext.getCurrentInstance() == null) {
            log.info("No FacesContext. Create new SessionHolder");
            return new SessionHolder();
        }
        HttpServletRequest request = (HttpServletRequest) FacesContext
                .getCurrentInstance().getExternalContext().getRequest();
        return (SessionHolder) request.getAttribute(SessionHolder.HIBERNATE_SESSION_ATTRIBUTE);
    }

}
