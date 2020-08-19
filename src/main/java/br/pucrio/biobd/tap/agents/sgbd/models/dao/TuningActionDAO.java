/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models.dao;

import br.pucrio.biobd.tap.agents.libraries.Config;
import br.pucrio.biobd.tap.agents.libraries.Log;
import br.pucrio.biobd.tap.agents.sgbd.models.MaterializedView;
import br.pucrio.biobd.tap.agents.sgbd.models.SQL;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Rafael
 */
public abstract class TuningActionDAO extends BasicDAO {

    public static TuningActionDAO getInstance(TuningAction tuningAction, String agent) {
        return tuningAction.getInstanceDAO(agent);
    }

    public boolean save(TuningAction action) {
        if (!action.isValid()) {
            return false;
        }
        if (action.getDdl().isEmpty()) {
            return false;
        }
        return this.insertTuningAction(action)
                && this.insertTuningActionAgent(action)
                && this.insertGlobalTuningAction(action)
                && this.insertWorkloadTNARelation(action);
    }

    public boolean insertTuningActionAgent(TuningAction action) {
        for (String agentName : action.agents) {
            if (!this.isTuningActionAgentSave(action.getId(), agentName)) {
                try {
                    PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("insertTbTuningActionAgent_" + Config.getProperty("sgbd")));
                    preparedStatement.setString(1, agentName);
                    preparedStatement.setInt(2, action.getId());
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                } catch (SQLException e) {
                    Log.error(e);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean insertTuningAction(TuningAction action) {
        if (this.isTuningActionSave(action)) {
            return this.updateTuningAction(action);
        } else {
            try {
                PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("insertTbTuningAction_" + Config.getProperty("sgbd")));
                Log.msg("INSERT TNA ID " + action.getId() + ": " + Log.removeNl(action.getDdl()));
                preparedStatement.setInt(1, action.getId());
                preparedStatement.setString(2, Log.removeNl(action.getDdl()));
                preparedStatement.setDouble(3, action.getCost());
                preparedStatement.setDouble(4, getProfit(action));
                preparedStatement.setString(5, action.getStatus());
                preparedStatement.setDouble(6, action.getSelectivity());
                preparedStatement.setString(7, Log.removeNl(action.getDdlToExecute()));
                preparedStatement.executeUpdate();
                preparedStatement.close();
                return true;
            } catch (SQLException e) {
                Log.error(e);
            }
        }

        return false;
    }

    private boolean updateTuningAction(TuningAction action) {
        try {
            if (action.isValid()) {
                PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("updateTbTuningAction_" + Config.getProperty("sgbd")));
                preparedStatement.setString(1, action.getDdl());
                preparedStatement.setDouble(2, action.getCost());
                preparedStatement.setDouble(3, getProfit(action));
                preparedStatement.setInt(4, action.getId());
                preparedStatement.executeUpdate();
                preparedStatement.close();
                this.readAgents(action);
                return true;
            }
        } catch (SQLException e) {
            Log.error(e);
        }
        return false;
    }

    private boolean insertWorkloadTNARelation(TuningAction action) {
        if (action.isValid()) {
            for (SQL sql : action.getSqlList()) {
                if (!isTuningActionTNASave(action.getId(), sql)) {
                    try {
                        SQLDAO sqlDao = new SQLDAO();
                        PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("inserTbWorkloadTNA_" + Config.getProperty("sgbd")));
                        if (sql.getParentSQL() == null) {
                            sqlDao.save(sql);
                            preparedStatement.setInt(1, sql.getId());
                        } else {
                            sqlDao.save(sql.getParentSQL());
                            preparedStatement.setInt(1, sql.getParentSQL().getId());
                        }
                        preparedStatement.setInt(2, action.getId());
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                        return true;
                    } catch (SQLException e) {
                        Log.msg(action.getDdl());
                        Log.msg("Size IDs: " + action.ids.size());
                        for (String id : action.ids) {
                            Log.msg("ID: " + id);
                        }
                        Log.msg("Size IDs: " + action.ids.size());
                        Log.error(e);
                    }
                }
            }
        }
        return false;
    }

    private boolean isTuningActionSave(TuningAction action) {
        try {
            PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("tuningActionIsSave_" + Config.getProperty("sgbd")));
            preparedStatement.setInt(1, action.getId());
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                result.close();
                preparedStatement.close();
                return true;
            }
            result.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            Log.msg("ERROR isTuningActionSave: " + action.getDdl());
            Log.error(ex);
        }
        return false;
    }

    private boolean isTuningActionAgentSave(long actionId, String agentName) {
        try {
            PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("tuningActionAgentIsSave_" + Config.getProperty("sgbd")));
            preparedStatement.setLong(1, actionId);
            preparedStatement.setString(2, agentName);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                result.close();
                preparedStatement.close();
                return true;
            }
            result.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            Log.msg("ERROR isTuningActionAgentSave: " + actionId);
            Log.error(ex);
        }
        return false;
    }

    private boolean isTuningActionTNASave(int actionID, SQL sql) {
        try {
            PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("workloadTNAIsSave_" + Config.getProperty("sgbd")));
            preparedStatement.setInt(1, actionID);
            if (sql.getParentSQL() == null) {
                preparedStatement.setInt(2, sql.getId());
            } else {
                preparedStatement.setInt(2, sql.getParentSQL().getId());
            }
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                result.close();
                preparedStatement.close();
                return true;
            }
            result.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            Log.error(ex);
        }
        return false;
    }

    public abstract ArrayList<TuningAction> getAllTuningActions();

    public boolean execute(TuningAction tuningAction) {
        try {
            if (this.checkStatus(tuningAction)) {
                Log.msg("");
                tuningAction.setStatus("R");
                Log.msg("CREATE TUNING ACTION: " + Log.removeNl(tuningAction.getDdlToExecute()));
                this.updateStatusTuningAction(tuningAction);
                if (Config.getProperty("executionSimulation").equals("0")) {
                    PreparedStatement preparedStatement = connection().prepareStatement(tuningAction.getDdlToExecute());
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                }
                this.generateFileTuningAction(tuningAction);
                Log.msg("");
                return true;
            }
        } catch (SQLException ex) {
            Log.msg("");
            Log.msg("DDL ERROR ID: " + tuningAction.getId());
            Log.msg("DDL ERROR: " + tuningAction.getDdl());
            Log.msg("DDL ERROR: " + tuningAction.getDdlToExecute());
            Log.msg("DDL ERROR REASON: " + ex.getMessage());
            Log.msg("");
            tuningAction.setStatus("E");
            this.updateStatusTuningAction(tuningAction);
        }
        return false;
    }

    private boolean updateStatusTuningAction(TuningAction tuningAction) {
        try {
            PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("updateTuningActionStatus_" + Config.getProperty("sgbd")));
            preparedStatement.setString(1, tuningAction.getStatus());
            preparedStatement.setInt(2, tuningAction.getId());
            preparedStatement.executeUpdate();
            preparedStatement.close();
            return true;
        } catch (SQLException e) {
            Log.error(e);
        }
        return false;
    }

    protected boolean isSave(TuningAction action) {
        try {
            String nameQuery = action.getClass().getSimpleName() + "_IsSave_" + Config.getProperty("sgbd");
            String query = Config.getProperty(nameQuery);
            PreparedStatement preparedStatement = connection().prepareStatement(query);
            preparedStatement.setInt(1, action.getId());
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                result.close();
                preparedStatement.close();
                return true;
            }
            result.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            Log.error(ex);
        }
        return false;
    }

    public abstract double getProfit(TuningAction tuningAction);

    private boolean checkStatus(TuningAction action) {
        try {
            String nameQuery = "checkStatusTuningAction_" + Config.getProperty("sgbd");
            String query = Config.getProperty(nameQuery);
            PreparedStatement preparedStatement = connection().prepareStatement(query);
            preparedStatement.setInt(1, action.getId());
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                boolean status = result.getString("tna_status").equals("M");
                result.close();
                preparedStatement.close();
                return status;
            }
            result.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            Log.error(ex);
        }
        return false;
    }

    private void generateFileTuningAction(TuningAction tuningAction) {
        try {
            String nameFileCreate = Config.getProperty("folderLog") + "create_" + tuningAction.getName().split(" ")[0] + ".sql";
            String nameFileUpdateStatistics = Config.getProperty("folderLog") + "update_statistics_" + tuningAction.getName().split(" ")[0] + ".sql";

            PrintWriter writer = new PrintWriter(nameFileCreate.toLowerCase(), "UTF-8");
            writer.print(tuningAction.getDdlToExecute());
            writer.close();

            if (tuningAction instanceof MaterializedView) {
                writer = new PrintWriter(nameFileUpdateStatistics.toLowerCase(), "UTF-8");
                writer.print(Config.getProperty("DDLUpdateMV_" + Config.getProperty("sgbd")).replace("$database$", Config.getProperty("databaseName")).replace("$viewname$", tuningAction.getName()));
                writer.close();
            }

        } catch (IOException ex) {
            Log.error(ex);
        }

    }

    protected TuningAction readAgents(TuningAction tna) {
        try {
            PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("readTuninActionAgentList_" + Config.getProperty("sgbd")));
            preparedStatement.setInt(1, tna.getId());
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                if (!tna.agents.contains(result.getString("agt_agent"))) {
                    tna.agents.add(result.getString("agt_agent"));
                }
            }
            result.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            Log.error(ex);
        }
        return tna;
    }

    private boolean insertGlobalTuningAction(TuningAction action) {
        if (action.isGlobal() && !this.isGlobalTuningActionSave(action)) {
            try {
                PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("inserTbCandidateGlobalTuning_" + Config.getProperty("sgbd")));
                preparedStatement.setInt(1, action.getId());
                preparedStatement.setInt(2, action.getParentTuningAction().getId());
                preparedStatement.executeUpdate();
                preparedStatement.close();
                return true;
            } catch (SQLException e) {
                if (!e.getMessage().contains("tb_candidate_global_tuning_pkey") && !e.getMessage().contains("unique")) {
                    Log.msg(e);
                    try {
                        connection().close();
                    } catch (SQLException er) {
                        Log.msg(er);
                    }
                }
                return false;
            }
        }
        return true;
    }

    private boolean isGlobalTuningActionSave(TuningAction action) {
        try {
            PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("globalTuningIsSave_" + Config.getProperty("sgbd")));
            preparedStatement.setInt(1, action.getId());
            preparedStatement.setInt(2, action.getParentTuningAction().getId());
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                result.close();
                preparedStatement.close();
                return true;
            }
            result.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            Log.error(ex);
        }
        return false;
    }

}
