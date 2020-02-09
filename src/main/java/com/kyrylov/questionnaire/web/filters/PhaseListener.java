package com.kyrylov.questionnaire.web.filters;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;

public class PhaseListener implements javax.faces.event.PhaseListener {

    private static final long serialVersionUID = -4714326587650811455L;

    @Override
    public void afterPhase(PhaseEvent phaseEvent) {
        System.out.println("After");
    }

    @Override
    public void beforePhase(PhaseEvent phaseEvent) {
        System.out.println("before");
    }

    private void createHibernateSession(){

    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }
}
