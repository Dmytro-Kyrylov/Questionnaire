package com.kyrylov.questionnaire.persistence.dao;

import com.kyrylov.questionnaire.persistence.domain.entities.Field;
import com.kyrylov.questionnaire.persistence.domain.entities.Field_;
import com.kyrylov.questionnaire.persistence.domain.entities.Option;
import com.kyrylov.questionnaire.persistence.domain.entities.Option_;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

@Getter
class DaoManagerTest extends JPAHibernateTest {

    @Test
    void get() throws DatabaseException {
        Field field = new Field();
        field.setLabel("test");
        DaoManager.save(field);

        DaoManager.getSession().evict(field);
        Assertions.assertFalse(DaoManager.getSession().contains(field));

        Field field1 = DaoManager.get(Field.class, field.getId());

        Assertions.assertEquals(field.getLabel(), field1.getLabel());
    }

    @Test
    void bracketsException() {
        Assertions.assertThrows(DatabaseException.class, () -> DaoManager.select(Field.class).where().openBracket().execute());
    }

    @Test
    void getCount() throws DatabaseException {
        Field field = new Field();
        field.setLabel("fieldTest1");
        Field field1 = new Field();
        field1.setLabel("fieldTest2");
        DaoManager.save(field);
        Long count = DaoManager.getCount(Field.class).execute().get(0);
        Assertions.assertEquals(1L, count);
        DaoManager.save(field1);
        count = DaoManager.getCount(Field.class).execute().get(0);
        Assertions.assertEquals(2L, count);
    }

    @Test
    @DisplayName("Select testing")
    void testSelect() throws DatabaseException {
        Field field = new Field();
        Option option = new Option();
        field.setLabel("fieldTest");
        option.setText("optionTest");
        option.setField(field);
        field.setOptions(Collections.singletonList(option));

        DaoManager.save(field);
        Assertions.assertNotNull(field.getId(), "field`s id is null");
        Assertions.assertTrue(field.getOptions().size() != 0, "options are empty");
        Assertions.assertNotNull(option.getId(), "option`s id is null");

        List<Option> options = DaoManager.select(Option.class).where().equal(Option_.FIELD, field).execute();
        Assertions.assertEquals(1, options.size());
        options = DaoManager.select(Option.class).innerJoin(Option_.FIELD, "f")
                .where().equal(Field_.ID, field.getId(), "f").execute();
        Assertions.assertEquals(1, options.size());
        Assertions.assertEquals("optionTest", options.get(0).getText());
        List<Field> fields = DaoManager.select(Field.class).execute();
        Assertions.assertEquals(1, fields.size());
        Assertions.assertEquals("fieldTest", fields.get(0).getLabel());
    }

    @Test
    void testSave() throws DatabaseException {
        Field field = new Field();
        field.setLabel("test");
        DaoManager.save(field);
        Assertions.assertNotNull(field.getId());
    }

    @Test
    void testDelete() throws DatabaseException {
        Field field = new Field();
        field.setLabel("test");
        DaoManager.save(field);
        Assertions.assertNotNull(field.getId());

        DaoManager.delete(field);
        Long count = DaoManager.getCount(Field.class).execute().get(0);
        Assertions.assertEquals(0L, count,"Get count returned not 0");
    }

}