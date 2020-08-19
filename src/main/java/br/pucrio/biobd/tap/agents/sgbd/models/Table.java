	/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models;

import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author Rafael
 */
public class Table {

    private String name;
    private String schema;
    private long numberRows;
    private long numberPages;
    private final ArrayList<Column> fields;

    public long getNumberPages() {
        return numberPages;
    }

    public void setNumberPages(long numberPages) {
        this.numberPages = numberPages;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public long getNumberRows() {
        return numberRows;
    }

    public void setNumberRows(long numberRows) {
        this.numberRows = numberRows;
    }

    public Table() {
        this.fields = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Column> getFields() {
        ArrayList<Column> copyOfColumns = new ArrayList<>();
        for (Column field : fields) {
            copyOfColumns.add(Column.clone(field));
        }
        return copyOfColumns;
    }

    public String getFieldsString() {
        String result = "";
        result = fields.stream().map((field) -> field.getName() + ", ").reduce(result, String::concat);
        return result;
    }

    public void setFields(ArrayList<Column> fields) {
        for (Column field : fields) {
            if (!this.containsField(field)) {
                this.fields.add(field);
            }
        }
    }

    private boolean containsField(Column field) {
        for (Column column : fields) {
            if (column.equals(field)) {
                return true;
            }
        }
        return false;
    }

    public Object getValue(String dataType) {
        switch (dataType) {
            case "temNome":
                return this.getName();
            case "temNumeroTuplas":
                return this.getNumberRows();
            case "temNumeroPaginas":
                return this.getNumberPages();
            default:
                return null;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.name);
        hash = 17 * hash + Objects.hashCode(this.schema);
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
        final Table other = (Table) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.schema, other.schema)) {
            return false;
        }
        return true;
    }

}
