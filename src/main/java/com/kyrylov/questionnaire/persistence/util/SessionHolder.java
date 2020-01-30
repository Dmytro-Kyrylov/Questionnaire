package com.kyrylov.questionnaire.persistence.util;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;

@Slf4j
public class SessionHolder {

    private Session session;

    public Session getSession() {
        if (this.session == null || !this.session.isConnected() || !this.session.isOpen()) {
            try {
                this.session = createSession();
            } catch (DatabaseException e) {
                log.error(e.getMessage(), e);
            }
        }
        return this.session;
    }

    private static Session createSession() throws DatabaseException {
        try {
            return HibernateUtil.getSessionFactoryInstance().openSession();
        } catch (Throwable ex) {
            throw new DatabaseException("Error of Hibernate Session creating", ex);
        }
    }

    public void closeSession() {
        if (this.session != null) {
            try {
                this.session.clear();
                this.session.close();
            } catch (Exception e) {
                this.session.cancelQuery();
                this.session.clear();
                this.session.close();
                log.error(e.getMessage(), e);
            }
            this.session = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            this.closeSession();
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
        }
    }
}
