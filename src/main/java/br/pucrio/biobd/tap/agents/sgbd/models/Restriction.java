/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models;

import br.pucrio.biobd.tap.agents.libraries.Log;
import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author Rafael
 */
public class Restriction {

    private final Column column;
    private final String operator;
    private final Object value;
    private long numRows;

    public long getNumRows() {
        return numRows;
    }

    public void setNumRows(long numRows) {
        this.numRows = numRows;
    }

    public Column getColumn() {
        return column;
    }

    public String getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }

    public String getFormatedValue(boolean isGlobal) {
        if (this.getValue() instanceof Column) {
            if (isGlobal) {
                return this.getColumn().getAlias() + " " + this.getOperator() + " " + ((Column) this.getValue()).getAlias();
            } else {
                return this.getColumn().getCompleteName() + " " + this.getOperator() + " " + ((Column) this.getValue()).getCompleteName();
            }
        } else {
            if (isGlobal) {
                return this.getColumn().getAlias() + " " + this.getOperator() + " " + this.getValue();
            } else {
                return this.getColumn().getCompleteName() + " " + this.getOperator() + " " + this.getValue();
            }
        }
    }

    public Restriction(Column column, String operator, Object value) {
        this.column = column;
        this.operator = operator;
        this.value = value;
    }

    public void print() {
        if (this.getValue() instanceof Column) {
            Log.msg(column.getCompleteName() + " " + operator + " " + ((Column) value).getCompleteName());
        } else {
            Log.msg(column.getCompleteName() + " " + operator + " " + value);
        }

    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + Objects.hashCode(this.column);
        hash = 11 * hash + Objects.hashCode(this.operator);
        if (value instanceof Column) {
            hash = 11 * hash + Objects.hashCode((Column) this.value);
        } else {
            hash = 11 * hash + Objects.hashCode(this.value);
        }
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
        final Restriction other = (Restriction) obj;
        if (!Objects.equals(this.operator, other.operator)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        if (!Objects.equals(this.column, other.column)) {
            return false;
        }
        return true;
    }

    public String getCommentNameForPartialIndex() {
        if (this.getValue() instanceof Column) {
            return this.getColumn().getCompleteName() + "_" + ((Column) value).getCompleteName();
        } else {
            return this.getColumn().getCompleteName() + "_" + value.toString();
        }
    }

    public ArrayList<Table> getTables() {
        ArrayList<Table> tables = new ArrayList<>();
        tables.add(column.getTable());
        if (this.getValue() instanceof Column) {
            tables.add(((Column) this.getValue()).getTable());
        }
        return tables;
    }
}
