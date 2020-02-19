package com.kyrylov.questionnaire.persistence.dao;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;

@Slf4j
abstract class JPAHibernateTest {

    @BeforeAll
    static void beforeAll() {
        DaoManager.getSession();
    }

}
