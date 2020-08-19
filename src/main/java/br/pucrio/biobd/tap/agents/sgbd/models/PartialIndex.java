/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models;

import br.pucrio.biobd.tap.agents.libraries.CaptorExecutionPlan;
import br.pucrio.biobd.tap.agents.libraries.Config;
import br.pucrio.biobd.tap.agents.libraries.Log;
import br.pucrio.biobd.tap.agents.sgbd.models.dao.PartialIndexDAO;
import br.pucrio.biobd.tap.agents.sgbd.models.dao.TuningActionDAO;
import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author Rafael
 */
public class PartialIndex extends TuningAction {

    private final ArrayList<Restriction> restrictions;
    private ArrayList<Column> indexedColumns;
    private int indId;
    private String typeColumn;
    private String indexType;
    private String filterType;
    private long numberOfRows;
    private long numberOfPages;
    private Table globalTable = null;
    private Plan hypoPlan;
    private double selectivity;

    @Override
    public double getCost() {
        return 2 * numberOfPages;
    }

    public Table getGlobalTable() {
        return globalTable;
    }

    public ArrayList<Column> getIndexedColumns() {
        return indexedColumns;
    }

    public void setGlobalTable(Table globalTable) {
        this.globalTable = globalTable;
    }

    public long getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(long numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    public ArrayList<Restriction> getRestrictions() {
        return restrictions;
    }

    public void addRestriction(Restriction restriction) {
        if (!this.restrictions.contains(restriction)) {
            this.restrictions.add(restriction);
        }
    }

    public void addIndexedColumn(Column column) {
        boolean has = false;
        for (Column indexedColumn : this.indexedColumns) {
            if (indexedColumn.getCompleteName().equals(column.getCompleteName())) {
                has = true;
                break;
            }
        }
        if (!has) {
            this.indexedColumns.add(column);
        }
    }

    public String getTypeColumn() {
        return typeColumn;
    }

    public void setTypeColumn(String typeColumn) {
        this.typeColumn = typeColumn;
    }

    public PartialIndex() {
        this.restrictions = new ArrayList<>();
        this.indexedColumns = new ArrayList<>();
    }

    public String getIndexType() {
        if (indexType == null) {
            return "S";
        } else {
            return indexType;
        }
    }

    @Override
    public int getId() {
        this.removeEqualsColumns();
        if (!getDdlToExecute().isEmpty()) {
            String sql = getDdlToExecute().toLowerCase().trim();
            int id = sql.hashCode();
            this.ids.add(id + "_" + this.isGlobal());
            return id;
        } else {
            return 0;
        }
    }

    @Override
    public String getName() {
        String comment = "";
        for (Column column : this.getIndexedColumns()) {
            comment += "_" + column.getCompleteName();
        }
        for (Restriction restriction : restrictions) {
            comment += "_" + restriction.getCommentNameForPartialIndex();
        }
        if (this.getGlobalTable() != null) {
            comment += "_" + this.getGlobalTable().getName();
        }
        return String.valueOf("PID_TAP_" + comment.hashCode()).replace("-", "N");
    }

    public boolean getHasFilter() {
        return this.restrictions.size() > 0;
    }

    public String getFilterType() {
        if (filterType == null) {
            return "equi";
        }
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public long getNumberOfRows() {
        if (numberOfRows == 0) {
            System.out.println(numberOfRows);
        }
        return numberOfRows;
    }

    public void setNumberOfRows(long numberOfRows) {
        this.numberOfRows = numberOfRows;
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

    public Plan getHypoPlan() {
        return hypoPlan;
    }

    public void setHypoPlan(Plan hypoPlan) {
        this.hypoPlan = hypoPlan;
    }

    @Override
    public void rating(String agent) {
        if (this.restrictions.size() > 0) {
            this.setHypoPlan(this.getExecutionPlan(agent));
            if (this.getHypoPlan() != null && this.getHypoPlan().operations.size() > 0) {
                this.setNumberOfRows(this.getHypoPlan().getNumRow());
                long numberOfPagesTemp = this.getHypoPlan().getNumRow() * this.getHypoPlan().getSizeRow();
                this.setNumberOfPages(numberOfPagesTemp);
                this.setValid(true);
            } else {
                this.setValid(false);
            }
        } else {
            this.setValid(false);
        }
    }

    @Override
    public void debug() {
    }

    private Plan getExecutionPlan(String agent) {
        CaptorExecutionPlan captorPlan = new CaptorExecutionPlan(agent);
        String query = this.generateSQLCaptureEstimatedPlan();
        Plan plan = captorPlan.getPlanExecution(query);
        if (plan.operations.size() > 0) {
            plan.operations.get(0).properties.put("sql", query);
        }
        return plan;

    }

    public long getIndexScanCost() {
        return this.hypoPlan.getNumRow() * this.hypoPlan.getSizeRow();
    }

    @Override
    public String getDdlToExecute() {
        return this.getDdl();
    }

    @Override
    public TuningActionDAO getInstanceDAO(String agent) {
        TuningActionDAO tuningActionDAO = new PartialIndexDAO();
        tuningActionDAO.setAgent(agent);
        return tuningActionDAO;
    }

    public String getRestrictionsFormatedToSQL() {
        String restrictionFilter = "";
        for (Restriction restriction : this.getRestrictions()) {
            if (restrictionFilter.isEmpty()) {
                restrictionFilter = restriction.getFormatedValue(this.isGlobal());
            } else {
                restrictionFilter += " AND " + restriction.getFormatedValue(this.isGlobal());
            }
        }
        return restrictionFilter;
    }

    private String getRestrictionsFormatedToPlanSQL() {
        String restrictionFilter = "";
        for (Restriction restriction : this.getRestrictions()) {
            if (restrictionFilter.isEmpty()) {
                restrictionFilter = restriction.getFormatedValue(false);
            } else {
                restrictionFilter += " AND " + restriction.getFormatedValue(false);
            }
        }
        return restrictionFilter;
    }

    public String getTableName() {
        String tableName = "";
        if (this.isGlobal()) {
            tableName = this.getGlobalTable().getName();
        } else {
            ArrayList<Table> tablesUsed = new ArrayList<>();
            for (Restriction restriction : this.getRestrictions()) {
                tablesUsed.add(restriction.getColumn().getTable());
            }
            for (Table table : tablesUsed) {
                if (!tableName.contains(table.getName())) {
                    if (!tableName.isEmpty()) {
                        tableName += ", ";
                    }
                    tableName += table.getName();
                }
            }
        }
        return tableName;
    }

    public Table getTable() {
        return this.getRestrictions().get(0).getTables().get(0);
    }

    public String getColumnIndexFormatedToSQL() {
        if (this.getIndexedColumns().isEmpty()) {
            Log.error("indexed column necessary!");
        }
        String columnIndex = "";
        for (Column column : this.getIndexedColumns()) {
            if (!columnIndex.contains(column.getName())) {
                if (!columnIndex.isEmpty()) {
                    columnIndex += ",";
                }
                if (this.isGlobal()) {
                    columnIndex += column.getAlias();
                } else {
                    columnIndex += column.getName();
                }
            }
        }
        return columnIndex;
    }

    public String getColumnIndexFormatedToSQLPlan() {
        if (this.getIndexedColumns().isEmpty()) {
            Log.error("indexed column necessary!");
        }
        String columnIndex = "";
        for (Column column : this.getIndexedColumns()) {
            if (!columnIndex.contains(column.getName())) {
                if (!columnIndex.isEmpty()) {
                    columnIndex += ",";
                }
                columnIndex += column.getName();
            }
        }
        return columnIndex;
    }

    @Override
    public double getSelectivity() {
        try {
            if (this.selectivity == 0) {
                PartialIndexDAO pindexDao = new PartialIndexDAO();
                this.selectivity = pindexDao.getSelectivity(this);
            }
            return this.selectivity;
        } catch (Exception e) {
            Log.msg(this.numberOfRows);
            Log.msg(this.restrictions);
            Log.error(e);
        }
        return 0;
    }

    @Override
    public String getDdl() {
        String ddlIndex = Config.getProperty("DDLCreatePartialIndex");
        ddlIndex = ddlIndex.replace("$nameIndex$", this.getName());
        ddlIndex = ddlIndex.replace("$nameTable$", this.getTableName());
        ddlIndex = ddlIndex.replace("$columnIndex$", this.getColumnIndexFormatedToSQL());
        ddlIndex = ddlIndex.replace("$restrictionFilter$", this.getRestrictionsFormatedToSQL());
        return ddlIndex;
    }

    private String generateSQLCaptureEstimatedPlan() {
        String query = Config.getProperty("captureEstimatedPlanPartialIndex_" + Config.getProperty("sgbd"));
        query = query.replace("$nameTable$", this.getTableForEstimatePlan());
        query = query.replace("$columnIndex$", this.getColumnIndexFormatedToSQLPlan());
        query = query.replace("$restrictionFilter$", this.getRestrictionsFormatedToPlanSQL());
        if (query.replace("  ", " ").replace(" ,", ",").contains("lineitem where l_orderkey = orders.o_orderkey")) {
            System.out.println(query);
        }
        return query;
    }

    private CharSequence getTableForEstimatePlan() {
        String tableName = " ";
        ArrayList<Table> tablesUsed = new ArrayList<>();
        for (Restriction restriction : this.getRestrictions()) {
            for (Table table : restriction.getTables()) {
                if (!tablesUsed.contains(table)) {
                    tablesUsed.add(table);
                }
            }
        }
        for (Column column : this.getIndexedColumns()) {
            if (!tablesUsed.contains(column.getTable())) {
                tablesUsed.add(column.getTable());
            }
        }
        for (Table table : tablesUsed) {
            if (!tableName.trim().isEmpty()) {
                tableName += ", ";
            }
            tableName += table.getName() + " ";
        }
        return tableName;

    }

    @Override
    public boolean isValid() {
        return super.isValid()
                && this.getCost() > 0
                && this.hasValidIndexedColumnsAndRestrictions();
    }

    private boolean hasValidIndexedColumnsAndRestrictions() {
        if (this.isGlobal()) {
            ArrayList<Column> columns = this.getIndexedColumns();
            for (Restriction restriction : this.getRestrictions()) {
                columns.add(restriction.getColumn());
                if (restriction.getValue() instanceof Column) {
                    columns.add((Column) restriction.getValue());
                }
            }
            ArrayList<SQL> sqls = this.getSqlList();
            for (Column indexedColumn : columns) {
                for (SQL sql : sqls) {
                    if (!sql.getFieldsQuery().contains(indexedColumn)) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            ArrayList<String> tableNames = new ArrayList<>();
            for (Column indexedColumn : this.getIndexedColumns()) {
                if (!tableNames.contains(indexedColumn.getTableName())) {
                    tableNames.add(indexedColumn.getTableName());
                }
            }
            for (Restriction restriction : this.getRestrictions()) {
                if (!tableNames.contains(restriction.getColumn().getTableName())) {
                    tableNames.add(restriction.getColumn().getTableName());
                }
                if (restriction.getValue() instanceof Column) {
                    if (!tableNames.contains(((Column) restriction.getValue()).getTableName())) {
                        tableNames.add(((Column) restriction.getValue()).getTableName());
                    }
                }
            }
            return tableNames.size() == 1;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.restrictions);
        hash = 83 * hash + Objects.hashCode(this.getIndexedColumns());
        hash = 83 * hash + Objects.hashCode(this.globalTable);
        return hash;
    }

    @Override
    public boolean equals(Object obj
    ) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PartialIndex other = (PartialIndex) obj;
        if (!Objects.equals(this.restrictions, other.restrictions)) {
            return false;
        }
        if (!Objects.equals(this.getIndexedColumns(), other.getIndexedColumns())) {
            return false;
        }
        if (!Objects.equals(this.globalTable, other.globalTable)) {
            return false;
        }
        return true;
    }

    public void setIndexedColumn(ArrayList<Column> columns) {
        for (Column column : columns) {
            this.addIndexedColumn(column);
        }
    }

    private void removeEqualsColumns() {
        ArrayList<Column> columns = new ArrayList<>();
        for (Column indexedColumn : this.indexedColumns) {
            if (!columns.contains(indexedColumn)) {
                columns.add(indexedColumn);
            }
        }
        this.indexedColumns = columns;
    }

    @Override
    public boolean selectedBySelectionHeuristic() {
        boolean valid = true;
        if (Config.getProperty("enableSelectionHeuristic").equals("1")) {
            if (valid) {
                valid = this.getProfit() > Double.valueOf(Config.getProperty("profitMoreThan"));
            }
            double selectivityLocal = this.getSelectivity();
            if (valid) {
                valid = selectivityLocal > Double.valueOf(Config.getProperty("selevityMoreThan"));
            }
        }
        return valid;
    }

}
