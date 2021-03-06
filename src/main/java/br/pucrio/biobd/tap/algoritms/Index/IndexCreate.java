/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.algoritms.Index;

import br.pucrio.biobd.tap.agents.sgbd.models.SQL;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;
import br.pucrio.biobd.tap.algoritms.Algorithm;
import java.util.List;

/**
 *
 * @author Rafael
 */
public class IndexCreate extends Algorithm {

    @Override
    public List<TuningAction> getTuningActions(SQL sql) {
        IHSTIS ihstis = new IHSTIS();
        ihstis.setConnection(connection);
        return ihstis.getTuningActions(sql);
    }

}
