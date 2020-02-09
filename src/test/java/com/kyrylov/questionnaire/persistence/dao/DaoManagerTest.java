package com.kyrylov.questionnaire.persistence.dao;

import com.kyrylov.questionnaire.persistence.domain.entities.*;
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
    void testGet() throws DatabaseException {
        Field field = new Field();
        field.setLabel("test");
        DaoManager.save(field, true);
        Assertions.assertNotNull(field.getId(), "Field was not saved");
        DaoManager.getSession().evict(field);
        Assertions.assertFalse(DaoManager.getSession().contains(field), "Field still in session");

        Field field1 = DaoManager.get(Field.class, field.getId());

        Assertions.assertEquals(field.getLabel(), field1.getLabel(), "Fields are different");
    }

    @Test
    void testGetByField() throws DatabaseException {
        User user = new User();
        user.setEmail("test");
        DaoManager.save(user, true);
        Assertions.assertNotNull(user.getId(), "User was not saved");
        DaoManager.getSession().evict(user);
        Assertions.assertFalse(DaoManager.getSession().contains(user), "User still in session");
        User userByField = DaoManager.getByField(User.class, User_.EMAIL, user.getEmail());
        Assertions.assertNotNull(userByField, "User was not found");
        Assertions.assertEquals(user.getEmail(), userByField.getEmail(), "Users are different");

        Assertions.assertNull(DaoManager.getByField(User.class, User_.EMAIL, "empty"), "User was found? WTF?");
    }

    @Test
    void testBracketsException() {
        Assertions.assertThrows(DatabaseException.class, () -> DaoManager.select(Field.class).where().openBracket().list());
        Assertions.assertThrows(DatabaseException.class, () -> DaoManager.select(Field.class).where().closeBracket().list());
    }

    @Test
    void getCount() throws DatabaseException {
        Long count1 = DaoManager.getCount(Field.class).singleResult();
        Assertions.assertNotNull(count1, "Count is empty");

        Field field = new Field();
        field.setLabel("fieldTest1");
        DaoManager.save(field, true);
        Assertions.assertNotNull(field.getId(), "Field was not saved");

        Long count2 = DaoManager.getCount(Field.class).singleResult();
        Assertions.assertNotNull(count1, "Count is empty");
        Assertions.assertEquals(count1 + 1, count2, "Wrong count of fields after saving");
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

        DaoManager.save(field, true);
        Assertions.assertNotNull(field.getId(), "field`s id is null");
        Assertions.assertNotNull(option.getId(), "option`s id is null");

        List<Option> options = DaoManager.select(Option.class).where().equal(Option_.FIELD, field).list();
        Assertions.assertEquals(1, options.size(), "Wrong count of options in database");

        Assertions.assertEquals(option.getText(), options.get(0).getText(), "Different options");

        List<Field> fields = DaoManager.select(Field.class).where().equal(Field_.ID, field.getId()).list();
        Assertions.assertEquals(field.getLabel(), fields.get(0).getLabel(), "Fields are different");
    }

    @Test
    void testSave() throws DatabaseException {
        Field field = new Field();
        field.setLabel("test");
        DaoManager.save(field, true);
        Assertions.assertNotNull(field.getId(), "Entity was not saved");
    }

    @Test
    void testDelete() throws DatabaseException {
        Field field = new Field();
        field.setLabel("test");
        DaoManager.save(field, true);
        Assertions.assertNotNull(field.getId(), "Entity was not saved");

        DaoManager.delete(field, true);
        DaoManager.getSession().evict(field);

        Assertions.assertThrows(DatabaseException.class, () -> DaoManager.get(Field.class, field.getId()));
    }

}