package com.kyrylov.questionnaire.web.filters;

import com.kyrylov.questionnaire.persistence.util.SessionHolder;
import com.kyrylov.questionnaire.web.beans.BasePageBean;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter("*")
public class HibernateRequestFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (servletRequest.getAttribute(SessionHolder.HIBERNATE_SESSION_ATTRIBUTE) != null) {
            SessionHolder sessionHolder = (SessionHolder) servletRequest.getAttribute(SessionHolder.HIBERNATE_SESSION_ATTRIBUTE);
            sessionHolder.closeSession();
        } else {
            servletRequest.setAttribute(SessionHolder.HIBERNATE_SESSION_ATTRIBUTE, new SessionHolder());
        }
        filterChain.doFilter(servletRequest, servletResponse);

        SessionHolder sessionHolder = (SessionHolder) servletRequest.getAttribute(SessionHolder.HIBERNATE_SESSION_ATTRIBUTE);
        sessionHolder.closeSession();
    }

}
