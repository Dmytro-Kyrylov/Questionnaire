package com.kyrylov.questionnaire.persistence.dao;

import com.kyrylov.questionnaire.persistence.domain.entities.Field;
import com.kyrylov.questionnaire.persistence.domain.entities.Field_;
import com.kyrylov.questionnaire.persistence.domain.entities.Option;
import com.kyrylov.questionnaire.persistence.domain.entities.Option_;
import com.kyrylov.questionnaire.persistence.domain.entities.User;
import com.kyrylov.questionnaire.persistence.domain.entities.User_;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import com.kyrylov.questionnaire.util.dto.UserDto;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
        User user = createUserWithEmail("testGetByField");

        DaoManager.getSession().evict(user);
        Assertions.assertFalse(DaoManager.getSession().contains(user), "User still in session");
        Optional<User> userByField = DaoManager.getByField(User.class, User_.EMAIL, user.getEmail());
        Assertions.assertTrue(userByField.isPresent(), "User was not found");
        Assertions.assertEquals(user.getEmail(), userByField.get().getEmail(), "Users are different");

        Assertions.assertFalse(DaoManager.getByField(User.class, User_.EMAIL, "empty").isPresent(),
                "User was found? WTF?");
    }

    @Test
    void testBracketsException() {
        Assertions.assertThrows(DatabaseException.class, () -> DaoManager.select(Field.class).where().openBracket().list());
        Assertions.assertThrows(DatabaseException.class, () -> DaoManager.select(Field.class).where().closeBracket().list());
    }

    @Test
    void dtoSelect() throws DatabaseException {
        User user = createUserWithEmail("dtoSelect");

        Optional<UserDto> userDto = DaoManager.select(User.class, UserDto.class)
                .where().equal(User_.EMAIL, user.getEmail()).readonly().singleResult();

        Assertions.assertTrue(userDto.isPresent(), "Dto was not created");
        Assertions.assertEquals(user.getEmail(), userDto.get().getEmail(), "Emails are different");
    }

    @Test
    void readonly() throws DatabaseException {
        User user = createUserWithEmail("readonly");

        DaoManager.getSession().evict(user);
        Assertions.assertFalse(DaoManager.getSession().contains(user), "User still in session");

        Optional<User> user1Optional = DaoManager.select(User.class).readonly().singleResult();
        Assertions.assertTrue(user1Optional.isPresent(), "User was not found");
        User user1 = user1Optional.get();

        user1.setEmail("test2");
        DaoManager.save(user1, true);

        DaoManager.getSession().evict(user1);
        Assertions.assertFalse(DaoManager.getSession().contains(user1), "User still in session");

        User user2 = DaoManager.get(User.class, user1.getId());
        Assertions.assertNotNull(user2, "User was not found");

        Assertions.assertEquals(user.getEmail(), user2.getEmail(), "Emails are not equal");
        Assertions.assertNotEquals(user1.getEmail(), user2.getEmail(), "Emails are equal");
    }

    private User createUserWithEmail(String uniqueEmail) throws DatabaseException {
        User user = new User();
        user.setEmail(uniqueEmail);
        DaoManager.save(user, true);
        Assertions.assertNotNull(user.getId(), "User was not saved");
        return user;
    }

    @Test
    void getCount() throws DatabaseException {
        Optional<Long> count1 = DaoManager.getCount(Field.class).singleResult();
        Assertions.assertTrue(count1.isPresent(), "Count is empty");

        Field field = new Field();
        field.setLabel("fieldTest1");
        DaoManager.save(field, true);
        Assertions.assertNotNull(field.getId(), "Field was not saved");

        Optional<Long> count2 = DaoManager.getCount(Field.class).singleResult();
        Assertions.assertTrue(count2.isPresent(), "Count is empty");
        Assertions.assertEquals(count1.get() + 1, count2.get(), "Wrong count of fields after saving");
    }

    @Test
    @DisplayName("Select testing")
    void testSelect() throws DatabaseException {
        Field field = new Field();
        Option option = new Option();

        field.setLabel("fieldTest");
        option.setText("optionTest");
        option.setField(field);
        field.setOptions(Collections.singleton(option));

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