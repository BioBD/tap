/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.algoritms.Index;

import br.pucrio.biobd.tap.agents.sgbd.models.SQL;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;
import br.pucrio.biobd.tap.algoritms.Algorithm;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author josemariamonteiro
 */
public class IHSTIS extends Algorithm {

    @Override
    public List<TuningAction> getTuningActions(SQL sql) {
        List<TuningAction> tuningActions = new ArrayList<>();
        IHSTIS_CI ci = new IHSTIS_CI();
        ci.setConnection(connection);
        tuningActions.addAll(ci.getTuningActions(sql));
        return tuningActions;
    }

}
