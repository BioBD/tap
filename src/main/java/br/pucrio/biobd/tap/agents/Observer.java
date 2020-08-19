/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents;

import br.pucrio.biobd.tap.agents.behaviours.CaptureWorkloadBehaviour;

/**
 *
 * @author Rafael
 */
public class Observer extends BasicAgent {

    @Override
    protected void setup() {
        addBehaviour(new CaptureWorkloadBehaviour());
    }

    @Override
    protected void takeDown() {
        System.out.println("Agent " + getAID().getName() + " is take down!");
    }

}
