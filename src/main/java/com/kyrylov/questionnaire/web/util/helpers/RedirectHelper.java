package com.kyrylov.questionnaire.web.util.helpers;

import com.kyrylov.questionnaire.web.util.Page;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.faces.context.FacesContext;
import java.io.IOException;

@Slf4j
public class RedirectHelper {

    @Getter
    public enum Parameter {
        ACTIVATION_KEY_PARAMETER("key"),
        ID_OF_ENTITY("id");

        private String parameter;

        Parameter(String parameter) {
            this.parameter = parameter;
        }
    }

    /**
     * Redirect user to special page
     *
     * @param page destination page
     */
    public static void sendRedirect(Page page) {
        String createdUrl = page.getUrl();
        if (!createdUrl.startsWith("/")) {
            createdUrl = ("/").concat(createdUrl);
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext.getResponseComplete()) {
            return;
        }
        try {
            facesContext.getExternalContext().redirect(createdUrl);
        } catch (IOException e) {
            log.error("", e);
        }
    }

}
