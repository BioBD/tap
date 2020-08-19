/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.behaviours;

import br.pucrio.biobd.tap.agents.sgbd.models.dao.SelectionHeuristicDAO;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

/**
 *
 * @author rpoat
 */
public class CombinedTuningActionSelection extends TickerBehaviour {

    public CombinedTuningActionSelection(Agent a, long period) {
        super(a, period);
    }

    @Override
    protected void onTick() {
        getPriorityQueriesByCost();
        mergeTuningActionsSameType();
        pruneTuningActions();
        selectTuningActionsForRestrictions();
        validateTuningActionsSelected();
    }

    private void getPriorityQueriesByCost() {
    }

    private void mergeTuningActionsSameType() {
    }

    private void pruneTuningActions() {
    }

    private void validateTuningActionsSelected() {
    }

    private void selectTuningActionsForRestrictions() {
        SelectionHeuristicDAO selectionHeuristic = new SelectionHeuristicDAO();
        selectionHeuristic.generateTestFilesSelectionHeuristicPartialIndex();
    }

}
