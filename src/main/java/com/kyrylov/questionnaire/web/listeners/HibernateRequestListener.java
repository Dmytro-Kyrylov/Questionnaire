package com.kyrylov.questionnaire.web.listeners;

import com.kyrylov.questionnaire.persistence.util.SessionHolder;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class HibernateRequestListener implements ServletRequestListener {

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        ((SessionHolder) sre.getServletRequest().getAttribute(SessionHolder.HIBERNATE_SESSION_ATTRIBUTE)).closeSession();
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        sre.getServletRequest().setAttribute(SessionHolder.HIBERNATE_SESSION_ATTRIBUTE, new SessionHolder());
    }
}
