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
public class RejectMessage extends Message {

    public RejectMessage(String sourceID) {
        super(sourceID);
    }

    @Override
    public MessageType getType() {
        return MessageType.REJECT;
    }

    @Override
    public String toString() {
        return "RejectMessage [proposalNumber=" + getNumber()
                + ", sourceId=" + getSourceID() + "]";
    }
}
