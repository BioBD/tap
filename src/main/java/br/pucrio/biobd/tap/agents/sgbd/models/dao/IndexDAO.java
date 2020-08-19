/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models.dao;

import br.pucrio.biobd.tap.agents.libraries.Config;
import br.pucrio.biobd.tap.agents.libraries.Log;
import br.pucrio.biobd.tap.agents.sgbd.models.Column;
import br.pucrio.biobd.tap.agents.sgbd.models.Index;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rafael
 */
public class IndexDAO extends TuningActionDAO {

    public void saveAllIndexes(List<Index> lCandidates) {
        for (Index lCandidate : lCandidates) {
            if (lCandidate.getTable() != null) {
                this.save(lCandidate);
            }
        }
    }

    @Override
    public boolean save(TuningAction action) {
        if (super.save(action)) {
            return insertIndexInMetabase((Index) action);
        }
        return false;
    }

    private boolean insertIndexInMetabase(Index index) {
        if (!index.getDdl().isEmpty()) {
            if (!this.isSave(index)) {
                try {
                    String queryTemp = Config.getProperty("insertCandidateIndex_" + Config.getProperty("sgbd"));
                    PreparedStatement preparedStatement = connection().prepareStatement(queryTemp);
                    preparedStatement.setLong(1, index.getId());
                    preparedStatement.setString(2, index.getTable().getName());
                    preparedStatement.setString(3, index.getIndexType());
                    preparedStatement.setInt(4, 0);
                    preparedStatement.setInt(5, 0);
                    preparedStatement.setInt(6, 0);
                    preparedStatement.setString(7, index.getName());
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                } catch (SQLException e) {
                    Log.error(e.getMessage());
                }
                for (int i = 0; i < index.getColumns().size(); i++) {
                    Column c = index.getColumns().get(i);
                    insertColumn(index.getId(), c);
                }
                return true;
            }
        }
        return false;
    }

    private void insertColumn(int tnaId, Column c) {
        try {
            String queryTemp = Config.getProperty("selectCandidateIndexColumn_" + Config.getProperty("sgbd"));
            PreparedStatement preparedStatement = connection().prepareStatement(queryTemp);
            preparedStatement.setInt(1, tnaId);
            ResultSet result = preparedStatement.executeQuery();
            if (!result.next()) {
                queryTemp = Config.getProperty("insertCandidateIndexColumn_" + Config.getProperty("sgbd"));
                PreparedStatement preparedStatementInsert = connection().prepareStatement(queryTemp);
                preparedStatementInsert.setInt(1, tnaId);
                preparedStatementInsert.setString(2, c.getName());
                preparedStatementInsert.setString(3, c.getType());
                preparedStatementInsert.executeUpdate();
                preparedStatementInsert.close();
            }
            result.close();
            preparedStatement.close();
        } catch (SQLException e) {
            Log.error(e.getMessage());
        }
    }

    public int getNumSQLUsingIndex(Index index) {
        int queryCount = 0;
        try {
            String queryTemp = Config.getProperty("queryCountUsingIndex_" + Config.getProperty("sgbd"));
            PreparedStatement preparedStatement = connection().prepareStatement(queryTemp);
            preparedStatement.setInt(1, index.getId());
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                queryCount = result.getInt("queryCount");
            }
            result.close();
            preparedStatement.close();
        } catch (SQLException e) {
            Log.error(e.getMessage());
        }
        return queryCount;
    }

    @Override
    public ArrayList<TuningAction> getAllTuningActions() {
        ArrayList<TuningAction> indexList = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("readToExecuteIndex_" + Config.getProperty("sgbd")));
            ResultSet result = preparedStatement.executeQuery();
            SQLDAO sqlDao = new SQLDAO();
            sqlDao.setAgent(agent);
            while (result.next()) {
                Index index = new Index();
                index.setIndexType(result.getString("cid_type"));
                index.setDdl(result.getString("tna_ddl"));
                index.setStatus(result.getString("tna_status"));
                index = (Index) sqlDao.readSQLList(index);
                indexList.add(index);
            }
            result.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            Log.error(ex);
        }
        return indexList;
    }

    @Override
    public double getProfit(TuningAction tuningAction) {
        long indexScanCost;
        long seqScanCost;
        long profit;
        int queryCount;
        indexScanCost = ((Index) tuningAction).getIndexScanCost();
        seqScanCost = ((Index) tuningAction).getTable().getNumberPages();
        queryCount = this.getNumSQLUsingIndex(((Index) tuningAction));
        if (indexScanCost < seqScanCost) {
            profit = (seqScanCost - indexScanCost) * queryCount;
        } else {
            profit = -1;
        }
        return profit;
    }

    public double getSelectivity(Index index) {
        double tableRows = index.getTable().getNumberRows();
        double nDistinct = 0;
        double selectivity;
        try {
            String query = Config.getProperty("getSelectivityIndex_" + Config.getProperty("sgbd"));
            nDistinct = index.getColumns().get(0).getSelectivity();
            if (nDistinct <= 0) {
                if (index.isGlobal()) {
                    nDistinct = index.getNumberOfRows();
                } else {
                    query = query.replace("$column$", index.getColumns().get(0).getName());
                    query = query.replace("$table$", index.getTable().getName());
                    PreparedStatement preparedStatement = connection().prepareStatement(query);
                    ResultSet result = preparedStatement.executeQuery();
                    if (result.next()) {
                        nDistinct = result.getInt("n_distinct");
                    }
                    result.close();
                    preparedStatement.close();
                }
            }
        } catch (SQLException ex) {
            Log.error(ex);
        }
        selectivity = (nDistinct / tableRows);

        DecimalFormat df = new DecimalFormat("0.00000");
        df.setRoundingMode(RoundingMode.CEILING);
        selectivity = Double.valueOf(df.format(selectivity).replace(",", "."));
        if (selectivity > 0 && selectivity <= 1) {
            selectivity = Double.valueOf("1") - selectivity;
        } else {
            selectivity = 0;
        }
        return selectivity;
    }
}
