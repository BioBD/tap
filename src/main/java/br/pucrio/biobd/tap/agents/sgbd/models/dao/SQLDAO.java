/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models.dao;

import br.pucrio.biobd.tap.agents.libraries.Config;
import br.pucrio.biobd.tap.agents.libraries.Log;
import br.pucrio.biobd.tap.agents.sgbd.ExtractDataFromSQL;
import br.pucrio.biobd.tap.agents.sgbd.models.Plan;
import br.pucrio.biobd.tap.agents.sgbd.models.PlanOperation;
import br.pucrio.biobd.tap.agents.sgbd.models.SQL;
import br.pucrio.biobd.tap.agents.sgbd.models.SQLExecution;
import br.pucrio.biobd.tap.agents.sgbd.models.Schema;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class SQLDAO extends BasicDAO {

    private ArrayList<String> propertiesPlanForSave;

    public SQLDAO() {
        String[] words = Config.getProperty("propertiesPlanForSave_" + Config.getProperty("sgbd")).split(";");
        this.propertiesPlanForSave = new ArrayList<>();
        for (String word : words) {
            propertiesPlanForSave.add(word);
        }
    }

    public boolean save(SQL sql) {
        if (sql.isValid()) {
            if (!this.isSave(sql)) {
                try {
                    PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("insertSQLTbWorkload_" + Config.getProperty("sgbd")));
                    Log.msg("INSERT TB_WORKLOAD: " + sql.getSql());
                    preparedStatement.setInt(1, sql.getId());
                    preparedStatement.setString(2, sql.getSql());
                    preparedStatement.setString(3, sql.getType());
                    preparedStatement.setBoolean(4, sql.isValid());
                    preparedStatement.setLong(5, sql.getLastExecution().getPlan().getCost());
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                    return this.insertQueryExecution(sql);
                } catch (SQLException e) {
                    Log.msg("ERROR SAVE SQL: " + e.getMessage());
                }
            } else {
                return this.updateSQL(sql);
            }
        }
        return false;
    }

    public SQL readExecutions(SQL sql) {
        try {
            PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("readExecution_" + Config.getProperty("sgbd")));
            preparedStatement.setInt(1, sql.getId());
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                SQLExecution execution = new SQLExecution(result.getTimestamp("exe_date"));
                if (!sql.executions.contains(execution)) {
                    execution.setPlan(this.readPlan(result.getInt("wld_id"), result.getTimestamp("exe_date")));
                    sql.executions.add(execution);
                }
            }
            result.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            Log.error(ex);
        }
        return sql;
    }

    private boolean isSave(SQL sql) {
        try {
            String query = Config.getProperty("workloadIsSave_" + Config.getProperty("sgbd"));

            PreparedStatement preparedStatement = connection().prepareStatement(query);
            preparedStatement.setInt(1, sql.getId());
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                preparedStatement.close();
                result.close();
                return true;
            }
            result.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            Log.msg("ERROR isSave SQL ID: " + sql.getId());
            Log.msg("ERROR isSave SQL: " + sql.getSql());
            Log.msg("ERROR isSave: " + ex.getMessage());
        }
        return false;
    }

    private boolean isExecutionSave(SQL sql, SQLExecution sqlExecution) {
        try {
            PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("SQLExecutionIsSave_" + Config.getProperty("sgbd")));
            preparedStatement.setInt(1, sql.getId());
            preparedStatement.setTimestamp(2, new Timestamp(sqlExecution.getDate().getTime()));
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                result.close();
                preparedStatement.close();
                return true;
            }
            result.close();
            preparedStatement.close();
            result.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            Log.msg("ERROR isExecutionSave SQL ID: " + sql.getId());
            Log.msg("ERROR isExecutionSave SQL: " + sql.getSql());
            Log.msg("ERROR isExecutionSave: " + ex.getMessage());
        }
        return false;
    }

    private boolean insertQueryExecution(SQL sql) {
        for (SQLExecution execution : sql.executions) {
            if (!isExecutionSave(sql, execution)) {
                try {
                    PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("insertTbExecution_" + Config.getProperty("sgbd")));
                    preparedStatement.setInt(1, sql.getId());
                    preparedStatement.setTimestamp(2, new Timestamp(execution.getDate().getTime()));
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                    if (!this.insertOperation(sql, execution)) {
                        return false;
                    }
                } catch (SQLException e) {
                    Log.msg("ERROR insertQueryExecution" + sql.getSql());
                    Log.msg("ERROR insertQueryExecution" + e.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    private boolean insertOperation(SQL sql, SQLExecution execution) {
        try {
            for (PlanOperation operation : execution.getPlan().operations) {
                PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("insertTbOperation_" + Config.getProperty("sgbd")));
                preparedStatement.setInt(1, sql.getId());
                preparedStatement.setTimestamp(2, new Timestamp(execution.getDate().getTime()));
                preparedStatement.setInt(3, operation.getOrder());
                preparedStatement.setString(4, operation.getName());
                preparedStatement.executeUpdate();
                preparedStatement.close();
                if (!this.insertOperationData(sql, execution, operation)) {
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            Log.msg("ERROR: SQL ID: " + sql.getId());
            Log.msg("ERROR: EXECUTION: " + execution.getDate());
            return false;
        }
    }

    public ArrayList<SQL> getWorkload(String algoritmName, Schema schema) {
        ArrayList<SQL> sqlList = new ArrayList<>();
        try {
            String queryName = "readWorkload_" + algoritmName + "_" + Config.getProperty("sgbd");
            String query = Config.getProperty(queryName);
            Connection conn = connection();
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet resultset = statement.executeQuery();
            if (resultset != null) {
                while (resultset.next()) {
                    SQL sql = this.getSQL(resultset.getString("wld_sql"));
                    if (sql.executions.size() > 0 && sql.getLastExecution().getPlan() != null && !sql.getLastExecution().getPlan().operations.isEmpty() && !sqlList.contains(sql)) {
                        sqlList.add(sql);
                    }
                }
            }
            resultset.close();
            statement.close();
        } catch (SQLException e) {
            Log.error(e);
        }
        sqlList = this.extractData(sqlList, schema);
        return sqlList;
    }

    public SQL getSQL(String sqlText) {
        SQL sql = new SQL();
        sql.setSql(sqlText);
        return readExecutions(sql);
    }

    private ArrayList<SQL> extractData(ArrayList<SQL> sqlList, Schema schema) {
        ExtractDataFromSQL extract = new ExtractDataFromSQL();
        for (SQL SQLToBeExtractedData : sqlList) {
            if (SQLToBeExtractedData.getTablesQuery().isEmpty()) {
                extract.extractData(SQLToBeExtractedData);
            }
        }
        return sqlList;
    }

    private boolean insertOperationData(SQL sql, SQLExecution execution, PlanOperation operation) {
        try {
            Iterator it = operation.properties.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (this.isRelevantInfo(pair.getKey().toString(), pair.getValue())) {
                    PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("insertTbOperationData_" + Config.getProperty("sgbd")));
                    preparedStatement.setInt(1, sql.getId());
                    preparedStatement.setTimestamp(2, new Timestamp(execution.getDate().getTime()));
                    preparedStatement.setInt(3, operation.getOrder());
                    preparedStatement.setString(4, String.valueOf(pair.getKey()));
                    preparedStatement.setString(5, String.valueOf(pair.getValue()));
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                }
            }
            return true;
        } catch (SQLException e) {
            Log.msg("ERROR insertOperationData" + sql.getSql());
            Log.msg("ERROR insertOperationData" + e.getMessage());
            return false;
        }
    }

    private boolean isRelevantInfo(String key, Object value) {
        return value != null && (Config.getProperty("sgbd").equals("postgresql") || propertiesPlanForSave.contains("all") || propertiesPlanForSave.contains(key));
    }

    private Plan readPlan(int sqlId, Timestamp executionTime) {
        Plan planResult = new Plan();
        planResult.operations = this.readOperations(executionTime, sqlId);
        return planResult;
    }

    private ArrayList<PlanOperation> readOperations(java.sql.Timestamp executionTime, int sqlId) {
        ArrayList<PlanOperation> operations = new ArrayList();
        try {
            PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("readOperation_" + Config.getProperty("sgbd")));
            preparedStatement.setInt(1, sqlId);
            preparedStatement.setTimestamp(2, executionTime);
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                PlanOperation operation = new PlanOperation();
                operation.setName(result.getString("ope_name"));
                operation.setOrder(result.getInt("ope_order"));
                this.readOperationProperties(sqlId, executionTime, operation);
                operations.add(operation);
            }
            result.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            Log.error(ex);
        }
        return operations;
    }

    private void readOperationProperties(int sqlId, Timestamp executionTime, PlanOperation operation) {
        try {
            PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("readOperationData_" + Config.getProperty("sgbd")));
            preparedStatement.setInt(1, sqlId);
            preparedStatement.setTimestamp(2, executionTime);
            preparedStatement.setInt(3, operation.getOrder());
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                operation.properties.put(result.getString("opd_name"), result.getString("opd_data"));
            }
            result.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            Log.error(ex);
        }
    }

    public TuningAction readSQLList(TuningAction mv) {
        try {
            PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("readTuninActionSQLList_" + Config.getProperty("sgbd")));
            preparedStatement.setInt(1, mv.getId());
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                SQL sql = new SQL();
                sql.setSql(result.getString("wld_sql"));
                if (!mv.getSqlList().contains(sql)) {
                    readExecutions(sql);
                    mv.addSQL(sql);
                }
            }
            result.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            Log.error(ex);
        }
        return mv;
    }

    private boolean updateSQL(SQL sql) {
        try {
            if (sql.isValid()) {
                PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("updateSQLTbWorkload_" + Config.getProperty("sgbd")));
                preparedStatement.setLong(1, sql.getLastExecution().getPlan().getCost());
                preparedStatement.setInt(2, sql.getId());
                preparedStatement.executeUpdate();
                preparedStatement.close();
                return true;
            }
        } catch (SQLException e) {
            Log.error(e);
        }
        return false;
    }
}
