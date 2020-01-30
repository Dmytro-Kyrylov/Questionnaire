package com.kyrylov.questionnaire.persistence.dao;

import com.kyrylov.questionnaire.persistence.domain.entities.Field;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import lombok.Getter;
import com.kyrylov.questionnaire.persistence.domain.IndexedEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Getter
class DaoManagerTest {


    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void get() throws DatabaseException {
        Field field = new Field();
        field.setLabel("test");
        Long id = testSave(field);
        DaoManager.getSession().evict(field);

        Field field1 = DaoManager.get(Field.class, id);

        Assertions.assertEquals(field.getLabel(),field1.getLabel());


    }

    private Long testSave(IndexedEntity entity) {
        try {
            DaoManager.save(entity, true);
        } catch (DatabaseException e) {
            e.printStackTrace();
            Assertions.fail();
        }
        return entity.getId();
    }

    @Test
    void select() {
    }

    @Test
    void getCount() {
    }

    @Test
    void testSelect() {
    }
}