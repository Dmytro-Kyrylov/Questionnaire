package com.kyrylov.questionnaire.persistence.util;

import lombok.Getter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

public class SessionManager {

    private Session session;
    private SessionFactory sessionFactory;

    private static SessionManager sessionManager;

    @Getter
    private static final String HIBERNATE_SESSION_ATTRIBUTE = "hibernateSessionAttribute";

    private SessionManager() {
        this.sessionFactory = HibernateUtil.getSessionFactoryInstance();
        this.session = this.sessionFactory.openSession();
    }

    public synchronized Session getApplicationSession() {
        if (this.session == null || !this.session.isConnected() || !this.session.isOpen()) {
            this.session = this.sessionFactory.openSession();
        }
        return this.session;
    }

    /**
     * Search sessionHolder in httpSession of current user and return it if exist, if not - create it
     * If there is no session, then return null
     *
     * @return session holder if exist or null
     */
    public SessionHolder getSessionByBean() {
        if (FacesContext.getCurrentInstance() != null) {
            HttpSession httpSession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);

            if (httpSession.getAttribute(HIBERNATE_SESSION_ATTRIBUTE) == null) {
                httpSession.setAttribute(HIBERNATE_SESSION_ATTRIBUTE, new SessionHolder());
            }

            return ((SessionHolder) httpSession.getAttribute(HIBERNATE_SESSION_ATTRIBUTE));
        }
        return null;
    }

    public synchronized static SessionManager getInstance() {
        if (sessionManager == null) {
            sessionManager = new SessionManager();
        }
        return sessionManager;
    }

}
