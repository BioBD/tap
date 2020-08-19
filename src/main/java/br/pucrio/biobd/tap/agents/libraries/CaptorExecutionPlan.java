/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.libraries;

import br.pucrio.biobd.tap.agents.sgbd.models.Plan;
import br.pucrio.biobd.tap.agents.sgbd.models.PlanOperation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Rafael
 */
public final class CaptorExecutionPlan {

    private Connection connection;
    private final String agent;

    public CaptorExecutionPlan(String agent) {
        this.agent = agent;
        ConnectionSGBD conn = new ConnectionSGBD();
        this.connection = conn.connection(agent);
    }

    private void resetConnection() {
        ConnectionSGBD conn = new ConnectionSGBD();
        this.connection = conn.resetConnection(agent);
    }

    public Plan getPlanExecution(String query) {
        switch (Config.getProperty("sgbd")) {
            case "postgresql":
                return this.getPlanExecutionPostgreSQL(query);
            case "sqlserver":
                return this.getPlanExecutionSQLServer(query);
            case "oracle":
                return this.getPlanExecutionOracle(query);
            default:
                return null;
        }
    }

    private Plan getPlanExecutionPostgreSQL(String query) {
        Plan planResult = new Plan();
        if (!query.isEmpty()) {
            try {
                String queryGetPlan = Config.getProperty("capturePlanQuery_" + Config.getProperty("sgbd"));
                queryGetPlan = queryGetPlan.replace("$query$", query);
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(Config.getProperty("signature") + " " + queryGetPlan);
                while (result.next()) {
                    planResult.addAllOperations(result.getString(1));
                }
                result.close();
                statement.close();
                if (planResult.operations.isEmpty()) {
                    planResult.addAllOperations(this.getResultEstimatedPlanQueryPostgreSQL(query));
                } else {
                    planResult.setHypotetical(false);
                }
            } catch (SQLException ex) {
                if (Config.getProperty("showErrorEstimatedPlan").equals("1")) {
                    Log.msg("ERROR ESITMATED PLAN SQL: " + Log.removeNl(query));
                    Log.msg("ERROR ESITMATED PLAN SQL: " + Log.removeNl(ex.getMessage()));
                }
            }
        }
        return planResult;
    }

    private Plan getPlanExecutionSQLServer(String query) {
        Plan planResult = new Plan();
        String queryToPlan = query.replace("'", "''");
        if (!query.isEmpty()) {
            try {
                String queryGetPlan = Config.getProperty("capturePlanQuery_" + Config.getProperty("sgbd"));
                queryGetPlan = queryGetPlan.replace("$query$", queryToPlan);
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(Config.getProperty("signature") + " " + queryGetPlan);
                while (result.next()) {
                    planResult.addAllOperations(result.getString(1));
                }
                result.close();
                statement.close();
                if (planResult.operations.isEmpty()) {
                    planResult.addAllOperations(this.getResultEstimatedPlanQuerySQLServer(query));
                } else {
                    planResult.setHypotetical(false);
                }
            } catch (SQLException ex) {
                Log.msg("QUERY WITH ERROR (getPlanExecutionSQLServer): " + Log.removeNl(query));
                Log.error(ex);
            }
        }
        return planResult;
    }

    private String getResultEstimatedPlanQuerySQLServer(String query) {
        String plan = "";
        try {
            this.execPlanQuerySQLServerShowPlanXMLOn();
            Statement statement = connection.createStatement();
            ResultSet resultset = statement.executeQuery(Config.getProperty("signature") + " " + query);
            if (resultset.next()) {
                plan = resultset.getString(1);
            }
            resultset.close();
            statement.close();
            execPlanQuerySQLServerShowPlanXMLOFF();
        } catch (SQLException ex) {
            Log.msg("ERROR ESITMATED PLAN SQL: " + Log.removeNl(query));
            Log.msg("ERROR ESITMATED PLAN SQL: " + Log.removeNl(ex.getMessage()));
            execPlanQuerySQLServerShowPlanXMLOFF();
        }
        return plan;
    }

    private void execPlanQuerySQLServerShowPlanXMLOn() {
        try {
            Statement statement = connection.createStatement();
            statement.execute(Config.getProperty("signature") + "SET SHOWPLAN_TEXT OFF");
            statement.execute(Config.getProperty("signature") + "SET SHOWPLAN_XML ON");
            statement.close();
        } catch (SQLException ex) {
            Log.msg("SET SHOWPLAN_XML ON: " + ex.getMessage());
            Log.msg(ex.getMessage());
            this.resetConnection();
        }
    }

    private void execPlanQuerySQLServerShowPlanXMLOFF() {
        try {
            Statement statement = connection.createStatement();
            statement.execute(Config.getProperty("signature") + "SET SHOWPLAN_TEXT OFF");
            statement.execute(Config.getProperty("signature") + "SET SHOWPLAN_XML OFF");
            statement.close();
        } catch (SQLException ex) {
            Log.msg("SET SHOWPLAN_XML ON: " + ex.getMessage());
            Log.msg(ex.getMessage());
            this.resetConnection();
        }

    }

    private Plan getPlanExecutionOracle(String query) {
        Plan planResult = new Plan();
        if (!query.isEmpty()) {
            try {
                Statement statement = connection.createStatement();
                statement.execute(Config.getProperty("signature") + Config.getProperty("getSqlCleanPlanExecutionForOracle"));
                statement.execute(Config.getProperty("signature") + Config.getProperty("getSqlGeneratePlanExecutionForOracle").replace("$QUERY$", query));
                query = query.replace("'", "''");

                statement.execute(Config.getProperty("signature") + Config.getProperty("getSqlUpdatePlanExecutionForOracle").replace("$QUERY$", query));
                ResultSet result = statement.executeQuery(Config.getProperty("signature") + " " + Config.getProperty("getSqlExtractPlanExecutionForOracle"));
                ResultSetMetaData rsmd = result.getMetaData();
                while (result.next()) {
                    PlanOperation planOp = new PlanOperation();
                    planOp.setOrder(result.getInt("id"));
                    planOp.setName(result.getString("operation"));
                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        planOp.properties.put(rsmd.getColumnName(i).toLowerCase(), result.getString(i));
                    }
                    if (!planResult.operations.contains(planOp)) {
                        planResult.operations.add(planOp);
                    }
                }
                if (planResult.operations.isEmpty()) {
                    return this.getEstimatedPlanExecutionOracle(query);
                } else {
                    planResult.setHypotetical(false);
                }
                result.close();
                statement.close();
            } catch (SQLException ex) {
                Log.msg(query);
                Log.msg(ex);
            }
        }
        return planResult;
    }

    private Plan getEstimatedPlanExecutionOracle(String query) {
        Plan planResult = new Plan();
        if (!query.isEmpty()) {
            String queryExplain = Config.getProperty("signature") + "EXPLAIN PLAN SET STATEMENT_ID = 'tap' for " + query;
            String queryGetPlan = Config.getProperty("signature") + " SELECT * FROM (SELECT * FROM plan_table CONNECT BY prior id = parent_id AND prior statement_id = statement_id START WITH id = 0 AND statement_id = 'tap') p ";
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(queryExplain);
                preparedStatement.executeUpdate(queryExplain);
                preparedStatement.close();
            } catch (Error | SQLException ex) {
                Log.msg("QUERY WITH ERROR (getEstimatedPlanExecutionOracle): " + Log.removeNl(queryExplain));
                return null;
            }
            try {
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(Config.getProperty("signature") + " " + queryGetPlan);
                ResultSetMetaData rsmd = result.getMetaData();
                int j = 0;
                while (result.next()) {
                    PlanOperation planOp = new PlanOperation();
                    planOp.setOrder(j++);
                    planOp.setName(result.getString("operation"));
                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        planOp.properties.put(rsmd.getColumnName(i).toLowerCase(), result.getString(i));
                    }
                    if (!planResult.operations.contains(planOp)) {
                        planResult.operations.add(planOp);
                    }
                }
                result.close();
                statement.close();
                statement = connection.createStatement();
                statement.executeUpdate(Config.getProperty("signature") + " delete from plan_table ");
                result.close();
                statement.close();
            } catch (SQLException ex) {
                Log.msg("QUERY WITH ERROR (getEstimatedPlanExecutionOracle): " + Log.removeNl(queryGetPlan));
                Log.error(ex);
            }
        }
        planResult.setHypotetical(true);
        return planResult;
    }

    private String getResultEstimatedPlanQueryPostgreSQL(String query) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Plan getEstimatedOraclePlan(String query) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
