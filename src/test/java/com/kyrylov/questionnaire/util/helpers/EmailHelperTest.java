package com.kyrylov.questionnaire.util.helpers;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;

import javax.mail.MessagingException;
import java.io.IOException;

class EmailHelperTest {

    @Disabled
    @RepeatedTest(10)
    void testSendEmail() throws IOException, MessagingException {
        String email = ResourceHelper.getProperties(ResourceHelper.ResourceProperties.EMAIL_PROPERTIES)
                .getProperty(ResourceHelper.ResourceProperties.EmailProperties.SMTPS_USER);
        EmailHelper.sendEmail(email, "test", "test");
    }
}