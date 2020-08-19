/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.libraries.messages;

/**
 *
 * @author Rafael
 */
public class PromiseMessage extends Message {

    public PromiseMessage(String sourceID) {
        super(sourceID);
    }

    @Override
    public MessageType getType() {
        return MessageType.PROMISE;
    }

    @Override
    public String toString() {
        return "PromiseMessage [proposalNumber=" + getNumber()
                + ", sourceId=" + getSourceID() + "]";
    }

}
