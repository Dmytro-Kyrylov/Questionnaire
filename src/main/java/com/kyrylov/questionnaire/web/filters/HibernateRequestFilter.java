package com.kyrylov.questionnaire.web.filters;

import com.kyrylov.questionnaire.persistence.util.SessionHolder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
