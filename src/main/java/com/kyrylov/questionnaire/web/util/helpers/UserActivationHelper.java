package com.kyrylov.questionnaire.web.util.helpers;

import com.kyrylov.questionnaire.persistence.dao.DaoManager;
import com.kyrylov.questionnaire.persistence.domain.entities.User;
import com.kyrylov.questionnaire.persistence.domain.entities.UserRole;
import com.kyrylov.questionnaire.persistence.domain.entities.User_;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import com.kyrylov.questionnaire.util.helpers.ResourceHelper;
import com.kyrylov.questionnaire.web.util.Page;
import lombok.extern.slf4j.Slf4j;

import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
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
        List<User> users = DaoManager.select(User.class)
                .where()
                .equal(User_.ACTIVATION_KEY, key)
                .execute();

        if (users != null && users.size() != 0) {
            User user = users.get(0);
            user.setActivationKey(null);
            user.setActive(true);
            user.getRoles().add(UserRole.getRoleByEnum(UserRole.RoleEnum.ROLE_ADMIN));

            DaoManager.save(user, true);

            return user;
        }
        return null;
    }

    public static String generateActivationKey(User user) {
        return user.getEmail().toLowerCase();
    }

    public static void sendActivationEmail(User user, Locale locale) throws IOException, MessagingException {
        String activationUrl = createActivationUrl(user.getActivationKey());
        EmailHelper.sendEmail(user.getEmail(), ResourceHelper.getMessageResource("activationEmailMessageSubject", locale),
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
