/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents;

import br.pucrio.biobd.tap.agents.behaviours.GlobalMaterializedViewTuningBehavior;
import br.pucrio.biobd.tap.agents.libraries.Config;

/**
 *
 * @author Rafael
 */
public class MaterializedViewAgent extends Predictor {

    @Override
    protected void setup() {
        super.setup();
        addBehaviour(new GlobalMaterializedViewTuningBehavior(this, Integer.valueOf(Config.getProperty("intervalGlobalTuning"))));
    }

}
