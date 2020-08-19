/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents;

import br.pucrio.biobd.tap.agents.behaviours.CombinedTuningActionSelection;
import br.pucrio.biobd.tap.agents.libraries.Config;

/**
 *
 * @author Rafael
 */
public class Executor extends BasicAgent {

    @Override
    protected void setup() {
        addBehaviour(new CombinedTuningActionSelection(this, Integer.valueOf(Config.getProperty("combinedTuningActionSelection"))));
    }

    @Override
    protected void takeDown() {
        System.out.println("Agent " + getAID().getName() + " is take down!");
    }

}
