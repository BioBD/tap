/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models.implementors;

import br.pucrio.biobd.tap.agents.sgbd.models.Restriction;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;

/**
 *
 * @author rpoat
 */
public class TuningActionOracleImplementor {

    public static boolean respectRestrictionsSGBD(TuningAction tuningAction) {
        return true;
    }

    public static boolean isAValidPartialIndexRestriction(Restriction restriction) {
        return true;
    }
}
