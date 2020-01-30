package com.kyrylov.questionnaire.persistence.util;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.reflections.Reflections;

import javax.persistence.Entity;
import java.util.Set;

/**
 * Main class to work with hibernate configuration
 *
 * @author Dmitrii
 */
@Slf4j
class HibernateUtil {

    private static SessionFactory sessionFactory;

    static {
        getSessionFactoryInstance();
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

}
