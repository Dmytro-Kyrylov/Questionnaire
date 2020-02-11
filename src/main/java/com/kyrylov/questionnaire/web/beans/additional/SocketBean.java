package com.kyrylov.questionnaire.web.beans.additional;

import lombok.AccessLevel;
import lombok.Getter;

import javax.enterprise.context.RequestScoped;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

/**
 * Class to work with sockets
 *
 * @author Dmitrii
 */
@Named
@RequestScoped
public class SocketBean implements Serializable {

    private static final long serialVersionUID = -2420219156541908659L;

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Getter(AccessLevel.PRIVATE)
    @Inject
    @Push(channel = "response_table")
    private PushContext pushContext;

    public void updateResponseTableByPushMessageInApplicationScope() {
        getPushContext().send("updateResponseTable");
    }
}
