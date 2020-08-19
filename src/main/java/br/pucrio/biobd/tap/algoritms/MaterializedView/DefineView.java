/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.algoritms.MaterializedView;

import br.pucrio.biobd.tap.agents.sgbd.models.Column;
import br.pucrio.biobd.tap.agents.sgbd.models.MaterializedView;
import br.pucrio.biobd.tap.agents.sgbd.models.SQL;
import br.pucrio.biobd.tap.agents.sgbd.models.Table;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;
import br.pucrio.biobd.tap.agents.sgbd.models.implementors.SQLImplementor;
import br.pucrio.biobd.tap.algoritms.Algorithm;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rafael
 */
public class DefineView extends Algorithm {

    protected String select;
    protected String from;
    protected String where;
    protected String groupBy;
    protected String orderBy;
    protected ArrayList<String> fieldsWhere;

    @Override
    public List<TuningAction> getTuningActions(SQL sql) {
        if (!sql.hasSubQuery()) {
            return this.getMaterializedView(sql);
        } else {
            return this.getMaterializedViewFromSubQuery(sql);
        }
    }

    private void initialize() {
        this.select = "";
        this.from = "";
        this.where = "";
        this.groupBy = "";
        this.orderBy = "";
        this.fieldsWhere = new ArrayList<>();
    }

    private ArrayList<TuningAction> getMaterializedView(SQL sql) {
        this.initialize();
        ArrayList<TuningAction> vms = new ArrayList<>();
        MaterializedView currentMV = new MaterializedView();
        String queryFromMV = this.getDdlCreateViewFromQuery(sql);
        currentMV.setDdl(queryFromMV);
        SQLImplementor captor = SQLImplementor.createNewSQLImplementor();
        sql.setSql(queryFromMV);
        captor.addExecution(sql, new Timestamp(System.currentTimeMillis()));
        currentMV.addSQL(sql);
        vms.add(currentMV);
        return vms;
    }

    private ArrayList<TuningAction> getMaterializedViewFromSubQuery(SQL sql) {
        ArrayList<TuningAction> vms = new ArrayList<>();
        ArrayList<SQL> subQueries = sql.getSubQueries();
        SQL subSQL = null;
        for (SQL subQuery : subQueries) {
            if (subSQL == null || subQuery.getSql().length() > subSQL.getSql().length()) {
                subSQL = subQuery;
            }
        }
        vms.addAll(this.getMaterializedView(subSQL));
        return vms;
    }

    public String getDdlCreateViewFromQuery(SQL sql) {
        this.gerateClauseWhereForDDLView(sql);
        this.gerateClauseSelectForDDLView(sql);
        this.gerateClauseFromForDDLView(sql);
        this.gerateClauseGroupByForDDLView(sql);
        this.gerateClauseOrderByForDDLView(sql);
        String resultDebug = this.getDdlCreateViewComplete();
        return resultDebug;
    }

    private String getDdlCreateViewComplete() {
        return (this.treatComma(this.select) + " "
                + treatComma(this.from) + " "
                + treatComma(this.where) + " "
                + treatComma(this.groupBy) + " "
                + treatComma(this.orderBy)).trim();
    }

    private String treatComma(String query) {
        query = query.trim();
        if (query.length() > 0 && query.charAt(query.length() - 1) == ',') {
            query = query.substring(0, query.length() - 1);
        }
        return query;
    }

    private void gerateClauseSelectForDDLView(SQL query) {
        this.select = query.getClause("select");
        SQLImplementor sqlImp = SQLImplementor.createNewSQLImplementor();
        String groupBySQL = query.getClause("group by");
        String sqlCompare = this.select.replaceAll("\\(.+?\\)", "");
        if (!this.select.trim().equals("select *")) {
            for (Column fieldWhere : query.getFieldsQuery()) {
                if (!sqlImp.containsFieldOrTable(sqlCompare, fieldWhere.getName())) {
                    this.select += ", " + this.getRenamedField(fieldWhere, query);
                    sqlCompare += ", " + this.getRenamedField(fieldWhere, query);
                }
                if (!sqlImp.containsFieldOrTable(groupBySQL, fieldWhere.getName())) {
                    this.groupBy += ", " + this.getRenamedField(fieldWhere, query);
                    groupBySQL += ", " + this.getRenamedField(fieldWhere, query);
                }
            }
        }
        if (!this.select.isEmpty()) {
            this.select = query.getComents() + "\n" + this.select;
        }
    }

    private void gerateClauseFromForDDLView(SQL query) {
        this.from = query.getClause("from");
        SQLImplementor sqlImp = SQLImplementor.createNewSQLImplementor();
        for (Table table : query.getTablesQuery()) {
            if (!sqlImp.containsFieldOrTable(this.from, table.getName())) {
                this.from += ", " + table.getName().toLowerCase();
            }
        }
    }

    private void gerateClauseGroupByForDDLView(SQL query) {
        if (!this.groupBy.isEmpty() && query.hasClause("group by")) {
            this.groupBy = query.getClause("group by") + this.groupBy;
        } else if (this.hasForceClauseGroupBy()) {
            this.groupBy = " group by " + this.groupBy.substring(1);
        } else {
            this.groupBy = query.getClause("group by");
        }
    }

    private void gerateClauseOrderByForDDLView(SQL query) {
        this.orderBy = query.getClause("order by");
    }

    private boolean hasForceClauseGroupBy() {
        return !this.groupBy.trim().isEmpty()
                && !this.groupBy.trim().equals(",")
                && (this.select.contains("sum(")
                || this.select.contains("count(")
                || this.select.contains("min(")
                || this.select.contains("avg("));
    }

    private void gerateClauseWhereForDDLView(SQL query) {
        String clause = query.getClause("where");
        if (!clause.isEmpty()) {
            Combinacao combination = new Combinacao();
            ArrayList<String> lista = combination.dividirExpressaoPredicado(clause);
            this.where = "";
            for (String constrain : lista) {
                if (isConstrainValid(constrain)
                        && !this.where.contains(constrain)) {
                    if (!this.where.isEmpty()) {
                        this.where += " and ";
                    }
                    this.where += " " + constrain;
                }
            }
            if (!this.where.isEmpty()) {
                this.where = "where " + this.where;
            }
        } else {
            this.where = "";
        }
    }

    private boolean isConstrainValid(String constrain) {
        if (constrain.contains("'") || constrain.contains("\"")) {
            return false;
        }
        String[] words = constrain.split(" ");
        for (String word : words) {
            if (this.containNumber(word)) {
                return false;
            }
        }
        return true;
    }

    private boolean containNumber(String word) {
        return word.contains("0")
                || word.contains("1")
                || word.contains("2")
                || word.contains("3")
                || word.contains("4")
                || word.contains("5")
                || word.contains("6")
                || word.contains("7")
                || word.contains("8")
                || word.contains("9");
    }

    private String getRenamedField(Column column, SQL query) {
        if (query.getClause("where").toLowerCase().contains(" na.")) {
            String[] sql = query.getClause("where").toLowerCase().split(" |=|\\(|\\)");
            String result = "";
            for (String field : sql) {
                if (field.toLowerCase().contains("." + column.getName())) {
                    if (!result.isEmpty()) {
                        result += ", ";
                    }
                    result += field + " as " + field.replace(".", "_");
                }
            }
            if (result.isEmpty()) {
                return column.getCompleteName();
            } else {
                return result;
            }
        }
        return column.getCompleteName();
    }

}
