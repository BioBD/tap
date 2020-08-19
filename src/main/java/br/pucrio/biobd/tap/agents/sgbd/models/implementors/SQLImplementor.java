/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models.implementors;

import br.pucrio.biobd.tap.agents.libraries.CaptorExecutionPlan;
import br.pucrio.biobd.tap.agents.libraries.Config;
import br.pucrio.biobd.tap.agents.libraries.Log;
import br.pucrio.biobd.tap.agents.sgbd.ExtractDataFromSQL;
import br.pucrio.biobd.tap.agents.sgbd.ReadSchemaDB;
import br.pucrio.biobd.tap.agents.sgbd.models.Column;
import br.pucrio.biobd.tap.agents.sgbd.models.Plan;
import br.pucrio.biobd.tap.agents.sgbd.models.SQL;
import br.pucrio.biobd.tap.agents.sgbd.models.SQLExecution;
import br.pucrio.biobd.tap.agents.sgbd.models.Schema;
import br.pucrio.biobd.tap.agents.sgbd.models.Table;
import br.pucrio.biobd.tap.algoritms.ExtractSubQueries;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author Rafael
 */
public abstract class SQLImplementor {

    protected SQL sql;

    public ArrayList<Table> extractTables(SQL sql, Schema schema) {
        this.setSql(sql);
        ArrayList<Table> tables = new ArrayList<>();
        String tableName = null;
        try {
            for (Table table : schema.tables) {
                tableName = table.getName();
                if (this.sql.getSql() != null && !this.sql.getTablesQuery().contains(table) && this.containsFieldOrTable(this.sql.getSql(), table.getName())) {
                    tables.add(table);
                }
            }
        } catch (Exception e) {
            Log.msg(tableName);
            Log.msg(schema.toString());
            Log.error(e);
        }
        return tables;
    }

    public ArrayList<Column> extractColumns(SQL sql, Schema schema) {
        this.setSql(sql);
        ArrayList<Column> columnsResult = new ArrayList<>();
        for (Table table : extractTables(sql, schema)) {
            columnsResult.addAll(this.getFieldsQueryByTable(table));
        }
        return columnsResult;
    }

    public String getClauseFromSql(String clause) {
        if (this.existClause(clause)) {
            int ini;
            if (clause.toLowerCase().contains("select") || clause.toLowerCase().contains("from") || clause.toLowerCase().contains("where")) {
                ini = this.sql.getSql().toLowerCase().indexOf(clause) + clause.length();
            } else {
                ini = this.sql.getSql().toLowerCase().lastIndexOf(clause) + clause.length();
            }
            int end = this.getEndClause(ini);
            String clauseComplete;
            if (end > 0) {
                clauseComplete = this.sql.getSql().toLowerCase().substring(ini, end).trim();
            } else {
                clauseComplete = this.sql.getSql().toLowerCase().substring(ini).trim();
            }
            return " " + clause + " " + clauseComplete;
        } else {
            return "";
        }
    }

    public SQL fixSQLSubQuery(SQL sql) {
        if ((!sql.getSql().isEmpty())) {
            Schema schema = ReadSchemaDB.getSchemaDB();
            for (Table table : schema.tables) {
                for (Column field : table.getFields()) {
                    if (sql.getSql().toLowerCase().contains(field.getName().toLowerCase()) && !sql.getTablesQuery().contains(table)) {
                        sql = this.removeAttribute(sql, field);
                    }
                }
            }
        }
        return sql;
    }

    public void setSql(SQL sql) {
        this.sql = sql;
    }

    public HashMap extractClauses(SQL sql) {
        this.setSql(sql);
        HashMap clauses = new HashMap<>();
        for (String nameClause : this.getNameClauses()) {
            String clause = this.getClauseFromSql(nameClause);
            if (!clause.isEmpty()) {
                clauses.put(nameClause, clause);
            }
        }
        return clauses;
    }

    private ArrayList<String> getNameClauses() {
        ArrayList<String> result = new ArrayList<>();
        result.add("select");
        result.add("from");
        result.add("where");
        result.add("group by");
        result.add("order by");
        result.add("having");
        result.add("limit");
        result.add("delete");
        result.add("update");
        result.add("set");
        result.add("insert");
        return result;
    }

    public boolean containsFieldOrTable(String clause, String field) {
        clause = clause.toLowerCase();
        field = field.toLowerCase();
        String[] lastWords = clause.split(" |\\.|\\,");
        String lastWord = "";
        if (lastWords.length > 0) {
            lastWord = lastWords[lastWords.length - 1];
        }
        return clause.contains(" " + field + " ")
                || clause.contains(" " + field + " ")
                || lastWord.equals(field)
                || clause.contains("." + field)
                || clause.contains("_" + field)
                || clause.contains(" " + field + ",")
                || clause.contains(" " + field + ";")
                || clause.contains(" " + field + "=")
                || clause.contains(" " + field + ">")
                || clause.contains(" " + field + "<")
                || clause.contains("," + field + " ")
                || clause.contains("," + field + ",")
                || clause.contains("," + field + ";")
                || clause.contains("," + field + "=")
                || clause.contains("," + field + ">")
                || clause.contains("," + field + "<")
                || clause.contains("." + field + " ")
                || clause.contains("." + field + ",")
                || clause.contains("." + field + ";")
                || clause.contains("." + field + "=")
                || clause.contains("." + field + ">")
                || clause.contains("." + field + "<")
                || clause.contains("(" + field + ")")
                || clause.contains(" " + field + ")")
                || clause.contains("(" + field + " ")
                || clause.contains("(" + field + ">")
                || clause.contains("(" + field + "<")
                || clause.contains("(" + field + "=")
                || clause.equals(field);
    }

    private ArrayList<Column> getFieldsQueryByTable(Table table) {
        ArrayList<Column> fields = new ArrayList<>();
        if (this.sql.getSql().toLowerCase().contains("select *")) {
            fields.addAll(table.getFields());
        } else {
            for (Column field : table.getFields()) {
                if (this.containsFieldOrTable(this.sql.getSql(), field.getName())) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    private boolean existClause(String clause) {
        return this.sql.getSql().toLowerCase().contains(clause);
    }

    private int getEndClause(int ini) {
        int current = this.sql.getSql().length();
        current = this.getSmaller(current, ini, this.sql.getSql().toLowerCase().substring(ini).indexOf(" from "));
        current = this.getSmaller(current, ini, this.sql.getSql().toLowerCase().substring(ini).indexOf(" where "));
        current = this.getSmaller(current, ini, this.sql.getSql().toLowerCase().substring(ini).indexOf(" group by "));
        current = this.getSmaller(current, ini, this.sql.getSql().toLowerCase().substring(ini).indexOf(" having "));
        current = this.getSmaller(current, ini, this.sql.getSql().toLowerCase().substring(ini).indexOf(" order by "));
        current = this.getSmaller(current, ini, this.sql.getSql().toLowerCase().substring(ini).indexOf(" limit "));
        if (current > ini) {
            return current;
        } else {
            return 0;
        }
    }

    private int getSmaller(int current, int ini, int end) {
        if (end < 0) {
            return current;
        }
        if ((ini + end) < current) {
            return (ini + end);
        } else {
            return current;
        }
    }

    private SQL removeAttribute(SQL sql, Column field) {
        ExtractSubQueries extract = new ExtractSubQueries();
        sql.setSql(extract.fixSubQueryFromWrongField(sql.getSql(), field.getName()));
        return sql;
    }

    public SQL createSQL(String sqlSintaxe, Timestamp time, Schema schema, ArrayList<SQL> SQListToBeProcessed) {
        ExtractDataFromSQL extract = new ExtractDataFromSQL();
        SQL sql = new SQL();
        sql.setSql(sqlSintaxe.trim());
        extract.extractData(sql);
        SQL sqlInList = getSQLToBeProcessed(sql.getId(), SQListToBeProcessed);
        if (sqlInList == null) {
            this.addExecution(sql, time);
            return sql;
        } else {
            this.addExecution(sqlInList, time);
            return sqlInList;
        }
    }

    public void addExecution(SQL sql, Timestamp timestamp) {
        SQLExecution execution = new SQLExecution(timestamp);
        if (!sql.executions.contains(execution)) {
            SQLExecution newExecution = this.newSQLExecution(timestamp, sql.getSql());
            if (newExecution == null) {
                sql.setValid(false);
            } else {
                sql.setValid(true);
                sql.executions.add(newExecution);
            }
        }
    }

    protected SQL getSQLToBeProcessed(int QueryId, ArrayList<SQL> SQListToBeProcessed) {
        for (int i = 0; i < SQListToBeProcessed.size(); i++) {
            if (SQListToBeProcessed.get(i).getId() == QueryId) {
                return SQListToBeProcessed.get(i);
            }
        }
        return null;
    }

    private SQLExecution newSQLExecution(Date date, String sql) {
        Plan plan = this.getPlanExecution(sql);
        if (plan != null && plan.operations.size() > 0) {
            return new SQLExecution(date, plan);
        } else {
            return null;
        }
    }

    private Plan getPlanExecution(String sql) {
        CaptorExecutionPlan captorPlan = new CaptorExecutionPlan("observer");
        return captorPlan.getPlanExecution(sql);
    }

    public static SQLImplementor createNewSQLImplementor() {
        SQLImplementor SQLImp;
        switch (Config.getProperty("sgbd")) {
            case "postgresql":
                SQLImp = new SQLPostgreSQLImplementor();
                break;
            case "oracle":
                SQLImp = new SQLOracleImplementor();
                break;
            default:
                SQLImp = new SQLSQLServerImplementor();
                break;
        }
        return SQLImp;
    }

}
