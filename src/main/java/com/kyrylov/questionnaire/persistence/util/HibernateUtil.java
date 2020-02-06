package com.kyrylov.questionnaire.persistence.util;

import com.kyrylov.questionnaire.util.helpers.ResourceHelper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.reflections.Reflections;

import javax.persistence.Entity;
import java.io.IOException;
import java.util.Set;

/**
 * Main class to work with hibernate configuration
 *
 * @author Dmitrii
 */
@Slf4j
public class HibernateUtil {

    private static SessionFactory sessionFactory;

    private static SessionManager sessionManager;

    static {
        getSessionFactoryInstance();
        getSessionManager();
    }

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            Reflections reflections = new Reflections("com.kyrylov.questionnaire.persistence.domain");
            Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Entity.class);
            for (Class<?> clazz : classes) {
                configuration.addAnnotatedClass(clazz);
            }

            configuration.configure("hibernate.cfg.xml");
            log.info("Hibernate Annotation Configuration loaded");

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties()).build();
            log.info("Hibernate Annotation serviceRegistry created");

            return configuration.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) {
            log.error("Initial SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    synchronized static SessionFactory getSessionFactoryInstance() {
        if (sessionFactory == null) {
            sessionFactory = buildSessionFactory();
        }
        return sessionFactory;
    }

    /**
     * Find correct class path in project.properties file, create its instance and return it
     *
     * @return implementation of SessionManager interface{@link SessionManager}
     */
    public static SessionManager getSessionManager() {
        if (sessionManager == null) {
            try {
                Class sessionManagerImplClass = Class.forName(
                        ResourceHelper.getProperties(ResourceHelper.ResourceProperties.PROJECT_PROPERTIES)
                                .getProperty(ResourceHelper.ResourceProperties.ProjectProperties.SESSION_MANAGER_CLASS));
                sessionManager = (SessionManager) sessionManagerImplClass.newInstance();
            } catch (ClassNotFoundException | IOException | InstantiationException | IllegalAccessException e) {
                log.error("Error when trying to get SessionManager implementation", e);
            }
        }
        return sessionManager;
    }

    @Override
    protected void finalize() throws Throwable {
        sessionFactory.close();
    }
}
