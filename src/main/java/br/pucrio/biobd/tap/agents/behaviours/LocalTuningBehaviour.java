/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.behaviours;

import br.pucrio.biobd.tap.agents.Predictor;
import br.pucrio.biobd.tap.agents.libraries.Config;
import br.pucrio.biobd.tap.agents.libraries.Log;
import br.pucrio.biobd.tap.agents.libraries.ReadXML;
import br.pucrio.biobd.tap.agents.sgbd.models.SQL;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;
import br.pucrio.biobd.tap.agents.sgbd.models.dao.SQLDAO;
import br.pucrio.biobd.tap.agents.sgbd.models.dao.TuningActionDAO;
import br.pucrio.biobd.tap.algoritms.Algorithm;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rafael
 */
public class LocalTuningBehaviour extends TickerBehaviour {

    private final ArrayList<Algorithm> algorithms;

    public LocalTuningBehaviour(Agent a, long period) {
        super(a, period);
        this.algorithms = new ArrayList<>();
        try {
            ReadXML read = new ReadXML();
            List<String> algoritms = read.getNodeValues("config/algoritms.xml", "algoritms", "behavior", "GenerateTuningActions" + this.myAgent.getLocalName());
            for (String algoritm : algoritms) {
                Log.msg("Algoritm GenerateTuningActions: " + algoritm);
                Class<?> c = Class.forName(algoritm);
                Algorithm algorithm = (Algorithm) c.newInstance();
                algorithm.setConnection(agent().getConnection());
                this.algorithms.add(algorithm);
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Log.error(ex);
        }
    }

    @Override
    protected void onTick() {
        SQLDAO sqlDao = new SQLDAO();
        sqlDao.setAgent(agent().getClass().getSimpleName());
        for (Algorithm algorithm : algorithms) {
            ArrayList<SQL> SQListToBeProcessed = sqlDao.getWorkload(algorithm.getClass().getSimpleName(), agent().schema);
            for (SQL sql : SQListToBeProcessed) {
                ArrayList<TuningAction> tuningActionsList = this.generateTuningActions(sql, algorithm);
                this.ratingTuningActions(tuningActionsList);
                this.insertTuningActions(tuningActionsList);

                for (TuningAction tuningAction : tuningActionsList) {
                    if (!((Predictor) this.myAgent).tuningActions.contains(tuningAction)) {
                        ((Predictor) this.myAgent).tuningActions.add(tuningAction);
                    }
                }
            }
        }
    }

    private void ratingTuningActions(ArrayList<TuningAction> tuningActionsList) {
        for (int i = 0; i < tuningActionsList.size(); ++i) {
            tuningActionsList.get(i).rating(agent().getClass().getSimpleName());
        }
    }

    private void insertTuningActions(ArrayList<TuningAction> tuningActionsList) {
        if (!tuningActionsList.isEmpty()) {
            for (int i = 0; i < tuningActionsList.size(); ++i) {
                TuningActionDAO tnaDao = TuningActionDAO.getInstance(tuningActionsList.get(i), agent().getClass().getSimpleName());
                tnaDao.save(tuningActionsList.get(i));
            }
            this.updateBenefitHeuristic();
        }
    }

    private Predictor agent() {
        return ((Predictor) getAgent());
    }

    private ArrayList<TuningAction> generateTuningActions(SQL sql, Algorithm algorithm) {
        ArrayList<TuningAction> tuningActionsList = new ArrayList<>();
        List<TuningAction> tuningActionLocalList = algorithm.getTuningActions(sql);
        for (TuningAction tuningAction : tuningActionLocalList) {
            TuningAction action = this.getTuningActionById(tuningAction.getId(), tuningActionsList);
            if (action == null) {
                tuningAction.agents.add(this.myAgent.getClass().getSimpleName());
                tuningActionsList.add(tuningAction);
            } else {
                action.addSQL(sql);
            }
        }
        return tuningActionsList;
    }

    public TuningAction getTuningActionById(int idAction, List<TuningAction> tuningActionLocalList) {
        for (TuningAction tuningAction : tuningActionLocalList) {
            if (tuningAction.getId() == idAction) {
                return tuningAction;
            }
        }
        return null;
    }

    private void updateBenefitHeuristic() {
        try {
            PreparedStatement preparedStatement = agent().getConnection().prepareStatement(Config.getProperty("updateTbTuningActionBenefitHeuristic_" + Config.getProperty("sgbd")));
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            Log.error(e);
        }
    }

}
