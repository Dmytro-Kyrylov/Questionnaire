package com.kyrylov.questionnaire.persistence.dao;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

@Slf4j
public abstract class JPAHibernateTest {

    private static SessionFactory sessionFactory;

    @BeforeAll
    static void beforeAll() {
        DaoManager.getSession();
    }

    @AfterAll
    static void afterAll() {

    }

}
