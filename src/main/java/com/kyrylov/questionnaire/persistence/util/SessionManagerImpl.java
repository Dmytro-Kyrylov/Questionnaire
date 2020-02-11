package com.kyrylov.questionnaire.persistence.util;

import lombok.extern.slf4j.Slf4j;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

@Slf4j
class SessionManagerImpl implements SessionManager {

    private static SessionHolder applicationSessionHolder;

    /**
     * Search sessionHolder in httpSession of current user and return it if exist, if not - create static holder for application
     *
     * @return session holder {@link SessionHolder}
     */
    @Override
    public SessionHolder getSessionHolder() {
        if (FacesContext.getCurrentInstance() == null) {
            log.warn("No FacesContext. Create new SessionHolder");
            if (applicationSessionHolder == null) {
                applicationSessionHolder = new SessionHolder();
            }
            return applicationSessionHolder;
        }
        HttpServletRequest request = (HttpServletRequest) FacesContext
                .getCurrentInstance().getExternalContext().getRequest();
        return (SessionHolder) request.getAttribute(SessionHolder.HIBERNATE_SESSION_ATTRIBUTE);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        applicationSessionHolder.closeSession();
    }
}
