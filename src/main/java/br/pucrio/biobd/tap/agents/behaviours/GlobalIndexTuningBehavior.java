/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.behaviours;

import br.pucrio.biobd.tap.agents.libraries.Config;
import br.pucrio.biobd.tap.agents.libraries.Log;
import br.pucrio.biobd.tap.agents.sgbd.models.Index;
import br.pucrio.biobd.tap.agents.sgbd.models.MaterializedView;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;
import br.pucrio.biobd.tap.agents.sgbd.models.dao.IndexDAO;
import br.pucrio.biobd.tap.algoritms.Index.INDEX_OVER_MV_GLOBAL_TUNING;
import jade.core.Agent;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rafael
 */
public class GlobalIndexTuningBehavior extends GlobalTuningBehavior {

    public GlobalIndexTuningBehavior(Agent a, long period) {
        super(a, period);
    }

    @Override
    protected List<TuningAction> generateGlobalAction(TuningAction action) {
        List<TuningAction> actions = new ArrayList<>();
        if (action instanceof MaterializedView && action.isValid() && !action.agents.contains(agentName())) {
            actions = this.generateIndexIntoMaterializedViews((MaterializedView) action);
        }
        return actions;
    }

    private List<TuningAction> generateIndexIntoMaterializedViews(MaterializedView action) {
        List<TuningAction> actions = new ArrayList<>();
        INDEX_OVER_MV_GLOBAL_TUNING indexOverMVAlg = new INDEX_OVER_MV_GLOBAL_TUNING();
        List<TuningAction> indexOverMVList = indexOverMVAlg.getTuningActions(action);
        for (TuningAction tuningAction : indexOverMVList) {
            actions.add(tuningAction);
        }
        return actions;
    }

    @Override
    public ArrayList<TuningAction> getAllTuningActions() {
        IndexDAO indexDao = new IndexDAO();
        return indexDao.getAllTuningActions();
    }

}
