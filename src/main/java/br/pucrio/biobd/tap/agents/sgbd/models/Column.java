/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models;

import java.util.Objects;

/**
 *
 * @author Rafael
 */
public class Column {

    private final String name;
    private Column foreignKey;
    private boolean primaryKey;
    private boolean uniqueKey;
    private final Table table;
    private boolean notNull;
    private String type;
    private int order;
    private String domainRestriction;
    private String alias;
    private double selectivity;

    public double getSelectivity() {
        return selectivity;
    }

    public void setSelectivity(double selectivity) {
        this.selectivity = selectivity;
    }

    public String getAlias() {
        if (this.hasAlias()) {
            return alias;
        } else {
            return this.getName();
        }
    }

    public boolean hasAlias() {
        return alias != null && !alias.isEmpty() && !alias.equals(name);
    }

    public static Column clone(Column columnToClone) {
        Column columnOut = new Column(columnToClone.getName(), columnToClone.getTable());
        columnOut.setOrder(columnToClone.getOrder());
        columnOut.setNotNull(columnToClone.isNotNull());
        columnOut.setType(columnToClone.getType());
        columnOut.setDomainRestriction(columnToClone.getDomainRestriction());
        columnOut.setPrimaryKey(columnToClone.isPrimaryKey());
        columnOut.setUniqueKey(columnToClone.isUniqueKey());
        columnOut.setSelectivity(columnToClone.getSelectivity());
        if (columnToClone.getForeignKey() != null) {
            columnOut.setForeignKey(Column.clone(columnToClone.getForeignKey()));
        }
        return columnOut;
    }

    public static Column createColumn(String name, Table table) {
        Column columnIn = new Column(name, table);
        return columnIn;
    }

    public void setAlias(String alias) {
        if (!this.hasAlias()) {
            this.alias = alias;
        }
    }

    public Column(String name, Table table) {
        this.name = name;
        this.table = table;
    }

    public String getDomainRestriction() {
        if (domainRestriction == null || domainRestriction.isEmpty()) {
            return "null";
        } else {
            return domainRestriction;
        }
    }

    public void setDomainRestriction(String domainRestriction) {
        this.domainRestriction = domainRestriction;
    }

    public boolean isUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(boolean uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public String getCompleteName() {
        return (table.getName() + "." + name).trim();
    }

    public Column getForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(Column foreignKey) {
        this.foreignKey = foreignKey;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getTableName() {
        return table.getName();
    }

    public Table getTable() {
        return table;
    }

    public String getType() {
        if (type == null) {
            return "N";
        } else {
            return type;
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Column other = (Column) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

}
