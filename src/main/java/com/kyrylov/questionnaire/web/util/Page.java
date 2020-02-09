package com.kyrylov.questionnaire.web.util;

import lombok.Getter;

@Getter
public enum Page {
    QUESTIONNAIRE("questionnaire.xhtml"),
    FIELDS("Server/Sec/Management/fields.xhtml"),
    RESPONSES("Server/Sec/Management/responses.xhtml"),
    RESPONSE_SUCCESS("Server/response_success.xhtml"),
    ACCOUNT_ACTIVATION("Server/User/activation.xhtml"),
    AUTHORIZATION("Server/User/authorization.xhtml"),
    ACCOUNT_EDIT("Server/Sec/User/account_edit.xhtml"),
    PASSWORD_EDIT("Server/Sec/User/change_password.xhtml");

    private String url;

    Page(String url) {
        this.url = url;
    }

}
