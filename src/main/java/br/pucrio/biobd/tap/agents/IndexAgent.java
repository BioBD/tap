/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents;

import br.pucrio.biobd.tap.agents.behaviours.GlobalIndexTuningBehavior;
import br.pucrio.biobd.tap.agents.libraries.Config;

/**
 *
 * @author Rafael
 */
public class IndexAgent extends Predictor {

    @Override
    protected void setup() {
        super.setup();
        addBehaviour(new GlobalIndexTuningBehavior(this, Integer.valueOf(Config.getProperty("intervalGlobalTuning"))));
    }
}
