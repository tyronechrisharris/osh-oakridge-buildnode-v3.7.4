package org.sensorhub.impl.datastore.postgis.builder.generator;

import org.sensorhub.impl.datastore.postgis.builder.ParameterizedQuery;

import java.util.ArrayList;
import java.util.List;

public abstract class FilterQueryGenerator {
    protected List<String> addConditions;
    protected List<String> orConditions;
    protected long limit = -1;

    protected  String tableName;
    protected List<String> selectFields;
    protected List<String> joins;
    protected List<Object> parameters = new ArrayList<>();

    public void tableName(String tableName) {
        this.tableName = tableName;
    }

    public void setLimit(long limit){
        this.limit = limit;
    }

    protected void checkAddConditions() {
        if (this.addConditions == null) {
            this.addConditions = new ArrayList<>();
        }
    }

    protected void checkOrConditions() {
        if (this.orConditions == null) {
            this.orConditions = new ArrayList<>();
        }
    }

    protected void checkFields() {
        if (this.selectFields == null) {
            this.selectFields = new ArrayList<>();
        }
    }

    protected void checkJoins() {
        if (this.joins == null) {
            this.joins = new ArrayList<>();
        }
    }

    public void addCondition(String condition) {
        this.checkAddConditions();
        this.addConditions.add(condition);
    }

    public void addParameter(Object parameter) {
        this.parameters.add(parameter);
    }

    public void addParameters(List<?> parameters) {
        this.parameters.addAll(parameters);
    }

    public void addJoin(String join) {
        this.checkJoins();
        this.joins.add(join);
    }

    public void orCondition(String condition) {
        this.checkOrConditions();
        this.orConditions.add(condition);
    }

    public void addSelectField(String field) {
        this.checkFields();
        this.selectFields.add(field);
    }

    public void setSelectedFields(List<String> fields) {
        this.selectFields = fields;
    }

    public List<Object> getParameters() {
        return List.copyOf(parameters);
    }

    public ParameterizedQuery toParameterizedQuery() {
        return new ParameterizedQuery(toQuery(), getParameters());
    }

    public abstract String toQuery();
}
