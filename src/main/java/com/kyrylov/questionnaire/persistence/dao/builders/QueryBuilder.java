package com.kyrylov.questionnaire.persistence.dao.builders;

import com.kyrylov.questionnaire.persistence.dao.QueryConfigurator;
import com.kyrylov.questionnaire.persistence.domain.interfaces.IEntity;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Main class to store all required data of database query
 *
 * @param <T> entity class type
 * @param <L> result class type
 * @author Dmitrii
 */
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PACKAGE)
public abstract class QueryBuilder<T extends IEntity, L extends Serializable> {

    private boolean readonly;

    private Predicate predicate;
    private List<Order> orders;
    private Map<String, From<? extends IEntity, ? extends IEntity>> fromMap;

    private Class<T> entityClass;
    private CriteriaBuilder cb;
    private CriteriaQuery<L> criteriaQuery;
    private Root<T> root;
    private Session session;

    /**
     * Initial constructor for new query. Should be called only on creating new query
     *
     * @param tClass          entity class
     * @param session         hibernate session
     * @param criteriaBuilder hibernate criteria builder
     * @param criteria        hibernate criteria query
     * @param root            entity root
     */
    QueryBuilder(Class<T> tClass, Session session, CriteriaBuilder criteriaBuilder, CriteriaQuery<L> criteria, Root<T> root) {
        init(tClass, session, criteriaBuilder, criteria, root);
        this.fromMap = new HashMap<>();
        this.fromMap.put(tClass.getSimpleName(), root);
    }

    /**
     * Constructor that used to create new builder from existing. Should be called from another builder
     *
     * @param queryBuilder existing builder
     */
    protected QueryBuilder(QueryBuilder<T, L> queryBuilder) {
        init(queryBuilder.getEntityClass(), queryBuilder.getSession(), queryBuilder.getCb(),
                queryBuilder.getCriteriaQuery(), queryBuilder.getRoot());
        this.fromMap = queryBuilder.getFromMap();
        this.predicate = queryBuilder.getPredicate();
        this.orders = queryBuilder.getOrders();
    }

    private void init(Class<T> tClass, Session session, CriteriaBuilder criteriaBuilder, CriteriaQuery<L> criteria, Root<T> root) {
        this.cb = criteriaBuilder;
        this.root = root;
        this.entityClass = tClass;
        this.criteriaQuery = criteria;
        this.session = session;
    }

    public QueryBuilder<T, L> readonly() {
        setReadonly(true);
        return this;
    }

    /**
     * Create new instance of configuration class and execute query
     * {@link QueryConfigurator}
     *
     * @return list of results
     * @throws DatabaseException if any exception occurs
     */
    public List<L> list() throws DatabaseException {
        return new QueryConfigurator<>(this).list();
    }

    /**
     * Create new instance of configuration class and execute query
     * {@link QueryConfigurator}
     *
     * @return single result
     * @throws DatabaseException if any exception occurs
     */
    public L singleResult() throws DatabaseException {
        return new QueryConfigurator<>(this).singleResult();
    }

}