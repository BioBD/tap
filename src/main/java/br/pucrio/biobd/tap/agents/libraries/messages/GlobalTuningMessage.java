/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.libraries.messages;

import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rafael
 */
public class GlobalTuningMessage extends Message {

    public List<TuningAction> actions;
    public List<String> agents;

    public GlobalTuningMessage(String sourceID) {
        super(sourceID);
        agents = new ArrayList<>();
    }

    @Override
    public MessageType getType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
