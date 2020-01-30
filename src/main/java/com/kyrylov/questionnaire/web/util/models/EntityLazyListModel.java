package com.kyrylov.questionnaire.web.util.models;

import com.kyrylov.questionnaire.persistence.dao.DaoManager;
import com.kyrylov.questionnaire.persistence.dao.QueryConfigurator;
import com.kyrylov.questionnaire.persistence.dao.builders.ConditionBuilder;
import com.kyrylov.questionnaire.persistence.dao.builders.JoinBuilder;
import com.kyrylov.questionnaire.persistence.dao.builders.OrderBuilder;
import com.kyrylov.questionnaire.persistence.domain.IndexedEntity;
import com.kyrylov.questionnaire.persistence.util.DatabaseException;
import com.kyrylov.questionnaire.web.beans.BaseLazyEntityModelBean;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Slf4j
@Getter
@Setter
public class EntityLazyListModel<T extends IndexedEntity> extends LazyDataModel<T> {

    private static final long serialVersionUID = 7903049714224380806L;

    private Class<T> entityClass;

    private int rowIndex = -1;

    private int totalNumRows;

    private List<T> list;

    private Set<String> aliasesWithAttributesSequence;

    private boolean calculated;

    private String entityAlias;

    private Function<JoinBuilder<T, ?>, JoinBuilder<T, ?>> joinsFunc;
    private Function<ConditionBuilder<T, ?>, ConditionBuilder<T, ?>> restrictionsFunc;
    private Function<OrderBuilder<T, ?>, OrderBuilder<T, ?>> ordersFunc;

    public EntityLazyListModel(Class<T> entityClass, Function<JoinBuilder<T, ?>, JoinBuilder<T, ?>> joins,
                               Function<ConditionBuilder<T, ?>, ConditionBuilder<T, ?>> restrictions,
                               Function<OrderBuilder<T, ?>, OrderBuilder<T, ?>> orders) {
        super();
        this.entityClass = entityClass;
        this.joinsFunc = joins;
        this.ordersFunc = orders;
        this.restrictionsFunc = restrictions;
    }

    @Override
    public Object getRowKey(T entity) {
        return entity.getId();
    }

    /**
     * Uses all restrictions that was specified in {@link BaseLazyEntityModelBean#loadList(Class, Function, Function, Function)} method,
     * and add additional restrictions from page if they are exist
     *
     * @param joinBuilder empty builder
     * @param sortField   page sorted field
     * @param sortOrder   sort order
     * @param filters     filters from page
     * @return configured database query {@link QueryConfigurator}
     * @throws DatabaseException if any exception occurs
     */
    @SuppressWarnings("JavadocReference")
    private QueryConfigurator<T, ?> getPreparedQuery(JoinBuilder<T, ? extends Serializable> joinBuilder, String sortField,
                                                     SortOrder sortOrder, Map<String, Object> filters)
            throws DatabaseException {
        try {
            joinBuilder = getJoinsFunc().apply(joinBuilder);

            ConditionBuilder<T, ?> conditionBuilder = getRestrictionsFunc().apply(joinBuilder.where());
            addMoreRestrictions(conditionBuilder, filters);

            OrderBuilder<T, ?> orderBuilder = getOrdersFunc().apply(conditionBuilder.orderBy());
            if (sortField != null) {
                addOrder(orderBuilder, sortField, sortOrder);
            }
            return new QueryConfigurator<>(orderBuilder);
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage(), e);
        }
    }


    /**
     * Lazy model load method.
     * Loads a certain number of objects with restrictions, and also counts the number of all objects with these restrictions
     *
     * @param first     offset of database select query
     * @param pageSize  size of list lo load
     * @param sortField page sorted field
     * @param sortOrder sort order
     * @param filters   page filters
     * @return list of objects with all restrictions in a certain range
     */
    @Override
    public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        try {
            JoinBuilder<T, T> joinBuilder = DaoManager.select(getEntityClass());

            QueryConfigurator<T, T> queryConfigurator =
                    (QueryConfigurator<T, T>) getPreparedQuery(joinBuilder, sortField, sortOrder, filters);

            queryConfigurator.setOffset(first);
            queryConfigurator.setLimit(pageSize);
            setEntityAlias(queryConfigurator.getMainEntityAlias());

            if (this.aliasesWithAttributesSequence == null) {
                this.aliasesWithAttributesSequence = queryConfigurator.getAliasesWithAttributesSequence();
            }

            this.list = queryConfigurator.execute();

            JoinBuilder<T, Long> joinBuilderForCount = DaoManager.getCount(getEntityClass());

            QueryConfigurator<T, Long> queryConfiguratorForCount =
                    (QueryConfigurator<T, Long>) getPreparedQuery(joinBuilderForCount, null, null, filters);

            this.setRowCount(queryConfiguratorForCount.execute().get(0).intValue());
        } catch (Exception e) {
            log.error("", e);
        }

        this.calculated = true;
        return list;
    }

    @Override
    public void setRowCount(int rowCount) {
        this.totalNumRows = rowCount;
        super.setRowCount(rowCount);
    }

    /**
     * Adds additional order to database query based on page order
     *
     * @param orderBuilder order builder {@link OrderBuilder}
     * @param sortField    filed to sort
     * @param sortOrder    sort order
     * @param <L>          result class type
     */
    private <L extends Serializable> void addOrder(OrderBuilder<T, L> orderBuilder, String sortField, SortOrder sortOrder) {
        if (sortField != null) {
            switch (sortOrder) {
                case ASCENDING:
                    orderBuilder.asc(sortField, getAliasForSequenceOfAttributes(sortField));
                case DESCENDING:
                    orderBuilder.desc(sortField, getAliasForSequenceOfAttributes(sortField));
            }
        }
    }

    /**
     * Adds additional restrictions to database query based on filters from page
     *
     * @param conditionBuilder condition builder {@link ConditionBuilder}
     * @param filters          filters from page
     * @param <L>              result class type
     */
    private <L extends Serializable> void addMoreRestrictions(ConditionBuilder<T, L> conditionBuilder,
                                                              Map<String, Object> filters) {
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            String attributeToSearch = entry.getKey();
            if (entry.getKey().contains(".")) {
                attributeToSearch = entry.getKey().substring(entry.getKey().lastIndexOf(".") + 1);
            }
            String alias = getAliasForSequenceOfAttributes(entry.getKey());
            Class aClass = getClassOfFieldFromSequence(entry.getKey());
            if (aClass.isEnum()) {
                try {
                    //noinspection unchecked
                    Enum value = Enum.valueOf(aClass, entry.getValue().toString().toUpperCase());
                    conditionBuilder.equal(attributeToSearch, value, alias);
                } catch (Exception e) {
                    conditionBuilder.isNull(attributeToSearch, alias);
                }
            } else {
                conditionBuilder.like(attributeToSearch, (String) entry.getValue(), alias);
            }
        }
    }

    /**
     * Search correct alis for database request
     *
     * @param sequence sequence of attributes from root entity.
     *                 Example response(Model`s root entity).user(entity`s attribute, that define some relationship(1->M,N->M,1->1)).id(final attribute to search)
     * @return correct alias for searching field
     * @throws IllegalArgumentException if passed sequence was not found in current database aliases {@link #getAliasesWithAttributesSequence()}
     */
    private String getAliasForSequenceOfAttributes(String sequence) throws IllegalArgumentException {

        if (sequence.contains(".")) {
            String correctSeq = sequence.substring(0, sequence.lastIndexOf("."));
            for (String s : getAliasesWithAttributesSequence()) {
                String[] attrs = s.substring(s.indexOf(".") + 1).split("\\.");
                StringBuilder sb = new StringBuilder();
                for (int i = attrs.length - 1; i >= 0; i--) {
                    sb.append(attrs[i]);
                }
                if (sb.toString().equals(correctSeq)) {
                    return s.substring(0, s.indexOf("."));
                }
            }

            throw new IllegalArgumentException("Maybe you should join table - " + sequence);
        }
        return getEntityAlias();
    }

    /***
     * Allows you to define a field class to make the correct database query
     *
     * @param sequence sequence of attributes from root entity.
     *                 Example response(Model`s root entity).user(entity`s attribute, that define some relationship(1->M,N->M,1->1)).id(final attribute to search)
     * @return class of final attribute from sequence
     */
    private Class getClassOfFieldFromSequence(String sequence) {
        String[] fields = sequence.split("\\.");
        Class clazz = getEntityClass();
        for (String field : fields) {
            try {
                clazz = clazz.getDeclaredField(field).getType();
            } catch (NoSuchFieldException e) {
                log.info(e.getMessage());
                return getEntityClass();
            }
        }
        return clazz;
    }

    public boolean isRowAvailable() {
        if (getList() == null) {
            return false;
        }

        int rowIndex = getRowIndex();
        if (rowIndex >= 0 && rowIndex < getList().size()) {
            return true;
        } else {
            return false;
        }
    }

    public int getRowCount() {
        if (!this.calculated) {
            try {
                JoinBuilder<T, Long> builder = DaoManager.getCount(getEntityClass());
                ConditionBuilder<T, ?> conditionBuilder = getJoinsFunc().apply(builder).where();
                Long count = (Long) getRestrictionsFunc().apply(conditionBuilder).execute().get(0);
                this.totalNumRows = count.intValue();
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return this.totalNumRows;
    }

    public T getRowData() {
        if (getList() == null || getList().size() == 0) {
            return null;
        } else if (!isRowAvailable()) {
            throw new IllegalArgumentException();
        } else {
            int dataIndex = getRowIndex();

            if (dataIndex >= 0) {
                return getList().get(dataIndex);
            } else {
                return null;
            }
        }
    }

    @Override
    public T getRowData(String rowKey) {
        try {
            return DaoManager.get(getEntityClass(), Long.parseLong(rowKey));
        } catch (DatabaseException e) {
            log.error("", e);
        }
        return null;
    }

    public int getRowIndex() {
        if (getPageSize() != 0) {
            return (this.rowIndex % getPageSize());
        } else {
            return 0;
        }
    }

}
