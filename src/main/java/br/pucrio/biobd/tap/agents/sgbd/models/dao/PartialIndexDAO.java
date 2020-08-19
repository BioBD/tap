/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models.dao;

import br.pucrio.biobd.tap.agents.libraries.CaptorExecutionPlan;
import br.pucrio.biobd.tap.agents.libraries.Config;
import br.pucrio.biobd.tap.agents.libraries.Log;
import br.pucrio.biobd.tap.agents.sgbd.models.PartialIndex;
import br.pucrio.biobd.tap.agents.sgbd.models.Plan;
import br.pucrio.biobd.tap.agents.sgbd.models.PlanOperation;
import br.pucrio.biobd.tap.agents.sgbd.models.Restriction;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;
import br.pucrio.biobd.tap.agents.sgbd.models.implementors.PlanImplementor;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 *
 * @author Rafael
 */
public class PartialIndexDAO extends TuningActionDAO {

    @Override
    public boolean save(TuningAction action) {
        if (super.save(action)) {
            return this.insertIndexInMetabase((PartialIndex) action);
        }
        return false;
    }

    private boolean insertIndexInMetabase(PartialIndex index) {
        if (!index.getDdl().isEmpty()) {
            if (!this.isSave(index)) {
                try {
                    String queryTemp = Config.getProperty("insertCandidatePartialIndex_" + Config.getProperty("sgbd"));
                    PreparedStatement preparedStatement = connection().prepareStatement(queryTemp);
                    preparedStatement.setLong(1, index.getId());
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                } catch (SQLException e) {
                    Log.error(e.getMessage());
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public ArrayList<TuningAction> getAllTuningActions() {
        ArrayList<TuningAction> indexList = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection().prepareStatement(Config.getProperty("readToExecutePartialIndex_" + Config.getProperty("sgbd")));
            ResultSet result = preparedStatement.executeQuery();
            SQLDAO sqlDao = new SQLDAO();
            sqlDao.setAgent(agent);
            while (result.next()) {
                PartialIndex index = new PartialIndex();
                index.setDdl(result.getString("tna_ddl"));
                index.setStatus(result.getString("tna_status"));
                index = (PartialIndex) sqlDao.readSQLList(index);
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
        long seqScanCost = 0;
        long profit;
        int queryCount;
        Plan planOriginalQuery = tuningAction.getLastSql().getLastExecution().getPlan();
        PlanImplementor planImp = PlanImplementor.createPlanImplementor();
        for (PlanOperation planOperation : planOriginalQuery.operations) {
            if (planOperation.properties.containsKey("Filter")) {
                String filter = planOperation.properties.get("Filter").toString();
                for (Restriction restriction : ((PartialIndex) tuningAction).getRestrictions()) {
                    if (filter.toLowerCase().contains(restriction.getColumn().getName())) {
                        if (planImp.getCostOperation(planOperation) > 0) {
                            seqScanCost = planImp.getCostOperation(planOperation);
                        }
                    }
                }
            }
        }
        indexScanCost = ((PartialIndex) tuningAction).getIndexScanCost();
        queryCount = this.getNumSQLUsingIndex(((PartialIndex) tuningAction));
        profit = (seqScanCost - indexScanCost) * queryCount;
        if (profit == 0) {
            System.out.println(profit);
        }
        tuningAction.setProfit(profit);
        return profit;
    }

    public int getNumSQLUsingIndex(PartialIndex index) {
        int queryCount = 1;
        try {
            String queryTemp = Config.getProperty("queryCountUsingPartialIndex_" + Config.getProperty("sgbd"));
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
            Log.error(e.getMessage());
        }
        if (queryCount == 0) {
            queryCount = 1;
        }
        return queryCount;
    }

    public double getSelectivity(PartialIndex pindex) {
        double selectivity = 0;
        double numberRows = 0;
        double allRows = pindex.getTable().getNumberRows();
        if (pindex.isGlobal()) {
            numberRows = pindex.getNumberOfRows();
        } else {
            String query = Config.getProperty("getSelectivityPartialIndex_" + Config.getProperty("sgbd"));

            query = query.replace("$columns$", pindex.getColumnIndexFormatedToSQL());
            query = query.replace("$table$", pindex.getTableName());
            query = query.replace("$conditions$", pindex.getRestrictionsFormatedToSQL());

            CaptorExecutionPlan captor = new CaptorExecutionPlan(agent);
            Plan plan = captor.getPlanExecution(query);
            numberRows = plan.getNumRow();
        }
        selectivity = (numberRows / allRows);

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
