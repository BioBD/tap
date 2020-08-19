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
import br.pucrio.biobd.tap.agents.sgbd.models.SQLExecution;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Rafael
 */
public class MaterializedViewDAO extends TuningActionDAO {

    @Override
    public boolean save(TuningAction action) {
        if (super.save(action)) {
            return this.saveMV((MaterializedView) action);
        }
        return false;
    }

    private boolean saveMV(MaterializedView action) {
        if (!isSave((MaterializedView) action)) {
            try {
                PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("inserTbCandidateMV_" + Config.getProperty("sgbd")));
                preparedStatement.setInt(1, action.getId());
                preparedStatement.executeUpdate();
                preparedStatement.close();
                return true;
            } catch (SQLException e) {
                Log.msg("MV ERROR: " + action.getId());
                Log.error(e);
            }
        }
        return false;
    }

    @Override
    public ArrayList<TuningAction> getAllTuningActions() {
        ArrayList<TuningAction> mvList = new ArrayList<>();
        SQLDAO sqlDao = new SQLDAO();
        sqlDao.setAgent(agent);
        try {
            PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("readToExecuteMV_" + Config.getProperty("sgbd")));
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                MaterializedView mv = new MaterializedView();
                mv.setDdl(result.getString("tna_ddl"));
                mv.setStatus(result.getString("tna_status"));
                mv = (MaterializedView) sqlDao.readSQLList(mv);
                mv = (MaterializedView) this.readAgents(mv);
                mvList.add(mv);
            }
            result.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            Log.error(ex);
        }
        return mvList;
    }

    @Override
    public double getProfit(TuningAction tuningAction) {
        long totalProfit = 0;
        long totalCost = 0;
        long subProfit;
        double hypoCostUsingMV = ((MaterializedView) tuningAction).getHypoNumPages();
        for (SQL sql : ((MaterializedView) tuningAction).getSqlList()) {
            for (SQLExecution sqlExecution : sql.executions) {
                totalCost += sqlExecution.getPlan().getCost();
                subProfit = sqlExecution.getPlan().getCost();
                totalProfit += subProfit - hypoCostUsingMV;
            }
        }
        if (totalProfit < 0) {
            if (((MaterializedView) tuningAction).isSubQuery()) {
                return totalCost / 2;
            } else {
                return -1;

            }
        } else {
            return totalProfit;

        }
    }

}
