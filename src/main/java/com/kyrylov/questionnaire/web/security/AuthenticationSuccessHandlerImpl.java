package com.kyrylov.questionnaire.web.security;

import com.kyrylov.questionnaire.persistence.domain.entities.User;
import com.kyrylov.questionnaire.web.beans.session.UserBean;
import com.kyrylov.questionnaire.web.util.Page;
import com.kyrylov.questionnaire.web.util.helpers.RedirectHelper;
import org.jboss.weld.serialization.spi.helpers.SerializableContextualInstance;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;

public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest,
                                        HttpServletResponse httpServletResponse, Authentication authentication)
            throws IOException, ServletException {
        setUserToSession(authentication, httpServletRequest);

        RedirectHelper.sendRedirect(Page.QUESTIONNAIRE);
    }

    /**
     * Get user{@link User} from authentication and set it to user`s session bean
     *
     * @param authentication     Spring security authentication
     * @param httpServletRequest current http request
     */
    private void setUserToSession(Authentication authentication, HttpServletRequest httpServletRequest) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            HttpSession session = httpServletRequest.getSession();
            Enumeration<String> attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String attribute = attributeNames.nextElement();
                if (attribute.contains(UserBean.class.getName())) {
                    UserBean userBean = (UserBean) (((SerializableContextualInstance) session.getAttribute(attribute)).getInstance());
                    userBean.setUser(userDetails.getUser());
                    userBean.setUserId(userDetails.getUserId());
                    break;
                }
            }
        }
    }
}
