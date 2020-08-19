/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models.implementors;

import br.pucrio.biobd.tap.agents.libraries.Config;
import br.pucrio.biobd.tap.agents.sgbd.models.Restriction;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;

/**
 *
 * @author Rafael
 */
public class TuningActionImplementor {

    public static boolean respectRestrictionsSGBD(TuningAction tuningAction) {
        switch (Config.getProperty("sgbd")) {
            case "postgresql":
                return TuningActionPostgreSQLImplementor.respectRestrictionsSGBD(tuningAction);
            case "oracle":
                return TuningActionOracleImplementor.respectRestrictionsSGBD(tuningAction);
            case "sqlserver":
                return TuningActionSQLServerImplementor.respectRestrictionsSGBD(tuningAction);
            default:
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    public static boolean isAValidPartialIndexRestriction(Restriction restriction) {
        switch (Config.getProperty("sgbd")) {
            case "postgresql":
                return TuningActionPostgreSQLImplementor.isAValidPartialIndexRestriction(restriction);
            case "oracle":
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            case "sqlserver":
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            default:
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
