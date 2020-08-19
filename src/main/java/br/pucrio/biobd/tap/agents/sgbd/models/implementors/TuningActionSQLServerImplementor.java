/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models.implementors;

import br.pucrio.biobd.tap.agents.sgbd.models.MaterializedView;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;

/**
 *
 * @author Rafael
 */
public class TuningActionSQLServerImplementor {

    public static boolean respectRestrictionsSGBD(TuningAction tuningAction) {
        if (tuningAction instanceof MaterializedView) {
            return respectRestrictionsMV(tuningAction);
        }
        return true;
    }

    private static boolean respectRestrictionsMV(TuningAction tuningAction) {
        String[] functionRestrictions = {"having", "min(", "max(", "sum(", "count(", "avg(", "top "};
        for (String functionRestriction : functionRestrictions) {
            if (tuningAction.getDdl().toLowerCase().contains(functionRestriction)) {
                return false;
            }
        }
        return true;
    }

}
