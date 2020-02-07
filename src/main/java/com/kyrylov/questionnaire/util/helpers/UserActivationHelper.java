package com.kyrylov.questionnaire.util.helpers;

import com.kyrylov.questionnaire.persistence.dao.DaoManager;
import com.kyrylov.questionnaire.persistence.domain.entities.User;
import com.kyrylov.questionnaire.persistence.domain.entities.UserRole;
import com.kyrylov.questionnaire.persistence.domain.entities.User_;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import com.kyrylov.questionnaire.web.util.Page;
import com.kyrylov.questionnaire.web.util.helpers.RedirectHelper;
import lombok.extern.slf4j.Slf4j;

import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Locale;

@Slf4j
public class UserActivationHelper {

    /**
     * Activate user account
     *
     * @param key activation key
     * @return activated user {@link User} or null
     * @throws DatabaseException if any exception occurs
     */
    public static User activateAccount(String key) throws DatabaseException {
        User user = DaoManager.getByField(User.class, User_.ACTIVATION_KEY, key);

        if (user != null) {
            user.setActivationKey(null);
            user.setActive(true);
            user.getRoles().add(UserRole.getRoleByEnum(UserRole.RoleEnum.ROLE_ADMIN));

            DaoManager.save(user, true);

            return user;
        }
        return null;
    }

    public static String generateActivationKey(String keyWord) {
        return keyWord.toLowerCase();
    }

    public static void sendActivationEmail(String activationUrl, String email, Locale locale) throws IOException, MessagingException {
        EmailHelper.sendEmail(email, ResourceHelper.getMessageResource("activationEmailMessageSubject", locale),
                ResourceHelper.getMessageResource("activationEmailMessage", locale) + " " + activationUrl);
    }

    public static String createActivationUrl(String activationKey) {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        return request.getScheme() +
                "://" +
                request.getServerName() +
                ":" +
                request.getServerPort() +
                "/" +
                Page.ACCOUNT_ACTIVATION.getUrl() +
                "?" +
                RedirectHelper.Parameter.ACTIVATION_KEY_PARAMETER.getParameter() +
                "=" +
                activationKey;
    }
}
