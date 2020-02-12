package com.kyrylov.questionnaire.persistence.dao.builders;

import com.kyrylov.questionnaire.persistence.domain.interfaces.IEntity;
import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import java.io.Serializable;

/**
 * First builder that created for configuring query. Allow to add joins
 * <p>
 * All joins stored in {@link QueryBuilder#getFromMap()} map. Main entity also has own alias - it is a simpleName of class
 *
 * @param <T> entity class type
 * @param <L> result class type
 * @author Dmitrii
 */
public class JoinBuilder<T extends IEntity, L extends Serializable> extends QueryBuilder<T, L> {

    public JoinBuilder(Class<T> tClass, Session session, CriteriaBuilder criteriaBuilder, CriteriaQuery<L> criteria, Root<T> root) {
        super(tClass, session, criteriaBuilder, criteria, root);
    }

    public JoinBuilder<T, L> innerJoin(String tableAttribute, String alias) {
        return join(this.getRoot(), tableAttribute, alias, JoinType.INNER, false);
    }

    public JoinBuilder<T, L> innerJoin(String tableAttribute, String alias, boolean fetch) {
        return join(this.getRoot(), tableAttribute, alias, JoinType.INNER, fetch);
    }

    public JoinBuilder<T, L> innerJoin(String mainTable, String tableAttribute, String alias) {
        return join(getFromMap().get(mainTable), tableAttribute, alias, JoinType.INNER, false);
    }

    public JoinBuilder<T, L> innerJoin(String mainTable, String tableAttribute, String alias, boolean fetch) {
        return join(getFromMap().get(mainTable), tableAttribute, alias, JoinType.INNER, fetch);
    }

    public JoinBuilder<T, L> leftJoin(String tableAttribute, String alias) {
        return join(this.getRoot(), tableAttribute, alias, JoinType.LEFT, false);
    }

    public JoinBuilder<T, L> leftJoin(String tableAttribute, String alias, boolean fetch) {
        return join(this.getRoot(), tableAttribute, alias, JoinType.LEFT, fetch);
    }

    public JoinBuilder<T, L> leftJoin(String mainTable, String tableAttribute, String alias) {
        return join(getFromMap().get(mainTable), tableAttribute, alias, JoinType.LEFT, false);
    }

    public JoinBuilder<T, L> leftJoin(String mainTable, String tableAttribute, String alias, boolean fetch) {
        return join(getFromMap().get(mainTable), tableAttribute, alias, JoinType.LEFT, fetch);
    }

    private <X extends IEntity> JoinBuilder<T, L> join(From<X, ? extends IEntity> from, String tableAttribute,
                                                       String alias, JoinType joinType, boolean fetch) {
        Join<X, ? extends IEntity> join;
        if (fetch) {
            //noinspection unchecked
            join = (Join) from.fetch(tableAttribute, joinType);
        } else {
            join = from.join(tableAttribute, joinType);
        }
        join.alias(alias);
        getFromMap().put(alias, join);
        return this;
    }

    public ConditionBuilder<T, L> where() {
        return new ConditionBuilder<>(this);
    }

    public OrderBuilder<T, L> orderBy() {
        return new OrderBuilder<>(this);
    }

}
