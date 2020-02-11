package com.kyrylov.questionnaire.web.security;

import com.kyrylov.questionnaire.persistence.domain.entities.User;
import com.kyrylov.questionnaire.util.dto.UserDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.faces.context.FacesContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityHelper {

    @Getter(AccessLevel.PRIVATE)
    public enum SpringSecurityAction {
        LOG_IN("/j_spring_security_check"),
        LOG_OUT("/logout");

        private String servletURL;

        SpringSecurityAction(String servletURL) {
            this.servletURL = servletURL;
        }
    }

    public static void performSpringSecurityAction(SpringSecurityAction action, HttpServletRequest request,
                                                   HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher requestDispatcher = request.getRequestDispatcher(action.getServletURL());
        requestDispatcher.forward(request, response);
        FacesContext.getCurrentInstance().responseComplete();
    }

    /**
     * Update user rights if his roles have changed and he is logged in
     */
    public static UserDto updateUserDetailsAndGetDtoOfUser(User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        Authentication newAuth = new UsernamePasswordAuthenticationToken(userDetails,
                authentication.getCredentials(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);
        return userDetails.getUser();
    }
}
