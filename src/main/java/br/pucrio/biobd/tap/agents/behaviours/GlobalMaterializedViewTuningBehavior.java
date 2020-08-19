/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.behaviours;

import br.pucrio.biobd.tap.agents.libraries.Config;
import br.pucrio.biobd.tap.agents.libraries.Log;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;
import br.pucrio.biobd.tap.agents.sgbd.models.dao.MaterializedViewDAO;
import jade.core.Agent;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rafael
 */
public class GlobalMaterializedViewTuningBehavior extends GlobalTuningBehavior {

    public GlobalMaterializedViewTuningBehavior(Agent a, long period) {
        super(a, period);
    }

    @Override
    protected void onTick() {
        if (Config.getProperty("globalTuningActive").equals("1")) {
            this.startRoundGlobalTuning();
        }
    }

    @Override
    protected List<TuningAction> generateGlobalAction(TuningAction action) {
        List<TuningAction> actions = new ArrayList<>();
        return actions;
    }

    @Override
    public ArrayList<TuningAction> getAllTuningActions() {
        MaterializedViewDAO mvDao = new MaterializedViewDAO();
        return mvDao.getAllTuningActions();
    }

}
