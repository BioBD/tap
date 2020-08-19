/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.behaviours;

import br.pucrio.biobd.tap.agents.BasicAgent;
import br.pucrio.biobd.tap.agents.sgbd.ReadSchemaDB;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;

/**
 *
 * @author Rafael
 */
public class ReadSchemaBehaviour extends Behaviour {

    public ReadSchemaBehaviour(Agent a) {
        super(a);
    }

    public BasicAgent agent() {
        return ((BasicAgent) getAgent());
    }

    @Override
    public void action() {
        if (agent().schema == null) {
            agent().schema = ReadSchemaDB.getSchemaDB(agent().getConnection());
        }
    }

    @Override
    public boolean done() {
        return true;
    }

}
