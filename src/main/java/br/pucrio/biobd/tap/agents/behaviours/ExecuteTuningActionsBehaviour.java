/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.behaviours;

import br.pucrio.biobd.tap.agents.Executor;
import br.pucrio.biobd.tap.agents.libraries.Log;
import br.pucrio.biobd.tap.agents.libraries.ReadXML;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;
import br.pucrio.biobd.tap.agents.sgbd.models.dao.TuningActionDAO;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rafael
 */
public class ExecuteTuningActionsBehaviour extends TickerBehaviour {

    int i = 0;
    private final ArrayList<TuningActionDAO> tuningActionsDAO;

    public ExecuteTuningActionsBehaviour(Agent a, long period) {
        super(a, period);
        this.tuningActionsDAO = new ArrayList<>();
        try {
            ReadXML read = new ReadXML();
            List<String> algoritms = read.getNodeValues("config/algoritms.xml", "algoritms", "behavior", "ExecuteTuningActionsBehaviour");
            for (String algoritm : algoritms) {
                Log.msg("Algoritm ExecuteTuningActionsBehaviour: " + algoritm);
                Class<?> c = Class.forName(algoritm);
                TuningActionDAO tuningActionDAO = (TuningActionDAO) c.newInstance();
                tuningActionDAO.setAgent(agent().getClass().getSimpleName());
                this.tuningActionsDAO.add(tuningActionDAO);
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Log.error(ex);
        }
    }

    public Executor agent() {
        return ((Executor) getAgent());
    }

    @Override
    protected void onTick() {
        ArrayList<TuningAction> tuningActionsList = new ArrayList<>();
        for (TuningActionDAO tuningActionDAO : tuningActionsDAO) {
            tuningActionsList.addAll(tuningActionDAO.getAllTuningActions());
        }
        for (TuningAction tuningAction : tuningActionsList) {
            if (tuningAction.getStatus().equals("M")) {
                TuningActionDAO tnaDao = TuningActionDAO.getInstance(tuningAction, agent().getClass().getSimpleName());
                tnaDao.execute(tuningAction);
            }
        }
    }

}
