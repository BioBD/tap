/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents;

import br.pucrio.biobd.tap.agents.behaviours.LocalTuningBehaviour;
import br.pucrio.biobd.tap.agents.sgbd.ReadSchemaDB;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rafael
 */
public abstract class Predictor extends BasicAgent {

    public static String omega;
    public List<TuningAction> tuningActions;

    @Override
    protected void setup() {
        this.tuningActions = new ArrayList<>();
        this.schema = ReadSchemaDB.getSchemaDB(getConnection());
        addBehaviour(new LocalTuningBehaviour(this, 2000));
    }

    @Override
    protected void takeDown() {
        System.out.println("Agent " + getAID().getName() + " is take down!");
    }

}
