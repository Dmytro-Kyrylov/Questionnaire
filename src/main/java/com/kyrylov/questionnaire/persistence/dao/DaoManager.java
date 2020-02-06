package com.kyrylov.questionnaire.persistence.dao;

import com.kyrylov.questionnaire.persistence.dao.builders.JoinBuilder;
import com.kyrylov.questionnaire.persistence.domain.IndexedEntity;
import com.kyrylov.questionnaire.persistence.domain.IndexedEntity_;
import com.kyrylov.questionnaire.persistence.domain.entities.Field;
import com.kyrylov.questionnaire.persistence.domain.entities.Field_;
import com.kyrylov.questionnaire.persistence.domain.interfaces.IEntity;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import com.kyrylov.questionnaire.persistence.util.HibernateUtil;
import com.kyrylov.questionnaire.persistence.util.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
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
     * @return session that linked to user httpSession or new session if no httpSession exist
     */
    public static Session getSession() {
        return SESSION_MANAGER.getSessionHolder().getSession();
    }

    /**
     * @param tClass entity class
     * @param id     id of the entity to search
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
     * A method for loading a list of entities from a database with conditions that will be set in the corresponding builders.
     * The result will be obtained at {@link DaoManager#select(QueryConfigurator)}
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
     * The result will be obtained at {@link DaoManager#select(QueryConfigurator)}
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
     * Method to get result of query
     *
     * @param queryConfigurator special class that contained all conditions for database query
     * @param <T>               searched entity class
     * @param <L>               result class type
     * @return list of results
     * @throws DatabaseException if an any exceptions occurs
     */
    static <T extends IEntity, L extends Serializable> List<L> select(QueryConfigurator<T, L> queryConfigurator) throws DatabaseException {
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

            return query.getResultList();
        } catch (Exception e) {
            throw new DatabaseException("Error when trying to load entities from database", e);
        }
    }

    public static void main(String[] args) throws DatabaseException {
        System.out.println(DaoManager.getCount(Field.class).where().openBracket().not().isNull(Field_.ACTIVE).and().not().isNull(Field_.ID).closeBracket().execute().get(0));
        ;
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
                        //noinspection unchecked
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
