package com.kyrylov.questionnaire.persistence.dao;

import com.kyrylov.questionnaire.persistence.util.SessionHolder;
import com.kyrylov.questionnaire.persistence.util.SessionManager;

public class SessionMangerTest implements SessionManager {

    private static SessionHolder sessionHolder;

    @Override
    public SessionHolder getSessionHolder() {
        if (sessionHolder == null) {
            sessionHolder = new SessionHolder();
        }
        return sessionHolder;
    }

}
