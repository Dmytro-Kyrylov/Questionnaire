package com.kyrylov.questionnaire.util.helpers;

import com.kyrylov.questionnaire.util.TypeOfLocale;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Class to work with resources
 *
 * @author Dmitrii
 */
@Slf4j
public class ResourceHelper {

    private static final Locale DEFAULT_APPLICATION_LOCALE;

    /**
     * All primary resources data in enum representation
     *
     * @author Dmitrii
     */
    @Getter(AccessLevel.PRIVATE)
    public enum ResourceProperties {
        PROJECT_PROPERTIES("project.properties"),
        EMAIL_PROPERTIES("email.properties");

        private interface PropertiesHandler {
            String getValue();
        }

        public enum EmailProperties implements PropertiesHandler {
            TRANSPORT_PROTOCOL("mail.transport.protocol"),
            SMTPS_AUTH("mail.smtps.auth"),
            SMTPS_HOST("mail.smtps.host"),
            SMTPS_USER("mail.smtps.user"),
            SMTPS_PASSWORD("mail.smtps.password");

            private String value;

            EmailProperties(String value) {
                this.value = value;
            }

            @Override
            public String getValue() {
                return this.value;
            }
        }

        public enum ProjectProperties implements PropertiesHandler {
            DEFAULT_APPLICATION_LOCALE("defaultLocale"),
            QUESTIONNAIRE_FILE_SAVE_PATH("questionnaireFileSavePath");

            private String value;

            ProjectProperties(String value) {
                this.value = value;
            }

            @Override
            public String getValue() {
                return this.value;
            }
        }

        private String fileName;

        ResourceProperties(String fileName) {
            this.fileName = fileName;
        }
    }

    @Getter
    public static class PropertiesWrapper {

        private Properties properties;

        PropertiesWrapper(Properties properties) {
            this.properties = properties;
        }

        public String getProperty(ResourceProperties.PropertiesHandler property) {
            return this.properties.getProperty(property.getValue());
        }
    }

    static {
        String applicationLocale = null;
        try {
            applicationLocale = getProperties(ResourceProperties.PROJECT_PROPERTIES)
                    .getProperty(ResourceProperties.ProjectProperties.DEFAULT_APPLICATION_LOCALE);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        TypeOfLocale locale = TypeOfLocale.getByCode(applicationLocale);
        DEFAULT_APPLICATION_LOCALE = locale.createLocale();
    }

    public static String getFileCreatingResource(String resourceName, Locale locale) {
        return getResource(resourceName, locale, "/locale/file_resources");
    }

    public static String getMessageResource(String resourceName, Locale locale) {
        return getResource(resourceName, locale, "/locale/messages");
    }

    private static String getResource(String resourceName, Locale locale, String bundleLocation) {
        String resource = null;

        Locale usedLocale = locale != null ? (Locale) locale.clone() : DEFAULT_APPLICATION_LOCALE;

        ResourceBundle bundle = ResourceBundle.getBundle(bundleLocation, usedLocale, getClassLoader());

        if (bundle != null) {
            try {
                resource = bundle.getString(resourceName);
            } catch (MissingResourceException ex) {
                log.info("Cannot get resource - {} ", resourceName);
            }
        }

        return resource != null ? resource : "???" + resourceName + "???";
    }

    public static PropertiesWrapper getProperties(ResourceProperties resourceProperties) throws IOException {
        Properties projectProperties = new Properties();
        try {
            InputStream is = getClassLoader().getResourceAsStream(resourceProperties.getFileName());
            projectProperties.load(is);
        } catch (IOException e) {
            throw new IOException("Properties hasn't been read", e);
        }
        return new PropertiesWrapper(projectProperties);
    }

    private static ClassLoader getClassLoader() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        return loader;
    }
}
