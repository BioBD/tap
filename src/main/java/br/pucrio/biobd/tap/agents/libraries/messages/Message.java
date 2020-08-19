/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.libraries.messages;

import jade.lang.acl.ACLMessage;

/**
 *
 * @author Rafael
 */
public abstract class Message extends ACLMessage {

    private long number = 0;
    private String sourceID;

    public Message(String sourceID) {
        super(ACLMessage.INFORM);
        this.sourceID = sourceID;
    }

    public String getSourceID() {
        return sourceID;
    }

    public void setSourceID(String sourceID) {
        this.sourceID = sourceID;
    }

    public abstract MessageType getType();

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

}
