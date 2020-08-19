/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models;

import br.pucrio.biobd.tap.agents.libraries.Log;
import br.pucrio.biobd.tap.agents.sgbd.ExtractDataFromSQL;
import br.pucrio.biobd.tap.algoritms.ExtractSubQueries;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Rafael
 */
public class SQL {

    private String sql;
    private SQL parentSQL;

    private ArrayList<Table> tablesQuery;
    private ArrayList<Column> fieldsQuery;
    public ArrayList<SQLExecution> executions;
    private HashMap clauses;
    private boolean tuningAction;
    private boolean valid;

    public boolean isTuningAction() {
        return tuningAction;
    }

    public SQL getParentSQL() {
        return parentSQL;
    }

    public void setTuningAction(boolean tuningAction) {
        this.tuningAction = tuningAction;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        if (this.valid) {
            this.valid = valid;
        }
    }

    public HashMap getClauses() {
        return clauses;
    }

    public String getClause(String nameClause) {
        if (clauses.containsKey(nameClause)) {
            return (String) clauses.get(nameClause);
        } else {
            return "";
        }
    }

    public boolean hasClause(String nameClause) {
        return clauses.containsKey(nameClause);
    }

    public void setClauses(HashMap clauses) {
        this.clauses = clauses;
    }

    public SQL() {
        this.tablesQuery = new ArrayList<>();
        this.fieldsQuery = new ArrayList<>();
        this.executions = new ArrayList<>();
        this.clauses = new HashMap<>();
        this.tuningAction = false;
        this.valid = true;
    }

    public ArrayList<Column> getFieldsQuery() {
        return fieldsQuery;
    }

    public void setFieldsQuery(ArrayList<Column> fieldsQuery) {
        this.fieldsQuery = fieldsQuery;
    }

    public ArrayList<Table> getTablesQuery() {
        return tablesQuery;
    }

    public void setTablesQuery(ArrayList<Table> tablesQuery) {
        this.tablesQuery = tablesQuery;
    }

    public void setSql(String sql) {
        this.sql = this.removerNl(sql.trim());
    }

    public void setParentSQLT(SQL parentSQL) {
        this.parentSQL = parentSQL;
    }

    public ArrayList<SQL> getSubQueries() {
        ArrayList<SQL> subQueries = new ArrayList<>();
        ExtractSubQueries extractSubQueries = new ExtractSubQueries();
        ExtractDataFromSQL extract = new ExtractDataFromSQL();
        ArrayList<String> subQueriesText = extractSubQueries.extractSubQueries(this.getSql());
        for (String subQuery : subQueriesText) {
            SQL sqlSub = new SQL();
            sqlSub.setSql(subQuery.trim());
            sqlSub.executions.addAll(this.executions);
            sqlSub = extract.extractData(sqlSub);
            sqlSub.setParentSQLT(this);
            subQueries.add(sqlSub);
        }
        return subQueries;
    }

    public String removerNl(String frase) {
        String padrao = "\\s{2,}";
        Pattern regPat = Pattern.compile(padrao);
        Matcher matcher = regPat.matcher(frase);
        String res = matcher.replaceAll(" ").trim();
        res = res.replaceAll("(\n|\r)+", " ");
        return res.trim();
    }

    public String getType() {
        if (this.getSql().toLowerCase().contains("select")) {
            return "Q";
        } else if (this.getSql().toLowerCase().contains("update")) {
            return "U";
        } else if (this.getSql().toLowerCase().contains("insert")) {
            return "I";
        }
        return null;
    }

    public boolean hasTableInTableQuery(ArrayList<Table> tables) {
        for (Table tableCheck : tables) {
            if (this.tablesQuery.contains(tableCheck)) {
                return true;
            }
        }
        return false;
    }

    public String getComents() {
        if (this.getSql() != null) {
            String sqlText = this.getSql();
            return this.getSubComment(sqlText);
        } else {
            return "";
        }
    }

    public String getComentsForFileName() {
        if (this.getSql() != null) {
            String sqlText = this.getSql();
            return this.getSubComment(sqlText).replace("-", "").replace("*", "").replace("/", "").trim().replace(" ", "_").replace("TPC_H_Query_", "Q");
        } else {
            return "";
        }
    }

    private String getSubComment(String sqlText) {
        int ini = sqlText.indexOf("/*");
        if (ini >= 0) {
            int end = sqlText.substring(ini).indexOf("*/") + ini + 2;
            String result = sqlText.substring(ini, end).trim() + " ";
            sqlText = sqlText.replace(result, "");
            if (sqlText.contains("/*")) {
                return result + " " + getSubComment(sqlText);
            } else {
                return result;
            }
        } else {
            return "";
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.getId();
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
        final SQL other = (SQL) obj;
        if (this.getId() != other.getId()) {
            return false;
        }
        return true;
    }

    public SQLExecution getFirstExecution() {
        if (executions.size() > 0) {
            return executions.get(0);
        } else {
            return null;
        }
    }

    public String getSql() {
        return sql;
    }

    public Column getFielFromQueryByName(String fieldName) {
        for (Column column : this.fieldsQuery) {
            if (column.getName().toLowerCase().equals(fieldName.toLowerCase())) {
                return column;
            }
        }
        return null;
    }

    public SQLExecution getLastExecution() {
        if (executions.size() > 0) {
            return executions.get(executions.size() - 1);
        } else {
            return null;
        }
    }

    public void print() {
        Log.msg("ID: " + this.getId());
        Log.msg("SQL: " + sql);
        Log.msg("Type: " + this.getType());
        Log.msg("Tables: ");
        for (Table tablesQuery1 : tablesQuery) {
            Log.msg("\t" + tablesQuery1.getName());
        }
        Log.msg("Fields Query: ");
        for (Column fieldsQuery1 : fieldsQuery) {
            Log.msg("\t" + fieldsQuery1.getName());
        }
        Log.msg("Execution(s): ");
        for (SQLExecution execution : executions) {
            Log.msg("\t" + execution.getDate());
            Log.msg("\tPlan(s): ");
            execution.getPlan().print("\t");
        }
    }

    public int getId() {
        if (this.getSql() != null) {
            return this.getSql().toLowerCase().trim().hashCode();
        } else {
            return -1;
        }
    }

    public boolean hasSubQuery() {
        return this.getSubQueries().size() > 0;
    }

    public ArrayList<PlanOperation> getOperationsByType(EnumPlanOperation operationType) {
        ArrayList<PlanOperation> operations = new ArrayList<>();
        for (SQLExecution execution : this.executions) {
            for (PlanOperation operation : execution.getPlan().getOperationsByType(operationType)) {
                if (!operations.contains(operation)) {
                    operations.add(operation);
                }
            }
        }
        return operations;
    }
}
