/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.algoritms;

import br.pucrio.biobd.tap.agents.sgbd.models.SQL;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rafael
 */
public abstract class Algorithm {

    protected Connection connection;

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public abstract List<TuningAction> getTuningActions(SQL sql);

    protected List<TuningAction> removeDuplicateTuningActions(List<TuningAction> tuningActions) {
        List<TuningAction> allTuningActions = new ArrayList<>();
        for (TuningAction tuningAction : tuningActions) {
            if (!allTuningActions.contains(tuningAction)) {
                allTuningActions.add(tuningAction);
            }
        }
        return allTuningActions;
    }

}
