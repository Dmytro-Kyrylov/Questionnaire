package com.kyrylov.questionnaire.persistence.dao;

import com.kyrylov.questionnaire.persistence.dao.builders.JoinBuilder;
import com.kyrylov.questionnaire.persistence.dao.builders.QueryBuilder;
import com.kyrylov.questionnaire.persistence.domain.interfaces.IEntity;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Session;
import org.hibernate.query.criteria.internal.path.AbstractJoinImpl;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Allow to add more special conditions to database query and also allow to execute it
 *
 * @param <T> entity class type
 * @param <L> result class type
 * @author Dmitrii
 */
@Getter
@Setter
public final class QueryConfigurator<T extends IEntity, L extends Serializable> extends QueryBuilder<T, L> {

    private int offset;
    private int limit;

    public QueryConfigurator(QueryBuilder<T, L> queryBuilder) {
        super(queryBuilder);
    }

    @Override
    public List<L> list() throws DatabaseException {
        return DaoManager.getList(this);
    }

    @Override
    public Optional<L> singleResult() throws DatabaseException {
        setLimit(1);
        return DaoManager.getSingleResult(this);
    }

    /**
     * Allow to get all aliases that was created by this query
     * {@link JoinBuilder}
     * alias string format ALIAS_NAME.CLASS_ATTRIBUTES_THAT_REFERS_TO_MAPPED_ENTITIES_SEPARATED_BY_POINT
     *
     * @return set of aliases
     */
    public Set<String> getAliasesWithAttributesSequence() {
        Set<String> aliasesWithAttributesSequence = new HashSet<>();
        for (Map.Entry<String, From<? extends IEntity, ? extends IEntity>> entry : getFromMap().entrySet()) {
            if (!entry.getKey().equals(getEntityClass().getSimpleName())) {
                StringBuilder sb = new StringBuilder(entry.getKey());

                Path join = entry.getValue();
                while (join.getJavaType() != getEntityClass()) {
                    sb.append(".").append(((AbstractJoinImpl) join).getAttribute().getName());
                    join = join.getParentPath();
                }
                aliasesWithAttributesSequence.add(sb.toString());
            }
        }
        return aliasesWithAttributesSequence;
    }

    public String getMainEntityAlias() {
        return super.getEntityClass().getSimpleName();
    }

    @Override
    protected Predicate getPredicate() {
        return super.getPredicate();
    }

    @Override
    protected List<Order> getOrders() {
        return super.getOrders();
    }

    @Override
    protected CriteriaQuery<L> getCriteriaQuery() {
        return super.getCriteriaQuery();
    }

    @Override
    protected Session getSession() {
        return super.getSession();
    }

    @Override
    protected boolean isReadonly() {
        return super.isReadonly();
    }
}
