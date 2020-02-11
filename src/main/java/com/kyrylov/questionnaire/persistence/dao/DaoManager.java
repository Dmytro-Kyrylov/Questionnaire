package com.kyrylov.questionnaire.persistence.dao;

import com.kyrylov.questionnaire.persistence.dao.builders.JoinBuilder;
import com.kyrylov.questionnaire.persistence.domain.IndexedEntity;
import com.kyrylov.questionnaire.persistence.domain.IndexedEntity_;
import com.kyrylov.questionnaire.persistence.domain.interfaces.IEntity;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import com.kyrylov.questionnaire.persistence.util.HibernateUtil;
import com.kyrylov.questionnaire.persistence.util.SessionManager;
import com.kyrylov.questionnaire.util.dto.IDto;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * Main class to work with database
 *
 * @author Dmitrii
 */
@Slf4j
public final class DaoManager {

    /**
     * Database operations
     */
    private enum Operation {
        SAVE, DELETE
    }

    private static final SessionManager SESSION_MANAGER = HibernateUtil.getSessionManager();

    /**
     * Return session from SessionManager {@link SessionManager}
     *
     * @return hibernate session
     */
    public static Session getSession() {
        return SESSION_MANAGER.getSessionHolder().getSession();
    }

    /**
     * Get indexed entity by its id
     *
     * @param tClass entity class
     * @param id     id of the entity to search
     * @param <T>    entity class type
     * @return entity related to the id argument
     * @throws DatabaseException if an any exceptions occurs
     */
    public static <T extends IndexedEntity> T get(Class<T> tClass, Long id) throws DatabaseException {
        try {
            Session session = getSession();
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<T> criteria = criteriaBuilder.createQuery(tClass);
            Root<T> root = criteria.from(tClass);

            criteria.where(criteriaBuilder.equal(root.get(IndexedEntity_.ID), id));
            criteria.select(root);
            Query<T> query = session.createQuery(criteria);
            return query.getSingleResult();
        } catch (Exception e) {
            throw new DatabaseException("Error when trying to get entity from database", e);
        }
    }

    /**
     * Get an entity with a specific field value
     *
     * @param tClass entity class
     * @param field  field of the entity through which the search will be conducted
     * @param value  value to find
     * @param <T>    entity class type
     * @return entity with the passed field value or null if no such entity in database
     * @throws DatabaseException if an any exceptions occurs
     */
    public static <T extends IEntity> T getByField(Class<T> tClass, String field, Object value) throws DatabaseException {
        try {
            Session session = getSession();
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<T> criteria = criteriaBuilder.createQuery(tClass);
            Root<T> root = criteria.from(tClass);

            criteria.where(criteriaBuilder.equal(root.get(field), value));
            criteria.select(root);

            Query<T> query = session.createQuery(criteria).setMaxResults(1);

            return query.getResultList().stream().findFirst().orElse(null);
        } catch (Exception e) {
            throw new DatabaseException("Error when trying to get entity from database", e);
        }
    }

    /**
     * A method for loading entities from a database with conditions that will be set in the corresponding builders.
     * The result will be obtained at {@link DaoManager#getList(QueryConfigurator)}
     *
     * @param tClass entity class
     * @return JoinBuilder.class that allow to configure joins, {@link JoinBuilder}
     * @throws DatabaseException if an any exceptions occurs
     */
    public static <T extends IEntity> JoinBuilder<T, T> select(Class<T> tClass) throws DatabaseException {
        try {
            Session session = getSession();
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<T> criteria = criteriaBuilder.createQuery(tClass);
            Root<T> root = criteria.from(tClass);

            criteria.select(root);

            return new JoinBuilder<>(tClass, session, criteriaBuilder, criteria, root);
        } catch (Exception e) {
            throw new DatabaseException("Error when trying to load entities from database", e);
        }
    }

    /**
     * A method for getting a count of entities from a database with conditions that will be set in the corresponding builders.
     * The result will be obtained at {@link DaoManager#getList(QueryConfigurator)}
     *
     * @param tClass entity class
     * @return JoinBuilder.class that allow to configure joins, {@link JoinBuilder}
     * @throws DatabaseException if an any exceptions occurs
     */
    public static <T extends IEntity> JoinBuilder<T, Long> getCount(Class<T> tClass) throws DatabaseException {
        try {
            Session session = getSession();
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Long> criteria = criteriaBuilder.createQuery(Long.class);
            Root<T> root = criteria.from(tClass);

            criteria.select(criteriaBuilder.count(root));

            return new JoinBuilder<>(tClass, session, criteriaBuilder, criteria, root);
        } catch (Exception e) {
            throw new DatabaseException("Error when trying to get count of entities from database", e);
        }
    }

    /**
     * Allow to load entities from a database as DTO objects with conditions that will be set in the corresponding builders.
     * The result will be obtained at {@link DaoManager#getList(QueryConfigurator)}
     * <p>
     * Loads all attributes that are defined as fields in the DTO class and are not static or transient.
     *
     * @param entityClass class of mapped by JPA entity
     * @param dtoClass    simple DTO class for entity class
     * @param <T>         entity class type
     * @param <D>         DTO class type
     * @return JoinBuilder.class that allow to configure joins, {@link JoinBuilder}
     * @throws DatabaseException if an any exceptions occurs
     */
    public static <T extends IEntity, D extends IDto> JoinBuilder<T, D> select(Class<T> entityClass, Class<D> dtoClass)
            throws DatabaseException {
        try {
            Session session = getSession();
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<D> criteria = criteriaBuilder.createQuery(dtoClass);
            Root<T> root = criteria.from(entityClass);

            List<Selection<?>> fields = new LinkedList<>();
            for (Field field : dtoClass.getDeclaredFields()) {
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())
                        && !java.lang.reflect.Modifier.isTransient(field.getModifiers())) {
                    fields.add(root.get(field.getName()));
                }
            }

            criteria.multiselect(fields);

            return new JoinBuilder<>(entityClass, session, criteriaBuilder, criteria, root);
        } catch (Exception e) {
            throw new DatabaseException("Error when trying to get count of entities from database", e);
        }
    }

    /**
     * Method to get list result of query
     *
     * @param queryConfigurator special class that contained all conditions for database query
     * @param <T>               searched entity class
     * @param <L>               result class type
     * @return list of results
     * @throws DatabaseException if an any exceptions occurs
     */
    static <T extends IEntity, L extends Serializable> List<L> getList(QueryConfigurator<T, L> queryConfigurator) throws DatabaseException {
        try {
            if (queryConfigurator.getPredicate() != null) {
                queryConfigurator.getCriteriaQuery().where(queryConfigurator.getPredicate());
            }
            if (queryConfigurator.getOrders() != null && queryConfigurator.getOrders().size() != 0) {
                queryConfigurator.getCriteriaQuery().orderBy(queryConfigurator.getOrders());
            }

            Query<L> query = queryConfigurator.getSession().createQuery(queryConfigurator.getCriteriaQuery());

            query.setFirstResult(queryConfigurator.getOffset());

            if (queryConfigurator.getLimit() != 0) {
                query.setMaxResults(queryConfigurator.getLimit());
            }

            query.setReadOnly(queryConfigurator.isReadonly());

            return query.getResultList();
        } catch (Exception e) {
            throw new DatabaseException("Error when trying to load entities from database", e);
        }
    }

    /**
     * Method to get single result of query
     *
     * @param queryConfigurator special class that contained all conditions for database query
     * @param <T>               searched entity class
     * @param <L>               result class type
     * @return single result or null
     * @throws DatabaseException if an any exceptions occurs
     */
    static <T extends IEntity, L extends Serializable> L getSingleResult(QueryConfigurator<T, L> queryConfigurator) throws DatabaseException {
        return getList(queryConfigurator).stream().findFirst().orElse(null);
    }

    public static <T extends IEntity> void save(T entity)
            throws DatabaseException {
        databaseOperation(entity, false, Operation.SAVE);
    }

    public static <T extends IEntity> void delete(T entity)
            throws DatabaseException {
        databaseOperation(entity, false, Operation.DELETE);
    }

    public static <T extends IEntity> void save(T entity, boolean beginTransaction)
            throws DatabaseException {
        databaseOperation(entity, beginTransaction, Operation.SAVE);
    }

    public static <T extends IEntity> void delete(T entity, boolean beginTransaction)
            throws DatabaseException {
        databaseOperation(entity, beginTransaction, Operation.DELETE);
    }

    /**
     * Method to work with single entity by according operation
     *
     * @param entity           entity class
     * @param beginTransaction flag to begin or not transaction
     * @param operation        database operation {@link Operation}
     * @param <T>              entity class type
     * @throws DatabaseException if an any exceptions occurs
     */
    private static <T> void databaseOperation(T entity, boolean beginTransaction, Operation operation)
            throws DatabaseException {
        Session session = getSession();
        Transaction tr = null;
        try {
            if (beginTransaction) {
                tr = session.beginTransaction();
            }
            switch (operation) {
                case SAVE:
                    if (session.contains(entity)) {
                        //noinspection unchecked,UnusedAssignment
                        entity = (T) session.merge(entity);
                    } else {
                        session.saveOrUpdate(entity);
                    }
                    break;
                case DELETE:
                    session.delete(entity);
                    break;
            }
        } catch (Exception e) {
            if (beginTransaction) {
                rollbackTransaction(tr);
            }
            throw new DatabaseException("Error when trying to process entity", e);
        } finally {
            if (beginTransaction) {
                commitTransaction(tr);
            }
        }
    }

    /**
     * Refresh state of passed entity according database
     *
     * @param object entity class
     * @param <T>    type of entity class
     * @throws DatabaseException if an any exceptions occurs
     */
    public static <T extends IEntity> void refresh(T object) throws DatabaseException {
        Transaction tr = null;
        try {
            if (DaoManager.getSession().isOpen()) {
                tr = DaoManager.getSession().getTransaction();
            } else {
                tr = DaoManager.getSession().beginTransaction();
            }
            getSession().refresh(object);
        } catch (Exception e) {
            rollbackTransaction(tr);
        } finally {
            commitTransaction(tr);
        }
    }

    public static void beginTransaction() throws DatabaseException {
        try {
            getSession().beginTransaction();
        } catch (Exception e) {
            throw new DatabaseException("Error when trying to start transaction", e);
        }
    }

    public static void commitTransaction() throws DatabaseException {
        Transaction tr = null;
        try {
            if (DaoManager.getSession().isOpen()) {
                tr = DaoManager.getSession().getTransaction();
            }
            commitTransaction(tr);
        } catch (Exception e) {
            try {
                rollbackTransaction(tr);
            } catch (DatabaseException ex) {
                throw new DatabaseException("Error when trying to rollback transaction after failure commit", e);
            }
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    public static void rollbackTransaction() {
        Transaction tr = null;
        try {
            if (DaoManager.getSession().isOpen()) {
                tr = DaoManager.getSession().getTransaction();
            }
            rollbackTransaction(tr);
        } catch (DatabaseException e) {
            log.error(e.getMessage(), e);
        }
    }

    private static void commitTransaction(Transaction tr) throws DatabaseException {
        try {
            if (tr != null && tr.isActive() && !tr.getStatus().equals(TransactionStatus.ROLLED_BACK)) {
                tr.commit();
            }
        } catch (Exception e) {
            throw new DatabaseException("Error when trying to commit transaction", e);
        }
    }

    private static void rollbackTransaction(Transaction tr) throws DatabaseException {
        try {
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
        } catch (Exception ex) {
            throw new DatabaseException("Error when trying to rollback transaction", ex);
        }
    }

}
