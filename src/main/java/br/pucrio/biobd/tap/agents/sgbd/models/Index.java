/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models;

import br.pucrio.biobd.tap.agents.sgbd.models.dao.IndexDAO;
import br.pucrio.biobd.tap.agents.sgbd.models.dao.TuningActionDAO;
import br.pucrio.biobd.tap.agents.sgbd.models.implementors.TuningActionImplementor;
import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author Rafael
 */
public class Index extends TuningAction {

    private int indId;
    private final ArrayList<Column> columns;
    private String typeColumn;
    private String hypotheticalPlan;
    private Table table;
    private String indexType;
    private boolean hasFilter;
    private String filterType;
    private long numberOfRows;
    private double selectivity;

    public String getHypotheticalPlan() {
        return hypotheticalPlan;
    }

    public void setHypotheticalPlan(String hypotheticalPlan) {
        this.hypotheticalPlan = hypotheticalPlan;
    }

    public String getTypeColumn() {
        return typeColumn;
    }

    public void setTypeColumn(String typeColumn) {
        this.typeColumn = typeColumn;
    }

    @Override
    public double getCost() {
        long seqScanCost = this.table.getNumberPages();
        return 2 * seqScanCost;

    }

    public String getIndexType() {
        return indexType;
    }

    public Index() {
        this.columns = new ArrayList<>();
    }

    @Override
    public String getName() {
        String indexName = "";
        if (this.table == null) {
            String[] index = this.getDdl().split(" ");
            for (int i = 0; i < index.length; i++) {
                if (index[i].toLowerCase().equals("index") && index[i - 1].toLowerCase().equals("create")) {
                    return index[i + 1];
                }
            }
        } else {
            indexName = table.getName() + "_" + this.getIndexType();
            for (int i = 0; i < columns.size(); i++) {
                indexName = indexName + "_" + columns.get(i).getName();
            }
            indexName = ("ID_TAP_" + this.getIndexType() + "_" + indexName.hashCode()).replace("-", "N");
        }
        return indexName;
    }

    @Override
    public String getDdl() {
        this.generateDDL();
        return super.getDdl();
    }

    @Override
    public int getId() {
        this.generateDDL();
        return super.getId();
    }

    private void generateDDL() {
        if (this.table != null && columns.size() > 0) {
            if ((columns.size() == 1) && (columns.get(0).getType().contains("CHAR"))) {
                this.generateBitmapIndex();
            } else {
                this.generateBtreeIndex();
            }
        }
    }

    public boolean getHasFilter() {
        return hasFilter;
    }

    public void setHasFilter(boolean hasFilter) {
        this.hasFilter = hasFilter;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public long getNumberOfRows() {
        return numberOfRows;
    }

    public void setNumberOfRows(long numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    public ArrayList<Column> getColumns() {
        return this.columns;
    }

    public void addColumn(Column field) {
        if (this.table == null) {
            this.table = field.getTable();
        }
        if (!this.containsColumns(field)) {
            this.columns.add(field);
        }
        this.generateDDL();
    }

    private boolean containsColumns(Column field) {
        for (Column column : columns) {
            if (column.equals(field)) {
                return true;
            }
        }
        return false;
    }

    public int getIndId() {
        return indId;
    }

    public void setIndId(int indId) {
        this.indId = indId;
    }

    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }

    @Override
    public void rating(String agent) {
    }

    @Override
    public void debug() {
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
        this.generateDDL();
    }

    public long getIndexScanCost() {
        long isCost = 0;
        long deepTree = 3;
        long numberOfTablePages = 1;
        long numberOfTableTuples = 1;
        numberOfTablePages += this.getTable().getNumberPages();
        numberOfTableTuples += this.getTable().getNumberRows();
        if (this.getHasFilter()) {
            if (this.getFilterType().equals("equi")) {
                if (this.getIndexType().equals("P")) {
                    isCost = deepTree + 1;
                } else {
                    isCost = deepTree + (this.getNumberOfRows() / ((numberOfTableTuples / numberOfTablePages) + 1));
                }
            } else if (this.getIndexType().equals("P")) {
                isCost = deepTree + (this.getNumberOfRows() / (numberOfTableTuples / numberOfTablePages));;
            } else {
                isCost = deepTree + this.getNumberOfRows();
            }
        }
        return isCost;
    }

    private void generateBtreeIndex() {
        String ddl = "CREATE INDEX "
                + this.getName()
                + " ON "
                + table.getName()
                + " (";
        String columnName = "";
        for (int i = 0; i < columns.size(); i++) {
            if (!columnName.isEmpty()) {
                columnName += ", ";
            }
            if (this.isGlobal()) {
                columnName += columns.get(i).getAlias();
            } else {
                columnName += columns.get(i).getName();
            }
        }
        ddl += columnName;
        ddl = ddl + ")";
        ddl += ";";
        this.setDdl(ddl);
    }

    private void generateBitmapIndex() {
        String ddl = "CREATE BITMAP INDEX "
                + this.getName()
                + " ON "
                + table.getName()
                + " (";
        String columnName;
        for (int i = 0; i < columns.size(); i++) {
            columnName = columns.get(i).getName();
            if (i == 0) {
                ddl = ddl + columnName + " ";
            } else {
                ddl = ddl + ", " + columnName + " ";
            }
        }
        ddl = ddl + ")";
        this.setDdl(ddl);
    }

    @Override
    public String getDdlToExecute() {
        return this.getDdl();
    }

    @Override
    public TuningActionDAO getInstanceDAO(String agent) {
        TuningActionDAO indexDao = new IndexDAO();
        indexDao.setAgent(agent);
        return indexDao;
    }

    @Override
    public boolean isValid() {
        return super.isValid()
                && hasValidIndexedColumnsAndRestrictions()
                && this.getCost() > 0
                && TuningActionImplementor.respectRestrictionsSGBD(this);
    }

    private boolean hasValidIndexedColumnsAndRestrictions() {
        if (this.isGlobal()) {
            for (Column column : columns) {
                if (!this.getLastSql().getFieldsQuery().contains(column)) {
                    return false;
                }
            }
            return true;
        } else {
            ArrayList<String> tableNames = new ArrayList<>();
            tableNames.add(table.getName());
            for (Column indexedColumn : this.getColumns()) {
                if (!tableNames.contains(indexedColumn.getTableName())) {
                    tableNames.add(indexedColumn.getTableName());
                }
            }
            return tableNames.size() == 1;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.columns);
        hash = 37 * hash + Objects.hashCode(this.table);
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
        final Index other = (Index) obj;
        if (!Objects.equals(this.columns, other.columns)) {
            return false;
        }
        if (!Objects.equals(this.table, other.table)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean selectedBySelectionHeuristic() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getSelectivity() {
        if (this.selectivity == 0) {
            IndexDAO indexDao = new IndexDAO();
            this.selectivity = indexDao.getSelectivity(this);
        }
        return this.selectivity;
    }

}
