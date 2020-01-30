package com.kyrylov.questionnaire.web.util.helpers;

import com.kyrylov.questionnaire.util.helpers.ResourceHelper;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;

public class EmailHelper {

    /**
     * Send message to passed email using email declared in project.properties
     *
     * @param email   destination email
     * @param subject subject of email
     * @param message email body
     */
    public static void sendEmail(String email, String subject, String message) throws IOException, MessagingException {
        ResourceHelper.PropertiesWrapper properties = ResourceHelper.getProperties(ResourceHelper.ResourceProperties.EMAIL_PROPERTIES);

        String fromEmail = properties.getProperty(ResourceHelper.ResourceProperties.EmailProperties.SMTPS_USER);
        String password = properties.getProperty(ResourceHelper.ResourceProperties.EmailProperties.SMTPS_PASSWORD);

        Session session = Session.getDefaultInstance(properties.getProperties());
        MimeMessage mimeMessage = new MimeMessage(session);
        try {
            mimeMessage.setFrom(new InternetAddress(fromEmail));
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(message);
            Transport tr = session.getTransport();
            tr.connect(null, password);
            tr.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
            tr.close();
        } catch (MessagingException e) {
            throw new MessagingException("Error when trying to send email", e);
        }
    }

}
