package com.kyrylov.questionnaire.persistence.dao;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

@Slf4j
abstract class JPAHibernateTest {

    @BeforeAll
    static void beforeAll() {
        DaoManager.getSession();
    }

    @AfterAll
    static void afterAll() {

    }

    @BeforeEach
    void setUp() {

    }

}
